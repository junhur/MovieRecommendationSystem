package com.example.favor8.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration()
@EnableJpaRepositories(basePackages = "com.example.favor8.dao.repository")
@MapperScan("com.example.favor8.dao.repository")
public class OrmConfig {
}
