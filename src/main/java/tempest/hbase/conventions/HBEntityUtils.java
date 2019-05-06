package tempest.hbase.conventions;

import tempest.hbase.HBColumn;
import tempest.hbase.HBColumnValue;
import tempest.hbase.HBEntity;
import org.apache.hadoop.hbase.TableName;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class HBEntityUtils {

    public static <T> Class<T> getHBEntityFromDAO(Class<?> daoClass) {
        return (Class<T>) ((ParameterizedType) daoClass.getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public static HBEntity getHBEntityAnnotation(Class<?> targetClass) {
        HBEntity annotation = targetClass.getAnnotation(HBEntity.class);
        if (isNull(annotation)) throw new RuntimeException("HBEntity Annotation not found!");
        return annotation;
    }

    public static String getAnnotatedNamespace(Class<?> targetClass) {
        return targetClass.getAnnotation(HBEntity.class).namespace();
    }

    public static String getAnnotatedFamily(Class<?> targetClass) {
        HBEntity annotation = targetClass.getAnnotation(HBEntity.class);
        return !isEmpty(annotation.family()) ? annotation.family() : getAnnotatedTableName(targetClass) + "_FAM";
    }

    public static String getAnnotatedTableName(Class<?> targetClass) {
        HBEntity annotation = targetClass.getAnnotation(HBEntity.class);
        return !isEmpty(annotation.table()) ? annotation.table() : splitCamelCase(targetClass.getSimpleName(), "_");
    }

    public static String getAnnotatedKeySeparator(Class<?> targetClass) {
        HBEntity annotation = targetClass.getAnnotation(HBEntity.class);
        return annotation.keySeparator();
    }

    public static String getQualifiedName(Class<?> targetClass) {
        HBEntity annotation = targetClass.getAnnotation(HBEntity.class);
        return annotation.namespace() + ":" + getAnnotatedTableName(targetClass);
    }

    public static String getHBEntityKey(Object target) {
        HBEntity hbEntity = target.getClass().getAnnotation(HBEntity.class);
        return Arrays.stream(target.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(HBColumn.class) && f.getAnnotation(HBColumn.class).key() >= 0)
                .sorted(Comparator.comparingInt(f -> f.getDeclaredAnnotation(HBColumn.class).key()))
                .map(f -> extractValue(f, target))
                .collect(Collectors.joining(hbEntity.keySeparator()));
    }

    public static List<HBColumnValue> getHBEntityMappedColumnValues(Object target) {
        return Arrays.stream(target.getClass().getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(HBColumn.class))
                .map(f -> {
                    HBColumn annotation = f.getAnnotation(HBColumn.class);
                    String columnName = !isEmpty(annotation.value()) ?
                            annotation.value() :
                            splitCamelCase(f.getName(), "_");
                    return new HBColumnValue(annotation.family(), columnName, extractValue(f, target));
                })
                .collect(Collectors.toList());
    }

    public static TableName getHBTableName(Class<?> targetClass) {
        return TableName.valueOf(getAnnotatedNamespace(targetClass), getAnnotatedTableName(targetClass));
    }

    public static String extractValue(Field f, Object target) {
        try {
            f.setAccessible(true);
            return f.get(target).toString();
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Illegal target");
        }
    }

    private static String splitCamelCase(String name, String separator) {
        StringBuilder ret = new StringBuilder();
        for (String x : name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            ret.append(x.toUpperCase()).append(separator);
        }
        ret.setLength(ret.length() - 1);  // Remove last "separator"
        return ret.toString();
    }

}
