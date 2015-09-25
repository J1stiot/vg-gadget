import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * 测试收取消息从对于的topic上
 */
public class SubTest {
    public static void main(String[] args) {

        String topic        = "just/just-vg-weather/device/1jy2pyghjjgaa/trigger";
        //String topic        = "test1";
        String broker       = "tcp://127.0.0.1:1883";
        int qos             = 1;
        String clientId     = "vmqsub-1";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            sampleClient.subscribe(topic, qos);
            sampleClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable arg0) {
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken arg0) {

                }

                @Override
                public void messageArrived(String tipic, MqttMessage msg) throws Exception {
                    System.out.println("["+tipic+"] channel sub message's "+ msg.toString());
                }

            });
            while(!Thread.interrupted()){
                try {
                    Thread.sleep(Integer.MAX_VALUE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
