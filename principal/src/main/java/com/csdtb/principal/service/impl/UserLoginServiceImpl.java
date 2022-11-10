package com.csdtb.principal.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.RegexType;
import com.csdtb.common.dto.user.AddUserDTO;
import com.csdtb.common.dto.user.UpdateUserDTO;
import com.csdtb.common.vo.PageData;
import com.csdtb.common.vo.user.UserPageVo;
import com.csdtb.database.entity.UserLoginEntity;
import com.csdtb.database.mapper.UserLoginMapper;
import com.csdtb.principal.service.UserLoginService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 账号管理表 服务实现类
 * </p>
 *
 * @author zhoujiacheng
 * @since 2022-11-09
 */
@Service
public class UserLoginServiceImpl implements UserLoginService {

    @Value("${md5.salt}")
    private String salt;
    @Resource
    private UserLoginMapper userLoginMapper;

    @Override
    public ResponseResult addUser(AddUserDTO dto) {
        //校验账户、密码格式
        if (!checkAccount(dto.getAccount())) {
            return ResponseResult.error("请校验手机号格式");
        }

        if (!ReUtil.isMatch(RegexType.PASSWORD.getType(), dto.getPassword())) {
            return ResponseResult.error("请校验密码格式");
        }

        //校验账户唯一性
        UserLoginEntity userEntity = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getAccount, dto.getAccount()));
        if (userEntity != null) {
            return ResponseResult.error("当前手机号已注册");
        }

        //密码加密
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String md5Password = md5.digestHex(dto.getPassword() + salt);

        //新增用户
        UserLoginEntity entity = new UserLoginEntity();
        BeanUtils.copyProperties(dto,entity);
        entity.setPassword(md5Password);

        try {
            userLoginMapper.insert(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("系统异常,新增用户失败");
        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult selectUserByPage(Integer page, Integer pageSize, String username, Long account) {
        //条件过滤
        LambdaQueryWrapper<UserLoginEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(UserLoginEntity::getUsername,username);
        }
        if (account != null) {
            wrapper.like(UserLoginEntity::getAccount,account);
        }

        Page<UserLoginEntity> pageInfo = userLoginMapper.selectPage(new Page(page, pageSize), wrapper);

        if (CollectionUtils.isEmpty(pageInfo.getRecords())) {
            return ResponseResult.success(PageData.initPageVo(pageInfo));
        }

        //组装数据
        return ResponseResult.success(PageData.initPageVo(pageInfo,backVo(pageInfo.getRecords())));
    }

    @Override
    public ResponseResult updateUser(UpdateUserDTO dto) {

        //校验当前数据库是否存在此用户
        UserLoginEntity user = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getId, dto.getId()));
        if (user == null) {
            return ResponseResult.error("当前用户不存在");
        }

        UserLoginEntity entity = new UserLoginEntity();
        BeanUtils.copyProperties(dto,entity);
        if (StringUtils.hasText(dto.getPassword())) {
            //校验密码格式
            if (!ReUtil.isMatch(RegexType.PASSWORD.getType(), dto.getPassword())) {
                return ResponseResult.error("请校验密码格式");
            }

            //密码加密
            Digester md5 = new Digester(DigestAlgorithm.MD5);
            String md5Password = md5.digestHex(dto.getPassword() + salt);
            entity.setPassword(md5Password);
        }

        try {
            userLoginMapper.updateById(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("修改用户信息失败");
        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult deleteUser(Long id) {

        try {
            userLoginMapper.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseResult.success();
    }

    private List<UserPageVo> backVo(List<UserLoginEntity> entityList) {
        List<UserPageVo> voList = new ArrayList<>(entityList.size());
        entityList.forEach(entity->{
            UserPageVo vo = new UserPageVo();
            BeanUtils.copyProperties(entity,vo);
            voList.add(vo);
        });
        return voList;
    }

    private boolean checkAccount(Long account) {
        String accountStr = String.valueOf(account);
        boolean flag1 = ReUtil.isMatch(RegexType.TELECOM.getType(), accountStr);
        boolean flag2 = ReUtil.isMatch(RegexType.UNICOM.getType(), accountStr);
        boolean flag3 = ReUtil.isMatch(RegexType.MOBILE.getType(), accountStr);

        return flag1 || flag2 || flag3;
    }
}
