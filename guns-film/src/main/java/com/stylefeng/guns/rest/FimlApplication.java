package com.stylefeng.guns.rest;


import com.alibaba.dubbo.config.spring.context.annotation.DubboComponentScan;
import com.alibaba.dubbo.spring.boot.annotation.EnableDubboConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.stylefeng.guns"})
@EnableDubboConfiguration
public class FimlApplication {


    public static void main(String[] args) {
        SpringApplication.run(FimlApplication.class, args);
    }
}
