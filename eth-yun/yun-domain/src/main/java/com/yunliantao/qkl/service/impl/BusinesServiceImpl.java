package com.yunliantao.qkl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunliantao.qkl.domain.entity.BusinessDo;
import com.yunliantao.qkl.mapper.BusinessDao;
import com.yunliantao.qkl.service.BusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/5/28  15:17
 */
@Slf4j
@Service
public class BusinesServiceImpl extends ServiceImpl<BusinessDao, BusinessDo> implements BusinessService {

    @Override
    public BusinessDo getByConfig(String SS) {
        return null;
    }
}
