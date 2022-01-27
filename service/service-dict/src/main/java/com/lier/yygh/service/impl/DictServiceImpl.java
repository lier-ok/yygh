package com.lier.yygh.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lier.yygh.listener.DictListener;
import com.lier.yygh.model.cmn.Dict;
import com.lier.yygh.model.hosp.Hospital;
import com.lier.yygh.service.DictService;
import com.lier.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.lier.yygh.mapper.DictMapper;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author lier
 * @date 2021/10/21 - 22:16
 * @Decription
 * @since jdk1.8
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {


    @Cacheable(value = "dict",keyGenerator = "keyGenerator")
    @Override
    public List<Dict> selectListById(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        List<Dict> dicts = baseMapper.selectList(wrapper);
        for(Dict dict : dicts){
            //判断是否有子节点后,设置对应hasChildren属性
            Long dictId = dict.getId();
            boolean hasChild = isHasChild(dictId);
            dict.setHasChildren(hasChild);
        }
        return dicts;
    }

    @Override
    public void exportDict(HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = "dict";
        //弹窗方式下载
        response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
        List<Dict> dicts = baseMapper.selectList(null);
        ArrayList<DictEeVo> dictEeVos = new ArrayList<>();
        for(Dict dict : dicts){
            DictEeVo dictEeVo = new DictEeVo();
            BeanUtils.copyProperties(dict,dictEeVo);
            dictEeVos.add(dictEeVo);
        }
        //通过EasyExcel写出
        try {
            EasyExcel.write(response.getOutputStream(),DictEeVo.class)
                    .sheet("dict")
                    .doWrite(dictEeVos);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //数据导入功能
    //allEntries = true表示清空缓存
    @CacheEvict(value = "dict",allEntries = true)
    @Override
    public void importFile(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(),DictEeVo.class,new DictListener(baseMapper))
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getMsgForHospital(String dictCode, String value) {
        if(StringUtils.isEmpty(dictCode)){ //查询医院地址
            QueryWrapper<Dict> wrapper = new QueryWrapper<>();
            wrapper.eq("value",value);
            Dict dict = baseMapper.selectOne(wrapper);
            return dict.getName();
        }else{ //查询医院等级
            //根据dictCode查询出对应id,查出该id的子节点
            QueryWrapper<Dict> wrapperForCode = new QueryWrapper<>();
            wrapperForCode.eq("dict_code",dictCode);
            Dict dict = baseMapper.selectOne(wrapperForCode);
            Long parentId = dict.getId();

            QueryWrapper<Dict> wrapperForParentId = new QueryWrapper<>();
            wrapperForParentId.eq("parent_id",parentId);
            wrapperForParentId.eq("value",value);
            Dict dictForHosType = baseMapper.selectOne(wrapperForParentId);
            return dictForHosType.getName();
        }
    }

    @Override
    public List<Dict> getHospChildByHosType(String dictCode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code",dictCode);
        Dict dict = baseMapper.selectOne(wrapper);

        QueryWrapper<Dict> wrapperForParentId = new QueryWrapper<>();
        wrapperForParentId.eq("parent_id",dict.getId());
        List<Dict> dicts = baseMapper.selectList(wrapperForParentId);
        return dicts;
    }

    //判断每个节点下是否有子节点的方法
    public boolean isHasChild(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id",id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
}
