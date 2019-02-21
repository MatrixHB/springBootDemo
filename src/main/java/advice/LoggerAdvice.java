package advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Component
@Aspect     //注解配置切面
public class LoggerAdvice {

    //注解配置切入点
    @Pointcut("execution(* dao.*.*(..))")
    public void pt1(){}

    //注解配置通知
    @Around("pt1()")
    public Object aroundLogger(ProceedingJoinPoint pjp){
        Object result = null;
        try {
            System.out.println("这是一个前置通知");
            result = pjp.proceed();             //原方法有返回值，所以环绕后的方法也要有返回值
            System.out.println("这是一个后置通知");
        } catch (Throwable throwable) {
            System.out.println("这是一个异常通知");
            throwable.printStackTrace();
        } finally {
            System.out.println("这是一个最终通知");
        }
        return result;
    }
}
