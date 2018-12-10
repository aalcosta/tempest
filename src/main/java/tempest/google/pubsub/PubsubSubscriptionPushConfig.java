package tempest.google.pubsub;

/**
 * Created by alexandre.costa on 04/09/17.
 */
public class PubsubSubscriptionPushConfig {

    public String pushEndpoint;

    public PubsubSubscriptionPushConfig() {}

    public PubsubSubscriptionPushConfig(String pushEndpoint) {
        this.pushEndpoint = pushEndpoint;
    }
}
