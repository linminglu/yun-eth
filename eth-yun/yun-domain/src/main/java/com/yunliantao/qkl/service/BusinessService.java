package com.yunliantao.qkl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunliantao.qkl.domain.entity.BusinessDo;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/5/28  15:14
 */
public interface BusinessService extends IService<BusinessDo> {

    BusinessDo getByConfig(String SS);
}
