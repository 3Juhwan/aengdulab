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
        RLock[] locks = getLocks(joinPoint, transactionalLock);
        RedissonMultiLock multiLock = new RedissonMultiLock(locks);

        boolean isLockAcquired = true;
        try {
            for (int attempt = 0; attempt < 10; attempt++) {
                isLockAcquired = acquireLock(multiLock);
                if (isLockAcquired) {
                    break;
                }
                Thread.sleep(1000);
            }

            if (!isLockAcquired) {
                throw new RuntimeException("락 획득 실패");
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
            multiLock.unlock();
        }
    }

    private boolean acquireLock(RedissonMultiLock multiLock) {
        try {
            return multiLock.tryLock(5, 100, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    private RLock[] getLocks(ProceedingJoinPoint joinPoint, TransactionalLock transactionalLock) {
        String[] lockKeys = transactionalLock.value();
        RLock[] locks = new RLock[lockKeys.length];
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < locks.length; i++) {
            String lockKey = String.format(lockKeys[i], args[i]);
            locks[i] = redissonClient.getLock(lockKey);
        }

        return locks;
    }
}
