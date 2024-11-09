## Redisson VS Lettuce

### Redisson
	 복잡한 분산 락 요구 사항을 처리할 수 있다.(ex. 락 획득 후 자동으로 연장, 락 공정성 보장)
	 Redis 클러스터 환경에서 락이 안전하게 관리되며, 장애 발생 시에도 락을 적절히 재획득
### Lettuce
	분산 락을 직접 구현해야 하며, 고급 락 관리 기능은 구현하지 않는다.
	락의 확장성이나 안정성을 개발자가 직접 처리해야 한다.

### Redisson이 더 좋은 이유
	고급 분산 락 기능이 내장되어 있어 복잡한 시스템에서도 안전하고 효율적으로 락을 관리할 수 있다.
	자동 락 해제, 연장, 공정 락 등 락 관리의 복잡성을 크게 줄여 준다.


## 해결 아이디어

`@Transactional`을 사용하고 있는 메서드 내부에 Lock을 거는 경우, 트랜잭션의 커밋이 Lock 해제 후에 실행되어 테스트가 통과하지 않는 문제가 발생했다.

그래서 다음과 같이 `@TransactionalLock` 내부에 시작 전 락을 획득하고, 트랜잭션 종료 후 락을 해제하도록 구현했다.

```java
@TransactionalLock(value = {"issuedTicket:memberId:%d", "issuedTicket:ticketId:%d"})  
public void issue(Long memberId, Long ticketId) {  
    Member member = getMember(memberId);  
    Ticket ticket = getTicket(ticketId);  
    validateIssuable(member, ticket);  
    memberTicketRepository.save(new MemberTicket(member, ticket));  
    ticket.decrementQuantity();  
}
```

먼저 어노테이션의 value로 할당된 문자열 형식에 맞춰 `RLock`을 생성하고, 그 값들을 이용해 `RedissonMultiLock`을 생성한다.

```java
RLock[] locks = getLocks(joinPoint, transactionalLock);  
RedissonMultiLock multiLock = new RedissonMultiLock(locks);
```

멀티락 획득에 실패하면, 최대 열 번까지 재시도를 수행한다.

```java
for (int attempt = 0; attempt < 10; attempt++) {  
    isLockAcquired = acquireLock(multiLock);  
    if (isLockAcquired) {  
        break;  
    } 
    Thread.sleep(1000);  
}
```


```java
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
```


> [! 실행 결과]
> - [멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다]: 139 ms
> - [티켓 재고에 맞게 발행이 제한된다]: 80.25 ms
> - [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다]: 59.75 ms

