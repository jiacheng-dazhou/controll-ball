package com.csdtb.principal.service.impl;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.ExamInfoEnum;
import com.csdtb.common.constant.ExcelPatternEnum;
import com.csdtb.common.constant.ResponseType;
import com.csdtb.common.constant.UserEnum;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.common.vo.PageData;
import com.csdtb.common.vo.records.ExamRecordDetailVo;
import com.csdtb.common.vo.records.ExamRecordsPageVo;
import com.csdtb.common.vo.records.ExcelCalculateVo;
import com.csdtb.common.vo.records.ExcelMonitorVo;
import com.csdtb.database.entity.*;
import com.csdtb.database.mapper.ExamInfoMapper;
import com.csdtb.database.mapper.ExamRecordsDetailMapper;
import com.csdtb.database.mapper.ExamRecordsMapper;
import com.csdtb.database.mapper.UserLoginMapper;
import com.csdtb.principal.exception.GlobalException;
import com.csdtb.principal.service.ExamRecordsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.list.TreeList;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 考试记录表 服务实现类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-18
 */
@Service
@Slf4j
public class ExamRecordsServiceImpl implements ExamRecordsService {

    @Resource
    private ExamRecordsMapper examRecordsMapper;

    @Resource
    private ExamRecordsDetailMapper examRecordsDetailMapper;

    @Resource
    private UserLoginMapper userLoginMapper;

    @Resource
    private ExamInfoMapper examInfoMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public ResponseResult selectRecordsByPage(Integer page, Integer pageSize, String examName, String userName, String token) {
        //根据角色条件过滤
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);

        Page<ExamRecordPageEntity> pageInfo = examRecordsMapper.selectPageByCondition(new Page(page, pageSize),
                user.getRole().equals(UserEnum.CONTROLLER.getRole()) ? user.getId() : null, examName, userName);

        List<ExamRecordPageEntity> entityList = pageInfo.getRecords();
        if (CollectionUtils.isEmpty(entityList)) {
            return ResponseResult.success(PageData.initPageVo(pageInfo));
        }

        return ResponseResult.success(PageData.initPageVo(pageInfo, backVo(entityList)));
    }

    @Override
    public ResponseResult selectRecordDetail(Integer id, String token) {
        //获取当前登录人信息
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);

        //根据考核记录id，获取考核、用户信息
        ExamRecordsEntity recordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                .eq(ExamRecordsEntity::getId, id));

        if (recordsEntity == null) {
            throw new GlobalException(ResponseResult.error("暂未查询到当前考核记录"));
        }

        //获取考试记录信息
        ExamInfoEntity examEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, recordsEntity.getExamId()));
        if (examEntity == null) {
            throw new GlobalException(ResponseResult.error("暂未查询到当前考核信息"));
        }
        //获取用户信息
        UserLoginEntity userEntity = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getId, recordsEntity.getUserId()));
        if (userEntity == null) {
            throw new GlobalException(ResponseResult.error("暂未查询到当前考生信息"));
        }
        //获取考核记录信息
        List<ExamRecordsDetailEntity> detailEntityList = examRecordsDetailMapper.selectList(new LambdaQueryWrapper<ExamRecordsDetailEntity>()
                .eq(ExamRecordsDetailEntity::getExamRecordsId, recordsEntity.getId()));

        //组装数据
        ExamRecordDetailVo examRecordDetailVo = new ExamRecordDetailVo();
        //基础信息
        backBaseInfo(examRecordDetailVo, examEntity, userEntity);
        //监控任务
        backMonitorInfo(examRecordDetailVo, examEntity, detailEntityList, user);
        //计算任务
        backCalculateInfo(examRecordDetailVo, examEntity, detailEntityList, user);

        return ResponseResult.success(examRecordDetailVo);
    }

    @Override
    public void exportRecordDetail(Integer id, String token, HttpServletResponse response) {
        //获取当前登录人信息
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);
        if (user == null) {
            log.info("获取用户信息失败");
            try {
                response.getWriter().write(JSON.toJSONString(ResponseResult.error("导出失败，获取用户信息失败")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
            log.info("管制员没有导出权限");
            try {
                response.getWriter().write(JSON.toJSONString(ResponseResult.error("导出失败，管制员没有导出权限")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        //获取导出数据
        ResponseResult<ExamRecordDetailVo> result = selectRecordDetail(id, token);

        if (!result.getCode().equals(ResponseType.SUCCESS.getCode())) {
            try {
                response.getWriter().write(JSON.toJSONString(ResponseResult.error("导出失败，获取考核详情失败")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        //获取考核名称
        ExamRecordsEntity recordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                .eq(ExamRecordsEntity::getId, id));
        ExamInfoEntity examEntity = examInfoMapper.selectOne(new LambdaQueryWrapper<ExamInfoEntity>()
                .eq(ExamInfoEntity::getId, recordsEntity.getExamId()));

        ExamRecordDetailVo examRecordDetailVo = result.getData();
        //组装导出数据
        excelExport(examRecordDetailVo, response, examEntity.getName());
    }

    @Override
    public void selectExamRecordVideo(Integer id, String token, HttpServletResponse response) throws Exception{
        //获取当前登录人信息
        UserDTO user = (UserDTO) redisTemplate.opsForValue().get(token);
        if (user == null || user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
            log.info("获取用户信息失败或当前用户无权限");
            response.getWriter().write(JSON.toJSONString(ResponseResult.error("查看录像失败,获取用户信息失败或无权限")));
            return;
        }
        //查询考核记录
        ExamRecordsEntity examRecordsEntity = examRecordsMapper.selectOne(new LambdaQueryWrapper<ExamRecordsEntity>()
                .eq(ExamRecordsEntity::getId, id));
        if (examRecordsEntity == null) {
            response.getWriter().write(JSON.toJSONString(ResponseResult.error("查看录像失败,获取考核记录为空")));
            return;
        }
        File file = new File(examRecordsEntity.getVideoPath());
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(FileUtil.readBytes(file));
        outputStream.flush();
        outputStream.close();
    }

    private void excelExport(ExamRecordDetailVo vo, HttpServletResponse response, String examName) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //标题
        writer.merge(8, String.format("%s_%s_考试记录", vo.getUsername(), examName));
        writer.passCurrentRow();
        //组装基本信息
        packagingBaseInfo(writer, vo);
        writer.passCurrentRow();
        //设置列宽
        for (int i = 0; i <= 6; i++) {
            writer.setColumnWidth(i, 15);
        }
        //组装监控任务
        packagingMonitorInfo(writer, vo);
        writer.passCurrentRow();
        //组装计算任务
        packagingCalculateInfo(writer, vo);
        writer.passCurrentRow();

        //脑电任务后续补充

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", String.format("attachment;filename=exam_records_%s.xlsx", System.currentTimeMillis()));
        ServletOutputStream out = null;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.flush(out, true);
            writer.close();
            IoUtil.close(out);
        }
    }

    private void packagingCalculateInfo(ExcelWriter writer, ExamRecordDetailVo vo) {
        //拼装监控任务
        List<String> list = new TreeList<>();
        ExamRecordDetailVo.CalculateVo calculateVo = vo.getCalculateVo();
        list.add(calculateVo.getTitleCount() + "道");
        list.add(calculateVo.getLevel().equals(1) ? "简单" : calculateVo.getLevel().equals(2) ? "一般" : "困难");
        writer.merge(8, appendContent(list, ExcelPatternEnum.CALCULATE_INFO.getPattern()[0]), true);
        writer.setRowHeight(writer.getCurrentRow() - 1, 25);

        List<ExamRecordDetailVo.CalculateVo.CalculateDetailVo> detailVoList = calculateVo.getCalculateDetailVoList();
        if (CollectionUtils.isEmpty(detailVoList)) {
            return;
        }

        //详情数据
        writer.writeRow(Arrays.asList(ExcelPatternEnum.CALCULATE_INFO.getPattern()[1].split(",")), true);
        List<ExcelCalculateVo> excelCalculateVoList = new TreeList<>();
        for (int i = 0; i < detailVoList.size(); i++) {
            ExamRecordDetailVo.CalculateVo.CalculateDetailVo detailVo = detailVoList.get(i);
            ExcelCalculateVo excelCalculateVo = new ExcelCalculateVo();
            BeanUtils.copyProperties(detailVo, excelCalculateVo);
            excelCalculateVo.setId(i + 1);
            excelCalculateVo.setIsCorrect(detailVo.getIsCorrect().equals(Boolean.TRUE) ? "是" : "否");
            excelCalculateVoList.add(excelCalculateVo);
        }
        writer.write(excelCalculateVoList);

        //正确率
        list.clear();
        list.add(calculateVo.getCalculateRightRate());
        writer.merge(8, appendContent(list, ExcelPatternEnum.CALCULATE_INFO.getPattern()[2]), true);
    }

    private void packagingMonitorInfo(ExcelWriter writer, ExamRecordDetailVo vo) {
        //拼装监控任务
        List<String> list = new TreeList<>();
        ExamRecordDetailVo.MonitorVo monitorVo = vo.getMonitorVo();
        list.add(monitorVo.getMonitorTime());
        list.add(monitorVo.getLevel().equals(1) ? "简单" : monitorVo.getLevel().equals(2) ? "一般" : "困难");
        list.add(monitorVo.getBoundsTotal() + "次");
        list.add(monitorVo.getCrashTotal() + "次");
        list.add(monitorVo.getTurnbackTotal() + "次");
        writer.merge(8, appendContent(list, ExcelPatternEnum.MONITOR_INFO.getPattern()[0]), true);
        writer.setRowHeight(writer.getCurrentRow() - 1, 25);

        List<ExamRecordDetailVo.MonitorVo.MonitorDetailVo> detailVoList = monitorVo.getMonitorDetailVoList();
        if (CollectionUtils.isEmpty(detailVoList)) {
            return;
        }

        //详情数据
        writer.writeRow(Arrays.asList(ExcelPatternEnum.MONITOR_INFO.getPattern()[1].split(",")), true);
        List<ExcelMonitorVo> excelMonitorVoList = new TreeList<>();
        for (int i = 0; i < detailVoList.size(); i++) {
            ExamRecordDetailVo.MonitorVo.MonitorDetailVo detailVo = detailVoList.get(i);
            ExcelMonitorVo excelMonitorVo = new ExcelMonitorVo();
            BeanUtils.copyProperties(detailVo, excelMonitorVo);
            excelMonitorVo.setId(i + 1);
            excelMonitorVo.setIsCorrect(detailVo.getIsCorrect().equals(Boolean.TRUE) ? "是" : "否");
            excelMonitorVo.setIsReactInAdvance(detailVo.getIsReactInAdvance().equals(Boolean.TRUE) ? "是" : "否");
            excelMonitorVoList.add(excelMonitorVo);
        }
        writer.write(excelMonitorVoList);

        //正确率
        list.clear();
        list.add(monitorVo.getBoundsRightRate());
        list.add(monitorVo.getTurnbackRightRate());
        list.add(monitorVo.getCrashRightRate());
        writer.merge(8, appendContent(list, ExcelPatternEnum.MONITOR_INFO.getPattern()[2]), true);
    }

    private void packagingBaseInfo(ExcelWriter writer, ExamRecordDetailVo vo) {
        writer.merge(1, "基本信息");
        //拼装基本信息
        List<String> list = new TreeList<>();
        list.add(vo.getUsername());
        list.add(String.valueOf(vo.getAge()));
        list.add(vo.getSex().equals(Boolean.FALSE) ? "男" : "女");
        list.add(vo.getControlUnit());
        list.add(vo.getPositionalTitle());
        list.add(vo.getPositionalJob());
        writer.merge(8, appendContent(list, ExcelPatternEnum.USER_BASE_INFO.getPattern()[0]), false);
        writer.setRowHeight(writer.getCurrentRow() - 1, 25);
        list.clear();
        list.add(vo.getStartTime());
        list.add(vo.getTotalTime());
        list.add(vo.getNowTime());
        list.add(vo.getBreakNumber() + "次");
        list.add(vo.getBreakTotalTime());
        writer.merge(8, appendContent(list, ExcelPatternEnum.USER_BASE_INFO.getPattern()[1]), false);
        writer.setRowHeight(writer.getCurrentRow() - 1, 25);
    }

    private String appendContent(List<String> list, String pattern) {
        return String.format(pattern.replaceAll("%s", " %s    "), list.toArray());
    }

    private void backCalculateInfo(ExamRecordDetailVo vo, ExamInfoEntity examEntity, List<ExamRecordsDetailEntity> detailEntityList, UserDTO user) {
        ExamRecordDetailVo.CalculateVo calculateVo = new ExamRecordDetailVo.CalculateVo();
        calculateVo.setLevel(examEntity.getCalculateLevel());

        List<ExamRecordsDetailEntity> calculateList = detailEntityList.stream()
                .filter(item -> item.getType().equals(ExamInfoEnum.CALCULATE.getStatus()))
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(calculateList)) {
            calculateVo.setTitleCount(0);
            vo.setCalculateVo(calculateVo);
            return;
        }
        calculateVo.setTitleCount(calculateList.size());

        //正确率
        long calculateRight = calculateList.stream().filter(item -> item.getIsCorrect().equals(Boolean.TRUE)).count();
        calculateVo.setCalculateRightRate(BigDecimal.valueOf(calculateRight * 100 / calculateList.size())
                .setScale(2, RoundingMode.HALF_UP) + "%");

        //管制员不看详情
        if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
            vo.setCalculateVo(calculateVo);
            return;
        }

        List<ExamRecordDetailVo.CalculateVo.CalculateDetailVo> calculateDetailVoList = new ArrayList<>(calculateList.size());
        calculateList.forEach(entity -> {
            ExamRecordDetailVo.CalculateVo.CalculateDetailVo detailVo = new ExamRecordDetailVo.CalculateVo.CalculateDetailVo();
            BeanUtils.copyProperties(entity, detailVo);
            detailVo.setJudgmentEfficiency(entity.getIsCorrect() ? setJudgmentEfficiency(entity.getReactionTime(),calculateRight) : "-");
            calculateDetailVoList.add(detailVo);
        });

        //排序
        calculateVo.setCalculateDetailVoList(calculateDetailVoList.stream()
                .sorted(Comparator.comparing(ExamRecordDetailVo.CalculateVo.CalculateDetailVo::getStartTime))
                .collect(Collectors.toList()));

        vo.setCalculateVo(calculateVo);
    }

    private String setJudgmentEfficiency(String reactionTime, long calculateRight) {
        reactionTime = reactionTime.replace("s", "");

        return BigDecimal.valueOf(Double.valueOf(reactionTime))
                .divide(BigDecimal.valueOf(calculateRight), 2, RoundingMode.HALF_UP) + "%";
    }

    private void backMonitorInfo(ExamRecordDetailVo vo, ExamInfoEntity examEntity, List<ExamRecordsDetailEntity> detailEntityList, UserDTO user) {
        ExamRecordDetailVo.MonitorVo monitorVo = new ExamRecordDetailVo.MonitorVo();
        monitorVo.setMonitorTime(vo.getNowTime());
        monitorVo.setLevel(examEntity.getMonitorLevel());
        if (CollectionUtils.isEmpty(detailEntityList)) {
            monitorVo.setBoundsTotal(0);
            monitorVo.setCrashTotal(0);
            monitorVo.setTurnbackTotal(0);
        } else {
            Map<Integer, List<ExamRecordsDetailEntity>> detailListMap = detailEntityList.stream()
                    .collect(Collectors.groupingBy(ExamRecordsDetailEntity::getType));
            List<ExamRecordsDetailEntity> turnbackList = detailListMap.get(ExamInfoEnum.TURNBACK.getStatus());
            List<ExamRecordsDetailEntity> boundsList = detailListMap.get(ExamInfoEnum.BOUNDS.getStatus());
            List<ExamRecordsDetailEntity> crashList = detailListMap.get(ExamInfoEnum.CRASH.getStatus());

            //监控统计
            monitorVo.setCrashTotal(CollectionUtils.isEmpty(crashList) ? 0 : crashList.size());

            if (CollectionUtils.isEmpty(turnbackList)) {
                monitorVo.setTurnbackTotal(0);
                monitorVo.setTurnbackRightRate("0.00%");
            } else {
                monitorVo.setTurnbackTotal(turnbackList.size());
                long turnBackRight = turnbackList.stream()
                        .filter(item -> item.getIsCorrect().equals(Boolean.TRUE))
                        .count();
                monitorVo.setTurnbackRightRate(CollectionUtils.isEmpty(turnbackList) ?
                        "0.00%" : BigDecimal.valueOf(turnBackRight * 100 / turnbackList.size())
                        .setScale(2, RoundingMode.HALF_UP) + "%");
            }

            if (CollectionUtils.isEmpty(boundsList)) {
                monitorVo.setBoundsTotal(0);
                monitorVo.setBoundsRightRate("0.00%");
            } else {
                monitorVo.setBoundsTotal(boundsList.size());
                long boundsRight = boundsList.stream()
                        .filter(item -> item.getIsCorrect().equals(Boolean.TRUE))
                        .count();
                monitorVo.setBoundsRightRate(CollectionUtils.isEmpty(boundsList) ?
                        "0.00%" : BigDecimal.valueOf(boundsRight * 100 / boundsList.size())
                        .setScale(2, RoundingMode.HALF_UP) + "%");
            }

            if (CollectionUtils.isEmpty(crashList)) {
                monitorVo.setCrashTotal(0);
                monitorVo.setCrashRightRate("0.00%");
            } else {
                monitorVo.setCrashTotal(crashList.size());
                long crashRight = crashList.stream()
                        .filter(item -> item.getIsCorrect().equals(Boolean.TRUE))
                        .count();
                monitorVo.setCrashRightRate(CollectionUtils.isEmpty(crashList) ?
                        "0.00%" : BigDecimal.valueOf(crashRight * 100 / crashList.size())
                        .setScale(2, RoundingMode.HALF_UP) + "%");
            }

            //管制员不看详情
            if (user.getRole().equals(UserEnum.CONTROLLER.getRole())) {
                vo.setMonitorVo(monitorVo);
                return;
            }

            List<ExamRecordsDetailEntity> monitorDetailList = detailEntityList.stream().filter(item -> !item.getType().equals(ExamInfoEnum.CALCULATE.getStatus())).collect(Collectors.toList());
            List<ExamRecordDetailVo.MonitorVo.MonitorDetailVo> detailVoList = new ArrayList<>(monitorDetailList.size());
            monitorDetailList.forEach(detailEntity -> {
                ExamRecordDetailVo.MonitorVo.MonitorDetailVo monitorDetailVo = new ExamRecordDetailVo.MonitorVo.MonitorDetailVo();
                BeanUtils.copyProperties(detailEntity, monitorDetailVo);
                monitorDetailVo.setJudgmentEfficiency(setJudgmentEfficiency(monitorDetailVo, monitorVo));
                detailVoList.add(monitorDetailVo);
            });

            //排序
            monitorVo.setMonitorDetailVoList(detailVoList.stream()
                    .sorted(Comparator.comparing(ExamRecordDetailVo.MonitorVo.MonitorDetailVo::getStartTime))
                    .collect(Collectors.toList()));

            vo.setMonitorVo(monitorVo);
        }
    }

    private String setJudgmentEfficiency(ExamRecordDetailVo.MonitorVo.MonitorDetailVo monitorDetailVo, ExamRecordDetailVo.MonitorVo monitorVo) {
        if (!monitorDetailVo.getIsCorrect()) {
            return "-";
        }
        String reactionTime = monitorDetailVo.getReactionTime().replace("s", "");
        String rightRate;
        switch (monitorDetailVo.getType()) {
            case 1:
                rightRate = monitorVo.getTurnbackRightRate().replace("%", "");
                break;
            case 2:
                rightRate = monitorVo.getBoundsRightRate().replace("%", "");
                break;
            case 3:
                rightRate = monitorVo.getCrashRightRate().replace("%", "");
                break;
            default:
                throw new GlobalException(ResponseResult.error("暂不支持的数据类型"));
        }
        if (!StringUtils.hasText(reactionTime) || reactionTime.equals("0.00") || rightRate.equals("0.00")) {
            return "0.00%";
        } else {
            return BigDecimal.valueOf(Double.valueOf(reactionTime))
                    .divide(BigDecimal.valueOf(Double.valueOf(rightRate)), 2, RoundingMode.HALF_UP) + "%";
        }
    }

    private void backBaseInfo(ExamRecordDetailVo vo, ExamInfoEntity examEntity, UserLoginEntity userEntity) {
        BeanUtils.copyProperties(userEntity, vo);
        vo.setStartTime(examEntity.getStartTime());
        //考核总时长
        vo.setTotalTime(toLocalTime(examEntity.getStartTime(), examEntity.getEndTime()));
        //考核时长
        if (examEntity.getStatus().equals(ExamInfoEnum.IN_PROGRESS.getStatus())) {
            //考试进行中
            String endTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            vo.setNowTime(toLocalTime(examEntity.getStartTime(), endTime));
        } else {
            //考试结束
            vo.setNowTime(vo.getTotalTime());
        }
        //休息次数
        Duration duration = Duration.between(LocalTime.parse("00:00:00"), LocalTime.parse(vo.getNowTime()));
        long breakNumber = duration.getSeconds() /
                (examEntity.getMonitorDuration() * 60 + examEntity.getMonitorSleepDuration());
        vo.setBreakNumber((int) breakNumber);
        //休息总时长
        Duration breakTime = Duration.ofSeconds(breakNumber * examEntity.getMonitorSleepDuration());
        vo.setBreakTotalTime(toLocalTime(breakTime));
    }

    /**
     * 计算时间差值（格式：HH:mm:ss）
     *
     * @param startTime 起始时间
     * @param endTime   结束时间
     * @return 返回（格式：HH:mm:ss）
     */
    private String toLocalTime(String startTime, String endTime) {
        LocalDateTime examStartTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LocalDateTime examEndTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Duration duration = Duration.between(examStartTime, examEndTime);
        if (duration.toHours() >= 24) {
            return "02:00:00";
        }
        int hours = (int) duration.toHours();
        int minute = (int) (duration.toMinutes() - duration.toHours() * 60);
        int seconds = (int) (duration.getSeconds() - duration.toMinutes() * 60);
        return LocalTime.of(hours, minute, seconds).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String toLocalTime(Duration duration) {
        int hours = (int) duration.toHours();
        int minute = (int) (duration.toMinutes() - duration.toHours() * 60);
        int seconds = (int) (duration.getSeconds() - duration.toMinutes() * 60);
        return LocalTime.of(hours, minute, seconds).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private List<ExamRecordsPageVo> backVo(List<ExamRecordPageEntity> entityList) {
        List<ExamRecordsPageVo> voList = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> {
            ExamRecordsPageVo vo = new ExamRecordsPageVo();
            BeanUtils.copyProperties(entity, vo);
            voList.add(vo);
        });
        return voList;
    }
}
