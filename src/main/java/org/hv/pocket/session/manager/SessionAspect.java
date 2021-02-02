package org.hv.pocket.session.manager;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.hv.pocket.annotation.DbSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author leyan95 2020/09/10
 */
@Aspect
@Component
public class SessionAspect {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionAspect.class);

    private final ApplicationContext context;

    public SessionAspect(ApplicationContext context) {
        this.context = context;
    }

    @Pointcut("@within(org.hv.pocket.annotation.DbSession)||@annotation(org.hv.pocket.annotation.DbSession)")
    public void verify() {
    }

    @Around("verify()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        DbSession affairs = method.getAnnotation(DbSession.class);
        if (affairs == null) {
            affairs = target.getClass().getAnnotation(DbSession.class);
        }
        String sessionName = affairs.value();
        if (StringUtils.isEmpty(sessionName)) {
            sessionName = context.getEnvironment().getProperty("spring.application.name");
            LOGGER.warn("The database session value cannot be found in your DBSession annotation, so I will use the application name as the session value by default - {}", sessionName);
        }
        boolean enableTransaction = affairs.tsOn();
        LOGGER.debug("方法入栈 >>>> {}.{}({})", target.getClass().getName(), method.getName(), org.apache.commons.lang3.StringUtils.join(joinPoint.getArgs(), ","));
        Object result;
        try {
            ActiveSessionCenter.register(sessionName, enableTransaction);
            result = joinPoint.proceed();
            ActiveSessionCenter.cancelTheRegistration(enableTransaction);
        } catch (Throwable throwable) {
            ActiveSessionCenter.handleException(throwable, enableTransaction);
            throw throwable;
        } finally {
            LOGGER.debug("方法出栈 >>>> {}.{}({})", target.getClass().getName(), method.getName(), org.apache.commons.lang3.StringUtils.join(joinPoint.getArgs(), ","));
        }
        return result;
    }
}
