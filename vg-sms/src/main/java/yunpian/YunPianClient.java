package yunpian;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.client.methods.RequestBuilder.post;

/**
 * send message to yunpian client
 */
public class YunPianClient {
    //logger
    private static final Logger logger = LoggerFactory.getLogger(YunPianClient.class);

    //developer AppKey
    private static final String ApiKey = "308e234ee5b3d555f81cae299a30fc9c";

    //智能匹配模版发送接口的http地址
    private static String URI_SEND_SMS = "http://yunpian.com/v1/sms/send.json";

    //编码格式。发送编码格式统一用UTF-8
    private static String ENCODING = "UTF-8";


    /**
     * send message to yunpian
     *
     * @param text   message content
     * @param mobile user mobile phone number
     *
     */
    public static String sendSms(int sendType,String text, String mobile) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("apikey", YunPianClient.ApiKey);
        switch (sendType){
            case 0:
                params.put("text",YunPianClient.getCodeTemp(text));
                params.put("mobile",mobile);
                break;
            case 1:
                params.put("text", text);
                params.put("mobile", mobile);
        }

        //send
        return post(URI_SEND_SMS, params);
    }

    private static String getCodeTemp(String code) {
        return "【just-io平台】您的验证码为："+code;
    }

    /**
     * 基于HttpClient 4.3的通用POST方法
     *
     * @param url       提交的URL
     * @param paramsMap 提交<参数，值>Map
     * @return 提交响应
     */

    public static String post(String url, Map<String, String> paramsMap) {
        CloseableHttpClient client = HttpClients.createDefault();
        String responseText = "";
        CloseableHttpResponse response = null;
        try {
            HttpPost method = new HttpPost(url);
            if (paramsMap != null) {
                List<NameValuePair> paramList = new ArrayList<NameValuePair>();
                for (Map.Entry<String, String> param : paramsMap.entrySet()) {
                    NameValuePair pair = new BasicNameValuePair(param.getKey(), param.getValue());
                    paramList.add(pair);
                }
                method.setEntity(new UrlEncodedFormEntity(paramList, ENCODING));
            }
            response = client.execute(method);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                responseText = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(response!=null){
                    response.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return responseText;
    }


}
