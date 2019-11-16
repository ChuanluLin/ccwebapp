package com.csye6225.demo.exception;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RequestLimitContract {
    private static final Logger logger = LoggerFactory.getLogger("RequestLimitLogger");
    private Map<String, Integer> LimitMap = new HashMap<>();

    @Before("within(@org.springframework.stereotype.Controller *) && @annotation(limit)")
    public void requestLimit(final JoinPoint joinPoint, RequestLimit limit) throws RequestLimitException {
        try{
            Object[] args = joinPoint.getArgs();
            HttpServletRequest request = null;
            for(int i=0; i< args.length; i++){
                if(args[i] instanceof  HttpServletRequest){
                    request = (HttpServletRequest) args[i];
                    break;
                }
            }
            if(request == null){
                throw new RequestLimitException("No HttpServletRequest");
            }
            String ip = request.getLocalAddr();
            String url = request.getRequestURL().toString();
            String key = "req_limit_".concat(url).concat(ip);
            if(LimitMap.get(key)==null || LimitMap.get(key)==0){
                LimitMap.put(key,1);
            }else{
                LimitMap.put(key,LimitMap.get(key)+1);
            }
            int count = LimitMap.get(key);
            if (count > 0) {
                Timer timer= new Timer();
                TimerTask task  = new TimerTask(){
                    @Override
                    public void run() {
                        LimitMap.remove(key);
                    }
                };
                timer.schedule(task, limit.time());
            }
            if (count > limit.count()) {
                //logger.info("user IP[" + ip + "] URL [" + url + "] exceed the limit[" + limit.count() + "]");
                throw new RequestLimitException();
            }
        }catch (RequestLimitException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
    }
}
