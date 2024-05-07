package com.ycr.usercenter.exception;

import com.ycr.usercenter.common.BaseResponse;
import com.ycr.usercenter.common.ErrorCode;
import com.ycr.usercenter.utils.ReturnResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理类
 *
 * @author null&&
 * @version 1.0
 * @date 2024/4/13 21:26
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public <T> BaseResponse<T> businessExceptionHandler(BusinessException e) {
        if (e.getCode() == ErrorCode.PARAMS_ERROR.getCode()) {
            log.error("BusinessException" + e.getMessage(), e);
            return ReturnResultUtils.error(ErrorCode.PARAMS_ERROR, e.getMessage(), e.getDesc());
        } else if (e.getCode() == ErrorCode.NOT_LOGIN.getCode()) {
            log.error("BusinessException" + e.getMessage(), e);
            return ReturnResultUtils.error(ErrorCode.NOT_LOGIN, e.getMessage(), e.getDesc());
        }
        log.error("BusinessException" + e.getMessage(), e);
        return ReturnResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), e.getDesc());
    }

    @ExceptionHandler(RuntimeException.class)
    public <T> BaseResponse<T> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ReturnResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
