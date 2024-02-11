package com.patex.messaging;

import com.patex.forever.service.Resources;
import com.patex.forever.service.LibUserService;
import com.patex.forever.model.Res;
import com.patex.forever.model.User;
import com.patex.forever.model.UserConfig;
import com.patex.forever.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Disabled
public class MessengerServiceTest {

    private static final String LOCALIZED_MESSAGE = "localizedMessage";
    private static final String MESSAGE_KEY = "message";
    private static final Object[] OBJS = new Object[0];

    @Mock
    private Resources res;

    @Mock
    private LibUserService userService;

    @Mock
    private Messenger messenger;

    @InjectMocks
    private MessengerService messengerService;

    private User user = new User();

    private Locale locale = Locale.PRC;
    private UserConfig userConfig = mock(UserConfig.class);

    @BeforeEach
    public void setUp() {
        messengerService.register(messenger);
        when(userConfig.getLocale()).thenReturn(locale);
        user.setUserConfig(userConfig);
        when(res.get(locale, MESSAGE_KEY, OBJS)).thenReturn(LOCALIZED_MESSAGE);
    }

    @Test
    public void shouldSendMessageRes() {
        messengerService.sendMessageToUser(new Res(MESSAGE_KEY, OBJS), user.getUsername());
        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user);
    }

    @Test
    public void shouldSendMessageToUserWithRole() {
        User user1 = new User();
        user1.setUsername("1");
        user1.setUserConfig(userConfig);
        User user2 = new User();
        user2.setUsername("2");
        user2.setUserConfig(userConfig);
        String role = "role";
        when(userService.getByRole(role)).thenReturn(Arrays.asList(user1, user2));

        messengerService.toRole(new Res(MESSAGE_KEY, OBJS), role);

        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user1);
        verify(messenger).sendToUser(LOCALIZED_MESSAGE, user2);
    }

    @Test
    public void shouldSendMessagetoAdminWhenStop() {
        when(userService.getByRole(UserService.ADMIN_AUTHORITY)).thenReturn(Collections.singletonList(user));
        String stopMessage = "stopMessage";
        when(res.get(locale, "lib.stopped")).thenReturn(stopMessage);

        messengerService.stopEvent();

        verify(messenger).sendToUser(stopMessage, user);

    }
}
