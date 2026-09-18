package br.com.easystatus.easystatus.repository;

import br.com.easystatus.easystatus.entity.HealthCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthCheckRepository extends JpaRepository<HealthCheck, Integer> {
    List<HealthCheck> findByCrmIdOrderByDataCriacaoDesc(Integer crmId);
}
