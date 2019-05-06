package tempest.hbase;

public class HBColumnValue {

    private String family;
    private String key;
    private String value;

    public HBColumnValue(String family, String key, String value) {
        this.family = family;
        this.key = key;
        this.value = value;
    }

    public String getFamily() {
        return family;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
