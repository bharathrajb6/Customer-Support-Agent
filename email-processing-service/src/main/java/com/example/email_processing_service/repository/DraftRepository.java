package com.example.email_processing_service.repository;

import com.example.email_processing_service.entity.DraftEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DraftRepository extends JpaRepository<DraftEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DraftEntity d join fetch d.email where d.id = :id")
    Optional<DraftEntity> findByIdForUpdate(@Param("id") Long id);
}
