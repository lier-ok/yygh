package com.lier.yygh.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lier.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @Author lier
 * @date 2021/10/21 - 22:16
 * @Decription
 * @since jdk1.8
 */
public interface DictService extends IService<Dict> {
    List<Dict> selectListById(Long id);

    void exportDict(HttpServletResponse response);

    void importFile(MultipartFile file);

    String getMsgForHospital(String dictCode, String value);

    List<Dict> getHospChildByHosType(String dictCode);
}
