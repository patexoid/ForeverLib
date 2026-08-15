package api.com.patex;

import com.patex.forever.messaging.TelegramMessenger;
import com.patex.forever.service.DirWatcherService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@org.springframework.boot.test.context.TestConfiguration
public class TestConfiguration {

    @MockitoBean
    DirWatcherService  dirWatcherService;

    @MockitoBean
    TelegramMessenger telegramMessenger;
}
