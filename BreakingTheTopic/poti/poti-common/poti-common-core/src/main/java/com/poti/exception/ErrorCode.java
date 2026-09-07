package com.poti.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "成功"),
    
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    ACCOUNT_DISABLED(1004, "账号已被禁用"),
    TOKEN_EXPIRED(1005, "Token已过期"),
    TOKEN_INVALID(1006, "Token无效"),
    
    QUESTION_NOT_FOUND(2001, "题目不存在"),
    QUESTION_ALREADY_EXISTS(2002, "题目已存在"),
    CATEGORY_NOT_FOUND(2003, "分类不存在"),
    TAG_NOT_FOUND(2004, "标签不存在"),
    
    INTERVIEW_NOT_FOUND(3001, "面试不存在"),
    INTERVIEW_ALREADY_FINISHED(3002, "面试已结束"),
    INTERVIEW_TIME_ERROR(3003, "面试时间错误"),
    
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_ERROR(4002, "文件类型错误"),
    FILE_SIZE_ERROR(4003, "文件大小超出限制"),
    
    AI_SERVICE_ERROR(5001, "AI服务异常"),
    AI_GENERATE_ERROR(5002, "AI生成失败"),
    
    DATABASE_ERROR(6001, "数据库操作失败"),
    CACHE_ERROR(6002, "缓存操作失败"),
    NETWORK_ERROR(6003, "网络连接失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
