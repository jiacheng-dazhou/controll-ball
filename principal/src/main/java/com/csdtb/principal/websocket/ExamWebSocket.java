package com.csdtb.principal.websocket;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.common.constant.UserEnum;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.common.dto.websocket.Ball;
import com.csdtb.common.dto.websocket.ExamNoticeResp;
import com.csdtb.database.entity.BallMonitorTaskEntity;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.database.entity.ExamRecordsDetailEntity;
import com.csdtb.database.entity.ExamRecordsEntity;
import com.csdtb.database.mapper.BallMonitorTaskMapper;
import com.csdtb.database.mapper.ExamInfoMapper;
import com.csdtb.database.mapper.ExamRecordsDetailMapper;
import com.csdtb.database.mapper.ExamRecordsMapper;
import com.csdtb.principal.exception.GlobalException;
import com.csdtb.principal.task.ExamInteractiveTask;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Component
@ServerEndpoint(value = "/exam-info/connect/{token}/{examId}")
@Slf4j
@Data
public class ExamWebSocket {

    /**
     * 存放所有考试用户信息
     */
    public static ConcurrentHashMap<String, ExamWebSocket> conns = new ConcurrentHashMap<>();

    /**
     * 存放考试线程
     */
    public static ConcurrentHashMap<Integer, ExamInteractiveTask> examTasks = new ConcurrentHashMap<>();

    private static ApplicationContext applicationContext;

    private static RedisTemplate redisTemplate;

    private static ExamInfoMapper examInfoMapper;

    private static ExamRecordsMapper examRecordsMapper;

    private static ExamRecordsDetailMapper examRecordsDetailMapper;

    private static ThreadPoolTaskExecutor taskExecutor;

    private static BallMonitorTaskMapper ballMonitorTaskMapper;

    /**
     * 用户信息
     */
    private UserDTO user;

    /**
     * 考试信息
     */
    private ExamInfoEntity examInfoEntity;

    /**
     * 考试记录id
     */
    private Integer examRecordsId;

    /**
     * 视频路径
     */
    private String videoPath;

    private static final String filePath = "D:/test/userVideo/";

    /**
     * 与某个客户端的连接会话，需要通过它来给客户端发送数据
     */
    private Session session;


    public static void setApplicationContext(ApplicationContext applicationContext) {
        ExamWebSocket.applicationContext = applicationContext;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token, @PathParam("examId") Integer examId) {
        init();

        this.session = session;
        //获取用户信息
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);

        if (user == null) {
            throw new GlobalException(ResponseResult.error("获取用户信息失败"));
        }

        //获取考试信息
        ExamInfoEntity examInfoEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, examId));
        this.examInfoEntity = examInfoEntity;

        //只对管制员生成考试记录
        if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
            insertExamRecord(user, examInfoEntity);
        }

        this.user = user;

        //存放当前用户考试信息
        conns.put(token, this);

        //一个考试开一个线程跑小球坐标
        if (examTasks == null || examTasks.get(examId) == null) {
            //根据监视难度获取要生成的小球信息
            Integer monitorLevel = this.examInfoEntity.getMonitorLevel();
            BallMonitorTaskEntity monitorTaskEntity = ballMonitorTaskMapper.selectOne(new LambdaQueryWrapper<BallMonitorTaskEntity>()
                    .eq(BallMonitorTaskEntity::getLevel, monitorLevel));
            //数量
            int ballNumber = monitorTaskEntity.getNumber();
            //折返颜色
            int turnbackColor = monitorTaskEntity.getTurnbackColor();
            //出界颜色
            int boundsColor = monitorTaskEntity.getBoundsColor();
            //速度
            int speed = monitorTaskEntity.getSpeed() * 5;
            //折返率
            int turnbackRate = monitorTaskEntity.getTurnbackRate();
            //出界率
            int boundsRate = monitorTaskEntity.getBoundsRate();
            ExamInteractiveTask examInteractiveTask = new ExamInteractiveTask
                    (this, ballNumber, turnbackRate, boundsRate, turnbackColor, boundsColor, speed);
            examTasks.put(examId, examInteractiveTask);
            taskExecutor.execute(examInteractiveTask);
        }
    }

    @OnMessage
    public void onMessage(String message, @PathParam("token") String token, @PathParam("examId") Integer examId) {
        //获取当前管制员考试信息
        ExamWebSocket examWebSocket = conns.get(token);
        if (examWebSocket == null) {
            return;
        }
        UserDTO user = examWebSocket.user;
        //获取消息
        ExamNoticeResp resp = JSON.parseObject(message, ExamNoticeResp.class);
        ExamInteractiveTask task = examTasks.get(examId);

        if (resp.getType().equals(ExamInfoEnum.CONTROL_BALL.getStatus())) {
            //控制小球
            if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
                //只有考核员或管理员可以控制小球
                return;
            }
            controlBall(task, resp);
        } else if (resp.getType().equals(ExamInfoEnum.CALCULATE.getStatus())) {
            //计算题
            ExamRecordsDetailEntity detailEntity = calculateResult(task, resp, examWebSocket);
            examRecordsDetailMapper.insert(detailEntity);
        } else {
            //小球事件记录
            ExamRecordsDetailEntity detailEntity = ballEvent(task, resp, examWebSocket);
            examRecordsDetailMapper.insert(detailEntity);
        }
    }

    private ExamRecordsDetailEntity ballEvent(ExamInteractiveTask task, ExamNoticeResp resp, ExamWebSocket examWebSocket) {
        ExamRecordsDetailEntity detailEntity = new ExamRecordsDetailEntity();
        String name = "";
        if (resp.getType().equals(ExamInfoEnum.TURNBACK.getStatus())) {
            name = ExamInfoEnum.TURNBACK.getDescription();
        } else if (resp.getType().equals(ExamInfoEnum.BOUNDS.getStatus())) {
            name = ExamInfoEnum.BOUNDS.getDescription();
        } else {
            name = ExamInfoEnum.CRASH.getDescription();
        }

        if (task.getEventType() != null && task.getEventTriggerTime() != null) {
            //出现时间
            LocalDateTime examStartTime = LocalDateTime.parse(examWebSocket.examInfoEntity.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Duration between = Duration.between(examStartTime, task.getEventTriggerTime());
            long hours = between.toHours();
            long minutes = between.toMinutes() - hours * 60;
            long seconds = between.getSeconds() - between.toMinutes() * 60;
            String startTime = LocalTime.of((int) hours, (int) minutes, (int) seconds).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            detailEntity.setStartTime(startTime);
            detailEntity.setName(name + "_" + startTime);
            //是否正确
            detailEntity.setIsCorrect(task.getEventType().equals(resp.getType()));
            if (detailEntity.getIsCorrect()) {
                //正确，记录反应时间，是否提前反应
                LocalDateTime now = LocalDateTime.now();
                Duration duration = Duration.between(task.getEventTriggerTime(), now);
                BigDecimal reactionTime = BigDecimal.valueOf(duration.toMillis())
                        .divide(BigDecimal.valueOf(1000L), 1, RoundingMode.HALF_UP);

                detailEntity.setReactionTime(reactionTime + "s");
                detailEntity.setIsReactInAdvance(false);
                if (reactionTime.compareTo(BigDecimal.ONE) <= 0) {
                    //反应时间小于1s，即提前反应
                    detailEntity.setIsReactInAdvance(true);
                }
            }
        } else {
            detailEntity.setName(name + "_错误");
        }
        detailEntity.setType(resp.getType());
        detailEntity.setExamRecordsId(examWebSocket.examRecordsId);

        return detailEntity;
    }

    private ExamRecordsDetailEntity calculateResult(ExamInteractiveTask task, ExamNoticeResp resp, ExamWebSocket examWebSocket) {
        ExamRecordsDetailEntity detailEntity = new ExamRecordsDetailEntity();
        String question = task.getQuestion();
        Integer rightAnswer;

        if (question.contains("+")) {
            String[] addArr = question.split("\\+");
            rightAnswer = Integer.parseInt(addArr[0]) + Integer.parseInt(addArr[1]);
        } else {
            String[] minusArr = question.split("-");
            rightAnswer = Integer.parseInt(minusArr[0]) - Integer.parseInt(minusArr[1]);
        }

        detailEntity.setIsCorrect(rightAnswer.equals(Integer.parseInt(resp.getAnswer())));
        if (detailEntity.getIsCorrect()) {
            //正确，记录反应时间
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime showTime = task.getShowTime();
            Duration between = Duration.between(showTime, now);
            String reactionTime = BigDecimal.valueOf(between.toMillis())
                    .divide(BigDecimal.valueOf(1000L), 1, RoundingMode.HALF_UP)
                    .toString();
            detailEntity.setReactionTime(reactionTime + "s");
        }

        //出现时间
        LocalDateTime examStartTime = LocalDateTime.parse(examWebSocket.examInfoEntity.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Duration between = Duration.between(examStartTime, task.getShowTime());
        long hours = between.toHours();
        long minutes = between.toMinutes() - hours * 60;
        long seconds = between.getSeconds() - between.toMinutes() * 60;
        String startTime = LocalTime.of((int) hours, (int) minutes, (int) seconds).format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        detailEntity.setName(question);
        detailEntity.setStartTime(startTime);
        detailEntity.setAnswer(resp.getAnswer());
        detailEntity.setExamRecordsId(examWebSocket.examRecordsId);
        detailEntity.setType(resp.getType());

        return detailEntity;
    }

    private void controlBall(ExamInteractiveTask task, ExamNoticeResp resp) {
        Vector<Ball> balls = task.getBalls();
        for (Ball ball : balls) {
            if (!ball.getId().equals(resp.getId())) {
                continue;
            }
            if (resp.getDirection() != null) {
                ball.setDirection(resp.getDirection());
            }
            if (resp.getSpeed() != null) {
                ball.setSpeed(resp.getSpeed());
            }
        }
    }

    @OnMessage
    public void onMessage(byte[] message, Session session) {
        try {
            //把传来的字节流数组写入到上面创建的文件对象里去
            saveFileFromBytes(message);
            log.info("视频传输中:" + message.toString());
            //只有客户端接受到ok才会传输下一段文件
            session.getBasicRemote().sendText("服务端保存中...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 实现服务
     * 器主动推送
     */
    public void sendMessage(String message) {
        try {
            this.session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo(String message) {

        conns.forEach((s, webSocketServer) -> {
            log.info(conns.get(s).getUser().toString());
            conns.get(s).sendMessage(message);
        });
//        if(StringUtils.isNotBlank(userId) && webSocketMap.containsKey(userId)){
//            webSocketMap.get(userId).sendMessage(message);
//        }else{
//            log.error("用户"+userId+",不在线！");
//        }
    }

    @OnError
    public void onError(Throwable error) {
        error.printStackTrace();
    }

    @OnClose
    public void onClose(Session session, @PathParam("token") String token, @PathParam("examId") Integer examId) {
        conns.remove(token);
        //当前考试连接用户为0时，删除task
        if (conns.isEmpty() || !hasExamConns(examId)) {
            ExamInteractiveTask task = examTasks.get(examId);
            task.setRun(false);
            examTasks.remove(examId);
        }
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean hasExamConns(Integer examId) {
        boolean flag = false;
        for (String key : conns.keySet()) {
            ExamWebSocket examWebSocket = conns.get(key);
            Integer id = examWebSocket.getExamInfoEntity().getId();
            if (id == examId) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public boolean saveFileFromBytes(byte[] b) {
        //创建文件流对象
        FileOutputStream fstream = null;
        //从map中获取file对象
        File file = new File(videoPath);
        //判断路径是否存在，不存在就创建
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            fstream = new FileOutputStream(file, true);
            fstream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fstream != null) {
                try {
                    fstream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return true;
    }

    private void insertExamRecord(UserDTO user, ExamInfoEntity examInfoEntity) {
        //新增考核记录
        ExamRecordsEntity examRecordsEntity = new ExamRecordsEntity();
        examRecordsEntity.setUserId(Math.toIntExact(user.getId()));
        examRecordsEntity.setExamId(examInfoEntity.getId());

        //视频存储路径
        StringBuilder builder = new StringBuilder();
        String path = builder.append(filePath)
                .append(user.getId())
                .append("/")
                .append(examInfoEntity.getId())
                .append("/")
                .append(System.currentTimeMillis())
                .append(".mp4")
                .toString();
        examRecordsEntity.setVideoPath(path);

        ExamRecordsEntity recordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                .eq(ExamRecordsEntity::getUserId, user.getId())
                .eq(ExamRecordsEntity::getExamId, examInfoEntity.getId()));

        //没有当前考试记录，新增
        if (recordsEntity == null) {
            examRecordsMapper.insertAndReturnId(examRecordsEntity);
            this.examRecordsId = examRecordsEntity.getId();
            this.videoPath = examRecordsEntity.getVideoPath();
        } else {
            this.examRecordsId = recordsEntity.getId();
            this.videoPath = recordsEntity.getVideoPath();
        }
    }

    private void init() {
        if (ExamWebSocket.redisTemplate == null) {
            RedisTemplate redisTemplate = applicationContext.getBean("redisTemplate", RedisTemplate.class);
            ExamWebSocket.redisTemplate = redisTemplate;
        }
        if (ExamWebSocket.examInfoMapper == null) {
            ExamInfoMapper examInfoMapper = applicationContext.getBean("examInfoMapper", ExamInfoMapper.class);
            ExamWebSocket.examInfoMapper = examInfoMapper;
        }
        if (ExamWebSocket.examRecordsMapper == null) {
            ExamRecordsMapper examRecordsMapper = applicationContext.getBean("examRecordsMapper", ExamRecordsMapper.class);
            ExamWebSocket.examRecordsMapper = examRecordsMapper;
        }
        if (ExamWebSocket.examRecordsDetailMapper == null) {
            ExamRecordsDetailMapper examRecordsDetailMapper = applicationContext.getBean("examRecordsDetailMapper", ExamRecordsDetailMapper.class);
            ExamWebSocket.examRecordsDetailMapper = examRecordsDetailMapper;
        }
        if (ExamWebSocket.taskExecutor == null) {
            ThreadPoolTaskExecutor taskExecutor = applicationContext.getBean("taskExecutor", ThreadPoolTaskExecutor.class);
            ExamWebSocket.taskExecutor = taskExecutor;
        }
        if (ExamWebSocket.ballMonitorTaskMapper == null) {
            BallMonitorTaskMapper ballMonitorTaskMapper = applicationContext.getBean("ballMonitorTaskMapper", BallMonitorTaskMapper.class);
            ExamWebSocket.ballMonitorTaskMapper = ballMonitorTaskMapper;
        }
    }

    public ConcurrentHashMap<String, ExamWebSocket> getConns() {
        return conns;
    }

    public ConcurrentHashMap getExamWebSocket() {
        return conns;
    }
}
