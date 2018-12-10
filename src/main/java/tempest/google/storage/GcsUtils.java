package tempest.google.storage;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.StringUtil;
import org.apache.commons.codec.binary.Base64;
import tempest.encoding.EncodingUtils;
import tempest.google.auth.IamUtils;
import tempest.rest.RestHeader;
import tempest.rest.RestResponse;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Logger;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;
import static javax.ws.rs.core.Response.Status.Family.familyOf;
import static tempest.encoding.EncodingUtils.DEFAULT_CHARSET;
import static tempest.rest.RestHelper.doGetRequest;
import static tempest.rest.RestHelper.doPostRequest;

public class GcsUtils {

    private static final Logger logger = Logger.getLogger(GcsUtils.class.getName());

    private static final AppIdentityService APP_IDENTITY_SERVICE = AppIdentityServiceFactory.getAppIdentityService();
    private static final boolean testing = !StringUtil.isEmptyOrWhitespace(System.getProperty("testing"));

    private static final String BASE_URL = "https://storage.googleapis.com";
    private static final String STORAGE_URL = BASE_URL + "/storage/v1";
    private static final String UPDLOAD_URL = BASE_URL + "/upload/" + STORAGE_URL;

    @Deprecated
    public static String getApplicationId() {
        return !testing ? APP_IDENTITY_SERVICE.getServiceAccountName() : "TEST_APP_ID";
    }

    public static String createSignedUrl(String httpMethod, String bucket, String contentName, String contentType, long expiration,
                                         String googleId) {
        final String gcsResource = "/" + bucket + "/" + contentName;
        final String stringToSign = stringToSign(httpMethod, "", contentType, expiration, "", gcsResource);
        final String signature = sign(stringToSign);
        final StringBuilder signedUrl = new StringBuilder(BASE_URL).append(gcsResource);
        signedUrl.append("?GoogleAccessId=").append(googleId);
        signedUrl.append("&Expires=").append(expiration);
        signedUrl.append("&Signature=").append(signature);
        return signedUrl.toString();
    }

    private static String stringToSign(String httpMethod, String contentMd5, String contentType, long expiration,
                                       String xHeaders, String resource) {
        return httpMethod + "\n" + contentMd5 + "\n" + contentType + "\n" + expiration + "\n" + xHeaders + resource;
    }

    private static String sign(final String toSign) {
        try {
            final AppIdentityService.SigningResult signingResult = APP_IDENTITY_SERVICE.signForApp(toSign.getBytes());
            final String signature = new String(Base64.encodeBase64(signingResult.getSignature()), DEFAULT_CHARSET);
            return URLEncoder.encode(signature, DEFAULT_CHARSET.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static RestResponse upload(String bucket, String contentName, String contentType, InputStream content) {
        String accessToken = IamUtils.getDefaultAccessToken();
        final String resource = UPDLOAD_URL + "/b/" + bucket + "/o?uploadType=media&name=" + contentName;

        logger.fine("Upload URL: " + resource);
        return doPostRequest(resource, content,
                new RestHeader(CONTENT_TYPE, contentType),
                new RestHeader(AUTHORIZATION, "Bearer " + accessToken)
        );
    }

//    public static RestResponse upload(String bucket, String contentName, String contentType, InputStream content) {
//        String accessToken = IamUtils.getDefaultAccessToken();
//        final String resource = UPDLOAD_URL + "/b/" + bucket + "/o?uploadType=multipart&name=" + contentName;
//        return doPostRequest(resource, content,
//                new RestHeader(CONTENT_TYPE, contentType),
//                new RestHeader(AUTHORIZATION, "Bearer " + accessToken)
//        );
//    }

    public static byte[] download(String bucket, String object) {
        String accessToken = IamUtils.getDefaultAccessToken();
        final String resource = STORAGE_URL + "/b/" + bucket + "/o/" + object;
        RestResponse restResponse = doGetRequest(resource,
                new RestHeader(AUTHORIZATION, "Bearer " + accessToken));
        return familyOf(restResponse.status) == SUCCESSFUL
                ? EncodingUtils.getBytes(restResponse.content)
                : null;
    }



}
