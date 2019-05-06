package tempest.hbase;

import tempest.hbase.conventions.HBEntityUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static tempest.hbase.conventions.HBEntityUtils.getHBEntityFromDAO;
import static tempest.hbase.conventions.HBEntityUtils.getHBEntityKey;
import static tempest.hbase.conventions.HBEntityUtils.getHBEntityMappedColumnValues;
import static tempest.hbase.conventions.HBEntityUtils.getQualifiedName;
import static java.util.Objects.isNull;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static org.apache.hadoop.hbase.util.Bytes.toBytes;

public class HBaseDAO<T> {

    private Connection connection;

    private HBConnectionFactory hbConnectionFactory;

    public HBaseDAO(HBConnectionFactory hbConnectionFactory) {
        this.hbConnectionFactory = hbConnectionFactory;
    }

    public Optional<Result> getRaw(String key) {
        return scanRaw(key).findFirst();
    }

    public Stream<Result> scanRaw(String key) {
        String hbQualifiedName = getQualifiedName(getHBEntityFromDAO(this.getClass()));
        try {
            Scan scan = new Scan()
                    .withStartRow(toBytes(key))
                    .withStopRow(toBytes(key))
                    .setRowPrefixFilter(toBytes(key));

            ResultScanner scanner = getHBConnection().getTable(TableName.valueOf(hbQualifiedName)).getScanner(scan);
            return StreamSupport.stream(spliteratorUnknownSize(scanner.iterator(), ORDERED), false);
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan table '" + hbQualifiedName + "' with key '" + key + "'", e);
        }
    }

    public String save(T target) throws RuntimeException {
        try {
            Class<?> hbMappedEntity = getHBEntityFromDAO(this.getClass());
            Table table = getHBConnection().getTable(TableName.valueOf(getQualifiedName(hbMappedEntity)));

            String hbKey = getHBEntityKey(target);
            Put put = new Put(toBytes(hbKey));
            getHBEntityMappedColumnValues(target).forEach(column -> {
                String annotatedFamily = column.getFamily();
                if (StringUtils.isEmpty(annotatedFamily)) {
                    annotatedFamily = HBEntityUtils.getAnnotatedFamily(hbMappedEntity);
                }
                put.addColumn(toBytes(annotatedFamily),
                        toBytes(column.getKey()),
                        toBytes(column.getValue()));
            });

            table.put(put);
            table.close();

            return hbKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save entity! Entity: " + target.toString(), e);
        }
    }

    protected String getAnnotatedKeySeparator() {
        return HBEntityUtils.getAnnotatedKeySeparator(getHBEntityFromDAO(this.getClass()));
    }

    protected String getAnnotatedFamily() {
        return HBEntityUtils.getAnnotatedFamily(getHBEntityFromDAO(this.getClass()));
    }

    protected String getStringValue(Result resultSet, String family, String qualify) {
        return Bytes.toString((resultSet.getValue(toBytes(family), toBytes(qualify))));
    }

    protected String getStringValue(Result resultSet, String qualify) {
        return Bytes.toString((resultSet.getValue(toBytes(getAnnotatedFamily()), toBytes(qualify))));
    }

    private Connection getHBConnection() throws RuntimeException {
        if (isNull(this.connection)) {
            try {
                this.connection = hbConnectionFactory.getConnection();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return connection;
    }


}
