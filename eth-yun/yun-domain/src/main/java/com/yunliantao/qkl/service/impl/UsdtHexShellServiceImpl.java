package com.yunliantao.qkl.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yunliantao.qkl.domain.entity.UsdtHex;
import com.yunliantao.qkl.domain.entity.UsdtHexShellDo;
import com.yunliantao.qkl.mapper.UsdtHexShellDao;
import com.yunliantao.qkl.service.UsdtHexShellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UsdtHexShellServiceImpl extends ServiceImpl<UsdtHexShellDao, UsdtHexShellDo> implements UsdtHexShellService {
    @Autowired
    private UsdtHexShellDao usdtHexShellDao;

    @Override
    public List<UsdtHex> selectByHexAndType(String txd, String s) {
        return null;
    }
}
