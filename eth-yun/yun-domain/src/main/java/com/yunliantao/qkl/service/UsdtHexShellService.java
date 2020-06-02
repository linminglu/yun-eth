package com.yunliantao.qkl.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yunliantao.qkl.domain.entity.UsdtHex;
import com.yunliantao.qkl.domain.entity.UsdtHexShellDo;

import java.util.List;

/**
 * 充值记录
 *
 * @author pc
 * @email 1992lcg@163.com
 * @date 2020-05-28 15:00:56
 */
public interface UsdtHexShellService extends IService<UsdtHexShellDo> {


    List<UsdtHex> selectByHexAndType(String txd, String s);
}
