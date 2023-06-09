package com.csdtb.principal.service.impl;

import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.csdtb.common.ResponseResult;
import com.csdtb.common.constant.RegexType;
import com.csdtb.common.dto.user.AddUserDTO;
import com.csdtb.common.dto.user.LoginDTO;
import com.csdtb.common.dto.user.UpdateUserDTO;
import com.csdtb.common.dto.user.UserDTO;
import com.csdtb.common.vo.PageData;
import com.csdtb.common.vo.user.LoginVo;
import com.csdtb.common.vo.user.UserPageVo;
import com.csdtb.database.entity.MenuEntity;
import com.csdtb.database.entity.RoleMenuEntity;
import com.csdtb.database.entity.UserLoginEntity;
import com.csdtb.database.mapper.MenuMapper;
import com.csdtb.database.mapper.RoleMenuMapper;
import com.csdtb.database.mapper.UserLoginMapper;
import com.csdtb.principal.exception.GlobalException;
import com.csdtb.principal.service.UserLoginService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Override
    public ResponseResult addUser(AddUserDTO dto) {

        //校验账户唯一性
        UserLoginEntity userEntity = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getAccount, dto.getAccount()));
        if (userEntity != null) {
            throw new GlobalException(ResponseResult.error("当前手机号已注册"));
        }

        //密码加密
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String md5Password = md5.digestHex(dto.getPassword() + salt);

        //新增用户
        UserLoginEntity entity = new UserLoginEntity();
        BeanUtils.copyProperties(dto, entity);
        entity.setPassword(md5Password);

        try {
            userLoginMapper.insert(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseResult.error("系统异常,新增用户失败"));
        }

        return ResponseResult.success();
    }

    @Override
    public ResponseResult selectUserByPage(Integer page, Integer pageSize, String username, String controlUnit) {
        //条件过滤
        LambdaQueryWrapper<UserLoginEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(UserLoginEntity::getUsername, username);
        }
        if (StringUtils.hasText(controlUnit)) {
            wrapper.like(UserLoginEntity::getAccount, controlUnit);
        }

        Page<UserLoginEntity> pageInfo = userLoginMapper.selectPage(new Page(page, pageSize), wrapper);

        if (CollectionUtils.isEmpty(pageInfo.getRecords())) {
            return ResponseResult.success(PageData.initPageVo(pageInfo));
        }

        //组装数据
        return ResponseResult.success(PageData.initPageVo(pageInfo, backVo(pageInfo.getRecords())));
    }

    @Override
    public ResponseResult updateUser(UpdateUserDTO dto) {

        //校验当前数据库是否存在此用户
        UserLoginEntity user = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getId, dto.getId()));
        if (user == null) {
            throw new GlobalException(ResponseResult.error("当前用户不存在"));
        }

        UserLoginEntity entity = new UserLoginEntity();
        BeanUtils.copyProperties(dto, entity);
        if (StringUtils.hasText(dto.getPassword())) {

            //密码加密
            Digester md5 = new Digester(DigestAlgorithm.MD5);
            String md5Password = md5.digestHex(dto.getPassword() + salt);
            entity.setPassword(md5Password);
        }

        try {
            userLoginMapper.updateById(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GlobalException(ResponseResult.error("修改用户信息失败"));
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

    @Override
    public ResponseResult toLogin(LoginDTO dto) {
        //查询数据库中是否存在当前账户
        UserLoginEntity userEntity = userLoginMapper.selectOne(new LambdaQueryWrapper<UserLoginEntity>()
                .eq(UserLoginEntity::getAccount, dto.getAccount()));

        if (userEntity == null) {
            throw new GlobalException(ResponseResult.error("当前账户不存在"));
        }
        //校验密码一致性
        String dbPassword = userEntity.getPassword();

        //密码加密
        Digester md5 = new Digester(DigestAlgorithm.MD5);
        String md5Password = md5.digestHex(dto.getPassword() + salt);

        if (!dbPassword.equals(md5Password)) {
            throw new GlobalException( ResponseResult.error("密码错误,请重新输入"));
        }

        //生成token,将用户信息存入redis,2小时过期,如果进行操作则刷新token过期时间
        String token = UUID.randomUUID().toString().replace("-","");
        UserDTO user = new UserDTO();
        BeanUtils.copyProperties(userEntity, user);
        String userLoginKey = "Login_User:" + userEntity.getId();

        //一个账户只能登录一次，因为后续考试需要保证同一时间同一个用户只在一个客户端开考
        if (redisTemplate.hasKey(userLoginKey)) {
            String oldToken = (String) redisTemplate.opsForValue().get(userLoginKey);
            redisTemplate.delete(oldToken);
        }

        redisTemplate.opsForValue().set(userLoginKey,token,2 * 60 * 60, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(token, user, 2 * 60 * 60, TimeUnit.SECONDS);

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setUserVo(user);

        //获取当前用户角色拥有的菜单
        List<RoleMenuEntity> roleMenuList = roleMenuMapper.selectList(new LambdaQueryWrapper<RoleMenuEntity>()
                .eq(RoleMenuEntity::getRole, userEntity.getRole()));

        if (!CollectionUtils.isEmpty(roleMenuList)) {
            List<MenuEntity> menuList = menuMapper.selectList(new LambdaQueryWrapper<MenuEntity>()
                    .in(MenuEntity::getId, roleMenuList
                            .stream()
                            .map(RoleMenuEntity::getMenuId)
                            .collect(Collectors.toList())));

            List<LoginVo.MenuVo> menuVoList = new ArrayList<>(menuList.size());
            menuList.forEach(item -> {
                menuVoList.add(LoginVo.MenuVo.builder()
                        .id(item.getId())
                        .title(item.getTitle())
                        .path(item.getPath())
                        .build());
            });
            loginVo.setMenuVoList(menuVoList);
        }

        return ResponseResult.success(loginVo);
    }

    @Override
    public ResponseResult loginOut(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token)) {
            redisTemplate.delete(token);
        }
        return ResponseResult.success();
    }

    private List<UserPageVo> backVo(List<UserLoginEntity> entityList) {
        List<UserPageVo> voList = new ArrayList<>(entityList.size());
        entityList.forEach(entity -> {
            UserPageVo vo = new UserPageVo();
            BeanUtils.copyProperties(entity, vo);
            voList.add(vo);
        });
        return voList;
    }
}
