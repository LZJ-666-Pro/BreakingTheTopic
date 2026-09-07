package com.poti.wrongbook.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.poti.wrongbook.entity.Wrongbook;

import java.util.Map;

public interface WrongbookService extends IService<Wrongbook> {

    Map<String, Object> getStatistics(Long userId);
}
