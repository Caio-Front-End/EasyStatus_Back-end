package br.com.easystatus.easystatus.repository;

import br.com.easystatus.easystatus.entity.Crm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrmRepository extends JpaRepository<Crm, Integer> {
    Optional<Crm> findByUrl(String url);
    Optional<Crm> findByName(String name);
}
