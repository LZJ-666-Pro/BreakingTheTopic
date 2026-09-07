package com.poti.user.service;

public interface SmsService {
    
    boolean sendVerifyCode(String phone);
    
    boolean verifyCode(String phone, String code);
    
    void deleteCode(String phone);
    
    boolean canSendCode(String phone);
    
    long getRemainingCooldown(String phone);
}
