package com.aengdulab.distributedmail;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.aengdulab.distributedmail.domain.Question;
import com.aengdulab.distributedmail.domain.SentMailEvent;
import com.aengdulab.distributedmail.domain.Subscribe;
import com.aengdulab.distributedmail.repository.QuestionRepository;
import com.aengdulab.distributedmail.repository.SentMailEventRepository;
import com.aengdulab.distributedmail.repository.SubscribeRepository;
import com.aengdulab.distributedmail.support.TestMailClient;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.LongStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

@Slf4j
@SuppressWarnings("NonAsciiCharacters")
@Import(TestConfig.class)
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
class MultiServerRequestTest {

    /*
    전체 구독자 수 설정
     */
    private static final int SUBSCRIBER_COUNT = 20;

    /*
    서버를 다중화할 경우, 새로운 요청에 대한 추가 포트를 설정
     */
    private static final List<Integer> serverPorts = List.of(8080, 9090, 9999);

    @Autowired
    private SentMailEventRepository sentMailEventRepository;

    @Autowired
    private SubscribeRepository subscribeRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TestMailClient testMailClient;

    private Question question;
    private List<Subscribe> subscribes;

    @BeforeEach
    void setUp() {
        testMailClient.deleteAll();
        sentMailEventRepository.deleteAllInBatch();
        subscribeRepository.deleteAllInBatch();

        question = questionRepository.save(new Question("About potato", "What is potato?"));
        subscribes = subscribeRepository.saveAll(createSubscribes());
    }

    private List<Subscribe> createSubscribes() {
        return LongStream.range(0, SUBSCRIBER_COUNT)
                .mapToObj(id -> new Subscribe(id, "user" + id + "@example.com", question))
                .toList();
    }

    @Test
    void 다중화된_서버에서_균등하고_중복_없이_모든_구독자에게_메일을_발송한다() throws Exception {
        try (ExecutorService threadPool = Executors.newFixedThreadPool(serverPorts.size())) {
            serverPorts.forEach(port -> sendRequest(threadPool, port));
        }

        Thread.sleep(2000);

        long sentMailCount = sentMailEventRepository.count();
        long mailReceivedSubscribeUniqueCount = getMailReceivedSubscribeUniqueCount();
        List<String> initSubscribes = getInitSubscribeEmails();
        List<String> mailReceivedSubscribes = testMailClient.getMailReceivedSubscribes();

        assertAll(
                () -> assertThat(sentMailCount).isEqualTo(SUBSCRIBER_COUNT),
                () -> assertThat(mailReceivedSubscribeUniqueCount).isEqualTo(SUBSCRIBER_COUNT),
                () -> assertThat(mailReceivedSubscribes).containsExactlyInAnyOrderElementsOf(initSubscribes)
        );
    }

    private void sendRequest(ExecutorService executorService, int portNumber) {
        executorService.submit(() -> {
            given()
                    .baseUri("http://localhost")
                    .port(portNumber)
                    .when()
                    .post("/send-mail")
                    .then()
                    .statusCode(200);
        });
    }

    private long getMailReceivedSubscribeUniqueCount() {
        List<SentMailEvent> sentMailEvents = sentMailEventRepository.findAll();
        return sentMailEvents.stream()
                .map(SentMailEvent::getSubscribe)
                .distinct()
                .count();
    }

    private List<String> getInitSubscribeEmails() {
        return subscribes.stream().map(Subscribe::getEmail).toList();
    }
}
