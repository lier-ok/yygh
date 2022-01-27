package com.lier.yygh.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.user.UserInfo;
import com.lier.yygh.vo.user.LoginVo;
import com.lier.yygh.vo.user.UserAuthVo;
import com.lier.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/19 - 22:06
 * @Decription
 * @since jdk1.8
 */
public interface UserInfoService extends IService<UserInfo> {
    //登录
    Map<String, Object> userLogin(LoginVo loginVo);

    //根据openid获取用户
    UserInfo getByOpenId(String open_id);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo);

    void lock(Long userId, Integer status);

    Map<String, Object> getDetail(Long userId);

    void approve(Long userId, Integer status);
}
