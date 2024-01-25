package org.springframework.cloud.stream.reproducer.stackoverflowerror;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.solace.SolaceContainer;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("test")
class StackOverflowErrorReproducerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackOverflowErrorReproducerTest.class);

    private static final SolaceContainer SOLACE_CONTAINER = new SolaceContainer("solace/solace-pubsub-standard:latest")
            .withExposedPorts(55555)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER));

    @Autowired
    private StreamBridge streamBridge;

    @BeforeAll
    static void beforeAll() throws InterruptedException {
        SOLACE_CONTAINER.start();
        assertTrue(SOLACE_CONTAINER.isRunning());

        System.setProperty("SOLACE_CLIENT_USERNAME", SOLACE_CONTAINER.getUsername());
        System.setProperty("SOLACE_CLIENT_PASSWORD", SOLACE_CONTAINER.getPassword());
        System.setProperty("SOLACE_MESSAGE_VPN", SOLACE_CONTAINER.getVpn());
        System.setProperty("SOLACE_BROKER_HOST", "%s:%s".formatted(SOLACE_CONTAINER.getHost(), SOLACE_CONTAINER.getMappedPort(55555)));

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
     void reproducesStackOverflowError_whenBinderIsStopped_always() {
        streamBridge.send("publishMessage-out-0", "testData");
    }
}