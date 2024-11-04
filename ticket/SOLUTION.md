# 우주여행 티켓 재고 관리 시스템 설계 해설

## 풀이 방법 - 낙관락

Ticket 엔티티의 quantity 필드에 낙관락을 건다. 

- @Version으로 낙관락을 걸어두면, JPA가 엔티티를 업데이트할 때 버전이 증가한다.
- quantity는 감소해야 하는데, @Version을 걸면 quantity가 증가한다. 
- @Version을 일반적으로 사용하기 어렵고 별도의 로직을 작성해야 한다. 
- 또는, 새로운 version을 위한 필드를 추가해도 된다. 

### Hibernate의 @Version 로직 가로채기

- Hibernate의 Event Listener 또는 Entity Listener를 커스텀하여 @Version의 로직을 가로챌 수 있다.
- 하지만 이는 비표준 방식이고, 예측 불가능한 코드를 만든다. 
- 애플리케이션 레벨에서 낙관락을 구현할 수 있다. 하지만 코드가 복잡해지고, 신뢰하기 어려운 로직이다. 
- 또한, 낙관락으로 동시성 문제를 완전히 해결할 수 없다. 

### version 필드 추가

```java
public class Ticket {

    @Version
    private Long version;
}

public class MemberTicketService {

    @Retryable(
            retryFor = {DataAccessException.class},
            maxAttempts = 10,
            backoff = @Backoff(delay = 1000, multiplier = 2, random = true)
    )
    @Transactional
    public void issue(Long memberId, Long ticketId) {
    }
}
```

- 이렇게 하면 동시에 여러 요청이 들어왔을 때, 하나의 요청만 성공하고 나머지는 실패한다. 나머지 요청은 재시도한다.
- 재시도할 경우, 순서가 보장되지 않는다. 
- 주의할 점으로, 현재 외래키가 걸려 있어서 데드락이 발생한다. 
- 데드락이 발생하더라도, 재시도를 걸어두었기 때문에 테스트는 통과한다.  
- 또, 재시도 횟수와 백오프, 지터를 잘 활용하면 데드락을 어느정도 피할 수 있다. 
- 데드락은 큰 비용이 발생하므로 데드락 자체를 방지하는 것이 좋아보인다.  

티켓 100개, 멤버 60명 동시 요청

수행 시간1 : 11799ms  
수행 시간2 : 11732ms  
수행 시간3 : 11740ms  

### version 필드 추가 + 외래키 제거

```java
public class MemberTicket {

    private Long memberId;

    private Long ticketId;
}
``` 

티켓 100개, 멤버 60명 동시 요청

수행 시간1: 11502ms  
수행 시간2: 12425ms  
수행 시간3: 11169ms  

- 더이상 데드락은 발생하지 않지만, 수행 시간에 유의미한 차이는 없다.  