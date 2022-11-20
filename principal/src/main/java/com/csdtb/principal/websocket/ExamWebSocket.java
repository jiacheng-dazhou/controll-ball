package com.csdtb.principal.websocket;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.csdtb.common.constant.UserEnum;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.common.dto.websocket.ExamNoticeResp;
import com.csdtb.database.entity.ExamInfoEntity;
import com.csdtb.database.entity.ExamRecordsDetailEntity;
import com.csdtb.database.entity.ExamRecordsEntity;
import com.csdtb.database.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
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
    public static Map<String, ExamWebSocket> conns = new ConcurrentHashMap<>();

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

    public static void setApplicationContext(ApplicationContext applicationContext) {
        ExamWebSocket.applicationContext = applicationContext;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token, @PathParam("examId") Integer examId) {
        init();

        //获取用户信息
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);

        //只对管制员进行数据交互
        if (!user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
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

    private void insertExamRecord(UserDTO user, ExamInfoEntity examInfoEntity) {
        //新增考核记录
        ExamRecordsEntity examRecordsEntity = new ExamRecordsEntity();
        examRecordsEntity.setUserId(Math.toIntExact(user.getId()));
        examRecordsEntity.setExamId(examInfoEntity.getId());

        ExamRecordsEntity recordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                .eq(ExamRecordsEntity::getUserId, user.getId())
                .eq(ExamRecordsEntity::getExamId, examInfoEntity.getId()));

        //没有当前考试记录，新增
        if (recordsEntity == null) {
            examRecordsMapper.insertAndReturnId(examRecordsEntity);
            this.examRecordsId = examRecordsEntity.getId();
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
}
