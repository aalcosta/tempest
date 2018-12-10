package tempest.google.pubsub;

import static java.text.MessageFormat.format;

/**
 * Created by alexandre.costa on 05/09/17.
 */
public class PubsubTopicName {

    private String project;
    private String topic;

    public PubsubTopicName() {
    }

    public PubsubTopicName(String project, String topic) {
        this.project = project;
        this.topic = topic;
    }

    public String getProject() {
        return this.project;
    }

    public String getTopic() {
        return this.topic;
    }

    @Override
    public String toString() {
        return format("projects/{0}/topics/{1}", project, topic);
    }
}
