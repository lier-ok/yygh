package com.lier.yygh.repository;

import com.lier.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @Author lier
 * @date 2021/11/11 - 17:59
 * @Decription
 * @since jdk1.8
 */
@Repository
public interface DepartmentMongoRepository extends MongoRepository<Department,String> {
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
