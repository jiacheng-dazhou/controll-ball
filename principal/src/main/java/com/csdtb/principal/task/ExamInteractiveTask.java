package com.csdtb.principal.task;

import com.alibaba.fastjson.JSON;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.common.dto.websocket.Ball;
import com.csdtb.common.dto.websocket.Frame;
import com.csdtb.common.dto.websocket.SendTemplateData;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.principal.websocket.ExamWebSocket;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zhoujiacheng
 * @Date 2022-12-13
 * @Description 考试交互线程
 **/
@Slf4j
public class ExamInteractiveTask implements Runnable {

    private ExamWebSocket examWebSocket;
    /**
     * 小球集合
     */
    private Vector<Ball> balls = new Vector<>();
    /**
     * 小球数量
     */
    private Integer ballNumber;
    /**
     * 折返率
     */
    private Integer turnbackRate;
    /**
     * 出界率
     */
    private Integer boundsRate;
    /**
     * 折返颜色
     */
    private Integer turnbackColor;
    /**
     * 出界颜色
     */
    private Integer boundsColor;
    /**
     * 小球速度
     */
    private Integer speed;
    /**
     * 默认开启时启动  考试终止或受到打断时结束
     */
    private Boolean isRun = true;
    /**
     * 事件类型 1-折返,2-出界,3-碰壁
     */
    private Integer eventType;
    /**
     * 事件触发时间
     */
    private LocalDateTime eventTriggerTime;
    /**
     * 出题
     */
    private String question;

    /**
     * 出题时间
     */
    private LocalDateTime showTime;

    public ExamInteractiveTask(ExamWebSocket examWebSocket, Integer ballNumber, Integer turnbackRate,
                               Integer boundsRate, Integer turnbackColor, Integer boundsColor, Integer speed) {
        this.examWebSocket = examWebSocket;
        this.ballNumber = ballNumber;
        this.turnbackRate = turnbackRate;
        this.boundsRate = boundsRate;
        this.turnbackColor = turnbackColor;
        this.boundsColor = boundsColor;
        this.speed = speed;
    }

    public Vector<Ball> getBalls() {
        return balls;
    }

    public void setRun(Boolean run) {
        isRun = run;
    }

    public String getQuestion() {
        return question;
    }

    public LocalDateTime getShowTime() {
        return showTime;
    }

    public Integer getEventType() {
        return eventType;
    }

    public LocalDateTime getEventTriggerTime() {
        return eventTriggerTime;
    }

    @Override
    public void run() {
        try {
            //初始化小球
            initBall();
            ExamInfoEntity examInfoEntity = examWebSocket.getExamInfoEntity();
            LocalDateTime startTime = LocalDateTime.parse(examInfoEntity.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTime = LocalDateTime.parse(examInfoEntity.getEndTime(),DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            //监视时长(分钟)
            Integer monitorDuration = examInfoEntity.getMonitorDuration();
            //休息时长(秒)
            Integer sleepDuration = examInfoEntity.getMonitorSleepDuration();
            //题库
            List<String> questions = Arrays.asList(examInfoEntity.getQuestions().split(","));
            //考试时长
            long totalSeconds = Duration.between(startTime, endTime).getSeconds();
            //出题频率(秒)
            long questionRate = totalSeconds / questions.size();
            while (isRun) {
                if (endTime.isBefore(LocalDateTime.now())) {
                    //考试结束
                    break;
                }
                if (startTime.isBefore(LocalDateTime.now())) {
                    //开始考试
                    beginExam(startTime,endTime, monitorDuration, sleepDuration, questionRate, questions);
                }
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("ball game over......");
    }

    private void beginExam(LocalDateTime startTime,LocalDateTime endTime, Integer monitorDuration, Integer sleepDuration, long questionRate, List<String> questions) {
        //校验小球是否重叠
        for (Ball ball : balls) {
            Ball crushBall = getCrushBall(ball, balls);
            if (crushBall != null) {
                //发生碰撞,记录事件触发
                eventTriggerTime = LocalDateTime.now();
                eventType = ExamInfoEnum.CRASH.getStatus();
                //小球重新生成坐标方向
                randomInitBall(crushBall, balls);
                randomInitBall(ball, balls);
                break;
            }
        }
        //校验小球是否出界,折返
        validateBallBoundsOrTurnback();

        //小球根据现有速度运动
        balls.forEach(this::ballSport);

        //当前时间推送题目
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = Duration.between(startTime, now).getSeconds();
        int indexQuestion = (int) ((int)nowSecond/questionRate);
        String questionIndex = questions.get(indexQuestion);
        //题目切换，获取记录当前时间
        if (question == null || !question.equals(questionIndex)) {
            showTime = now;
        }
        question = questionIndex;


        int status = 0;
        LocalDateTime sleepTimeStart = startTime.plusMinutes(monitorDuration);
        LocalDateTime sleepTimeEnd = sleepTimeStart.plusSeconds(sleepDuration);
        for (;;) {
            if (sleepTimeStart.isAfter(now)) {
                //监视
                break;
            }
            if (sleepTimeStart.isBefore(now) && sleepTimeEnd.isAfter(now)) {
                status = 1;
                break;
            }
            sleepTimeStart = sleepTimeStart.plusMinutes(monitorDuration);
            sleepTimeEnd = sleepTimeStart.plusSeconds(sleepDuration);
        }

        //考试倒计时
        Duration countDown = Duration.between(now, endTime);
        long hours = countDown.toHours();
        long minutes = countDown.toMinutes() - hours * 60;
        long seconds = countDown.getSeconds() - countDown.toMinutes() * 60;
        LocalTime countDownTime = LocalTime.of((int) hours, (int) minutes, (int) seconds);

        //推送当前数据给考试的所有人
        pushData(status,countDownTime);
    }

    private void pushData(int status,LocalTime countDownTime){
        //发送模板
        SendTemplateData data = new SendTemplateData();
        data.setBalls(balls)
            .setBoundsColor(boundsColor)
            .setTurnbackColor(turnbackColor)
            .setQuestion(question)
            .setStatus(status)
            .setExamCountDown(countDownTime);

        ConcurrentHashMap<String, ExamWebSocket> conns = this.examWebSocket.getConns();
        Integer examId = examWebSocket.getExamInfoEntity().getId();
        //需要发送模板的用户
        for (String key : conns.keySet()) {
            ExamWebSocket examWebSocket = conns.get(key);
            if (examId.equals(examWebSocket.getExamInfoEntity().getId())) {
                try {
                    examWebSocket.getSession().getBasicRemote().sendText(JSON.toJSONString(data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void validateBallBoundsOrTurnback() {
        for (Ball ball : balls) {
            boolean flag = isTouchInnerBorder(ball);
            if (!flag) {
                continue;
            }
            if (ball.isBounds()) {
                //出界且超出外边界，即重新生成小球坐标,重置出界状态
                boolean isTouchOuterBorder = isTouchOuterBorder(ball);
                if (!isTouchOuterBorder) {
                    continue;
                }
                randomInitBall(ball, balls);
                ball.setBounds(false);
                //记录事件触发
                eventTriggerTime = LocalDateTime.now();
                eventType = ExamInfoEnum.BOUNDS.getStatus();
                continue;
            }
            //根据概率选择出界还是折返
            boolean isBounds = (int) (Math.random() * (turnbackRate + boundsRate)) < boundsRate;
            ball.setBounds(isBounds);
            if (!isBounds) {
                //折返变更方向
                changeBallDirection(ball);
                //记录事件触发
                eventTriggerTime = LocalDateTime.now();
                eventType = ExamInfoEnum.TURNBACK.getStatus();
            }
        }
    }

    private boolean isTouchOuterBorder(Ball ball) {
        boolean flag = false;
        switch (ball.getDirection()) {
            case 0:
                //校验小球顶部是否碰到外边界
                flag = ball.getY() - Frame.ballRadius < 0;
                break;
            case 1:
                //校验小球右边是否碰到外边界
                flag = ball.getX() + Frame.ballRadius > Frame.margin_x;
                break;
            case 2:
                //校验小球底部是否碰到外边界
                flag = ball.getY() + Frame.ballRadius > Frame.margin_y;
                break;
            case 3:
                //校验小球左边是否碰到外边界
                flag = ball.getX() - Frame.ballRadius < 0;
                break;
        }
        return flag;
    }


    private void changeBallDirection(Ball ball) {
        switch (ball.getDirection()){
            case 0:
                ball.setDirection(2);
                break;
            case 1:
                ball.setDirection(3);
                break;
            case 2:
                ball.setDirection(0);
                break;
            case 3:
                ball.setDirection(1);
                break;
        }
    }

    private boolean isTouchInnerBorder(Ball ball) {
        boolean flag = false;
        switch (ball.getDirection()) {
            case 0:
                //校验小球顶部是否碰到内边界
                flag = ball.getY() - Frame.ballRadius < Frame.distance;
                break;
            case 1:
                //校验小球右边是否碰到内边界
                flag = ball.getX() + Frame.ballRadius > Frame.innerBorder_x;
                break;
            case 2:
                //校验小球底部是否碰到内边界
                flag = ball.getY() + Frame.ballRadius > Frame.innerBorder_y;
                break;
            case 3:
                //校验小球左边是否碰到内边界
                flag = ball.getX() - Frame.ballRadius < Frame.distance;
                break;
        }
        return flag;
    }

    private Ball getCrushBall(Ball ball, Vector<Ball> balls) {
        boolean flag = false;
        for (Ball item : balls) {
            if (ball.getId().equals(item.getId())) {
                continue;
            }
            Integer direction = item.getDirection();
            //小球方向不一致，直接不重叠
            if (!direction.equals(ball.getDirection())) {
                continue;
            }
            //校验x,y坐标是否重叠
            switch (direction) {
                case 0:
                case 2:
                    //比较x坐标
                    if (ball.getX() > item.getX()) {
                        //右边
                        flag = (ball.getX() - Frame.ballRadius) < (item.getX() + Frame.ballRadius);
                    } else {
                        //左边
                        flag = (ball.getX() + Frame.ballRadius) > (item.getX() - Frame.ballRadius);
                    }
                    break;
                case 1:
                case 3:
                    //比较y坐标
                    if (ball.getY() > item.getY()) {
                        //下边
                        flag = (ball.getY() - Frame.ballRadius) < (item.getY() + Frame.ballRadius);
                    } else {
                        //上边
                        flag = (ball.getY() + Frame.ballRadius) > (item.getY() - Frame.ballRadius);
                    }
                    break;
            }
            //重叠即退出遍历
            if (flag) {
                return item;
            }
        }
        return null;
    }

    private void ballSport(Ball ball) {
        switch (ball.getDirection()) {
            case 0:
                ball.setY(ball.getY() - ball.getSpeed());
                break;
            case 1:
                ball.setX(ball.getX() + ball.getSpeed());
                break;
            case 2:
                ball.setY(ball.getY() + ball.getSpeed());
                break;
            case 3:
                ball.setX(ball.getX() - ball.getSpeed());
                break;
        }
    }


    private void initBall() {
        for (int i = 0; i < ballNumber; i++) {
            Ball ball = new Ball();
            ball.setId(i);
            //小球随机初始化，每个新生成的小球不与之前的重叠
            randomInitBall(ball, balls);
            balls.add(ball);
        }
    }

    private void randomInitBall(Ball ball, Vector<Ball> balls) {
        //先生成小球坐标再校验是否重叠
        randomBall(ball);
        if (balls.isEmpty()) {
            return;
        }
        //校验小球是否重叠(碰撞),重新生成小球坐标
        if (validateBallCrush(ball, balls)) {
            randomInitBall(ball, balls);
        }
    }

    private boolean validateBallCrush(Ball ball, Vector<Ball> balls) {
        boolean flag = false;
        for (Ball item : balls) {
            //剔除掉本身
            if (item.getId().equals(ball.getId())) {
                continue;
            }
            Integer direction = item.getDirection();
            //小球方向不一致，直接不重叠
            if (!direction.equals(ball.getDirection())) {
                continue;
            }
            //校验x,y坐标是否重叠
            switch (direction) {
                case 0:
                case 2:
                    //比较x坐标
                    if (ball.getX() > item.getX()) {
                        //右边
                        flag = (ball.getX() - Frame.ballRadius) < (item.getX() + Frame.ballRadius);
                    } else {
                        //左边
                        flag = (ball.getX() + Frame.ballRadius) > (item.getX() - Frame.ballRadius);
                    }
                    break;
                case 1:
                case 3:
                    //比较y坐标
                    if (ball.getY() > item.getY()) {
                        //下边
                        flag = (ball.getY() - Frame.ballRadius) < (item.getY() + Frame.ballRadius);
                    } else {
                        //上边
                        flag = (ball.getY() + Frame.ballRadius) > (item.getY() - Frame.ballRadius);
                    }
                    break;
            }
            //重叠即退出遍历
            if (flag) {
                break;
            }
        }
        return flag;
    }

    private void randomBall(Ball ball) {
        //随机生成小球的方向
        int direction = (int) (Math.random() * 4);
        //随机生成小球的x,y初始坐标,初始点默认在边界
        int x = 0;
        int y = 0;
        switch (direction) {
            //0-上，1-右，2-下，3-左
            case 0:
                //方向向上，小球初始化底部
                x = (int) (Math.random() * (Frame.innerBorder_x - Frame.ballRadius - (Frame.distance + Frame.ballRadius)))
                        + Frame.distance + Frame.ballRadius;
                y = Frame.innerBorder_y - Frame.ballRadius;
                break;
            case 1:
                //方向向右，小球初始化左边
                x = Frame.distance + Frame.ballRadius;
                y = (int) (Math.random() * (Frame.innerBorder_y - Frame.ballRadius - (Frame.distance + Frame.ballRadius)))
                        + Frame.distance + Frame.ballRadius;
                break;
            case 2:
                //方向向下，小球初始化顶部
                x = (int) (Math.random() * (Frame.innerBorder_x - Frame.ballRadius - (Frame.distance + Frame.ballRadius)))
                        + Frame.distance + Frame.ballRadius;
                y = Frame.distance + Frame.ballRadius;
                break;
            case 3:
                //方向向左，小球初始化在右边
                x = Frame.innerBorder_x - Frame.ballRadius;
                y = (int) (Math.random() * (Frame.innerBorder_y - Frame.ballRadius - (Frame.distance + Frame.ballRadius)))
                        + Frame.distance + Frame.ballRadius;
                break;
        }
        //小球初始速度为一开始定义速度,后续根据考核员变动改变
        ball.setX(x).setY(y).setSpeed(speed).setDirection(direction);
    }
}
