package com.zen.smartweather;

import com.zen.smartweather.quartz.OpenWeatherJob;
import com.zen.smartweather.tcp.MqttConnThread;
import com.zen.smartweather.tcp.Registry;
import com.zen.smartweather.util.ExcelUtils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * 启动服务
 */
public class SmartWeatherService {
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(SmartWeatherService.class);

    public static void main(String[] args) throws Exception {
        // 加载配置文件
        String f = args.length >= 1 ? args[0] : "tcp.properties";
        PropertiesConfiguration config = new PropertiesConfiguration(f);

        // 解析 areaid_f.xlsx文件，获取区域码
        Set<String> areaIds = ExcelUtils.loadAreaIds();

        // 启动tcp连接客户端
        final String url = "tcp://" + config.getString("tcp.host") + ":" + config.getString("tcp.port");
        MqttClient mqttClient;
        MqttConnectOptions options = new MqttConnectOptions();

        //远程mqtt服务器连接选项设置
        //options.setUserName(config.getString("mqtt.userName"));
        //options.setPassword(config.getString("mqtt.passWord").toCharArray());
        options.setKeepAliveInterval(30);
        MemoryPersistence persistence = new MemoryPersistence();

        //把每个地区都作为一个设备连接到mqtt
        for (final String areaId : areaIds) {
            mqttClient = new MqttClient(url, areaId, persistence);
            MqttConnThread mqttConnThread = new MqttConnThread(mqttClient, options);
            //添加到线程池
            Registry.INSTANCE.startThread(mqttConnThread);
            //保存mqtt连接信息
            Registry.INSTANCE.saveSession(areaId, mqttConnThread);
        }

        // 启动quartz任务
        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        try {
            for (String areaId : areaIds) {
                Trigger trigger = newTrigger()
                        .withIdentity(areaId, "weather")
                        .withSchedule(cronSchedule(config.getString("scheduler.cron")))
                        .build();
                JobDetail job = newJob(OpenWeatherJob.class)
                        .withIdentity(areaId, "weather")
                        .usingJobData("AreaId", areaId)
                        .build();
                scheduler.scheduleJob(job, trigger);
            }
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error("Error when adding Open Weather job to Quartz scheduler: {}", ExceptionUtils.getMessage(e));
        }
    }
}
