package tempest.rest;

import java.util.HashMap;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;

/**
 * Agnostic Representation of HTTP Response
 *
 * Created by alexandre on 1/27/17.
 */
public class RestResponse {

    public String content;
    public int status;
    public Map<String, String> headers = new HashMap<>();

    public RestResponse(String strContent, int statusCode) {
        this.content = strContent;
        this.status = statusCode;
    }

    public boolean isSuccess() {
        return familyOf(this.status) == SUCCESSFUL;
    }

    @Override
    public String toString() {
        return "RestResponse{" +
                "content='" + content + '\'' +
                ", status=" + status +
                ", headers=" + headers +
                '}';
    }
}
