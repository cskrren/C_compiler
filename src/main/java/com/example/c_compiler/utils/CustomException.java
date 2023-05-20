package com.example.c_compiler.utils;

public class CustomException extends Error {
    public CustomException(String message) {
        super(message);
    }
}