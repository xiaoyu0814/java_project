package cn.piesat.common;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;


@Aspect
@Component
public class WebLogAspect {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Pointcut("execution(public * cn.piesat.controller.*.*(..))")
    private void webLog(){};

    @Before("webLog()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String url = request.getRequestURL().toString();

        // 记录下请求内容
        log.info("URL : " + url);
        log.debug("HTTP_METHOD : " + request.getMethod());
        log.debug("IP : " + request.getRemoteAddr());
        log.debug("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(returning = "result", pointcut = "webLog()")
    public void doAfterReturning(JoinPoint joinPoint, Result result) throws Throwable {
        // 处理完请求，返回内容
        log.info("RESPONSE : " + result.getMessage());
    }

    @AfterThrowing(value = "webLog()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Throwable e) throws Throwable {
        Logger log = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        log.error("-------------------afterThrowing.handler.start-------------------");
        log.error("异常名称：" + e.getClass().toString());
        log.error("e.getMessage():" + e.getMessage(), e);
        log.error("-------------------afterThrowing.handler.end-------------------");
    }

}
