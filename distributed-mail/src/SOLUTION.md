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
`serverCount`는 환경 변수로 처리 하면 될 것 같은데 `serverIndex`는 더 공부해봐야겠네요.


추가로 테스트의 `Thread.sleep(2000);`의 시간이 부족한 지 어떨 때는 `sentMailCount`와 `mailReceivedSubscribeUniqueCount`, `mailReceivedSubscribes`가 0으로 나올때도 있고 20으로 통과할 때도 있고 오락가락해서 한번 `Thread.sleep(5000);`으로 해봤습니다.
그랬더니 테스트가 실패할때는 `sentMailCount`와 `mailReceivedSubscribeUniqueCount`는 0이 나오는데 `mailReceivedSubscribes`에는 정상적으로 20개가 다 들어있었습니다.
그래서 `Thread.sleep(10000);`으로 늘렸더니 정상적으로 통과합니다. 해당 부분은 좀 더 공부해봐야겠네요.
