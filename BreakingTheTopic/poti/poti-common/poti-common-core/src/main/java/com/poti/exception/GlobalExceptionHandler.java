package com.poti.exception;

import com.poti.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return R.error(400, "参数错误: " + e.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public R<Void> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常", e);
        return R.error(500, "系统内部错误");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        log.warn("缺少请求参数: {}", e.getParameterName());
        return R.error(400, "缺少必要参数: " + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        log.warn("参数类型转换失败: name={}, value={}", e.getName(), e.getValue());
        return R.error(400, "参数类型错误: " + e.getName());
    }

    @ExceptionHandler(TypeMismatchException.class)
    public R<Void> handleTypeMismatchException(TypeMismatchException e) {
        log.warn("类型转换失败: value={}, requiredType={}", e.getValue(), e.getRequiredType());
        return R.error(400, "参数类型错误");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return R.error(400, "请求体格式错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数校验失败: {}", message);
        return R.error(400, "参数校验失败: " + message);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("参数绑定失败: {}", message);
        return R.error(400, "参数绑定失败: " + message);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e) {
        log.warn("接口不存在: {}", e.getRequestURL());
        return R.error(404, "接口不存在: " + e.getRequestURL());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.error(500, "系统繁忙，请稍后再试");
    }
}