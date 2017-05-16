package game.chat;


import game.player.Player;
import game.player.PlayerState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Pavlo Putrenkov
 */
@EnableScheduling
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);
    private Player player;
    private Player client;
    private String clientLink;
    private AtomicBoolean accessSend;

    @Value("${client.port}")
    private int clientPort;

    @Value("${domain.node}")
    private boolean isDomainNode;

    @PostConstruct
    public void init() {
        player = new Player(isDomainNode);
        log.info("Create {}", player);
        accessSend = new AtomicBoolean(false);
        client = null;
        clientLink = CLIENT_HOST.concat(String.valueOf(clientPort));
    }

    @Override
    public void setReady() {
        player.setReady();
        log.info("Player {} is READY", player.getId());
    }

    @Override
    public boolean getPlayerState() {
        return !Objects.isNull(player) && player.isReady();
    }

    @Override
    public void regClient(String id, boolean isDomainNode) {
        if (Objects.isNull(id) || id.isEmpty()) {
            log.error("Bad player id - NULL or EMPTY", id);
            return;
        }

        client = new Player(id, isDomainNode, PlayerState.READY);
        log.info("Sign in new {}", client);
    }

    @Override
    public void start() {
        log.info("Waiting ...");
        while (!isPartnerReady()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Program do not wait for an answer and interrupted");
                return;
            }
        }

        boolean isReg = signIn();
        if (isReg)
            log.info("Player id: {} successfully registered", player.getId());
        else {
            log.info("Player id: {} could not sign in", player.getId());
            System.exit(0);
        }

        if (player.isMaster() && Objects.nonNull(client)) {
            player.incr();
            log.info("Player with id: {} send first message", player.getId());
            sendMessage();
        }
    }

    @Override
    public void sendMessage() {
        String uri = clientLink +
                "/message" +
                "?" +
                "id=" +
                player.getId() +
                "&msg=" +
                player.getMessage() +
                "&count=" + player.getStringCount();

        log.info("Sent message: {}", player.getMessage());
        httpRegistration(uri);
    }

    @Scheduled(fixedDelay = 1000)
    public void scheduleSend() {
        if (Objects.isNull(client))
            return;

        if (client.getCount() == COUNT_LIMIT) {
            exit(client.getId());
        }

        if (accessSend.get()) {
            player.incr();
            if (player.getCount() <= COUNT_LIMIT) {
                String m = client.getMessage()
                        .concat(":")
                        .concat(player.getStringCount());

                player.setMessage(m);
                sendMessage();
                accessSend.getAndSet(false);
            }
        }

        if (player.getCount() == COUNT_LIMIT) {
            exit(player.getId());
        }
    }

    @Override
    public void updateClient(String id, String msg, int count) {
        if (containsClientId(id)) {
            client.setMessage(msg);
            client.setCount(count);
            log.info("Updating player with id: {}, count: {}", id, count);
            accessSend.getAndSet(true);
        }
    }

    @Override
    public boolean containsClientId(String id) {
        return !(Objects.isNull(id) || id.isEmpty()) && !(Objects.isNull(client) || !id.equals(client.getId()));

    }

    @Override
    public void exit(String id) {
        log.info("Player: {} got limit of message", id);
        System.exit(0);
    }

    private boolean isPartnerReady() {
        return httpRegistration(clientLink.concat(READY_LINK));
    }

    private boolean signIn() {
        String buff = clientLink + "/signin" +
                "?id=" +
                player.getId() +
                "&domain=" +
                player.isMaster();

        return httpRegistration(buff);
    }

    private boolean httpRegistration(String link) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<Boolean> response
                    = restTemplate.getForEntity(link, Boolean.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            if (!link.contains(READY_LINK))
                log.error("Problem with connection to {}, {}", link, e.getMessage());

            return false;
        }
    }
}
