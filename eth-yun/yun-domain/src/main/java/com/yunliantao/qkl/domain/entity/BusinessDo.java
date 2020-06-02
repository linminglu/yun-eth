/**
 *
 */
package com.yunliantao.qkl.domain.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

/**
 * 参数配置
 * @author
 *
 */
@Data
@TableName(value = "tbus_business")
public class BusinessDo {
    @TableId
    @Column(name = "fi_id")
    private Integer fiId;
    @Column(name = "fs_config")
    private String fsConfig;    /* 配置项 */
    @Column(name = "fs_value")
    private String fsValue;    /* 值 */
    @Column(name = "fs_unit")
    private String fsUnit;    /* 单位 */
    @Column(name = "fs_descr")
    private String fsDescr;    /* 描述 */
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;


}
