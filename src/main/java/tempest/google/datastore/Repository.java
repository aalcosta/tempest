package tempest.google.datastore;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alexandre on 3/14/17.
 */
public abstract class Repository<E> {

    public static final String SEPARATOR = "#";
    protected static DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

    public void delete(String id) {
        final Key dsKey = KeyFactory.createKey(kind(), id);
        dataStore.delete(dsKey);
    }

    public E get(String id) {
        try {
            final Key dsKey = dsKey(id);
            final Entity dsEntity = dataStore.get(dsKey);
            return fromEntity(dsEntity);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    protected Key dsKey(String id) {
        return KeyFactory.createKey(kind(), id);
    }

    public Key put(E entity) {
        return dataStore.put(toEntity(entity));
    }

    public List<Key> put(Collection<E> entitiesToUpdate) {
        List<Entity> entities = new ArrayList<>();

        for (E entity : entitiesToUpdate) {
            entities.add(toEntity(entity));
        }

        return dataStore.put(entities);
    }

    public abstract String kind();

    protected String getTextPropertyFrom(Entity dsEntity, String propName) {
        Object value = dsEntity.getProperty(propName);
        return value != null ? ((Text) value).getValue() : null;
    }

    protected abstract Entity toEntity(E type);

    protected abstract E fromEntity(Entity entity);

}
