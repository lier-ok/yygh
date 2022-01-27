package com.lier.yygh.service;

import com.lier.yygh.model.hosp.Department;
import com.lier.yygh.vo.hosp.DepartmentQueryVo;
import com.lier.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/11 - 18:06
 * @Decription
 * @since jdk1.8
 */
public interface DepartmentService {
    void save(Map<String, Object> stringObjectMap);

    Page<Department> selectPage(int page, int pageSize, DepartmentQueryVo queryVo);

    void delete(String hoscode, String depcode);

    List<DepartmentVo> getDepartments(String hoscode);

    String getDepname(String hoscode, String depcode);

    Department getDepartment(String hoscode, String depcode);
}
