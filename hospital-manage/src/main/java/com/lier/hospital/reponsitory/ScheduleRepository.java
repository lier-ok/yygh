package com.lier.hospital.reponsitory;

import com.lier.hospital.model.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Author lier
 * @date 2022/1/21 - 18:59
 * @Decription
 * @since jdk1.8
 */
public interface ScheduleRepository extends MongoRepository<Schedule,String> {

    Schedule getScheduleById(String scheduleId);

}
