package game;


import game.chat.ChatService;
import game.chat.ChatServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Pavlo Putrenkov
 */
@SpringBootApplication
public class MessageGameApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context =
                SpringApplication.run(MessageGameApplication.class, args);

        ChatService chatService = context.getBean(ChatServiceImpl.class);
        chatService.setReady();
        chatService.start();
    }
}
