package com.yunliantao.qkl.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunliantao.qkl.domain.entity.UsdtHexShellDo;
import com.yunliantao.qkl.domain.entity.UserinfoDo;
import com.yunliantao.qkl.domain.vo.ResultUtil;
import com.yunliantao.qkl.service.impl.CommonImplService;
import com.yunliantao.qkl.service.UsdtHexShellService;
import com.yunliantao.qkl.service.UserinfoService;
import com.yunliantao.qkl.utils.ETHCoinUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


/**
 * ERC-20 USDT（模块）
 *
 * @author pc
 * @email 1992lcg@163.com
 * @date 2020-05-28 15:00:56
 */

@Controller
@Slf4j
@RequestMapping("/usdtHexShell")
public class UsdtHexShellController {

    @Resource
    private UserinfoService userinfoService;
    /**
     * 生成eth地址
     */
    @RequestMapping("/getethaddress")
    @ResponseBody
    public ResultUtil getETHaddress(String password) {
        String address=null;
        try {
            address = ETHCoinUtils.newAccount(password);

        } catch (Exception e) {
            log.info("eth地址成生成失败",e);
        }
        //TODO 获取token 用户id
        UserinfoDo userinfoDo =new UserinfoDo();
        userinfoDo.setUsdtAddress(address);
        userinfoDo.setUsername(password);
        userinfoDo.setPassword(password);
        userinfoDo.setState(1);
        userinfoDo.setCreateAt(new Date());
        userinfoService.save(userinfoDo);

        return ResultUtil.ok(address);
    }

    @Resource
    private CommonImplService commonImplService;


    /**
     *扫描公链，充值
     */
    @RequestMapping("/test")
    @ResponseBody
    public ResultUtil test(@RequestParam(name = "block") int block) {
        System.out.println(block);
        Boolean aBoolean = commonImplService.parseTxTrantions(block, 1);

        return ResultUtil.ok(aBoolean);
    }


    /**
     *加油
     */
    @RequestMapping("/test2")
    @ResponseBody
    public ResultUtil test2(@RequestParam(name = "block") int block) {
        System.out.println(block);

        commonImplService.sendEthfree(ETHCoinUtils.FREEADDRESS, "0x4540573482aca63251ba415efd52336dedd86599", "3900000000000000", "y1995lol(.)H", "1");

        return ResultUtil.ok(null);
    }


    @Resource
    private UsdtHexShellService usdtHexShellService;
    @Resource
    private UserinfoService userInfoService;

    /**
     * usdt 归集
     * @return
     */
    @ResponseBody
    @PostMapping("/suredagin")
    public ResultUtil suredagin() {
        List<UsdtHexShellDo> nottrans = usdtHexShellService.list();
        int size = 0;
        int suc = 0;
        if (nottrans != null && nottrans.size() > 0) {
            size = nottrans.size();
            for (UsdtHexShellDo uhs : nottrans) {
                UserinfoDo one = userInfoService.getOne(new LambdaQueryWrapper<UserinfoDo>().eq(UserinfoDo::getId, uhs.getUid()));
                String inzd = ETHCoinUtils.sendTokenTransaction(uhs.getUsdtaddr(), one.getUsername(), ETHCoinUtils.MAINADDRESS, uhs.getContract(), uhs.getAmont().toBigInteger());
                if (inzd != null && inzd.length() > 0 && !inzd.contains("{")) {
                    uhs.setSured(true);
                    uhs.setHex(inzd);
                    usdtHexShellService.updateById(uhs);
                    suc += 1;
                }
            }
        }
        return ResultUtil.ok("msg", "已转入手续费，未归集成功的总" + size + "条,本次成功归集：" + suc + "条");
    }


}
