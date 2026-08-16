package com.priye.streamvault.messaging.outbox.repository;

import com.priye.streamvault.common.enums.OutboxStatus;
import com.priye.streamvault.messaging.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EnableJpaRepositories
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEvent e SET e.status = :processingStatus, e.processingAt = :processingAt WHERE e.id = :id AND e.status = :pendingStatus")
    int claimEvent(@Param("id") UUID id,
            @Param("pendingStatus") OutboxStatus pendingStatus,
            @Param("processingStatus") OutboxStatus processingStatus,
            @Param("processingAt") LocalDateTime processingAt
    );

    @Modifying
    @Transactional
    @Query("UPDATE OutboxEvent e SET e.status = :pendingStatus, e.processingAt = null WHERE e.status = :processingStatus AND e.processingAt < :cutoffTime")
    int recoverStuckEvents(@Param("processingStatus") OutboxStatus processingStatus,
            @Param("pendingStatus") OutboxStatus pendingStatus,
            @Param("cutoffTime") LocalDateTime cutoffTime
    );

}