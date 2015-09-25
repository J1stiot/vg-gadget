import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * sms service
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class SmsService {
    public static void main(String[] args){
        //启动应用程序
        SpringApplication.run(SmsService.class,args);
    }

}
