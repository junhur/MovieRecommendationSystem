package com.example.apigateway.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserInfoDto {
    private String userId;
    @JsonProperty("movie_title")
    private String movieTitle = "default";
    private Integer score = -1;

    public UserInfoDto(Integer userId) {
        this.userId = String.valueOf(userId);
    }
}
