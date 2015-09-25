package com.zen.smartweather.tcp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MQTT 客户端连接注册表
 */
public enum Registry {

    INSTANCE;

    private final Map<String,MqttConnThread> map=new ConcurrentHashMap<>();

    // connect pool
    private final ExecutorService es = Executors.newFixedThreadPool(400);

    //save session
    public void saveSession(String areaId,MqttConnThread client){
        this.map.put(areaId,client);
    }

    //delete session
    public void removeSession(String areaId){
        this.map.remove(areaId);
    }

    //get Session
    public Map<String,MqttConnThread> getSession(){
        return this.map;
    }

    //start client Thread
    public void startThread(MqttConnThread mqttConnThread){
        this.es.submit(mqttConnThread);
    }

}
