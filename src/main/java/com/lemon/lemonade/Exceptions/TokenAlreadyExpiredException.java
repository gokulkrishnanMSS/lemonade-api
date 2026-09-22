package com.lemon.lemonade.Exceptions;

public class TokenAlreadyExpiredException extends RuntimeException {
    public TokenAlreadyExpiredException(String message) {
        super(message);
    }
}
