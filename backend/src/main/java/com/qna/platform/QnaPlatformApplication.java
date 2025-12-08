package com.qna.platform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 在线问答平台启动类
 * 
 * @author QnA Platform
 * @version 1.0.0
 */
@SpringBootApplication
@MapperScan("com.qna.platform.mapper")
@EnableAsync
@EnableScheduling
public class QnaPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(QnaPlatformApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("   QnA Platform Started Successfully!   ");
        System.out.println("   API文档: http://localhost:8080/api  ");
        System.out.println("========================================\n");
    }
}
