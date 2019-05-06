package tempest.hbase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface HBEntity {

    String namespace();

    String table() default "";

    String family() default "";

    String keySeparator() default "|";

}
