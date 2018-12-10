package tempest.rest;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
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
 *
 * Developed as and strategy implementation, it adjusts the RestRequest to be executed by one of the two relying
 * HTTP libs apache HttpClient (for generic test and usage) or Google URL Fetch to be used inside Google App Engine
 *
 * Created by alexandre on 8/17/16.
 */
class RestInvoker {

    private static final Logger logger = Logger.getLogger(RestInvoker.class.getName());

    // Determinines the startegy based on 'testing' system property
    private static final boolean testing = !StringUtil.isEmptyOrWhitespace(System.getProperty("testing"));

    /**
     * Executes an agnostic RestRequest
     * @param request
     * @return Agnostic RestResponse
     */
    public static RestResponse doRequest(RestRequest request) {
        return doRequest(request.method, request.url, request.body, request.headers.toArray(new RestHeader[]{}));
    }

    /**
     * Executes an agnostic Http request based on the params
     * @param method
     * @param uri
     * @param body
     * @param headers
     * @return Agnostic RestResponse
     */
    public static RestResponse doRequest(String method, String uri, Object body, RestHeader... headers) {
        return testing ? doHttpClientRequest(method, uri, body, headers)
                : doUrlFetchRequest(method, uri, body, headers);
    }

    /**
     * Create an URL Fetch Request
     * @param method
     * @param uri
     * @return
     * @throws IOException
     */
    private static HTTPRequest createUrlFetchRequest(String method, String uri) throws IOException {
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

    /**
     * Create an HttpClient Request
     * @param method
     * @param uri
     * @return
     * @throws IOException
     */
    private static Request createHttpClientRequest(String method, String uri) {
        if (HttpMethod.DELETE.equals(method)) {
            return Request.Delete(uri);
        } else if (HttpMethod.GET.equals(method)) {
            return Request.Get(uri);
        } else if (HttpMethod.POST.equals(method)) {
            return Request.Post(uri);
        } else if (HttpMethod.PUT.equals(method)) {
            return Request.Put(uri);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static RestResponse doUrlFetchRequest(String method, String url, Object payload, RestHeader... headers) {
        try {
            final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

            String requestContentType = ContentType.APPLICATION_JSON.toString();
            final HTTPRequest request = createUrlFetchRequest(method, url);
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

    private static RestResponse doHttpClientRequest(String method, String uri, Object payload, RestHeader... headers) {
        try {
            ContentType requestContentType = ContentType.APPLICATION_JSON; // default
            Request request = createHttpClientRequest(method, uri);
            if (headers != null) {
                for (RestHeader header : headers) {
                    if (header.name.equals(HttpHeaders.CONTENT_LENGTH)) continue;

                    if (header.name.equals(HttpHeaders.CONTENT_TYPE)) {
                        requestContentType = parseHttpClientContentType(header);
                    }

                    request.setHeader(new BasicHeader(header.name, header.value));
                }
            }
            if (payload != null) {
                if (payload instanceof InputStream) {
                    request.bodyStream((InputStream) payload, requestContentType);
                } else if (payload instanceof String) {
                    request.bodyString((String) payload, requestContentType);
                } else {
                    request.bodyString(JsonUtils.toJson(payload), requestContentType);
                }
            }

            final HttpResponse httpResponse = request.execute().returnResponse();
            final HttpEntity responseEntity = httpResponse.getEntity();
            final String content = (responseEntity == null) ? null : IOUtils.toString(responseEntity.getContent(), EncodingUtils.DEFAULT_CHARSET);

            final RestResponse response = new RestResponse(content, httpResponse.getStatusLine().getStatusCode());
            if (response.status == 201) {
                response.headers.put(HttpHeaders.LOCATION, httpResponse.getFirstHeader(HttpHeaders.LOCATION).getValue());
            }
            return response;
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private static ContentType parseHttpClientContentType(RestHeader header) {
        String[] aux = header.value.split("; charset=");
        String mimeType = aux[0];
        String charset = aux.length > 1 ? aux[1] : null;
        return ContentType.create(mimeType,
                StringUtils.isEmpty(charset) ? EncodingUtils.DEFAULT_CHARSET.name() : charset);
    }

}
