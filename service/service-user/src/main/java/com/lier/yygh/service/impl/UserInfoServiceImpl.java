package com.lier.yygh.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.enums.AuthStatusEnum;
import com.lier.yygh.helper.JwtHelper;
import com.lier.yygh.mapper.UserInfoMapper;
import com.lier.yygh.model.user.Patient;
import com.lier.yygh.model.user.UserInfo;
import com.lier.yygh.service.PatientService;
import com.lier.yygh.service.UserInfoService;
import com.lier.yygh.vo.user.LoginVo;
import com.lier.yygh.vo.user.UserAuthVo;
import com.lier.yygh.vo.user.UserInfoQueryVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/19 - 22:06
 * @Decription
 * @since jdk1.8
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private PatientService patientService;

    @Override
    public Map<String, Object> userLogin(LoginVo loginVo) {
        //获取验证码和手机号
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();
        //判断手机号或者验证码是否为空
        if(StringUtils.isEmpty(code) || StringUtils.isEmpty(phone)){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //从redis中获取验证码
        String phoneCode = redisTemplate.opsForValue().get(phone);
        if(phoneCode == null){
            throw new YyghException(ResultCodeEnum.CODE_EXPIRE);
        }
        //判断验证码是否相同
        if(!code.equals(phoneCode)){
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }
        //微信扫码登录,判断是否绑定手机号码
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())){
            userInfo = this.getByOpenId(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }

        }
        if(null == userInfo){ //手机号登录
            //查看该手机号是否已经注册
            QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(queryWrapper);
            if(null == userInfo) { //没注册
                userInfo = new UserInfo();
                userInfo.setName("");
                userInfo.setPhone(phone);
                userInfo.setStatus(1); //设置为未禁用
                this.save(userInfo);
            }
        }



        //判断是否被禁用
        if(userInfo.getStatus() == 0){
            throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
        }
        //封装结果
        Map<String, Object> map = new HashMap<>();
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getNickName();//换为昵称
        }
        if(StringUtils.isEmpty(name)) {
            name = userInfo.getPhone();//换为电话号码
        }
        map.put("name", name);

        //生成token数据
        String token = JwtHelper.createToken(userInfo.getId(), userInfo.getName());
        map.put("token", token);
        return map;
    }

    @Override
    public UserInfo getByOpenId(String open_id) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("openid",open_id);
        UserInfo userInfo = baseMapper.selectOne(wrapper);
        return userInfo;
    }

    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        //查询用户
        UserInfo userInfo = baseMapper.selectById(userId);
        //将传过来的值设置进去
        userInfo.setName(userAuthVo.getName());
        userInfo.setCertificatesType(userAuthVo.getCertificatesType());
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo());
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl());
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus());
        //保存结果到数据库
        baseMapper.updateById(userInfo);
    }

    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> pageParam, UserInfoQueryVo userInfoQueryVo) {
        //取出查询条件值进行判空,合成查询条件
        String name = userInfoQueryVo.getKeyword();//用户名称
        Integer status = userInfoQueryVo.getStatus();//用户状态
        Integer authStatus = userInfoQueryVo.getAuthStatus();//用户认证状态
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin();//开始时间
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();//结束时间

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        if(!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if(!StringUtils.isEmpty(status)){
            wrapper.like("status",status);
        }
        if(!StringUtils.isEmpty(authStatus)){
            wrapper.like("auth_status",authStatus);
        }
        if(!StringUtils.isEmpty(createTimeBegin)){
            wrapper.like("create_time",createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){
            wrapper.like("create_time",createTimeEnd);
        }

        IPage<UserInfo> userInfoPage = baseMapper.selectPage(pageParam, wrapper);

        //对查询结果处理编号
        userInfoPage.getRecords().forEach(this::packageResult);

        return userInfoPage;
    }

    //锁定
    @Override
    public void lock(Long userId, Integer status) {
        if(status == 0 || status == 1){
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("id",userId);
            UserInfo userInfo = baseMapper.selectOne(wrapper);
            userInfo.setStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    @Override
    public Map<String, Object> getDetail(Long userId) {
        HashMap<String, Object> resultMap = new HashMap<>();
        UserInfo userInfo = this.packageResult(baseMapper.selectById(userId));
        resultMap.put("userInfo",userInfo);

        List<Patient> allPatients = patientService.getAllPatients(userId);
        resultMap.put("patients",allPatients);
        return resultMap;
    }

    @Override
    public void approve(Long userId, Integer status) {
        if(status == -1 || status == 2){ //-1:不通过,2:通过
            UserInfo userInfo = baseMapper.selectById(userId);
            userInfo.setAuthStatus(status);
            baseMapper.updateById(userInfo);
        }
    }

    private UserInfo packageResult(UserInfo item) {
        //处理认证状态
        item.getParam()
                .put("authStatusString",AuthStatusEnum.getStatusNameByStatus(item.getAuthStatus()));

        //处理用户状态
        String statusString = item.getStatus().intValue() == 0 ? "锁定" : "正常";
        item.getParam().put("statusString",statusString);

        return item;
    }

}
