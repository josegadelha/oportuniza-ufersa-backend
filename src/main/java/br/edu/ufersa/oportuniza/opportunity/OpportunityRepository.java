package br.edu.ufersa.oportuniza.opportunity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
interface OpportunityRepository extends JpaRepository<Opportunity, Long> {

    java.util.List<Opportunity> findByProfessorId(Long professorId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Opportunity o where o.id = :opportunityId")
    Optional<Opportunity> findByIdForUpdate(@Param("opportunityId") Long opportunityId);
}