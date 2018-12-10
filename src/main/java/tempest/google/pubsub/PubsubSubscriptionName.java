package tempest.google.pubsub;

import static java.text.MessageFormat.format;

/**
 * Created by alexandre.costa on 05/09/17.
 */
public class PubsubSubscriptionName {

    private String project;
    private String subscription;

    public PubsubSubscriptionName() {
    }

    public PubsubSubscriptionName(String project, String subscription) {
        this.project = project;
        this.subscription = subscription;
    }

    public String getProject() {
        return this.project;
    }

    public String getSubscriptionName() {
        return this.subscription;
    }

    @Override
    public String toString() {
        return format("projects/{0}/subscriptions/{1}", project, subscription);
    }

}
