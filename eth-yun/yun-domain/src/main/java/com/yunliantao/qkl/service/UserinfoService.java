package com.yunliantao.qkl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunliantao.qkl.domain.entity.UserinfoDo;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/5/28  15:26
 */
public interface UserinfoService extends IService<UserinfoDo> {
    UserinfoDo selectInfoByUsdtAddress(String to);
}
