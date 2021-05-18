package cn.piesat.common;

import lombok.Data;
@Data
public class MyException extends Exception{

    private String message;

    public MyException(String message){
        this.message = message;
    }

}
