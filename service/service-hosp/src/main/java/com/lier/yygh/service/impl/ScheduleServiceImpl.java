package com.lier.yygh.service.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lier.yygh.config.exception.YyghException;
import com.lier.yygh.config.result.ResultCodeEnum;
import com.lier.yygh.mapper.ScheduleMapper;
import com.lier.yygh.model.hosp.BookingRule;
import com.lier.yygh.model.hosp.Department;
import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.model.hosp.Schedule;
import com.lier.yygh.repository.ScheduleRepository;
import com.lier.yygh.service.DepartmentService;
import com.lier.yygh.service.HospitalService;
import com.lier.yygh.service.ScheduleService;
import com.lier.yygh.util.DayOfWeek;
import com.lier.yygh.vo.hosp.BookingScheduleRuleVo;
import com.lier.yygh.vo.hosp.ScheduleOrderVo;
import com.lier.yygh.vo.hosp.ScheduleQueryVo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author lier
 * @date 2021/11/11 - 19:02
 * @Decription
 * @since jdk1.8
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper,Schedule> implements ScheduleService {
    @Resource
    private ScheduleRepository scheduleRepository;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private HospitalService hospitalService;

    @Resource
    private DepartmentService departmentService;

    @Override
    public void save(Map<String, Object> stringObjectMap) {
        //???????????????
        String jsonString = JSONObject.toJSONString(stringObjectMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);

        //???????????????????????? ????????????????????????????????????
        Schedule isExist = scheduleRepository
                .getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(),schedule.getHosScheduleId());

        if(null != isExist){
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else{
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }
    }

    @Override
    public Page<Schedule> findPage(int page, int pageSize, ScheduleQueryVo queryVo) {
        PageRequest condition = PageRequest.of(page-1, pageSize);

        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase(true);
        Schedule schedule = new Schedule();
        BeanUtils.copyProperties(queryVo,schedule);
        Example<Schedule> example = Example.of(schedule, exampleMatcher);
        Page<Schedule> all = scheduleRepository.findAll(example, condition);
        return all;
    }

    @Override
    public void deleteSchedule(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if(null != schedule){
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    @Override
    public Map<String, Object> findSchedule(Integer page, Integer limit, String hoscode, String depcode) {
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode);

        Aggregation aggregation = Aggregation.newAggregation( //??????????????????
                Aggregation.match(criteria),
                Aggregation.group("workDate")
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"),

                Aggregation.sort(Sort.Direction.DESC,"workDate"),
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate
                                    .aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);

        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults();

        Aggregation aggregation1 = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> aggregate1 = mongoTemplate.aggregate(aggregation1, Schedule.class, BookingScheduleRuleVo.class);

        int total = aggregate1.getMappedResults().size(); //????????????

        //????????????????????????????????????????????????
        for(BookingScheduleRuleVo bookingScheduleRuleVo : aggregate){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = DayOfWeek.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //????????????
        Map<String,Object> result = new HashMap<>();
        result.put("bookingScheduleRuleVoList",mappedResults);
        result.put("total",total);


        Map<String,Object> baseMap = new HashMap<>();
        String hosName = hospitalService.getHospName(hoscode);
        baseMap.put("hosname",hosName);
        result.put("baseMap",baseMap);

        return result;

    }

    @Override
    public List<Schedule>   getScheduleDetail(String hoscode, String depcode, String workDate) {
        List<Schedule> list = scheduleRepository
                .getScheduleByHoscodeAndDepcodeAndWorkDate(hoscode,depcode,new DateTime(workDate).toDate());

        //?????????Schedule??????????????????
        list.stream().forEach(item -> {
            this.addDetail(item);
        });
        return list;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //?????????
        HashMap<String, Object> result = new HashMap<>();
        //??????????????????
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.HOSCODE_EXIST);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //?????????????????????????????????
        IPage iPage = this.getDatePageData(page,limit,bookingRule);
        List<Date> records = iPage.getRecords();
        //????????????????????????????????????
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode).and("workDate").in(records);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//????????????
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();

        //???????????? ???????????????ScheduleVo?????????????????????????????????BookingRuleVo,
        //key?????????,value???????????????,???????????????
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList
                    .stream()
                    .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //???????????????????????????
        List<BookingScheduleRuleVo> bookingRuleList = new ArrayList<>();
        for(int i = 0,len = records.size();i < len;i++){
            Date date = records.get(i);
            BookingScheduleRuleVo bookingScheduleRule = scheduleVoMap.get(date);
            if(null == bookingScheduleRule){//????????????,???????????????0
                bookingScheduleRule = new BookingScheduleRuleVo();
                bookingScheduleRule.setDocCount(0);//????????????
                bookingScheduleRule.setAvailableNumber(-1);//-1?????????????????????0
            }
            bookingScheduleRule.setWorkDate(date);
            bookingScheduleRule.setWorkDateMd(date);
            String dayOfWeek = DayOfWeek.getDayOfWeek(new DateTime(date));
            bookingScheduleRule.setDayOfWeek(dayOfWeek);

            //?????????????????????????????????????????????   ?????? 0????????? 1??????????????? -1????????????????????????
            if(i == len - 1 && page == iPage.getPages()){
                bookingScheduleRule.setStatus(1);
            }else{
                bookingScheduleRule.setStatus(0);
            }

            if(i == 0 && page == 1){
                bookingScheduleRule.setStatus(-1);
            }
            bookingRuleList.add(bookingScheduleRule);
        }

        //???????????????????????????
        result.put("bookingScheduleList", bookingRuleList);
        result.put("total", iPage.getTotal());
        //??????????????????
        Map<String, String> baseMap = new HashMap<>();
        //????????????
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //??????
        Department department =departmentService.getDepartment(hoscode, depcode);
        //???????????????
        baseMap.put("bigname", department.getBigname());
        //????????????
        baseMap.put("depname", department.getDepname());
        //???
        baseMap.put("workDateString", new DateTime().toString("yyyy???MM???"));
        //????????????
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //????????????
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;
    }

    @Override
    public Schedule getScheduleById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        this.addDetail(schedule);
        return schedule;
    }

    @Override
    public ScheduleOrderVo getOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        //??????????????????
        Schedule schedule = scheduleRepository.getScheduleById(scheduleId);
        if(schedule == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //????????????????????????
        Hospital hospital = hospitalService.getHospitalByHoscode(schedule.getHoscode());
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //???????????????????????????
        scheduleOrderVo.setHoscode(schedule.getHoscode());
        scheduleOrderVo.setHosname(hospitalService.getHospName(schedule.getHoscode()));
        scheduleOrderVo.setDepcode(schedule.getDepcode());
        scheduleOrderVo.setDepname(departmentService.getDepname(schedule.getHoscode(), schedule.getDepcode()));
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId());
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber());
        scheduleOrderVo.setTitle(schedule.getTitle());
        scheduleOrderVo.setReserveDate(schedule.getWorkDate());
        scheduleOrderVo.setReserveTime(schedule.getWorkTime());
        scheduleOrderVo.setAmount(schedule.getAmount());

        //?????????????????????????????????????????????-1????????????0???
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //??????????????????
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //??????????????????
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //????????????????????????
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    //?????????????????????????????????
    private IPage getDatePageData(Integer page, Integer limit, BookingRule bookingRule) {
        //??????????????????
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //????????????
        Integer cycle = bookingRule.getCycle();
        //??????????????????????????????????????????????????????
        if(releaseTime.isBeforeNow()){
            cycle += 1;//?????????1
        }
        //???????????????????????????????????????????????????
        List<Date> dateList = new ArrayList<>();
        for(int i = 0; i < cycle; i++){
            DateTime dateTime = new DateTime().plusDays(i);
            String date = dateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(date).toDate());
        }
        //?????????????????????
        int start = (page - 1) * limit;
        int end = (page - 1) * limit + limit;
        if(end > dateList.size()){
            end = dateList.size();
        }
        List<Date> pageListDate = new ArrayList<>();
        for (int i = start; i < end; i++) {
            pageListDate.add(dateList.get(i));
        }

        IPage resPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page(page,7, dateList.size());

        IPage iPage = resPage.setRecords(pageListDate);
        return iPage;
    }

    /**
     * ???Date?????????yyyy-MM-dd HH:mm????????????DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }



    //Schedule??????????????????
    private Schedule addDetail(Schedule schedule){
        //????????????
        String hospName = hospitalService.getHospName(schedule.getHoscode());
        //????????????
        String depName = departmentService.getDepname(schedule.getHoscode(),schedule.getDepcode());

        //??????
        String dayOfWeek = DayOfWeek.getDayOfWeek(new DateTime(schedule.getWorkDate()));

        schedule.getParam().put("hospName",hospName);
        schedule.getParam().put("depName",depName);
        schedule.getParam().put("dayOfWeek",dayOfWeek);

        return schedule;
    }

}
