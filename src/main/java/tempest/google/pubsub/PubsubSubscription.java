package tempest.google.pubsub;

/**
 * Created by alexandre.costa on 04/09/17.
 */
public class PubsubSubscription {

    public String topic;
    public PubsubSubscriptionPushConfig pushConfig;

    public PubsubSubscription() {
    }

    public PubsubSubscription(PubsubTopicName topicName) {
        this(topicName, null);
    }

    public PubsubSubscription(PubsubTopicName topicName, String pushEndpoint) {
        this.topic = topicName.toString();
        this.pushConfig = new PubsubSubscriptionPushConfig(pushEndpoint);
    }

}
