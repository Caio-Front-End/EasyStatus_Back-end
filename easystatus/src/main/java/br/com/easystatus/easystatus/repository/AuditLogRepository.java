package br.com.easystatus.easystatus.repository;

import br.com.easystatus.easystatus.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
}
