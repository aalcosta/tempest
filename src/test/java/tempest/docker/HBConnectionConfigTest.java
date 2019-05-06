package tempest.docker;

import tempest.hbase.HBConnectionConfig;
import org.junit.Ignore;

@Ignore
public class HBConnectionConfigTest implements HBConnectionConfig {

    @Override
    public String getHost() {
        return "hbase-server";
    }

    @Override
    public String getPort() {
        return "2181";
    }

    @Override
    public String getUser() {
        return "";
    }

    @Override
    public String getKeyTabPath() {
        return "";
    }

    @Override
    public Boolean isKerberosLogin() {
        return false;
    }

}
