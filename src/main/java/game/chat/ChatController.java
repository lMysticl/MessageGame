package game.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * @author Pavlo Putrenkov
 */
@RestController
@RequestMapping
public class ChatController implements Field {

    private final ChatService chatService;
    private Predicate<String> IS_STR_VALID = str -> true;

    @Autowired
    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @RequestMapping("/signin")
    public boolean signIn(@RequestParam("id") String id,
                          @RequestParam(name = "domain", defaultValue = "false") boolean isDomainNode) {

        chatService.regClient(id, isDomainNode);
        return true;
    }

    @RequestMapping("/ready")
    public boolean isReady() {
        return !Objects.isNull(chatService) && chatService.getPlayerState();

    }

    @RequestMapping("/message")
    public boolean getMessage(@RequestParam(ID) String id,
                              @RequestParam(MSG) String msg,
                              @RequestParam(COUNT) int count) {

        if (IS_STR_VALID.test(id)) {
            chatService.updateClient(id, msg, count);
            return true;
        }

        return false;
    }

}
