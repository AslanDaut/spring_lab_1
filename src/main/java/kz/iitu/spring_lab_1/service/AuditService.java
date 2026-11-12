package kz.iitu.spring_lab_1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    // REQUIRES_NEW создает новую независимую транзакцию
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAuditRecord(String message) {
        // В реальном проекте здесь была бы запись в БД (таблицу audit_logs)
        // Для лабы мы просто выводим лог, который имитирует сохранение аудита
        log.info("[AUDIT SAVED in REQUIRES_NEW] - {}", message);
    }
}