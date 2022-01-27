package com.lier.yygh.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.lier.yygh.mapper.DictMapper;
import com.lier.yygh.model.cmn.Dict;
import com.lier.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

/**
 * @Author lier
 * @date 2021/10/29 - 20:41
 * @Decription
 * @since jdk1.8
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {

    private DictMapper dictMapper;

    //构造器注入
    public DictListener(DictMapper dictMapper){
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo,dict);
        dictMapper.insert(dict);//插入数据库中
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
