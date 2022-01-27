package com.lier.yygh.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lier.yygh.model.user.Patient;
import com.lier.yygh.model.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author lier
 * @date 2021/12/4 - 16:45
 * @Decription
 * @since jdk1.8
 */
@Mapper
public interface PatientMapper  extends BaseMapper<Patient> {
}
