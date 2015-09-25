package com.zen.smartweather.entity;

/**
 * 虚拟天气设备上发天气 trigger
 */
public class WeatherTrigger {
    //triggerId
    private String triggerId;
    //timestamp
    private long timestamp;
    //policy
    private int policy;
    //notify
    private int notify;

    //attributes
    WeatherAttributes attributes;

    public String getTriggerId() {
        return triggerId;
    }

    public void setTriggerId(String triggerId) {
        this.triggerId = triggerId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public WeatherAttributes getAttributes() {
        return attributes;
    }

    public void setAttributes(WeatherAttributes attributes) {
        this.attributes = attributes;
    }

    public int getPolicy() {
        return policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }

    public int getNotify() {
        return notify;
    }

    public void setNotify(int notify) {
        this.notify = notify;
    }

    public WeatherTrigger() {
    }

    public WeatherTrigger(String triggerId, long timestamp, int policy, int notify, WeatherAttributes attributes) {
        this.triggerId = triggerId;
        this.timestamp = timestamp;
        this.policy = policy;
        this.notify = notify;
        this.attributes = attributes;
    }
}
