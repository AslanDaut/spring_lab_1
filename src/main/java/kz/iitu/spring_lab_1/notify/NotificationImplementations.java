package kz.iitu.spring_lab_1.notify;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Fallback;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component("console")
@Order(1)
class ConsoleNotifier implements Notifier {
    private static final Logger log = LoggerFactory.getLogger(ConsoleNotifier.class);

    @Override
    public String send(String message) {
        log.info("CONSOLE >> {}", message);
        return "console: " + message;
    }

    @Override
    public String channel() { return "console"; }
}

@Component("email")
@Primary
@Order(2)
class EmailNotifier implements Notifier{
    private static final Logger log = LoggerFactory.getLogger(EmailNotifier.class);

    @Override
    public String send(String message) {
        log.info("EMAIL >> {}", message);
        return "email: " + message;
    }

    @Override
    public String channel() { return "email"; }
}

// Индивидуальное задание (Вариант 1)
@Component("upper")
@Order(3)
class UpperNotifier implements Notifier {
    private static final Logger log = LoggerFactory.getLogger(UpperNotifier.class);

    @PostConstruct
    void init() {
        log.info("LIFECYCLE >> UpperNotifier bean initialized!");
    }

    @Override
    public String send(String message) {
        return message.toUpperCase();
    }

    @Override
    public String channel() { return "upper"; }
}

@Component("noop")
@Fallback
@Order(99)
class NoopNotifier implements Notifier {
    @Override
    public String send(String message) { return "noop"; }

    @Override
    public String channel() { return "noop"; }
}