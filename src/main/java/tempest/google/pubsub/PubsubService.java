package tempest.google.pubsub;

import tempest.google.auth.IamUtils;
import tempest.rest.RestHeader;
import tempest.rest.RestResponse;

import static java.text.MessageFormat.format;
import static tempest.rest.RestHelper.doDeleteRequest;
import static tempest.rest.RestHelper.doPostRequest;
import static tempest.rest.RestHelper.doPutRequest;

/**
 * Created by alexandre.costa on 05/09/17.
 */
public class PubsubService {

    private final String endPoint;

    public PubsubService() {
        this.endPoint = "https://pubsub.googleapis.com/v1";
    }

    public PubsubService(String protocol, String host, int port) {
        this.endPoint = format("{0}://{1}:{2}/v1", protocol.toLowerCase(), host, String.valueOf(port));
    }

    public String getEndPoint() {
        return this.endPoint;
    }

    public RestResponse createTopic(PubsubTopicName topicName) {
        String resource = format("projects/{0}/topics/{1}", topicName.getProject(), topicName.getTopic());
        return doPutRequest(format("{0}/{1}", endPoint, resource), null);
    }

    public RestResponse deleteTopic(PubsubTopicName topicName) {
        return doDeleteRequest(format("{0}/{1}", endPoint, topicName));
    }

    public RestResponse createSubscription(PubsubTopicName topicName, PubsubSubscriptionName subscriptionName, String pushEndPoint) {
        String resource = format("projects/{0}/subscriptions/{1}", subscriptionName.getProject(), subscriptionName.getSubscriptionName());
        return doPutRequest(format("{0}/{1}", endPoint, resource), new PubsubSubscription(topicName, pushEndPoint));
    }

    public RestResponse deleteSubscription(PubsubSubscriptionName subscriptionName) {
        return doDeleteRequest(format("{0}/{1}", endPoint, subscriptionName));
    }

    public RestResponse publish(PubsubTopicName topicName, String message) {
        String resource = format("{0}/{1}:publish", endPoint, topicName);
        return doPostRequest(resource, this.createPubsubMessage(message),
                new RestHeader("Authorization", "Bearer " + IamUtils.getDefaultAccessToken()));
    }

    private PubsubMessages createPubsubMessage(String message) {
        PubsubMessages pubsubMessages = new PubsubMessages();
        pubsubMessages.messages.add(new PubsubMessage(message));
        return pubsubMessages;
    }


}
