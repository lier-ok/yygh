package com.lier.yygh.controller.api;

import com.alibaba.fastjson.JSONObject;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.Result;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.helper.JwtHelper;
import com.lier.yygh.model.user.UserInfo;
import com.lier.yygh.service.UserInfoService;
import com.lier.yygh.util.HttpClientUtils;
import com.lier.yygh.util.WeiXinPropertiesUtils;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/25 - 20:14
 * @Decription
 * @since jdk1.8
 */
@Slf4j
@Controller
@RequestMapping("/api/ucenter/wx")
public class WeiXinApiController {

    @Resource
    private UserInfoService userInfoService;

    @ApiOperation("返回微信登录二维码需要的数据")
    @ResponseBody
    @GetMapping("getLoginParams")
    public Result getLoginParams(){
        Map<String,Object> result = new HashMap<>();
        result.put("app_id", WeiXinPropertiesUtils.WX_OPEN_APP_ID);
        result.put("scope", "snsapi_login");
        result.put("state", System.currentTimeMillis()+"");
        //根据微信开放平台文档,对转发地址进行编码设置
        try {
            String url = WeiXinPropertiesUtils.WX_OPEN_REDIRECT_URL;
            String encodeUrl = URLEncoder.encode(url, "utf-8");
            result.put("redirect_url",encodeUrl);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Result.ok(result);
    }

    @ApiOperation("微信回调接口")
    @GetMapping("callback")
    public String callback(String code,String state){
        //判断传递参数是否正确
        if(StringUtils.isEmpty(state) || StringUtils.isEmpty(code)){
            log.error("非法回调请求");
            throw new YyghException(ResultCodeEnum.ILLEGAL_CALLBACK_REQUEST_ERROR);
        }
        //通过app_id,app_secret和code请求微信提供的地址获取access_token
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                WeiXinPropertiesUtils.WX_OPEN_APP_ID,
                WeiXinPropertiesUtils.WX_OPEN_APP_SECRET,
                code);
        //通过httpClientUtils工具类请求该拼接的地址
        String accessTokenString = null;
        try{
            accessTokenString = HttpClientUtils.get(accessTokenUrl);
        }catch(Exception e){
            throw new YyghException(ResultCodeEnum.FETCH_ACCESSTOKEN_FAILD);
        }

        //将得到的access_token字符串解析为json,方便操作
        JSONObject jsonObject = JSONObject.parseObject(accessTokenString);
        //获取opnid和accessToken
        String access_token = jsonObject.getString("access_token");
        String open_id = jsonObject.getString("openid");
        UserInfo userInfo = userInfoService.getByOpenId(open_id);
           if(userInfo == null){
               //根据access_token和open_id获取微信用户信息
               String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                       "?access_token=%s" +
                       "&openid=%s";
               String userInfoUrl = String.format(baseUserInfoUrl, access_token, open_id);
               String resultUserInfo = null;
               try {
                   resultUserInfo = HttpClientUtils.get(userInfoUrl);
               } catch (Exception e) {
                   throw new YyghException(ResultCodeEnum.FETCH_USERINFO_ERROR);
               }

               //解析结果
               JSONObject userInfoResult = JSONObject.parseObject(resultUserInfo);
               String nickname = userInfoResult.getString("nickname");
               String headImgUrl = userInfoResult.getString("headimgurl");//头像

               //将结果保存到数据库中
               userInfo = new UserInfo();
               userInfo.setNickName(nickname);
               userInfo.setOpenid(open_id);
               userInfo.setStatus(1);
               userInfoService.save(userInfo);
           }

            Map<String, Object> map = new HashMap<>();
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name", name);

            //以后看集合中是否存在openid判断用户扫码后是否已绑定手机号码
            if(StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid", userInfo.getOpenid());
            } else {
                map.put("openid", "");
            }
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);

        return "redirect:" + WeiXinPropertiesUtils.YYGH_BASE_URL +
                "/weixin/callback?token="+map.get("token")+"&openid="+
                map.get("openid")+"&name="+URLEncoder.encode((String)map.get("name"));
    }

}
