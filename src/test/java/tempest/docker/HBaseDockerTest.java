package tempest.docker;

import tempest.hbase.HBConnectionFactory;
import org.apache.hadoop.hbase.client.Connection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;

@Ignore
public class HBaseDockerTest {

    protected HBConnectionFactory mockConnectionFactory;
    protected Connection hbConnection;
    protected static Process hbase;

    @BeforeClass
    public static void startHBase() throws Exception {
        hbase = Runtime.getRuntime().exec("docker-compose up hbase");
        Thread.sleep(2000);
    }

    @AfterClass
    public static void stopHBase() {
        hbase.destroy();
    }

    public void setupConnectionFactory() throws Exception {
        mockConnectionFactory = new HBConnectionFactory(new HBConnectionConfigTest());
        hbConnection = mockConnectionFactory.getConnection();
    }

}
