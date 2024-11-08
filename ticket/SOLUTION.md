# 우주여행 티켓 재고 관리 시스템 설계 해설

## 풀이 방법 - MySQL의 User Level Lock

- 기존에 사용 중인 MySQL의 User Level Lock (이하 네임드 락)으로 분산락을 구현한다.
- 네임드 락은 [mysql 8.4 공식 문서](https://dev.mysql.com/doc/refman/8.4/en/locking-functions.html) 참고

### 네임드 락 사용

- 네임드 락 획득과 해제는 `GET_LOCK`과 `RELEASE_LOCK` 쿼리를 다룬다.
- 구현하는 방법이 다양하다. Jdbc, Spring Jdbc, Spring Data JPA 뭐든 상관 없다.  
- 중요한 것은, 네임드 락 획득과 해제가 실제 트랜잭션을 완전히 감싸야 한다.
- `issue` 메서드는 `@Transactional`이 적용되어 있어, 프록시로 동작함에 유의해야 한다. 
- 따라서, 네임드락 획득/해제 트랜잭션과 실제 트랜잭션을 분리해야 한다.

```java
// NamedLockServiceWrapper.java
public void executeInNamedLock(String lockName, Runnable runnable) {
    try (Connection connection = dataSource.getConnection()) {
        try {
            getLock(connection, lockName);
            runnable.run();
        } finally {
            releaseLock(connection, lockName);
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

// 테스트 코드
public class TestClass {
    
    void test() {
        // ...
        namedLockServiceWrapper.executeInNamedLock(
                "ticket", () -> memberTicketService.issue(member.getId(), ticketId)
        );
    }
}
```

- 문제는 Lock 거는 스레드가 커넥션 풀에서 관리하는 커넥션을 모두 사용할 수 있다. 
- HikariCP의 경우, 기본 커넥션은 10개이다. 
- 만약, 10개 이상의 동시 요청이 들어오면 각 요청 스레드는 커넥션을 가져가고 네임드 락을 시도한다. 
- 그 중 단 하나의 스레드만 락을 획득하고 나머지는 락을 획득할 때까지 대기한다.
- 락을 획득한 스레드도 비즈니스 로직을 수행하기 위해 새로운 커넥션이 필요하다. 
- 그러나, 이미 커넥션 풀이 모두 사용 중이기 때문에 새로운 커넥션을 가져올 수 없다. 
- 커넥션 풀을 늘리면 해결되지만, 무한정 늘릴 수 없기에 근본적인 해결책이 필요하다.  

### Lock CP와 로직 CP 분리

- Lock을 얻는데 사용하는 Connection Pool(Datasource)과, 로직을 수행하는데 사용되는 ConnectionPool(Datasource)을 분리한다.

### 추가로 고려할 점

- User Level Lock은 단일 서버에 대해서만 잠금이 발생한다. (세션 독립적, 서버 독립적)
- statement-based replication에서는 안전하지 않다. 
- NDB 클러스터 환경에서는 적합하지 않다.
- 비관락 vs 네임드락 
  - 네임드 락 timeout 설정이 가능하지만, 이는 비관락도 가능한 방법이다. 
  - 락의 대상이 다르다. 비관락은 테이블, 레코드, auto_increment 등인데, 네임드락은 문자열 그 자체다. 
  - 비관락은 레코드 레벨에 for update 구문으로 락을 건다. 이러면 다른 태스크의 트랜잭션도 해당 레코드에 접근할 수 없다.  
  - 네임드 락은 x-lock을 걸지 않기 때문에 다른 태스크의 트랜잭션도 해당 레코드에 접근할 수 있다.
  - 네임드 락의 경우에 데드락에 대한 우려가 낮다. 하지만 비관락도 외래키를 제거하면 데드락을 방지할 수 있다. 
