package kz.iitu.spring_lab_1.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class CallCounterAspect {
    private final Map<String, Integer> counterMap = new ConcurrentHashMap<>();

    @Before("kz.iitu.spring_lab_1.aspect.Pointcuts.serviceLayer()")
    public void countCall(JoinPoint jp) {
        String methodName = jp.getSignature().toShortString();
        counterMap.merge(methodName, 1, Integer::sum);
    }

    public Map<String, Integer> getStatistics() {
        return counterMap;
    }
}