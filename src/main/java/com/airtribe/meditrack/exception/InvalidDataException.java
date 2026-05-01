package com.airtribe.meditrack.exception;

public class InvalidDataException extends RuntimeException{

    private final String fieldName;

    public InvalidDataException(String fieldName,String message){
        super("Invalid value for '"+ fieldName + "'; "+ message);
        this.fieldName =fieldName;
    }

    public InvalidDataException(String fieldName,String message,Throwable cause){
        super("Invalid value for '"+ fieldName + "'; "+ message, cause);
        this.fieldName =fieldName;
    }

    public  String getFieldName(){
        return  fieldName;
    }
}
