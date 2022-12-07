package com.csdtb.principal.websocket;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.constant.UserEnum;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.common.dto.websocket.ExamNoticeResp;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.database.entity.ExamRecordsDetailEntity;
import com.csdtb.database.entity.ExamRecordsEntity;
import com.csdtb.database.mapper.ExamInfoMapper;
import com.csdtb.database.mapper.ExamRecordsDetailMapper;
import com.csdtb.database.mapper.ExamRecordsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author zhoujiacheng
 * @Date 2022-11-17
 **/
@Component
@ServerEndpoint(value = "/exam-info/connect/{token}/{examId}")
@Slf4j
public class ExamWebSocket {

    /**
     * 存放所有考试用户信息
     */
    public static ConcurrentHashMap<String, ExamWebSocket> conns = new ConcurrentHashMap<>();

    private static ApplicationContext applicationContext;

    private static RedisTemplate redisTemplate;

    private static ExamInfoMapper examInfoMapper;

    private static ExamRecordsMapper examRecordsMapper;

    private static ExamRecordsDetailMapper examRecordsDetailMapper;

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

    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
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

        //只对管制员进行数据交互
        if (user == null || !user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
            return;
        }

        this.user = user;

        //获取考试信息
        ExamInfoEntity examInfoEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, examId));
        this.examInfoEntity = examInfoEntity;

        //生成考试记录
        insertExamRecord(user, examInfoEntity);

        //存放当前管制员考试信息
        conns.put(token, this);
    }

    @OnMessage
    public void onMessage(String message,@PathParam("token") String token) {
        //获取当前管制员考试信息
        ExamWebSocket examWebSocket = conns.get(token);
        if (examWebSocket == null) {
            return;
        }
        ExamInfoEntity examInfoEntity = examWebSocket.examInfoEntity;
        UserDTO user = examWebSocket.user;
        //获取消息
        ExamNoticeResp resp = JSON.parseObject(message, ExamNoticeResp.class);
        ExamRecordsDetailEntity detailEntity = new ExamRecordsDetailEntity();
        if (examWebSocket.examRecordsId != null) {
            detailEntity.setExamRecordsId(examWebSocket.examRecordsId);
        }else{
            //通过查询考试记录获取
            ExamRecordsEntity recordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                    .eq(ExamRecordsEntity::getExamId, examInfoEntity.getId())
                    .eq(ExamRecordsEntity::getUserId, user.getId()));
            if (recordsEntity != null) {
                detailEntity.setExamRecordsId(recordsEntity.getId());
            }
        }
        BeanUtils.copyProperties(resp,detailEntity);
        examRecordsDetailMapper.insert(detailEntity);
    }

    @OnMessage
    public void onMessage(byte[] message,Session session){
        try{
            //把传来的字节流数组写入到上面创建的文件对象里去
            saveFileFromBytes(message);
            log.info("视频传输中:"+message.toString());
            //只有客户端接受到ok才会传输下一段文件
            session.getBasicRemote().sendText("服务端保存中...");
        }catch (IOException e){
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
    public void onClose(Session session, @PathParam("token") String token) {
        conns.remove(token);
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        }else{
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
    }

    public ConcurrentHashMap getExamWebSocket(){
        return conns;
    }
}
