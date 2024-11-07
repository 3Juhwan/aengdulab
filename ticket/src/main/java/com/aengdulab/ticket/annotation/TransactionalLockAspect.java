package com.aengdulab.ticket.annotation;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Aspect
@Component
@RequiredArgsConstructor
public class TransactionalLockAspect {

    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    @Around("@annotation(transactionalLock)")
    public Object handleLockRelease(ProceedingJoinPoint joinPoint, TransactionalLock transactionalLock)
            throws Throwable {

        String[] lockKeys = transactionalLock.value();
        RLock[] locks = new RLock[lockKeys.length];
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < locks.length; i++) {
            String lockKey = String.format(lockKeys[i], args[i]);
            locks[i] = redissonClient.getLock(lockKey);
        }

        boolean isLockAcquired = true;
        try {
            for (int attempt = 0; attempt < 10; attempt++) {
                isLockAcquired = true;
                RedissonMultiLock multiLock = new RedissonMultiLock(locks);
                try {
                    if (multiLock.tryLock(5, 100, TimeUnit.SECONDS)) {
                        break;
                    } else {
                        throw new IllegalStateException("!!!!!!!");
                    }
                } catch (Exception e) {
                    isLockAcquired = false;
                    for (RLock lock : locks) {
                        if (lock.isHeldByCurrentThread()) {
                            lock.unlock();
                        }
                    }
                    System.out.println("Lock acquisition failed, retrying...");
                    Thread.sleep(1000);
                }
            }

            if (!isLockAcquired) {
                throw new RuntimeException("Lock acquisition failed after multiple attempts");
            }

            return transactionTemplate.execute(status -> {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    throw new ThrownException(e);
                }
            });
        } catch (ThrownException e) {
            throw e.getCause();
        } finally {
            for (RLock lock : locks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
