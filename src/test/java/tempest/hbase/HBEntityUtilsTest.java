package tempest.hbase;

import tempest.hbase.conventions.HBEntityUtils;
import org.apache.hadoop.hbase.TableName;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class HBEntityUtilsTest {

    @HBEntity(namespace = "ABC")
    private class HBEntitySimple {
        @HBColumn(key = 1)
        private String keyCol;
        @HBColumn
        private String dataCol;
    }

    @HBEntity(namespace = "ABC", table = "COMPLEX_TABLE", family = "COMPLEX_FAMILY", keySeparator = "#")
    private class HBEntityComplex {
        @HBColumn(key = 1)
        private String keyOne;
        @HBColumn(key = 2)
        private String keyTwo;
        @HBColumn
        private String strCol;
        @HBColumn
        private String dtCol;
        @HBColumn(value = "CPF")
        private String cpfCol;
    }

    @Test
    public void getAnnotatedNamespace() {
        Assert.assertEquals("ABC", HBEntityUtils.getAnnotatedNamespace(HBEntitySimple.class));
        assertEquals("ABC", HBEntityUtils.getAnnotatedNamespace(HBEntityComplex.class));
    }

    @Test
    public void getAnnotatedFamily() {
        assertEquals("HB_ENTITY_SIMPLE_FAM", HBEntityUtils.getAnnotatedFamily(HBEntitySimple.class));
        assertEquals("COMPLEX_FAMILY", HBEntityUtils.getAnnotatedFamily(HBEntityComplex.class));
    }

    @Test
    public void getAnnotatedTableName() {
        assertEquals("HB_ENTITY_SIMPLE", HBEntityUtils.getAnnotatedTableName(HBEntitySimple.class));
        assertEquals("COMPLEX_TABLE", HBEntityUtils.getAnnotatedTableName(HBEntityComplex.class));
    }

    @Test
    public void getQualifiedName() {
        assertEquals("ABC:HB_ENTITY_SIMPLE", HBEntityUtils.getQualifiedName(HBEntitySimple.class));
        assertEquals("ABC:COMPLEX_TABLE", HBEntityUtils.getQualifiedName(HBEntityComplex.class));
    }

    @Test
    public void getAnnotatedKeySeparator() {
        assertEquals("|", HBEntityUtils.getAnnotatedKeySeparator(HBEntitySimple.class));
        assertEquals("#", HBEntityUtils.getAnnotatedKeySeparator(HBEntityComplex.class));
    }

    @Test
    public void getHBEntityKey() {
        HBEntitySimple simple = new HBEntitySimple();
        simple.keyCol = "key";
        simple.dataCol = "data";
        assertEquals("key", HBEntityUtils.getHBEntityKey(simple));

        String strDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        HBEntityComplex complex = new HBEntityComplex();
        complex.keyOne = "A";
        complex.keyTwo = strDate;
        complex.strCol = "COLUMN";
        complex.dtCol = strDate;
        complex.cpfCol = "11111111111";
        assertEquals("A#" + strDate, HBEntityUtils.getHBEntityKey(complex));
    }

    @Test(expected = RuntimeException.class)
    public void extractHBKey_noHBEntity() {
        HBEntityUtils.getHBEntityKey(new Object());
    }

    @Test
    public void getHBEntityMappedColumnValues() {
        HBEntitySimple simple = new HBEntitySimple();
        simple.keyCol = "key";
        simple.dataCol = "data";
        List<HBColumnValue> values = HBEntityUtils.getHBEntityMappedColumnValues(simple);
        assertEquals("KEY_COL", values.get(0).getKey());
        assertEquals("key", values.get(0).getValue());
        assertEquals("DATA_COL", values.get(1).getKey());
        assertEquals("data", values.get(1).getValue());

        String strDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        HBEntityComplex complex = new HBEntityComplex();
        complex.keyOne = "A";
        complex.keyTwo = strDate;
        complex.strCol = "COLUMN";
        complex.dtCol = strDate;
        complex.cpfCol = "11111111111";
        values = HBEntityUtils.getHBEntityMappedColumnValues(complex);
        assertEquals("KEY_ONE", values.get(0).getKey());
        assertEquals("A", values.get(0).getValue());
        assertEquals("KEY_TWO", values.get(1).getKey());
        assertEquals(strDate, values.get(1).getValue());
        assertEquals("STR_COL", values.get(2).getKey());
        assertEquals("COLUMN", values.get(2).getValue());
        assertEquals("DT_COL", values.get(3).getKey());
        assertEquals(strDate, values.get(3).getValue());
        assertEquals("CPF", values.get(4).getKey());
        assertEquals("11111111111", values.get(4).getValue());
    }

    @Test
    public void extractHBValues_noHBEntity() {
        assertTrue(HBEntityUtils.getHBEntityMappedColumnValues(new Object()).isEmpty());
    }

    @Test
    public void getHBTableName() {
        assertEquals(TableName.valueOf("ABC", "HB_ENTITY_SIMPLE"),
                HBEntityUtils.getHBTableName(HBEntitySimple.class));
        assertEquals(TableName.valueOf("ABC", "COMPLEX_TABLE"),
                HBEntityUtils.getHBTableName(HBEntityComplex.class));
    }

}
