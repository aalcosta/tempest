package tempest.rest;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Hashtable;

import static javax.ws.rs.HttpMethod.DELETE;
import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
import static javax.ws.rs.HttpMethod.PUT;
import static tempest.encoding.EncodingUtils.DEFAULT_CHARSET;

/**
 * Created by alexandre on 8/17/16.
 */
public class RestHelper {

    private static final Hashtable<RestRequest, RestResponse> standardRequests = new Hashtable<>();

    public static void clearStandardRequests() {
        standardRequests.clear();
    }

    public static void setStandardRequest(RestRequest request, RestResponse response) {
        standardRequests.put(request, response);
    }

    public static Response buildCreateResponse(UriInfo uriInfo, Object resourceId) {
        UriBuilder builder = uriInfo.getAbsolutePathBuilder();
        final URI uri = builder.path(resourceId.toString()).build();
        return Response.created(uri).build();
    }

    public static RestResponse doDeleteRequest(String uri, RestHeader... headers) {
        return executeRequest(new RestRequest(DELETE, uri, null, headers));
    }

    public static RestResponse doGetRequest(String uri, RestHeader... headers) {
        return executeRequest(new RestRequest(GET, uri, null, headers));
    }

    public static RestResponse doPostRequest(String uri, Object entity, RestHeader... headers) {
        return executeRequest(new RestRequest(POST, uri, entity, headers));
    }

    public static RestResponse doPutRequest(String uri, Object entity, RestHeader... headers) {
        return executeRequest(new RestRequest(PUT, uri, entity, headers));
    }

    public static String getCreatedEntityId(RestResponse response) {
        final String value = response.headers.get(HttpHeaders.LOCATION);
        return value.substring(value.lastIndexOf("/") + 1);
    }

    public static String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private static RestResponse executeRequest(RestRequest request) {
        final RestResponse response = standardRequests.get(request);
        return (response != null) ? response : HttpClientInvoker.doRequest(request);
    }

}
