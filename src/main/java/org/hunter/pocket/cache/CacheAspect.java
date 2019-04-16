package org.hunter.pocket.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.hunter.pocket.annotation.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static org.hunter.pocket.constant.LockNames.BASE_CACHE_LOCK;
import static org.hunter.pocket.constant.LockNames.BASE_CACHE_UNLOCK;

/**
 * @author wujianchuan 2019/3/22
 * @version 1.0
 */
@Aspect
@Component
public class CacheAspect {

    private final
    ApplicationContext context;

    @Autowired
    public CacheAspect(ApplicationContext context) {
        this.context = context;
    }

    @Around("@annotation(org.hunter.pocket.annotation.Cache)")
    public Object before(ProceedingJoinPoint joinPoint) throws Throwable {
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

        String key = method.getDeclaringClass() + "." + method.getReturnType().getSimpleName() + "-" + method.getName() + "-" + cacheKeyValue;
        return this.getCache(key, cache, joinPoint);
    }

    /**
     * 从 redis 获取数据，有则返回无则缓存数据到 redis
     *
     * @param key       cache key word.
     * @param cache     cache annotation.
     * @param joinPoint joint point.
     * @return result.
     * @throws Throwable e.
     */
    private Object getCache(String key, Cache cache, ProceedingJoinPoint joinPoint) throws Throwable {

        CacheUtils cacheUtils = (CacheUtils) context.getBean(cache.target().getCacheUtilsName());
        Object result = cacheUtils.getObj(key);
        if (result != null) {
            return result;
        } else {
            boolean lock = false;
            try {
                lock = cacheUtils.getMapLock().putIfAbsent(key, key) == null;
                if (lock) {
                    result = joinPoint.proceed();
                    cacheUtils.set(key, result, cache.duration());
                    synchronized (BASE_CACHE_UNLOCK) {
                        BASE_CACHE_UNLOCK.notifyAll();
                    }
                } else {
                    synchronized (BASE_CACHE_LOCK) {
                        BASE_CACHE_LOCK.wait(10);
                        result = this.getCache(key, cache, joinPoint);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e.getMessage());
            } finally {
                if (lock) {
                    cacheUtils.getMapLock().remove(key);
                }
            }
        }

        return result;
    }
}
