package com.zen.smartweather.tcp;

import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * mqtt客户端连接线程
 *
 */

public class MqttConnThread implements Callable {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(MqttConnThread.class);

    //mqtt client
    private MqttClient mqttClient;
    //mqtt 连接选项
    MqttConnectOptions options;

    @Override
    public Object call() throws Exception {
        try {
            mqttClient.connect(options);
            //判断客户端是否连接上
            if (mqttClient.isConnected()) {
                //回调程序，处理断线
                mqttClient.setCallback(new MqttCallback() {
                    @Override
                    public void connectionLost(Throwable throwable) {
                        logger.debug("线程:{}开始重连：", mqttClient.getClientId());
                        Registry.INSTANCE.startThread(new MqttConnThread(mqttClient, options));
                    }

                    @Override
                    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {

                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                    }
                });
            }
            logger.debug("后台mqtt客户端:{}连接服务器 broker成功！", mqttClient.getClientId());
        } catch (Exception e) {
            logger.error("后台mqtt客户端:{}连接服务器 broker失败！", mqttClient.getClientId());
            Registry.INSTANCE.startThread(new MqttConnThread(mqttClient, options));
            Thread.sleep(2000);
        }
        return null;
    }

    public MqttConnThread(MqttClient mqttClient, MqttConnectOptions options) {
        this.mqttClient = mqttClient;
        this.options = options;
    }

    //发布信息
    public void sendMessage(String topic,String message){
        try {
            this.mqttClient.publish(topic,new MqttMessage(message.getBytes("utf-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MqttClient getMqttClient() {
        return mqttClient;
    }

    public MqttConnectOptions getOptions() {
        return options;
    }

    public void setOptions(MqttConnectOptions options) {
        this.options = options;
    }
}
