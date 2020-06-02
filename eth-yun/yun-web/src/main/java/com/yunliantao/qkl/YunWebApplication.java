package com.yunliantao.qkl;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@MapperScan("com.yunliantao.qkl.mapper" )
//@ComponentScan(basePackages = {"com.yunliantao.qkl","com.yunliantao.qkl.service"})
@EnableTransactionManagement
public class YunWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(YunWebApplication.class, args);
    }

}
