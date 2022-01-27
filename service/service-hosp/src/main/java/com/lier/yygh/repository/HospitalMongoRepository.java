package com.lier.yygh.repository;

import com.lier.yygh.model.hosp.Hospital;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author lier
 * @date 2021/11/10 - 16:48
 * @Decription
 * @since jdk1.8
 */
@Repository
public interface HospitalMongoRepository extends MongoRepository<Hospital,String> {

    Hospital getHospitalByHoscode(String hoscode);

    List<Hospital> getHospitalByHosnameLike(String hosName);
}
