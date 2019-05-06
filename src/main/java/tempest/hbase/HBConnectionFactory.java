package tempest.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.security.UserGroupInformation;

public class HBConnectionFactory {

    private HBConnectionConfig config;

    public HBConnectionFactory(HBConnectionConfig config) {
        this.config = config;
    }

    public Connection getConnection() throws Exception {
        return this.getConnection(config.getHost(), config.getPort(), config.getUser(), config.getKeyTabPath());
    }

    public Connection getConnection(String quorum, String port, String user, String keyPath) throws Exception {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", quorum);
        conf.set("hbase.zookeeper.port", port);

        if (config.isKerberosLogin()) {
            conf.set("hbase.rpc.protection", "authentication");
            conf.set("hadoop.security.authentication", "kerberos");
            conf.set("hbase.security.authentication", "kerberos");
            conf.set("hbase.regionserver.kerberos.principal", "hbase/_HOST@BR.EXPERIAN.LOCAL");

            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(user, keyPath);
            UserGroupInformation.getLoginUser().checkTGTAndReloginFromKeytab();
        }

        return ConnectionFactory.createConnection(conf);
    }

}
