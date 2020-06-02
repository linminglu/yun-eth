package com.yunliantao.qkl.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 充值记录 erc20的充值记录
 *
 * @author chglee
 * @email 1992lcg@163.com
 * @date 2019-04-20 16:28:55
 */
@Data
@TableName(value = "t_usdt_hex_shell")
public class UsdtHexShellDo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;
    //是否已经归集
    private Boolean sured;
    //来源地址
    private String inaddr;
    //接收地址
    private String usdtaddr;

    private String uid;
    //金额
    private BigDecimal amont;
    //合约地址
    private String contract;
    /*是否成功打入归集所需 eth手续费*/
    private Boolean tranin;
    /*ERC20归集txHash*/
    private String hex;
    /*类型*/
    private String type;
    /*类型*/
    private Date createAt;

}
