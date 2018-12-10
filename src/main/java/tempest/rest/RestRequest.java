package tempest.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Agnostic representation for a HttpRequest
 *
 * Created by alexandre on 1/27/17.
 */
public class RestRequest {

    public String method;
    public String url;
    public List<RestHeader> headers = new ArrayList<>();
    public Object body;

    public RestRequest(String method, String url) {
        this.method = method;
        this.url = url;
    }

    public RestRequest(String method, String url, Object body, RestHeader... headers) {
        this(method, url);
        this.body = body;
        this.headers.addAll(Arrays.asList(headers));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RestRequest that = (RestRequest) o;

        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        return url == null ? that.url == null
                : url.indexOf('?') < 0 ? url.equals(that.url)
                        : url.substring(0, url.indexOf('?')).equals(that.url.substring(0, url.indexOf('?')));
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        return result;
    }

}
