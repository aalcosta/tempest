package tempest.google.pubsub;

import tempest.encoding.EncodingUtils;

/**
 * Created by alexandre.costa on 05/09/17.
 */
public class PubsubMessage {

    private String messageId;

    private String data;

    public PubsubMessage() {}

    public PubsubMessage(String message) {
        this.setRawData(message);
    }

    public String getMessageId() {
        return messageId;
    }

    public String getData() { return EncodingUtils.decodeBase64ToString(data); }

    public String getRawData() { return data; }

    public void setRawData(String message) {
        this.data = EncodingUtils.encodeBase64(message);
    }

}
