package com.pit.web.advice;

import com.pit.core.exception.CommonException;
import com.pit.web.model.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author gy
 * @version 1.0
 * @date 2020/7/4.
 */
@Slf4j
@ControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(CommonException.class)
    @ResponseBody
    public CommonResult handleStudentException(HttpServletRequest request, CommonException ex) {
        log.error("Common exception : {}", ex.getMessage(), ex);
        return new CommonResult(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonResult handleException(HttpServletRequest request, Exception ex) {
        log.error("Exception : {}", ex.getMessage(), ex);
        return new CommonResult(500, ex.getMessage());
    }
}
