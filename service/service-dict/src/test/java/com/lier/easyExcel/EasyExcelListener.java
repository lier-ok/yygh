package com.lier.easyExcel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

/**
 * @Author lier
 * @date 2021/10/29 - 15:51
 * @Decription 读操作的监听器
 * @since jdk1.8
 */
public class EasyExcelListener extends AnalysisEventListener<User> {

    @Override
    public void invoke(User user, AnalysisContext analysisContext) {//读出数据的操作
        System.out.println(user);
    }

    @Override//读之后的操作
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
