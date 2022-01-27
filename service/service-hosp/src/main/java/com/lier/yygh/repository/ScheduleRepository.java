package com.lier.yygh.repository;

import com.lier.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;

/**
 * @Author lier
 * @date 2021/11/11 - 19:01
 * @Decription
 * @since jdk1.8
 */
public interface ScheduleRepository extends MongoRepository<Schedule,String> {

    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    List<Schedule> getScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date toDate);

    Schedule getScheduleById(String scheduleId);

}
