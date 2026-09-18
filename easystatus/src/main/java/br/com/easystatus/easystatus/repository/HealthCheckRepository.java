package br.com.easystatus.easystatus.repository;

import br.com.easystatus.easystatus.entity.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Integer> {
    List<HealthCheck> findByCrmIdOrderByDataCriacaoDesc(Integer crmId);

    @Modifying
    @Transactional
    void deleteByDataCriacaoBefore(LocalDateTime cutoffDate);
}
