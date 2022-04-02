package com.example.apigateway.controller;

import lombok.Data;

import java.util.List;

@Data
public class ResponseDto {
    private List<String> recommendations;
}
