## PESSIMISTIC_WRITE

```java
@Repository  
public interface MemberTicketRepository extends JpaRepository<MemberTicket, Long> {  
  
    int countByMember(Member member);  
  
    @Lock(LockModeType.PESSIMISTIC_WRITE)  
    @Query("select count(mt.id) from MemberTicket mt where mt.member = :member")  
    int countByMemberWithLock(Member member);
}
```

```java
@Repository  
public interface TicketRepository extends JpaRepository<Ticket, Long> {  
  
    @Lock(LockModeType.PESSIMISTIC_WRITE)  
    @Query("select t from Ticket t where t.id = :ticketId")  
    Optional<Ticket> findByIdWithLock(Long ticketId);  
}
```

```java
@Service  
@Transactional(readOnly = true)  
@RequiredArgsConstructor  
public class MemberTicketService {  
  
    private final MemberRepository memberRepository;  
    private final TicketRepository ticketRepository;  
    private final MemberTicketRepository memberTicketRepository;  
  
    @Transactional  
    public synchronized void issue(Long memberId, Long ticketId) {  
        Member member = getMember(memberId);  
        Ticket ticket = getTicket(ticketId);  
        validateIssuable(member, ticket);  
        memberTicketRepository.save(new MemberTicket(member, ticket));  
        ticket.decrementQuantity();  
    }  
  
    private Member getMember(Long memberId) {  
        return memberRepository.findById(memberId)  
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));  
    }  
  
    private Ticket getTicket(Long ticketId) {  
        return ticketRepository.findByIdWithLock(ticketId)  
                .orElseThrow(() -> new IllegalArgumentException("티켓이 존재하지 않습니다."));  
    }  
  
    private void validateIssuable(Member member, Ticket ticket) {  
        if (!ticket.issuable()) {  
            throw new IllegalArgumentException("티켓 재고가 소진되었습니다.");  
        }  
        int issuedMemberTicketCount = memberTicketRepository.countByMemberWithLock(member);  
        
        if (issuedMemberTicketCount >= MemberTicket.MEMBER_TICKET_COUNT_MAX) {  
            throw new IllegalArgumentException("계정당 구매할 수 있는 티켓 수량을 넘었습니다.");  
        }  
    }}
```

위와 같이 락을 걸었을 때, 계정별로 발행할 수 있는 티켓 제한이 올바르게 검증되지 않았다. `countByMemberWithLock` 메서드는 `count(mt.id)`로 단순히 `MemberTicket` 개수를 세기만 하기 때문에, 실제로 `MemberTicket` 엔티티를 업데이트하거나 변경하는 작업과는 무관하다. (불필요하게 성능이 저하될 수도 있음) 또한, JPA가 제공하는 비관적 락 기능은 일반적으로 조회하는 데이터에 대해 락을 걸도록 설계되었으므로, 단순히 카운트 조회에는 적절하지 않았다.

비관적 락을 올바르게 적용하기 위해 `MemberTicket` 엔티티를 직접 조회하면서 락을 거는 방식으로 코드를 수정했다.  `countByMemberWithLock` 대신 `findByMemberWithLock` 메서드처럼 `MemberTicket`을 직접 조회하는 메서드를 사용해 락을 걸었다.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE) 
@Query("select mt from MemberTicket mt where mt.member = :member")
List<MemberTicket> findByMemberWithLock(Member member);
```

> [! 실행 결과]
> [멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] : 94.78 ms
> [티켓 재고에 맞게 발행이 제한된다]: 37.78 ms
> [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다]: 28.67 ms

## 역정규화

계정별 티켓 발행 한도를 검증하기 위해 `List<MemberTicket>`에 락을 거는 것이 비효율적이라고 판단해, `Member`에 `issuedTicketCount` 컬럼을 추가하는 방향으로 리팩터링

```java
@Entity  
@Getter  
@EqualsAndHashCode(of = "id")  
@NoArgsConstructor(access = AccessLevel.PROTECTED)  
public class Member {  
	  
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;  
	  
    private String name;  
	  
    private Integer issuedTicketCount; // 추가된 컬럼
	  
    public void increaseIssuedTicketCount() {  
        issuedTicketCount++;  
    }  
	  
    public boolean canIssueTicket() {  
        return issuedTicketCount < MemberTicket.MEMBER_TICKET_COUNT_MAX;  
    }  
}
```

`Member`가 이미 티켓 발행 개수를 알기 때문에 레포지토리에 별도의 조회 없이 `MemberTicket`만으로 발행 가능 여부 검증을 할 수 있다.

```java
@Entity  
@Getter  
@EqualsAndHashCode(of = "id")  
@NoArgsConstructor(access = AccessLevel.PROTECTED)  
public class MemberTicket {  
	  
    public static final int MEMBER_TICKET_COUNT_MAX = 2;  
	  
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;  
	  
    @ManyToOne  
    private Member member;  
	  
    @ManyToOne  
    private Ticket ticket;  
	
    public boolean issuable() {  
        return ticket.issuable() && member.canIssueTicket();  
    }  
}
```

그리고 서비스에서는 `List<MemberTicket>` 대신 `Member`에만 락을 건다.

```java
@Service  
@Transactional(readOnly = true)  
@RequiredArgsConstructor  
public class MemberTicketService {  
  
    private final MemberRepository memberRepository;  
    private final TicketRepository ticketRepository;  
    private final MemberTicketRepository memberTicketRepository;  
  
    @Transactional  
    public synchronized void issue(Long memberId, Long ticketId) {  
        Member member = getMember(memberId);  
        Ticket ticket = getTicket(ticketId);  
        MemberTicket memberTicket = new MemberTicket(member, ticket);  
        if (!memberTicket.issuable()) {  
            throw new IllegalArgumentException("티켓을 발급할 수 없습니다.");  
        }  
        memberTicketRepository.save(memberTicket);  
        ticket.decrementQuantity();  
        member.increaseIssuedTicketCount();  
    }  
  
    private Member getMember(Long memberId) {  
        return memberRepository.findByIdWithLock(memberId)  
                .orElseThrow(() -> new IllegalArgumentException("멤버가 존재하지 않습니다."));  
    }  
  
    private Ticket getTicket(Long ticketId) {  
        return ticketRepository.findByIdWithLock(ticketId)  
                .orElseThrow(() -> new IllegalArgumentException("티켓이 존재하지 않습니다."));  
    }  
}
```

테스트 실행 시 이전 방식보다 조금 더 빨라진 것을 확인할 수 있었다.

> [! 실행 결과]
> [멤버 티켓 최댓값에 맞게 계정별로 발행이 제한된다] : 75.9 ms
> [티켓 재고에 맞게 발행이 제한된다] : 32.6 ms
> [멤버 티켓을 발행하면 티켓 재고가 올바르게 수정된다] : 29.4 ms

