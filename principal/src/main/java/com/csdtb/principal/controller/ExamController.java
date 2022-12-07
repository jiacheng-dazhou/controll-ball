package com.csdtb.principal.controller;

import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.principal.service.ExamInfoService;
import com.csdtb.principal.websocket.ExamWebSocket;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@EnableScheduling
@RestController
@RequestMapping("/exam")
public class ExamController {

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private ScheduledFuture<?> future;

    @Autowired
    private ExamWebSocket examWebSocket;

    @Autowired
    private ExamInfoService examInfoService;

    private static String cron = "0/1 * * * * ?";
    private static ConcurrentHashMap<String, ExamInfoEntity> conditionMap = new ConcurrentHashMap<>();

    @GetMapping("/startExam")
    public void startExam(String examId, HttpServletRequest request){
        ExamInfoEntity examInfoEntity = examInfoService.getById(examId);
        conditionMap.putIfAbsent(examId, examInfoEntity);
        start();
    }

    @GetMapping("/test")
    public void test(){
        System.out.println("这是一个测试");
        start();
    }

    @GetMapping("/endTest")
    public void endTest(){
        stopCron();
    }

    private void start() {
        future = threadPoolTaskScheduler.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null != examWebSocket.getExamWebSocket() && examWebSocket.getExamWebSocket().size() > 0) {
                        if (null != conditionMap && conditionMap.size() > 0){
                            conditionMap.forEach((key,value)->{
                                if (conditionMap.get(key) != null) {
//                                    PredictTimeVo predictTimeVo = conditionMap.get("condition");
//                                    if (predictTimeVo.getCount() == null) {
//                                        predictTimeVo.setCount(0);
//                                    } else {
//                                        predictTimeVo.setCount(predictTimeVo.getCount() + 1);
//                                    }
//                                    System.out.println(predictTimeVo);
//                                    //定时结束仿真
//                                    if (null != predictTimeVo.getScheduledEndTime()) {
//                                        if (predictTimeVo.getTimestamp() + predictTimeVo.getCount() * predictTimeVo.getInterval() >= predictTimeVo.getScheduledEndTime()) {
//                                            stopScheduledTask("condition");
//                                        }
//                                    }
                                    examInfoService.getExamData(value);
                                }
                            });
                        }
                    }
                    System.out.println("执行定时器任务");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                if (StringUtils.isNotBlank(cron)) {
                    CronTrigger trigger = new CronTrigger(cron);
                    System.out.println("cron:" + cron);
                    Date nextExecutor = trigger.nextExecutionTime(triggerContext);
                    return nextExecutor;
                } else {
                    return null;
                }
            }
        });
    }

    private void stopCron() {
        if (future != null) {
            future.cancel(true);//取消任务调度
        }
    }
}
