package com.csye6225.demo.metrics;

import com.timgroup.statsd.StatsDClient;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.LogManager;

@Aspect
public class MethodProfiler {

    private final StatsDClient statsd;

    public MethodProfiler(StatsDClient statsd) {
        this.statsd = statsd;
    }

    private static final Logger logger = LoggerFactory.getLogger(MethodProfiler.class);

    @Pointcut("execution(* com.csye6225.demo.controller.*.*(..)) || execution(* com.csye6225.demo.repository.*.*(..))")
    public void restServiceMethods() {
    }

    @Around("restServiceMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {

        // execute the method, record the result and measure the time
        long startTime = System.currentTimeMillis();
        Object output = pjp.proceed();
        long endTime = System.currentTimeMillis();

        // send the recorded time to statsd
        String key = String.format("%s.%s", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName());
        statsd.recordExecutionTime(key, endTime-startTime);
        // send the count to statsd
        statsd.incrementCounter(key);

        // print log
        logger.info(key);

        // return the recorded result
        return output;

    }

}
