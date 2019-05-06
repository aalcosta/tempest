package tempest.hbase;

public interface HBConnectionConfig {

    String getHost();

    String getPort();

    String getUser();

    String getKeyTabPath();

    Boolean isKerberosLogin();

}
