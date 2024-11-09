## Ticket에 낙관적 락 적용

```java
@Entity  
@Getter  
@EqualsAndHashCode(of = "id")  
@NoArgsConstructor(access = AccessLevel.PROTECTED)  
public class Ticket {  
  
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;  
  
    private String name;  
  
    private Long quantity;  
  
    @Version  
    private Long version;  
  
    public Ticket(String name, Long quantity) {  
        this(null, name, quantity);  
    }  
  
    public Ticket(Long id, String name, Long quantity) {  
        this.id = id;  
        this.name = name;  
        this.quantity = quantity;  
    }  
  
    public boolean issuable() {  
        return quantity > 0;  
    }  
  
    public void decrementQuantity() {  
        quantity--;  
    }  
}
```

```java
@Repository  
public interface TicketRepository extends JpaRepository<Ticket, Long> {  
  
    @Lock(LockModeType.OPTIMISTIC)  
    @Query("select t from Ticket t where t.id = :ticketId")  
    Optional<Ticket> findByIdWithLock(Long ticketId);  
}
```

그리고 점유되고 있는 데이터로 인해 트랜잭션이 실패하는 경우에 대비해, 재시도 설정을 서비스에 추가해 주었다.

```java
@Transactional  
@Retryable(  
        retryFor = { ObjectOptimisticLockingFailureException.class },  
        maxAttempts = 10,  
        backoff =  @Backoff(delayExpression = "T(java.lang.Math).random() * 1000 + 200")  
)  
public void issue(Long memberId, Long ticketId) {  
	// ...발행 로직
}
```

티켓에 낙관적 락을 걸어도 여전히 해결되지 않는 문제가 있다.

```java
private void validateIssuable(Member member, Ticket ticket) {  
    if (!ticket.issuable()) {  
        throw new IllegalArgumentException("티켓 재고가 소진되었습니다.");  
    }  
    int issuedMemberTicketCount = memberTicketRepository.countByMember(member);  
    if (issuedMemberTicketCount >= MemberTicket.MEMBER_TICKET_COUNT_MAX) {  
        throw new IllegalArgumentException("계정당 구매할 수 있는 티켓 수량을 넘었습니다.");  
    }  
}
```

`MemberTicketService`는 검증 로직에서 멤버별로 발행할 수 있는 티켓의 개수를 제한하고 있다. 그러나 이 부분에는 락이 걸리지 않아서 다음과 같이 멤버별 발행 개수 제한을 넘어서 발행을 허용하는 경우가 발생한다.

`Member`가 `Ticket`을 추가로 발행한 경우에, `Member`가 가진 컬럼에 직접적인 변경 사항은 없지만 해당 `Member`가 추가로 발행할 수 있는 티켓의 개수가 줄어든 것에 해당하니 이 부분에도 락을 걸어 주었다.

```java
@Entity  
@Getter  
@ToString  
@EqualsAndHashCode(of = "id")  
@NoArgsConstructor(access = AccessLevel.PROTECTED)  
public class Member {  
  
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;  
  
    private String name;  
  
    @Version  
    private Long version;  
  
    public Member(String name) {  
        this(null, name);  
    }  
  
    public Member(Long id, String name) {  
        this.id = id;  
        this.name = name;  
    }  
}
```

```java
@Repository  
public interface MemberRepository extends JpaRepository<Member, Long> {  
  
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)  
    @Query("select m from Member m where m.id = :memberId")  
    Optional<Member> findByIdWithLock(Long memberId);  
}
```

`OPTIMISTIC_FORCE_INCREMENT`는 **낙관적 락**을 적용한 후, 엔티티의 버전 정보를 강제로 증가시키는 방식이다. 즉, 트랜잭션이 완료되기 전에 다른 트랜잭션이 데이터를 수정할 경우, 충돌이 발생하고 예외가 발생한다. 또한, 이 잠금 방식은 엔티티의 버전 컬럼을 이용하여 변경된 상태를 추적한다.

> [! 실행결과]
> - [멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다]: 약 85.78 ms
> - [티켓 재고에 맞게 발행이 제한된다]: 약 18.22 ms
> - [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다]: 약 12.33 ms ​

