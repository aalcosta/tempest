package tempest.rest;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.entity.ContentType;
import tempest.encoding.EncodingUtils;
import tempest.json.JsonUtils;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;

/**
 * Handler for executing RestRequests and returning RestResponses.
 * <p>
 * Developed as and strategy implementation to implement integration with Google URL Fetch to be used inside Google App Engine
 * <p>
 * Created by alexandre on 8/17/16.
 */
public class UrlFetchInvoker {

    private static final Logger logger = Logger.getLogger(UrlFetchInvoker.class.getName());

    /**
     * Executes an agnostic RestRequest
     *
     * @param request
     * @return Agnostic RestResponse
     */
    public static RestResponse doRequest(RestRequest request) {
        return doRequest(request.method, request.url, request.body, request.headers.toArray(new RestHeader[]{}));
    }

    /**
     * Executes an agnostic Http request based on the params
     * @param method
     * @param url
     * @param payload
     * @param headers
     * @return Agnostic utils.rest.RestResponse
     */
    public static RestResponse doRequest(String method, String url, Object payload, RestHeader... headers) {
        try {
            final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

            String requestContentType = ContentType.APPLICATION_JSON.toString();
            final HTTPRequest request = createRequest(method, url);
            for (RestHeader header : headers) {
                if (header.name.equals("Content-Type")) requestContentType = header.value;
                request.setHeader(new HTTPHeader(header.name, header.value));
            }
            if (payload != null) {
                request.setHeader(new HTTPHeader(CONTENT_TYPE, requestContentType));
                byte[] payloadData = null;
                if (payload instanceof InputStream) {
                    payloadData = IOUtils.toByteArray((InputStream) payload);
                } else if (payload instanceof String) {
                    payloadData = EncodingUtils.getBytes((String) payload);
                } else {
                    payloadData = EncodingUtils.getBytes(JsonUtils.toJson(payload));
                }
                request.setPayload(payloadData);
            }
            final HTTPResponse fetch = urlFetchService.fetch(request);
            final byte[] httpResponseContent = fetch.getContent();
            final String responseContent = httpResponseContent != null ? EncodingUtils.asString(httpResponseContent) : null;

            final int responseCode = fetch.getResponseCode();
            final RestResponse response = new RestResponse(responseContent, responseCode);
            if (responseCode == 201) {
                for (HTTPHeader respHeader : fetch.getHeadersUncombined()) {
                    if (respHeader.getName().equals(HttpHeaders.LOCATION)) {
                        response.headers.put(HttpHeaders.LOCATION, respHeader.getValue());
                    }
                }
            }
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Create an URL Fetch Request
     *
     * @param method
     * @param uri
     * @return
     * @throws IOException
     */
    private static HTTPRequest createRequest(String method, String uri) throws IOException {
        if (HttpMethod.DELETE.equalsIgnoreCase(method)) {
            return new HTTPRequest(new URL(uri), HTTPMethod.DELETE);
        } else if (HttpMethod.GET.equalsIgnoreCase(method)) {
            return new HTTPRequest(new URL(uri), HTTPMethod.GET);
        } else if (HttpMethod.POST.equalsIgnoreCase(method)) {
            return new HTTPRequest(new URL(uri), HTTPMethod.POST);
        } else if (HttpMethod.PUT.equalsIgnoreCase(method)) {
            return new HTTPRequest(new URL(uri), HTTPMethod.PUT);
        } else {
            throw new UnsupportedOperationException("Unknown method " + method);
        }
    }

}
