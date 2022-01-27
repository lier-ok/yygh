package com.lier.yygh.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.model.user.UserInfo;
import com.lier.yygh.service.UserInfoService;
import com.lier.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author lier
 * @date 2022/1/15 - 17:30
 * @Decription
 * @since jdk1.8
 */
@RestController
@RequestMapping("admin/user")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("分页查询用户列表")
    @GetMapping("/{pageNo}/{pageSize}")
    public Result getAllUserPage(@PathVariable Long pageNo, @PathVariable Long pageSize,
                                 UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> pageParam = new Page(pageNo,pageSize);
        IPage<UserInfo> userInfoPage =  userInfoService.selectPage(pageParam,userInfoQueryVo);
        return Result.ok(userInfoPage);
    }

    @ApiOperation("锁定与取消锁定")
    @GetMapping("lock/{userId}/{status}")
    public Result lock(@PathVariable Integer status, @PathVariable Long userId){
        userInfoService.lock(userId,status);
        return Result.ok();
    }

    @ApiOperation("详情")
    @GetMapping("detail/{userId}")
    public Result getUserDetail(@PathVariable Long userId){
        Map<String,Object> resultMap = userInfoService.getDetail(userId);
        return Result.ok(resultMap);
    }

    @ApiOperation("用户认证审批")
    @GetMapping("approve/{userId}/{status}")
    public Result approveUser(@PathVariable Integer status, @PathVariable Long userId){
        userInfoService.approve(userId,status);
        return Result.ok();
    }
}
