package tempest.google.auth;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import tempest.google.pubsub.PubsubService;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by alexandre.costa on 01/10/17.
 */
public class IamUtils {

    public final static String SCOPE_CLOUD_PLATFORM = "https://www.googleapis.com/auth/cloud-platform";
    public final static String SCOPE_PUBSUB = "https://www.googleapis.com/auth/pubsub";
    public final static String SCOPE_TASK_QUEUE = "https://www.googleapis.com/auth/taskqueue";
    public final static String SCOPE_TASK_QUEUE_CONSUMER = "https://www.googleapis.com/auth/taskqueue.consumer";


    public final static List<String> DEFAULT_APP_SCOPES =
            Arrays.asList(SCOPE_CLOUD_PLATFORM, SCOPE_PUBSUB, SCOPE_TASK_QUEUE, SCOPE_TASK_QUEUE_CONSUMER);

    private static final Logger logger = Logger.getLogger(PubsubService.class.getName());

    private static final AppIdentityService DEFAULT_SERVICE_ACCOUNT = AppIdentityServiceFactory.getAppIdentityService();

    public static String getDefaultApplicationName() {
        return DEFAULT_SERVICE_ACCOUNT.getServiceAccountName();
    }

    public static String getDefaultAccessToken() {
        try {
            return DEFAULT_SERVICE_ACCOUNT.getAccessToken(DEFAULT_APP_SCOPES).getAccessToken();
        } catch (Exception e) {
            logger.log(Level.FINE, "Error obtaining access token for {0}!", e);
            return "INVALID";
        }
    }

}
