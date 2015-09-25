package com.zen.smartweather.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 天气设备gadgetId生成规则
 */
public class GadgetIdUtil {
    /**
     * Generate Device Id based on Area Id
     *
     * @param areaId Area Id
     * @return Device Id
     */
    public static String getWeatherDeviceId(String areaId) {
        try {
            String url = "https://zeninfor.com/just-io/evo/weather?areaId=" + areaId;
            URI uri = new URI(url);
            return UuidUtils.shortUuid(uri);
        } catch (URISyntaxException ignore) {
            // never happens
            return null;
        }
    }
}

