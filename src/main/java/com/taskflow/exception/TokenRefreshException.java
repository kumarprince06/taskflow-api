package com.taskflow.exception;

public class TokenRefreshException extends RuntimeException{

    public TokenRefreshException(String message){
        super(message);
    }
}
