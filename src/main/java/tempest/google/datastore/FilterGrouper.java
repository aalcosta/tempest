package tempest.google.datastore;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;

import java.util.ArrayList;
import java.util.List;

public class FilterGrouper {

    private List<Filter> filterList;

    public FilterGrouper() {
        this.filterList = new ArrayList<>();
    }

    public void addFilter(Filter filter) {
        if (filter != null) {
            filterList.add(filter);
        }
    }

    public Filter joinFilters(Query.CompositeFilterOperator operator) {
        if (filterList.isEmpty()) {
            return null;
        }
        if (filterList.size() == 1) {
            return filterList.get(0);
        }

        return operator.of(filterList);

    }

}
