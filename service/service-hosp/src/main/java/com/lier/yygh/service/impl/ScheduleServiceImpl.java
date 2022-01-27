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
        //转为实体类
        String jsonString = JSONObject.toJSONString(stringObjectMap);
        Schedule schedule = JSONObject.parseObject(jsonString, Schedule.class);

        //查看排班是否存在 根据医院编号和排班号查询
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

        Aggregation aggregation = Aggregation.newAggregation( //构建查询条件
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

        int total = aggregate1.getMappedResults().size(); //总记录数

        //为每个实例对象中设置日期对应周几
        for(BookingScheduleRuleVo bookingScheduleRuleVo : aggregate){
            Date workDate = bookingScheduleRuleVo.getWorkDate();
            String dayOfWeek = DayOfWeek.getDayOfWeek(new DateTime(workDate));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
        }

        //封装结果
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

        //为每个Schedule添加详细信息
        list.stream().forEach(item -> {
            this.addDetail(item);
        });
        return list;
    }

    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        //结果集
        HashMap<String, Object> result = new HashMap<>();
        //获取预约规则
        Hospital hospital = hospitalService.getHospitalByHoscode(hoscode);
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.HOSCODE_EXIST);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        //获取可预约日期分页数据
        IPage iPage = this.getDatePageData(page,limit,bookingRule);
        List<Date> records = iPage.getRecords();
        //获取预约日期剩余可预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode)
                .and("depcode").is(depcode).and("workDate").in(records);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")//分组字段
                        .first("workDate").as("workDate")
                        .count().as("docCount")
                        .sum("availableNumber").as("availableNumber")
                        .sum("reservedNumber").as("reservedNumber")
        );
        AggregationResults<BookingScheduleRuleVo> aggregationResults = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> scheduleVoList = aggregationResults.getMappedResults();

        //合并数据 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo,
        //key为日期,value为预约规则,剩余数量等
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(scheduleVoList)) {
            scheduleVoMap = scheduleVoList
                    .stream()
                    .collect(Collectors.toMap(BookingScheduleRuleVo::getWorkDate,
                                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }
        //获取可预约排班规则
        List<BookingScheduleRuleVo> bookingRuleList = new ArrayList<>();
        for(int i = 0,len = records.size();i < len;i++){
            Date date = records.get(i);
            BookingScheduleRuleVo bookingScheduleRule = scheduleVoMap.get(date);
            if(null == bookingScheduleRule){//没有医生,可预约数为0
                bookingScheduleRule = new BookingScheduleRuleVo();
                bookingScheduleRule.setDocCount(0);//没有医生
                bookingScheduleRule.setAvailableNumber(-1);//-1表示可预约数为0
            }
            bookingScheduleRule.setWorkDate(date);
            bookingScheduleRule.setWorkDateMd(date);
            String dayOfWeek = DayOfWeek.getDayOfWeek(new DateTime(date));
            bookingScheduleRule.setDayOfWeek(dayOfWeek);

            //最后一页最后一条记录为即将预约   状态 0：正常 1：即将放号 -1：当天已停止挂号
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

        //可预约日期规则数据
        result.put("bookingScheduleList", bookingRuleList);
        result.put("total", iPage.getTotal());
        //其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        //医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        //科室
        Department department =departmentService.getDepartment(hoscode, depcode);
        //大科室名称
        baseMap.put("bigname", department.getBigname());
        //科室名称
        baseMap.put("depname", department.getDepname());
        //月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        //放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        //停号时间
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
        //获取排班信息
        Schedule schedule = scheduleRepository.getScheduleById(scheduleId);
        if(schedule == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //获取预约规则信息
        Hospital hospital = hospitalService.getHospitalByHoscode(schedule.getHoscode());
        if(hospital == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        BookingRule bookingRule = hospital.getBookingRule();
        if(bookingRule == null){
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        //设置值到返回结果中
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

        //退号截止天数（如：就诊前一天为-1，当天为0）
        int quitDay = bookingRule.getQuitDay();
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        //预约开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        //预约截止时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(bookingRule.getCycle()).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        //当天停止挂号时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStartTime(stopTime.toDate());
        return scheduleOrderVo;
    }

    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }

    //获取可预约日期分页数据
    private IPage getDatePageData(Integer page, Integer limit, BookingRule bookingRule) {
        //当天放号时间
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        //预约周期
        Integer cycle = bookingRule.getCycle();
        //判断是否当前时间是否超过当天放号时间
        if(releaseTime.isBeforeNow()){
            cycle += 1;//周期加1
        }
        //根据周期和当前时间将日期放入集合中
        List<Date> dateList = new ArrayList<>();
        for(int i = 0; i < cycle; i++){
            DateTime dateTime = new DateTime().plusDays(i);
            String date = dateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(date).toDate());
        }
        //对日期判断分页
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
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }



    //Schedule添加详细信息
    private Schedule addDetail(Schedule schedule){
        //医院名称
        String hospName = hospitalService.getHospName(schedule.getHoscode());
        //科室名称
        String depName = departmentService.getDepname(schedule.getHoscode(),schedule.getDepcode());

        //周几
        String dayOfWeek = DayOfWeek.getDayOfWeek(new DateTime(schedule.getWorkDate()));

        schedule.getParam().put("hospName",hospName);
        schedule.getParam().put("depName",depName);
        schedule.getParam().put("dayOfWeek",dayOfWeek);

        return schedule;
    }

}
