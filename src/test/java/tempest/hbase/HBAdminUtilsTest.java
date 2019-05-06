package tempest.hbase;

import tempest.docker.HBaseDockerTest;
import tempest.hbase.conventions.HBEntityUtils;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotEnabledException;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Admin;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static tempest.hbase.conventions.HBAdminUtils.createNamespace;
import static tempest.hbase.conventions.HBAdminUtils.createTable;
import static tempest.hbase.conventions.HBAdminUtils.dropNamespace;
import static tempest.hbase.conventions.HBAdminUtils.dropTable;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@Ignore
public class HBAdminUtilsTest extends HBaseDockerTest {

    @HBEntity(namespace = "ABC")
    private static class HBEntitySimple {

        @HBColumn(key = 1)
        private String keyCol;
        @HBColumn
        private String dataCol;
    }

    @Before
    public void setup() throws Exception {
        super.setupConnectionFactory();
        this.cleanNamespaces();
    }

    @After
    public void tearDown() throws Exception {
        this.cleanNamespaces();
    }

    @Test
    public void createDropNamespace() throws Exception {
        Admin admin = hbConnection.getAdmin();
        assertFalse(dropNamespace(admin, "ABC"));
        assertTrue(createNamespace(admin, "ABC"));
        assertFalse(createNamespace(admin, "ABC"));
        assertTrue(createNamespace(admin, "XYZ"));
        assertTrue(dropNamespace(admin, "XYZ"));
        assertTrue(dropNamespace(admin, "ABC"));
    }

    @Test
    public void createDropTable() throws Exception {
        Admin admin = hbConnection.getAdmin();
        assertTrue(createNamespace(admin, "ABC"));
        assertTrue(createTable(admin, HBEntitySimple.class));
        assertFalse(createTable(admin, HBEntitySimple.class));
        assertTrue(dropTable(admin, HBEntitySimple.class));
        assertFalse(dropTable(admin, HBEntitySimple.class));
        assertTrue(dropNamespace(admin, "ABC"));
    }

    private void cleanNamespaces() throws IOException {
        Admin admin = hbConnection.getAdmin();
        TableName tableName = HBEntityUtils.getHBTableName(HBEntitySimple.class);
        try {
            admin.disableTable(tableName);
        } catch (TableNotFoundException | TableNotEnabledException e) {
        }
        try {
            admin.deleteTable(tableName);
        } catch (TableNotFoundException e) {
        }
        try {
            admin.deleteNamespace("ABC");
        } catch (NamespaceNotFoundException e) {
        }
        try {
            admin.deleteNamespace("XYZ");
        } catch (NamespaceNotFoundException e) {
        }
    }

}
