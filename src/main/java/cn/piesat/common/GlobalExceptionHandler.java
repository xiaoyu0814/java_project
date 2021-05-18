package cn.piesat.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result handleException(Exception e) {
        Result result = new Result(-1);
        e.printStackTrace();
        if (e.getClass().equals(NullPointerException.class)){
            result.setMessage("数据异常");
        }else {
            result.setMessage(e.getMessage());
        }
        return result;
    }

    @ExceptionHandler(MyException.class)
    @ResponseBody
    public Result handleException(MyException e) {
        return new Result(-1,e.getMessage());
    }
}
