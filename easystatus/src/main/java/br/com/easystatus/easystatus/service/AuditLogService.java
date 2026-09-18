package br.com.easystatus.easystatus.service;

public interface AuditLogService {
    void logAction(String action, String tableName, Integer recordId, Object oldValue, Object newValue);
}
