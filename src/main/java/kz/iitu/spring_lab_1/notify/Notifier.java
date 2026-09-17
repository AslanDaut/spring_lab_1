package kz.iitu.spring_lab_1.notify;

public interface Notifier {
    String send(String message);
    String channel();
}