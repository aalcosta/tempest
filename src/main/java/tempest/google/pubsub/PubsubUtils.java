package tempest.google.pubsub;

import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import tempest.json.JsonUtils;
import tempest.rest.RestResponse;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by alexandre.costa on 22/09/17.
 */
public class PubsubUtils {

    private static final Logger logger = Logger.getLogger(PubsubUtils.class.getName());

    public static void validateToken(HttpServletRequest request, String subscriptionToken) {
        if (!subscriptionToken.equals(request.getParameter("token"))) {
            logger.warning("Security alert! Pubsub PUSH message received with invalid TOKEN! Dropping message...");
            Response.noContent().build();
        }
    }

    public static PubsubMessage getMessage(HttpServletRequest request) throws IOException {
        String rawMessage = IOUtils.toString(request.getInputStream(), UTF_8);
        return JsonUtils.fromJson(rawMessage, PubsubSubscribedMessage.class).message;
    }

    public static <T> T getParsedMessage(HttpServletRequest request, Class<T> jsonClass) throws IOException {
        PubsubMessage message = getMessage(request);
        return JsonUtils.fromJson(message.getData(), jsonClass);
    }

    public static RestResponse publishMessage(String pubsubTopicName, Object object) {
        PubsubService pubsubService = new PubsubService();
        PubsubTopicName topicName = new PubsubTopicName(System.getProperty("app.id"), pubsubTopicName);

        return pubsubService.publish(topicName, new Gson().toJson(object));
    }

}
