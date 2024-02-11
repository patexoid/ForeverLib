package api.com.patex;

import com.patex.messaging.TelegramMessenger;
import com.patex.forever.service.DirWatcherService;
import org.springframework.boot.test.mock.mockito.MockBean;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @MockBean
    DirWatcherService  dirWatcherService;

    @MockBean
    TelegramMessenger telegramMessenger;
}
