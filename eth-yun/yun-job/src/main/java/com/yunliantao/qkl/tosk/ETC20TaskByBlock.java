package com.yunliantao.qkl.tosk;




import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunliantao.qkl.domain.entity.UsdtHexShellDo;
import com.yunliantao.qkl.domain.entity.UserinfoDo;
import com.yunliantao.qkl.service.CommonService;
import com.yunliantao.qkl.service.UsdtHexShellService;
import com.yunliantao.qkl.service.UserinfoService;
import com.yunliantao.qkl.utils.ETHCoinUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


/**
 * usdt 区块交易监测 按区块
 */
@Slf4j
@Component
@EnableScheduling
public class ETC20TaskByBlock {

    @Resource
    public CommonService commonImplService;

    @Resource
    public UsdtHexShellService usdtHexShellService;

    @Resource
    public UserinfoService userInfoService;


    /**
     * 时时扫描公链得到充值记录
     */
    /*1分钟执行一次*/
    @Scheduled(cron="0 0/1 *  * * ? ")
    public void doServicerechargeRecord() {
        log.info("开始执行定时----------usdt充值任务");
        commonImplService.rechargeRecord();

    }


    /**
     * usdt归集到中央账户
     */
    /*半小时执行一次*/
    @Scheduled(cron="0 0/10 *  * * ? ")
    public void doServicerechargeRecord2() {
        log.info("开始执行定时---------归集任务");
        List<UsdtHexShellDo> nottrans = usdtHexShellService.list(new LambdaQueryWrapper<UsdtHexShellDo>().eq(UsdtHexShellDo::getSured,false));
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

        log.info("已转入手续费，未归集成功的总" + size + "条,本次成功归集：" + suc + "条");

    }

}