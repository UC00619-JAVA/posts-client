package com.example.postsclient.dto;

public record ApiError(int status, String code, String message) {
}
