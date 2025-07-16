package com.talentradar.apigatewayrw.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class APIResponse<T> {
    private Boolean status;
    private String message;
    private T data;
    private List<Map<String, String>> errors;
}
