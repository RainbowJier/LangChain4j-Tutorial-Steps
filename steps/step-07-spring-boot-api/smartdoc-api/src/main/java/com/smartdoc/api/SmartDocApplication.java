package com.smartdoc.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用主入口
 *
 * 【Step 06 对照】Step 06 的入口是一个带 main() 方法的普通 Java 类：
 *
 *   // Step 06 的写法：
 *   public class Step06FullAiService {
 *       public static void main(String[] args) {
 *           // 手动创建所有对象
 *           ChatModel chatModel = ...;
 *           EmbeddingStore store = ...;
 *           // 手动组装 AiServices
 *           Assistant assistant = AiServices.builder()...build();
 *           // 手动处理用户输入
 *           Scanner scanner = new Scanner(System.in);
 *           ...
 *       }
 *   }
 *
 * Spring Boot 方式：
 * - @SpringBootApplication：启用自动配置、组件扫描、配置属性绑定
 * - SpringApplication.run()：启动内嵌 Tomcat、初始化 IoC 容器、注册所有 Bean
 * - 所有组件通过 @Component/@Service/@Configuration 自动发现和注册
 * - 不需要手动创建任何对象，Spring IoC 容器管理一切
 *
 * @SpringBootApplication 包含：
 * - @SpringBootConfiguration：标记为配置类
 * - @EnableAutoConfiguration：启用自动配置（根据 classpath 自动装配）
 * - @ComponentScan：扫描 com.smartdoc 包下的所有组件
 */
@SpringBootApplication
public class SmartDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDocApplication.class, args);
    }
}
