package tempest.json;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Helper utilities to make JSON usage, parse and format more intuitive
 *
 * Created by alexandre on 9/5/16.
 */
public class JsonUtils {

    private static GsonBuilder GSON_BUILDER;
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    static {
        GSON_BUILDER = new GsonBuilder();
        GSON_BUILDER.setDateFormat(ISO_DATE_FORMAT);
    }

    public static String toJson(Object entity) {
        return GSON_BUILDER.create().toJson(entity);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        return json != null ? GSON_BUILDER.create().fromJson(json, clazz) : null;
    }

    public static<T> T fromJsonElement(JsonElement jsonElement, Class<T> clazz) {
        return jsonElement != null ? GSON_BUILDER.create().fromJson(jsonElement, clazz) : null;
    }

    public static <T> List<T> fromJsonArray(String json, Class<T> clazz) {
        return GSON_BUILDER.create().fromJson(json, new EntityList<T>(clazz));
    }

    public static String parseJson(Reader reader) {
        return new JsonParser().parse(reader).getAsString();
    }

    private static class EntityList<X> implements ParameterizedType {

        private Class<?> wrapped;

        public EntityList(Class<X> wrapped) {
            this.wrapped = wrapped;
        }

        public Type[] getActualTypeArguments() {
            return new Type[]{wrapped};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }

    }

}
