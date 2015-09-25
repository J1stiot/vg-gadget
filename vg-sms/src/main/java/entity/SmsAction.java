package entity;

/**
 * receive action json fofrmat
 *
 * */
public class SmsAction {

    //actionId
    private String actionId;

    //timestamp 时间戳
    private long timestamp;

    //attributes
    public SmsAttributes attributes;

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public SmsAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(SmsAttributes attributes) {
        this.attributes = attributes;
    }
}
