package tempest.hbase;

import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tempest.docker.HBaseDockerTest;
import tempest.hbase.conventions.HBAdminUtils;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HBaseDAOTest extends HBaseDockerTest {

    private class HBEntitySimpleDAO extends HBaseDAO<HBEntitySimple> {
        public HBEntitySimpleDAO(HBConnectionFactory hbConnectionFactory) { super(hbConnectionFactory); }
    }
    private HBEntitySimpleDAO simpleEntityDAO;

    @Before
    public void setup() throws Exception {
        super.setupConnectionFactory();
        simpleEntityDAO = new HBEntitySimpleDAO(mockConnectionFactory);

        Admin hbAdmin = hbConnection.getAdmin();
        cleanTestNamespace(hbAdmin);
        HBAdminUtils.createNamespace(hbAdmin, "ABC");
        HBAdminUtils.createTable(hbAdmin, HBEntitySimple.class);
    }

    @After
    public void tearDown() throws Exception {
        Admin hbAdmin = hbConnection.getAdmin();
        cleanTestNamespace(hbAdmin);
    }

    @Test
    public void save() {
        String key = "A|B";
        HBEntitySimple simple = new HBEntitySimple("A", "B", "data");
        assertEquals(key, simpleEntityDAO.save(simple));
    }

    @Test
    public void getRaw() {
        String key = "A|B";

        assertFalse(simpleEntityDAO.getRaw(key).isPresent());

        assertEquals(key, simpleEntityDAO.save(new HBEntitySimple("A", "B", "data")));
        Optional<Result> data = simpleEntityDAO.getRaw(key);
        assertTrue(data.isPresent());
        assertEquals("data", simpleEntityDAO.getStringValue(data.get(), "DATA"));

        assertEquals(key, simpleEntityDAO.save(new HBEntitySimple("A", "B", "data2")));
        data = simpleEntityDAO.getRaw(key);
        assertTrue(data.isPresent());
        assertEquals("data2", simpleEntityDAO.getStringValue(data.get(), "DATA"));
    }


    @Test
    public void scanRaw() {
        assertEquals(0, simpleEntityDAO.scanRaw("A").count());

        assertEquals("A|X", simpleEntityDAO.save(new HBEntitySimple("A", "X", "X")));
        assertEquals("A|Y", simpleEntityDAO.save(new HBEntitySimple("A", "Y", "Y")));
        assertEquals("B|Z", simpleEntityDAO.save(new HBEntitySimple("B", "Z", "Z")));
        assertEquals(2, simpleEntityDAO.scanRaw("A").count());
        assertEquals(1, simpleEntityDAO.scanRaw("A|X").count());
        assertEquals(1, simpleEntityDAO.scanRaw("B").count());
        assertEquals(0, simpleEntityDAO.scanRaw("C").count());
    }

    private void cleanTestNamespace(Admin hbAdmin) throws IOException {
        HBAdminUtils.dropTable(hbAdmin, HBEntitySimple.class);
        HBAdminUtils.dropNamespace(hbAdmin, "ABC");
    }
}

@HBEntity(namespace = "ABC")
class HBEntitySimple {
    @HBColumn(key = 1) private String k1;
    @HBColumn(key = 2) private String k2;
    @HBColumn private String data;

    public HBEntitySimple(String k1, String k2, String data) {
        this.k1 = k1;
        this.k2 = k2;
        this.data = data;
    }
}
