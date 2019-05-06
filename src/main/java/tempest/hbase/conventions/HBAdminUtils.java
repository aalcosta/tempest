package tempest.hbase.conventions;

import org.apache.hadoop.hbase.NamespaceDescriptor;
import org.apache.hadoop.hbase.NamespaceExistException;
import org.apache.hadoop.hbase.NamespaceNotFoundException;
import org.apache.hadoop.hbase.TableExistsException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotEnabledException;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder;
import org.apache.hadoop.hbase.client.TableDescriptorBuilder;

import java.io.IOException;

public class HBAdminUtils {

    public static boolean createNamespace(Admin admin, String namespace) throws IOException {
        NamespaceDescriptor nsDesc = NamespaceDescriptor.create(namespace).build();
        try {
            admin.createNamespace(nsDesc);
            return true;
        } catch (NamespaceExistException e) {
            return false;
        }
    }

    public static boolean createTable(Admin admin, Class<?> targetEntity) throws IOException {
        try {
            admin.createTable(TableDescriptorBuilder
                    .newBuilder(HBEntityUtils.getHBTableName(targetEntity))
                    .setColumnFamily(ColumnFamilyDescriptorBuilder.of(HBEntityUtils.getAnnotatedFamily(targetEntity)))
                    .build());
        } catch (TableExistsException e) {
            return false;
        }
        return true;
    }

    public static boolean dropNamespace(Admin admin, String namespace) throws IOException {
        try {
            admin.deleteNamespace(namespace);
            return true;
        } catch (NamespaceNotFoundException e) {
            // DO NOTHING;
            return false;
        }
    }

    public static boolean dropTable(Admin admin, Class<?> targetEntity) throws IOException {
        TableName tableName = TableName.valueOf(HBEntityUtils.getAnnotatedNamespace(targetEntity), HBEntityUtils.getAnnotatedTableName(targetEntity));
        try {
            admin.disableTable(tableName);
        } catch (TableNotFoundException e) {
            return false;
        } catch (TableNotEnabledException e) {
            // DO NOTHING
        }

        try {
            admin.deleteTable(tableName);
        } catch (TableNotFoundException e) {
            return false;
        }
        return true;
    }

}
