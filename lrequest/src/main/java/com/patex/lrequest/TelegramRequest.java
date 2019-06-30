package com.patex.lrequest;

import com.patex.messaging.MessengerListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.stream.Stream;

//@RequiredArgsConstructor
@Service
public class TelegramRequest implements MessengerListener {

    private final ActionBuilder actionBuilder;

//  private final StateMachine stateMachine;

    public TelegramRequest(ActionBuilder actionBuilder) {
        this.actionBuilder = actionBuilder;
//    this.stateMachine = stateMachine;
    }

    @Override
    @Transactional(readOnly = true)
    public Stream<String> createResponse(String request) {
        try {

            Value<?> result = actionBuilder.execute(request);
            if (result instanceof StreamValue) {
                return ((StreamValue<?>) result).getResultSupplier().get()
                        .map(String::valueOf)
                        .collect(Collectors.toList())
                        .stream();
            } else {
                Object value = result.getResultSupplier().get();
                return Stream.of(String.valueOf(value));
            }
        } catch (ParseException | WrongActionSyntaxException e) {
            return Stream.of(e.getMessage());
        }
    }

    @Override
    public boolean requireUserAuth() {
        return true;
    }
}
