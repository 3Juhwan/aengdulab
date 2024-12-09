## 요구사항

1. **메일 발송**
    - **매일 아침 9시**에 메일 발송을 시작한다.
    - 메일 발송 지연을 고려하여 메일 발송 요청은 **비동기**로 처리한다.

2. **메일 발송 요청 서버 다중화**
    - 메일 발송 요청은 **다중화된 환경**, 즉 2대 이상의 서버에서 발생한다.
    - 메일 발송 요청은 **균등하게 분배**한다.
    - 일부 서버가 다운되어도 **모든 사용자에게 메일을 발송**해야 한다.

3. **메일 중복 발송 방지**
    - 한 사용자에게 **중복으로 메일을 발송하지 않도록** 한다.
    - 중복 발송을 방지하기 위해 **메일 발송 이력을 저장**한다.

## 구현 방법 1(그냥 깡 구현)
아래와 같은 클래스를 만들어 현재 서버의 포트를 가져오도록 구현했습니다.
```java
@Component
public class ServerPortProvider {

    private final Environment environment;

    public ServerPortProvider(Environment environment) {
        this.environment = environment;
    }

    public int getServerPort() {
        return Integer.parseInt(Objects.requireNonNull(environment.getProperty("local.server.port")));
    }
}
```
그리고는 말 그대로 깡구현(하드코딩)을 통해 라운드 로빈을 구현해봤습니다
```java
    private final ServerPortProvider serPortProvider;

    @Transactional
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void sendQuestion() {
        List<Subscribe> subscribes = getSubscribesForThisServer(serPortProvider.getServerPort());
        sendQuestionMails(subscribes);
        updateNextQuestions(subscribes);
    }

    private List<Subscribe> getSubscribesForThisServer(int serPort) {
        int serverIndex = getServerIndex(serPort);
        int serverCount = getTotalServerCount();
        return subscribeRepository.findAll().stream()
                .filter(subscribe -> isResponsibleServer(subscribe.getId().intValue(), serverCount, serverIndex))
                .toList();
    }

    private int getServerIndex(int serPort) {
        if (serPort == 8080) {
            return 0;
        } else if (serPort == 9090) {
            return 1;
        }
        return 2;
    }

    private int getTotalServerCount() {
        return 3;
    }
```
추가로 테스트의 `Thread.sleep(2000);`의 시간이 부족한 지 어떨 때는 `sentMailCount`와 `mailReceivedSubscribeUniqueCount`, `mailReceivedSubscribes`가 0으로 나올때도 있고 20으로 통과할 때도 있고 오락가락해서 한번 `Thread.sleep(5000);`으로 해봤습니다. 
그랬더니 테스트가 실패할때는 `sentMailCount`와 `mailReceivedSubscribeUniqueCount`는 0이 나오는데 `mailReceivedSubscribes`에는 정상적으로 20개가 다 들어있었습니다.
그래서 `Thread.sleep(10000);`으로 늘렸더니 정상적으로 통과합니다. 해당 부분은 좀 더 연구해봐야겠네요.
