package com.bidly.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@Interceptor
@Logged
@Priority(Interceptor.Priority.APPLICATION)
public class LoggingInterceptor {

    private static final Logger LOG = Logger.getLogger(LoggingInterceptor.class.getName());

    @AroundInvoke
    public Object logMethodCall(InvocationContext ctx) throws Exception {
        String cls = ctx.getTarget().getClass().getSimpleName();
        String method = ctx.getMethod().getName();
        LOG.log(Level.FINE, "Executing: {0}.{1}", new Object[]{cls, method});

        long start = System.currentTimeMillis();
        try {
            Object result = ctx.proceed();
            long ms = System.currentTimeMillis() - start;
            LOG.log(Level.FINE, "{0}.{1} finished in {2}ms", new Object[]{cls, method, ms});
            return result;
        } catch (Exception e) {
            long ms = System.currentTimeMillis() - start;
            LOG.log(Level.WARNING, "{0}.{1} failed after {2}ms: {3}",
                    new Object[]{cls, method, ms, e.getMessage()});
            throw e;
        }
    }
}

