package mqtt;

import entity.SmsAction;
import entity.SmsAttributes;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import util.JsonUtils;
import yunpian.YunPianClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * sms message subscribe client
 */
@Component
public class SmsSubClient extends Thread {

    //Logger
    private static Logger logger = LoggerFactory.getLogger(SmsSubClient.class);

    //mqtt client
    private MqttClient mqttClient;

    // mqtt connect options
    private MqttConnectOptions options;

    //tcp
    private String tcp;

    //clentId
    private String clientId;

    //topic
    private String topic = "just/just-vg-sms/gadget/*";

    @PostConstruct
    public void init(){
        logger.debug("短信客户端开始启动...");
        //加载配置文件
        PropertiesConfiguration properties = null;
        try {
            logger.debug("初始化加载配置文件开始...");
            properties = new PropertiesConfiguration("config.properties");
            if (!properties.getString("tcp.host").equals("")) {
                options = new MqttConnectOptions();
                options.setUserName(properties.getString("mqtt.userName"));
                options.setPassword(properties.getString("mqtt.passWord").toCharArray());
                tcp = "tcp://" + properties.getString("tcp.host") + ":" + properties.getString("tcp.port");
                clientId = properties.getString("clientId");
                MemoryPersistence persistence = new MemoryPersistence();
                mqttClient = new MqttClient(tcp, clientId, persistence);
                mqttClient.connect(options);
            }
            logger.debug("初始化加载配置文件成功！");
        } catch (Exception e) {
            logger.debug("客户端加载配置文件失败！");
            e.printStackTrace();
        }

        if (this.mqttClient.isConnected()) {
            this.start();
        } else {
            new SmsSubClient();
        }

    }

    @Override
    public void run() {
        //订阅消息
        try {
            this.mqttClient.subscribe(topic,2);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        this.mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                new SmsSubClient();
            }

            //处理接收到的消息
            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                //消息到达，解析获取json数据
                String action = mqttMessage.toString();
                SmsAttributes smsAction = JsonUtils.OBJECT_MAPPER.readValue(action, SmsAttributes.class);
                System.out.println("客户端收到的消息为：" + action);
                //SmsAttributes attributes = smsAction.getAttributes();
                //发送的
                //执行action
                YunPianClient.sendSms(smsAction.getSendtype(), smsAction.getContent(), smsAction.getMobile());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    /**
     * 客户端生命周期的开始  通过Spring IoC调用
     */
    @PreDestroy
    public void shutDown() {
        logger.info("Sms MQTT 客户端正在停止...");
        try {
            mqttClient.disconnect();
            logger.info("Sms MQTT 客户端成功停止...");
        } catch (MqttException e) {
            logger.error("Sms MQTT 客户端终止失败：{}", e.getMessage());
        }
    }

}
