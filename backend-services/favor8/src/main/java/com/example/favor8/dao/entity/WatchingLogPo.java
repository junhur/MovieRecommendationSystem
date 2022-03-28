package com.example.favor8.dao.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WatchingLogPo {
    private Integer userId;
    private Integer status;
    private String movieTitle;
    private Integer minute;
    private LocalDateTime requestedAt;
}
