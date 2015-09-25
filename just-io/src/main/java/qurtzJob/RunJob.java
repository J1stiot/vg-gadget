package qurtzJob;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by jack-pc on 2015/9/21.
 */
public class RunJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        //获取订阅的消息

        //解析订阅的消息

        //发送手机消息

    }
}
