package com.lier.yygh.config.exception;

import com.lier.yygh.config.result.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author lier
 * @date 2021/10/22 - 19:37
 * @Decription
 * @since jdk1.8
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result error(Exception exception){
        exception.printStackTrace();
        return Result.fail();
    }
    @ExceptionHandler(YyghException.class)
    @ResponseBody
    public Result error(YyghException exception){
        exception.printStackTrace();
        return Result.fail();
    }
}
