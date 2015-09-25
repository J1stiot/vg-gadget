package com.zen.smartweather.quartz;

import com.zen.smartweather.desc.Description;
import com.zen.smartweather.entity.*;
import com.zen.smartweather.tcp.MqttConnThread;
import com.zen.smartweather.tcp.Registry;
import com.zen.smartweather.util.GadgetIdUtil;
import com.zen.smartweather.util.JsonUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * OpenWeather Client
 * Fetch weather data from http://openweather.weather.com.cn
 */
public class OpenWeatherJob implements Job {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(OpenWeatherJob.class);

    // Open Weather Api Key
    private static final String APP_ID = "7cd2cc51b3087e54";
    private static final String PRIVATE_KEY = "8e6a2b_SmartWeatherAPI_71c071e";

    /**
     * Each (and every) time the scheduler executes the job, it creates a new instance of the class before calling its execute(..) method.
     * When the execution is complete, references to the job class instance are dropped, and the instance is then garbage collected.
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // The JobDataMap that is found on the JobExecutionContext during job execution serves as a convenience.
        // It is a merge of the JobDataMap found on the JobDetail and the one found on the trigger,
        // with the value in the latter overriding any same-named values in the former.
        // Storing JobDataMap values on a trigger can be useful in the case where you have a job
        // that is stored in the scheduler for regular/repeated use by multiple triggers,
        // yet with each independent triggering, you want to supply the job with different data inputs.
        // As a best practice, the code within the Job.execute( ) method should generally retrieve values from the JobDataMap
        // on found on the JobExecutionContext, rather than directly from the one on the JobDetail.
        JobDataMap jdm = context.getMergedJobDataMap();

        // Area Id should be passed into
        String areaId = jdm.getString("AreaId");
        if (StringUtils.isBlank(areaId)) {
            return;
        }

        // fetch with http client
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // forge url
            String date = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            String url = getUrl(areaId, date);
            // http get
            HttpGet httpGet = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                    logger.warn("Http error when getting weather information from OpenWeather, status code:{}", statusCode);
                }
                HttpEntity entity = response.getEntity();
                String content = EntityUtils.toString(entity, "UTF-8");
                if (content.equals("data error")) {
                    logger.warn("Data error when getting weather information from OpenWeather");
                } else {

                    // 获取预测信息
                    // parse json
                    ForecastResult forecast = JsonUtils.OBJECT_MAPPER.readValue(content, ForecastResult.class);
                    //根据预测结果获取attributes
                    WeatherAttributes weatherAttributes = getWeatherAttributes(forecast);

                    // 构造weather trigger
                    WeatherTrigger weatherTrigger = new WeatherTrigger("updateWeatherInfo",System.currentTimeMillis(),4,0,weatherAttributes);

                    // 发送消息至mqtt
                    //topic name 构造规则 just/{template}/gadget/{gadgetId}/trigger
                    String topic;

                    // template Description id
                    final String template = "just-vg-weather";

                    // 虚拟天气设备id使用对于的areaId唯一标识
                    String gadgetId;

                    // 发送trigger至mqtt
                    MqttConnThread mqttConnThread = Registry.INSTANCE.getSession().get(areaId);
                    if (mqttConnThread.getMqttClient().isConnected()) {
                        gadgetId = GadgetIdUtil.getWeatherDeviceId(areaId);
                        // publish<Trigger>的 topic
                        topic = getTopic(template, gadgetId);
                        System.out.println("发送的topic为："+topic);
                        mqttConnThread.sendMessage(topic, JsonUtils.OBJECT_MAPPER.writeValueAsString(weatherTrigger));
                        System.out.println("上传的trigger为："+JsonUtils.OBJECT_MAPPER.writeValueAsString(weatherTrigger));
                    }
                }
            } catch (ClientProtocolException e) {
                logger.error("Http erro when getting weather information from OpenWeather: {}", ExceptionUtils.getMessage(e));
            } catch (IOException e) {
                logger.error("IO error when dealing with weather information: {}", ExceptionUtils.getMessage(e));
            }

            //防止任务执行过快阻塞 weather server
            Thread.sleep(30000);
        } catch (Exception ignore) {
        }
    }

    /**
     * 获取attributes,从 forecast
     */
    protected WeatherAttributes getWeatherAttributes(ForecastResult forecast) {
        WeatherAttributes weatherAttributes = new WeatherAttributes();
        // forecast
        List<Forecast> forecasts = forecast.getForecast3Day().getForecast3d();
        int i = 1;
        for (Forecast f : forecasts) {
            switch (i) {
                case 1:
                    weatherAttributes.setD1DayWeather(f.getDayWeather());
                    weatherAttributes.setD1NightWeather(f.getNightWeather());
                    weatherAttributes.setD1DayTemp(f.getDayTemp());
                    weatherAttributes.setD1NightTemp(f.getNightTemp());
                    weatherAttributes.setD1DayWindDir(f.getDayWindDirection());
                    weatherAttributes.setD1NightWindDir(f.getNightWindDirection());
                    weatherAttributes.setD1DayWindForce(f.getDayWindForce());
                    weatherAttributes.setD1NightWindForce(f.getNightWindForce());
                    break;
                case 2:
                    weatherAttributes.setD2DayWeather(f.getDayWeather());
                    weatherAttributes.setD2NightWeather(f.getNightWeather());
                    weatherAttributes.setD2DayTemp(f.getDayTemp());
                    weatherAttributes.setD2NightTemp(f.getNightTemp());
                    weatherAttributes.setD2DayWindDir(f.getDayWindDirection());
                    weatherAttributes.setD2NightWindDir(f.getNightWindDirection());
                    weatherAttributes.setD2DayWindForce(f.getDayWindForce());
                    weatherAttributes.setD2NightWindForce(f.getNightWindForce());
                    break;
                case 3:
                    weatherAttributes.setD3DayWeather(f.getDayWeather());
                    weatherAttributes.setD3NightWeather(f.getNightWeather());
                    weatherAttributes.setD3DayTemp(f.getDayTemp());
                    weatherAttributes.setD3NightTemp(f.getNightTemp());
                    weatherAttributes.setD3DayWindDir(f.getDayWindDirection());
                    weatherAttributes.setD3NightWindDir(f.getNightWindDirection());
                    weatherAttributes.setD3DayWindForce(f.getDayWindForce());
                    weatherAttributes.setD3NightWindForce(f.getNightWindForce());
                    break;
            }
            i++;
        }
        return weatherAttributes;
    }

    /**
     * Get Topic
     *
     * @param template device description id
     * @param gadgetId 设备id
     */
    public String getTopic(String template, String gadgetId) {
        return "just/" + template + "/gadget/" + gadgetId + "/trigger";
    }

    /**
     * Get Url
     *
     * @param areaId Area Id, single are '101010100', multiple area '101010100|101010200'
     * @param date   Date in 'yyyyMMddHHmm' format
     * @return Public Key
     */
    public String getUrl(String areaId, String date) {
        String key = getKey(areaId, date);
        return "http://open.weather.com.cn/data/?areaid=" + areaId + "&type=forecast_f&date=" + date + "&appid=" + APP_ID.substring(0, 6) + "&key=" + key;
    }

    /**
     * Get Key
     */
    protected String getKey(String areaId, String date) {
        try {
            String publicKey = getPublicKey(areaId, date);
            // hmac sha1
            final SecretKeySpec signingKey = new SecretKeySpec(PRIVATE_KEY.getBytes(), "HmacSHA1");
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(publicKey.getBytes());
            // base64 & url escape
            return URLEncoder.encode(Base64.encodeBase64String(rawHmac), "UTF-8");
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException ignore) {
            // never happens
            throw new IllegalArgumentException();
        }
    }

    /**
     * Get Public Key
     */
    protected String getPublicKey(String areaId, String date) {
        return "http://open.weather.com.cn/data/?areaid=" + areaId + "&type=forecast_f&date=" + date + "&appid=" + APP_ID;
    }

}
