package com.yunliantao.qkl.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;


/**
 * 用户信息
 */
@Data
@TableName(value = "t_user_info")
public class UserinfoDo implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId
    private String id;
    //用户名
    private String username;
    //区号
    private String global;
    //手机号
    private String phone;
    //密码
    private String password;
    //1正常  2封停 3删除
    private Integer state;
    //
    private String usdtAddress;

    /*创建时间*/
    private Date createAt;

    private BigDecimal jyUsdt;



}
