# 우주여행 티켓 재고 관리 시스템 설계 해설

## 풀이 방법 - 비관락

적절하게 비관락을 사용하여 풀이하면 된다.

### 비관락 + synchronized

```java

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Ticket t where t.id = :id")
    Optional<Ticket> findByIdForUpdate(Long id);
}
```

```java

@Component
@RequiredArgsConstructor
public class SynchronizedServiceWrapper {

    private final MemberTicketService memberTicketService;

    public void issue(Long memberId, Long ticketId) {
        synchronized (memberId) {
            memberTicketService.issue(memberId, ticketId);
        }
    }
}
```

- 티켓을 조회할 때에 비관락을 사용한다. 각 티켓에 대한 동시성이 보장된다.
- 한 회원이 여러 티켓을 동시에 예약할 수 있기 때문에, 회원을 기준으로 동기화도 필요하다.
- 이를 위해 `SynchronizedServiceWrapper`를 만들어 회원을 기준으로도 동기화를 한다.

티켓 100개, 멤버 60명 동시 요청

실행 시간1: 350ms  
실행 시간2: 424ms  
실행 시간3: 378ms  



