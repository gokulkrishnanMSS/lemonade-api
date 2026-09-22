package com.lemon.lemonade.Exceptions;

public class TeamAlreadyExistException extends RuntimeException {
    public TeamAlreadyExistException(String message) {
        super(message);
    }
}
