package com.yunliantao.qkl.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunliantao.qkl.domain.entity.UserinfoDo;
import com.yunliantao.qkl.mapper.UserinfoDao;
import com.yunliantao.qkl.service.UserinfoService;
import org.springframework.stereotype.Service;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/5/28  15:16
 */
@Service
public class UserinfoServiceImpl extends ServiceImpl<UserinfoDao, UserinfoDo> implements UserinfoService {
    @Override
    public UserinfoDo selectInfoByUsdtAddress(String to) {
        return null;
    }
}
