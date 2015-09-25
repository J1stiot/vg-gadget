package entity;

/**
 * sms action attributes
 */
public class SmsAttributes {

    //phone number
    public String mobile;

    //message type:code or message
    private int sendtype;

    //message content
    private String content;

    public SmsAttributes() {
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getSendtype() {
        return sendtype;
    }

    public void setSendtype(int sendtype) {
        this.sendtype = sendtype;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
