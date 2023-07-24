package org.dromara.dynamictp.starter.adapter.webserver.adapter.proxy;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.threads.ThreadPoolExecutor;
import org.dromara.dynamictp.core.notifier.alarm.ThreadPoolAlarm;
import org.dromara.dynamictp.core.notifier.alarm.ThreadPoolAlarmHelper;
import org.dromara.dynamictp.core.reject.RejectedInvocationHandler;
import org.dromara.dynamictp.core.support.ExecutorWrapper;

import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

/**
 * Tomcat ThreadPool Proxy
 * @author kyao
 * @since 1.1.4
 */
@Slf4j
public class TomcatThreadProxy extends ThreadPoolExecutor implements ThreadPoolAlarm {

    private ThreadPoolAlarmHelper helper;

    private TomcatThreadProxy(ThreadPoolExecutor executor) {
        super(executor.getCorePoolSize(), executor.getMaximumPoolSize(), executor.getKeepAliveTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, executor.getQueue(), executor.getThreadFactory(), executor.getRejectedExecutionHandler());
    }

    public TomcatThreadProxy(ExecutorWrapper executorWrapper) {
        this((ThreadPoolExecutor) executorWrapper.getExecutor().getOriginal());
        helper = ThreadPoolAlarmHelper.of(executorWrapper);

        RejectedExecutionHandler handler = getRejectedExecutionHandler();
        setRejectedExecutionHandler((RejectedExecutionHandler) Proxy
                .newProxyInstance(handler.getClass().getClassLoader(),
                        new Class[]{RejectedExecutionHandler.class},
                        new RejectedInvocationHandler(handler)));
    }

    @Override
    public void execute(Runnable command) {
        executeAlarmEnhance(command);
        super.execute(command);
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        beforeExecuteAlarmEnhance(t, r);
    }


    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        afterExecuteAlarmEnhance(r, t);
        super.afterExecute(r, t);
    }

    @Override
    public ThreadPoolAlarmHelper getThirdPartTpAlarmHelper() {
        return helper;
    }
}
