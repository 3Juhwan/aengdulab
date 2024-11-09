
`@Transactional`로 트랜잭션을 적용한 메서드는 프록시로 로직이 호출되어, `synchronized`가 제대로 동작하지 않는다.
트랜잭션 커밋이 `synchronized` 로 제한되는 범위 밖에서 실행되기 때문

## 메서드 내에 트랜잭션 적용

```java
@Service  
@RequiredArgsConstructor  
public class MemberTicketService {  
  
    private final MemberRepository memberRepository;  
    private final TicketRepository ticketRepository;  
    private final MemberTicketRepository memberTicketRepository;  
    private final TransactionTemplate transactionTemplate;  
  
    public synchronized void issue(Long memberId, Long ticketId) {  
        transactionTemplate.execute(status -> {  
            Member member = getMember(memberId);  
            Ticket ticket = getTicket(ticketId);  
  
            validateIssuable(member, ticket);  
            memberTicketRepository.save(new MemberTicket(member, ticket));  
            ticket.decrementQuantity();  
            return null;  
        });  
    }
```

>[! 실행 결과]
>[멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] : 124 ms
> [티켓 재고에 맞게 발행이 제한된다]: 66 ms
> [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다]: 44 ms

## MemberTicketIssueService 분리

- 위 문제를 해결하기 위해 트랜잭션이 미리 적용된 `MemberTicketIssueService`의 메서드를 `MemberTicketService`에서 `synchronized` 하게 호출해, 트랜잭션 시작과 커밋을 범위 내에 포함시킬 수 있다.

### 1. MemberTicketService의 트랜잭션 제거

```java
@Service  
@RequiredArgsConstructor  
public class MemberTicketService {  
  
    private final MemberTicketIssueService memberTicketIssueService;  
  
    public synchronized void issue(Long memberId, Long ticketId) {  
        memberTicketIssueService.issue(memberId, ticketId);  
    }  
}
```

```java
@Service  
@RequiredArgsConstructor  
public class MemberTicketIssueService {  
  
    private final MemberRepository memberRepository;  
    private final TicketRepository ticketRepository;  
    private final MemberTicketRepository memberTicketRepository;  
  
    @Transactional  
    public void issue(Long memberId, Long ticketId) {  
        Member member = getMember(memberId);  
        Ticket ticket = getTicket(ticketId);  
  
        validateIssuable(member, ticket);  
        memberTicketRepository.save(new MemberTicket(member, ticket));  
        ticket.decrementQuantity();  
    }
}
```

> [!실행 결과]
>
> - [멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다]: 약 1385.9 ms
> - [티켓 재고에 맞게 발행이 제한된다]: 약 447.0 ms
> - [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다]: 약 210.6 ms
>

### 2. @Transactional(propagation = REQUIRES_NEW)

```java
@Service  
@Transactional(readOnly = true)  
@RequiredArgsConstructor  
public class MemberTicketService {  
  
    private final MemberTicketIssueService memberTicketIssueService;  
  
    @Transactional  
    public synchronized void issue(Long memberId, Long ticketId) {  
        memberTicketIssueService.issue(memberId, ticketId);  
    }  
}
```

```java
@Service  
@RequiredArgsConstructor  
public class MemberTicketIssueService {  
  
    private final MemberRepository memberRepository;  
    private final TicketRepository ticketRepository;  
    private final MemberTicketRepository memberTicketRepository;  
  
    @Transactional(propagation = Propagation.REQUIRES_NEW)  
    public void issue(Long memberId, Long ticketId) {  
        Member member = getMember(memberId);  
        Ticket ticket = getTicket(ticketId);  
  
        validateIssuable(member, ticket);  
        memberTicketRepository.save(new MemberTicket(member, ticket));  
        ticket.decrementQuantity();  
    }
}
```

`MemberTicketService` 에  `@Transactional` 을 함께 적용하고 `MemberTicketIssueService`  메서드의 트랜잭션 전파를 `Propagation.REQUIRES_NEW`로 설정하는 경우 다음과 같은 오류가 발생한다.

```
2024-11-03T23:18:20.941+09:00  WARN 8000 --- [ool-1-thread-14] o.h.engine.jdbc.spi.SqlExceptionHelper   : SQL Error: 0, SQLState: null
2024-11-03T23:18:20.946+09:00 ERROR 8000 --- [ool-1-thread-14] o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30006ms (total=10, active=10, idle=0, waiting=0)
2024-11-03T23:18:20.942+09:00 ERROR 8000 --- [ool-1-thread-15] o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30006ms (total=10, active=10, idle=0, waiting=0)
2024-11-03T23:18:20.942+09:00 ERROR 8000 --- [ool-1-thread-11] o.h.engine.jdbc.spi.SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30006ms (total=10, active=10, idle=0, waiting=0)
```

이 오류는 HikariCP 연결 풀에서 발생하는 오류로, 데이터베이스 연결을 요청했지만 사용할 수 있는 커넥션이 없어서 타임아웃이 발생했음을 의미한다.

테스트 실행 전까지는 `synchronized`가 걸려 있는 `MemberTicketService`의 메서드에 의한 트랜잭션 하나, 그리고 내부에서 호출된 `MemberTicketIssueService`의 메서드에서 새로 생성된 트랜잭션 하나로 총 두 개의 커넥션이 생성될 것이라고 예상했었다. 커넥션 풀의 기본값은 10으로 2보다 훨씬 작은 값이다. 그러나 테스트 결과를 통해 활성 커넥션을 초과하는 요청이 발생하는 가능성을 열어두고, 원인을 찾아 봤다.

우선 애플리케이션의 datasource 설정에서 hikari.maxPoolSize를 20으로 늘리고, 테스트에 다음과 같이 활성 커넥션 개수를 주기적으로 출력하는 스케줄러를 추가했다.

```java
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);  
scheduler.scheduleAtFixedRate(() -> {  
    int activeConnections = dataSource.getHikariPoolMXBean().getActiveConnections();  
    System.out.println("Current Active Connections: " + activeConnections);  
}, 0, 300, TimeUnit.MICROSECONDS);
```

그리고 모든 테스트에서 동일하게 최대 `활성화된 스레드 개수 + 1` 개의 커넥션이 활성화되는 것을 확인할 수 있었다. 스프링이 생성한 프록시에서 `synchronized` 범위에 진입하기 전, 커넥션을 요청하기 때문에 위와 같은 결과가 나타난 것 같다.

> [! 실행 결과]
> [멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] : 108 ms
> [티켓 재고에 맞게 발행이 제한된다]: 55 ms
> [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다]: 40 ms

