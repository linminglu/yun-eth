package com.yunliantao.qkl.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yunliantao.qkl.domain.entity.BusinessDo;
import com.yunliantao.qkl.domain.entity.UsdtHexShellDo;
import com.yunliantao.qkl.domain.entity.UserinfoDo;
import com.yunliantao.qkl.service.BusinessService;
import com.yunliantao.qkl.service.CommonService;
import com.yunliantao.qkl.service.UsdtHexShellService;
import com.yunliantao.qkl.service.UserinfoService;
import com.yunliantao.qkl.utils.ETHCoinUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/5/28  15:50
 */
@Slf4j
@Service
public class CommonImplService implements CommonService {

    @Resource
    private BusinessService businessService;

    @Resource
    private UserinfoService infoService;
    @Resource
    private UsdtHexShellService usdtHexShellService;

    public void rechargeRecord() {
        //获取最新的交易
        //获取最新高度；
        Integer blockCount = null;
        try {
            blockCount = ETHCoinUtils.getCurrentBlockNumber().intValue();
        } catch (Throwable throwable) {
            log.error("钱包出错了" + throwable.getMessage());
            blockCount = 0;
        }
        BusinessDo businessDo = businessService.getOne(new LambdaQueryWrapper<BusinessDo>().eq(BusinessDo::getFsConfig, "eth_blockcount"));
        Integer blockParseedCount = Integer.parseInt(businessDo.getFsValue().trim());
        log.info("eth当前高度是：{} 处理记录高度：{}", blockCount, blockParseedCount);




        if ( blockCount >= blockParseedCount ) {
            int index = blockParseedCount - 1;
            //重新跑2个区块只要3个确认就算用户充值成功
            while (index <= blockCount) {
                /*区块验证*/
                if ( parseTxTrantions(index, blockCount) ) {
                    index++;
                } else {
                    break;
                }
            }

            if ( index == blockCount.intValue() ) {
                //所有区块都已经处理完
                log.info("区块扫描完成---->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
                businessDo.setFsValue(index + "");
                businessService.updateById(businessDo);
            } else {
                businessDo.setFsValue((index - 1) + "");
                System.out.println("已经扫描区块高度--------" + businessDo);
                businessService.updateById(businessDo);
            }
            log.info("ETH区块扫描完成---->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

    }

    /*充值查询*/
    public Boolean parseTxTrantions(int block, int blockCount) {
        try {
            EthBlock ethBlock = ETHCoinUtils.getBlockEthBlock(block);
            if ( ethBlock == null ) {
                //无交易
                return true;
            }
            List<EthBlock.TransactionResult> transactions = ethBlock.getResult().getTransactions();
            for (EthBlock.TransactionResult tx : transactions) {
                EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) tx.get();
                /* 交易量，代币都是0*/
                if ( transaction.getValue().compareTo(BigInteger.ZERO) > 0 ) {
                    //eth充值
                    continue;
                } else {
                    /*交易hash*/
                    String hash = transaction.getHash();
                    log.info("充币交易hash>>>" + hash);
                    if ( transaction.getTo() == null ) {
                        continue;
                    }
                    /*USDT 合约地址*/
                    String hexAddress = "0xdac17f958d2ee523a2206206994597c13d831ec7";
                    //不是usdt的代币充值 ，不用归集
                    if ( !transaction.getTo().trim().equals(hexAddress) ) {
                        continue;
                    }

                    EthGetTransactionReceipt ethTransaction = ETHCoinUtils.ethGetTransactionReceipt(hash);
                    if ( ethTransaction != null ) {
                        List<Log> logs = ethTransaction.getResult().getLogs();
                        if ( logs == null || logs.size() <= 0 ) {
                            continue;
                        }
                        if ( logs.get(0) != null && logs.get(0).getAddress().equals(hexAddress) ) {

                            if ( logs.get(0).getTopics().size() >= 3 ) {

                                String to = logs.get(0).getTopics().get(2);
                                /*usdt代币接收地址*/
                                to = to.substring(0, 2) + to.substring(26);

                                /*中央地址||燃料地址*/
                                if ( to.equals(ETHCoinUtils.MAINADDRESS) || to.equals(ETHCoinUtils.FREEADDRESS) ) {
                                    continue;
                                }

                                UserinfoDo info = infoService.getOne(new LambdaQueryWrapper<UserinfoDo>().eq(UserinfoDo::getUsdtAddress, to));
                                if ( info == null ) {
                                    //不是平台用户代币充值记录
                                    continue;
                                }
                                String data = logs.get(0).getData();
                                //平台代币交易 获取用户充值额度
                                log.info("用户充值数量 erc20 usdt--------->" + data + "usdt地址----------->" + to);
                                if ( data == null ) {
                                    continue;
                                } else {
                                    try {
                                        data = data.substring(2);
                                        BigInteger bi = new BigInteger(data, 16);
                                        BigDecimal amount = webTogetherYc(bi);//
                                        log.info("充值金额------->" + amount);
                                        log.info("有用户充值usdt：" + info.getUsername());
                                        //记录用户充值的shell并 将地址转入 eth gas燃料
                                        //转手续费到地址
                                        String noce = "1";
                                        UsdtHexShellDo usdtHexShell = new UsdtHexShellDo();
                                        usdtHexShell.setAmont(new BigDecimal(bi));
                                        usdtHexShell.setCreateAt(new Date());
                                        usdtHexShell.setSured(false);
                                        usdtHexShell.setUid(info.getId());
                                        usdtHexShell.setInaddr(transaction.getFrom().trim());
                                        usdtHexShell.setUsdtaddr(to);
                                        usdtHexShell.setContract(transaction.getTo());
                                        usdtHexShellService.save(usdtHexShell);

                                        log.info("为用户地址转入eth---->作用erc20-usdt归集");
                                        String inzd = sendEthfree(ETHCoinUtils.FREEADDRESS, to, "3900000000000000", "y1995lol(.)H", noce);
                                        log.info("为用户地址转入eth----> hex->" + inzd);
                                        if ( inzd != null && inzd.length() > 0 && !inzd.contains("{") ) {
                                            usdtHexShell.setTranin(true);
                                        } else {
                                            usdtHexShell.setTranin(false);
                                        }
                                        usdtHexShellService.updateById(usdtHexShell);

                                    } catch (Exception zx) {
                                        zx.printStackTrace();
                                    }

                                }
                            }
                        }
                    }
                }

            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            log.error("出错了---------》" + throwable.getMessage());
            return false;
        }
        return true;
    }

    private BigDecimal webTogetherYc(BigInteger value) {
        return new BigDecimal(value).divide(new BigDecimal(Math.pow(10, 6))).setScale(6, RoundingMode.HALF_UP);
    }


    public static String sendEthfree(String fromAddr, String toAddr, String value, String password, String noce) {
        try {
            long gasPrice = 3L;
            String hex = ETHCoinUtils.transionfree(fromAddr, password, BigInteger.valueOf(gasPrice * 10000000000L), BigInteger.valueOf(21000), toAddr, value);
            log.info(hex);
            return hex;
        } catch (Exception e) {
            log.error("为归集erc20 打人eth燃料失败");
            e.printStackTrace();
            return "";
        }
    }


}
