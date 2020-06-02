//package com.yunliantao.qkl.service.impl;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * @author yang杭
// * 文本说明：
// * @date 2020/6/1  18:43
// */
//public class Task implements Runnable {
//    private String name;
//
//    public Task(String name) {
//        this.name = name;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void run() {
//        try {
//            Long duration = (long) (Math.random() * 10);
//            System.out.println("Executing : " + name);
//            TimeUnit.SECONDS.sleep(duration);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//}