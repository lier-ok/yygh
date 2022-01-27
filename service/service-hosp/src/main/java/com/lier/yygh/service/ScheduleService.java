package com.lier.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.hosp.Schedule;
import com.lier.yygh.vo.hosp.ScheduleOrderVo;
import com.lier.yygh.vo.hosp.ScheduleQueryVo;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @Author lier
 * @date 2021/11/11 - 18:59
 * @Decription
 * @since jdk1.8
 */
public interface ScheduleService{
    void save(Map<String, Object> stringObjectMap);

    Page<Schedule> findPage(int page, int pageSize, ScheduleQueryVo queryVo);

    void deleteSchedule(String hoscode, String hosScheduleId);

    Map<String, Object> findSchedule(Integer page, Integer limit, String hoscode, String depcode);

    List<Schedule> getScheduleDetail(String hoscode, String depcode, String workDate);

    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    Schedule getScheduleById(String scheduleId);

    ScheduleOrderVo getOrderVo(String scheduleId);

    void update(Schedule schedule);

}
