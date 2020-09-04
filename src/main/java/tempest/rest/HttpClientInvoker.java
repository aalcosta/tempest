package tempest.rest;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handler for executing RestRequests and returning RestResponses.
 *
 * Developed as and strategy implementation, it adjusts the utils.rest.RestRequest to be executed by one of the two relying
 * HTTP libs apache HttpClient (for generic test and usage) or Google URL Fetch to be used inside Google App Engine
 *
 * Created by alexandre on 8/17/16.
 */
public class HttpClientInvoker {

    private static final Logger logger = Logger.getLogger(HttpClientInvoker.class.getName());

    /**
     * Executes an agnostic utils.rest.RestRequest
     * @param request
     * @return Agnostic utils.rest.RestResponse
     */
    public static RestResponse doRequest(RestRequest request) {
        return doRequest(request.method, request.url, request.body, request.headers.toArray(new RestHeader[]{}));
    }

    /**
     * Executes an agnostic Http request based on the params
     * @param method
     * @param uri
     * @param payload
     * @param headers
     * @return Agnostic utils.rest.RestResponse
     */
    public static RestResponse doRequest(String method, String uri, Object payload, RestHeader... headers) {
        try {
            ContentType requestContentType = ContentType.APPLICATION_JSON; // default
            Request request = createRequest(method, uri);
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

    private static Request createRequest(String method, String uri) {
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

    private static ContentType parseHttpClientContentType(RestHeader header) {
        String[] aux = header.value.split("; charset=");
        String mimeType = aux[0];
        String charset = aux.length > 1 ? aux[1] : null;
        return ContentType.create(mimeType,
                StringUtils.isEmpty(charset) ? EncodingUtils.DEFAULT_CHARSET.name() : charset);
    }

}
