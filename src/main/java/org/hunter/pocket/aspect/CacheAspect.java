package org.hunter.pocket.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hunter.pocket.annotation.Cache;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author wujianchuan 2019/3/22
 * @version 1.0
 */
@Aspect
@Component
public class CacheAspect {
    @Around("@annotation(org.hunter.pocket.annotation.Cache)")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Cache cache = method.getAnnotation(Cache.class);
        String cacheKey = cache.key();

        ExpressionParser parser = new SpelExpressionParser();
        Expression cacheKeyExpression = parser.parseExpression(cacheKey);
        EvaluationContext context = new StandardEvaluationContext();
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        String[] argsName = discoverer.getParameterNames(method);
        Object[] argsValue = joinPoint.getArgs();
        if (argsName != null) {
            for (int index = 0; index < argsName.length; index++) {
                context.setVariable(argsName[index], argsValue[index]);
            }
        } else {
            throw new RuntimeException("can not found any arg.");
        }
        Object cacheKeyValue = cacheKeyExpression.getValue(context);
        // TODO 缓存到Redis
        return result;
    }
}
