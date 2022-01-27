package com.lier.yygh.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lier.yygh.model.hosp.Department;
import com.lier.yygh.repository.DepartmentMongoRepository;
import com.lier.yygh.service.DepartmentService;
import com.lier.yygh.vo.hosp.DepartmentQueryVo;
import com.lier.yygh.vo.hosp.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author lier
 * @date 2021/11/11 - 18:07
 * @Decription
 * @since jdk1.8
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentMongoRepository departmentMongoRepository;

    @Override
    public void save(Map<String, Object> stringObjectMap) {
        //将map转为实体类
        String departmentString = JSONObject.toJSONString(stringObjectMap);
        Department department = JSONObject.parseObject(departmentString, Department.class);

        String hoscode = department.getHoscode();
        String depcode = department.getDepcode();
        //查询mongodb中是否存在
        Department departmentExist = departmentMongoRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);

        if(null != departmentExist){
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentMongoRepository.save(department);
        }else{
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentMongoRepository.save(department);
        }
    }

    @Override
    public Page<Department> selectPage(int page, int pageSize, DepartmentQueryVo queryVo) {
        PageRequest pageRequest = PageRequest.of(page-1, pageSize);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);//忽略大小写

        Department department = new Department();
        BeanUtils.copyProperties(queryVo,department);

        Example<Department> of = Example.of(department, exampleMatcher);

        Page<Department> all = departmentMongoRepository.findAll(of, pageRequest);
        return all;
    }

    @Override
    public void delete(String hoscode, String depcode) {
        Department department = departmentMongoRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(null != department){
            departmentMongoRepository.deleteById(department.getId());
        }

    }

    @Override
    public List<DepartmentVo> getDepartments(String hoscode) {
        //用于封装结果数据
        List<DepartmentVo> result = new ArrayList<>();

        //根据hoscode查询出所有排班信息
        Department condition = new Department();//封装条件
        condition.setHoscode(hoscode);
        Example<Department> example = Example.of(condition);
        List<Department> departments = departmentMongoRepository.findAll(example);

        //根据大科室编号分组结果
        Map<String, List<Department>> collect = departments.stream()
                .collect(Collectors.groupingBy(Department::getBigcode));

        //循环封装排班结果
        for(Map.Entry<String,List<Department>> entry : collect.entrySet()){
            String bigcode = entry.getKey();//大科室编号

            List<Department> departmentList = entry.getValue();

            //封装大科室
            DepartmentVo bigDepartmentVo = new DepartmentVo();
            bigDepartmentVo.setDepcode(bigcode);
            bigDepartmentVo.setDepname(departmentList.get(0).getDepname());

            //封装小科室
            List<DepartmentVo> smallDepartment = new ArrayList<>();
            for(Department department : departmentList){
                DepartmentVo smallDepartmentVo = new DepartmentVo();
                smallDepartmentVo.setDepcode(department.getDepcode());
                smallDepartmentVo.setDepname(department.getDepname());
                smallDepartment.add(smallDepartmentVo);
            }
            //小科室集合放入大科室
            bigDepartmentVo.setChildren(smallDepartment);
            //将大科室放入整个集合中
            result.add(bigDepartmentVo);
        }
        return result;
    }

    @Override
    public String getDepname(String hoscode, String depcode) {
        Department department = departmentMongoRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);

        if(department != null){
            return department.getDepname();
        }
        return null;
    }

    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentMongoRepository.getDepartmentByHoscodeAndDepcode(hoscode,depcode);
    }


}
