package com.yunliantao.qkl.service.impl;

import java.util.concurrent.*;

/**
 * @author yang杭
 * 文本说明：
 * @date 2020/6/1  18:29
 */
public class dome {
    public static void main(String[] args) {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        Integer blockCount = 1;
        Integer blockCount2 = 10000;

        for (int i = blockCount; i <= blockCount2; i++) {
            Task task = new Task("AAAAAAAAAAAAA"+i);
            System.out.println("Created : " + task.getName());

            executor.execute(task);
        }
        executor.shutdown();
    }

}
class Task implements Runnable {
    private String name;

    public Task(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void run() {
        try {
            Long duration = (long) (Math.random() * 10);
            System.out.println("Executing : " + name);
//            TimeUnit.SECONDS.sleep(duration);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}