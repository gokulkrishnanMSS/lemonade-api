package com.lemon.lemonade.Exceptions;

public class ApplicationAlreadyExist extends RuntimeException{
    public ApplicationAlreadyExist(String message){
        super(message);
    }
}
