package com.lier.yygh.controller;

import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.user.UserInfo;
import com.lier.yygh.service.UserInfoService;
import com.lier.yygh.util.UserInfoHelper;
import com.lier.yygh.vo.user.LoginVo;
import com.lier.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/19 - 22:09
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("登录")
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        Map<String,Object> result = userInfoService.userLogin(loginVo);
        return Result.ok(result);
    }

    @ApiOperation("用户认证")
    @PostMapping("auth/userAuth")
    public Result userAuth(HttpServletRequest request, @RequestBody UserAuthVo userAuthVo){
        Long userId = UserInfoHelper.getUserId(request);
        userInfoService.userAuth(userId,userAuthVo);
        return Result.ok();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        Long userId = UserInfoHelper.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);
        return Result.ok(userInfo);
    }
}
