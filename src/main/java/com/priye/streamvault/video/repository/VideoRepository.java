package com.priye.streamvault.video.repository;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    List<Video> findByStatus(VideoStatus status);

    @Modifying
    @Query("""
            UPDATE Video v
            SET v.status = :processingStatus,
                v.processingEventId = :eventId,
                v.updatedAt = CURRENT_TIMESTAMP
            WHERE v.id = :videoId
              AND v.status = :uploadedStatus
            """)
    int claimProcessing(
            @Param("videoId") UUID videoId,
            @Param("eventId") UUID eventId,
            @Param("uploadedStatus") VideoStatus uploadedStatus,
            @Param("processingStatus") VideoStatus processingStatus
    );

    @Modifying
    @Query("""
            UPDATE Video v
            SET v.status = :uploadedStatus,
                v.processingEventId = NULL,
                v.updatedAt = CURRENT_TIMESTAMP
            WHERE v.id = :videoId
              AND v.status = :processingStatus
              AND v.processingEventId = :eventId
            """)
    int resetProcessingToUploaded(
            @Param("videoId") UUID videoId,
            @Param("eventId") UUID eventId,
            @Param("processingStatus") VideoStatus processingStatus,
            @Param("uploadedStatus") VideoStatus uploadedStatus
    );

    @Modifying
    @Query("""
            UPDATE Video v
            SET v.status = :failedStatus,
                v.processingEventId = NULL,
                v.updatedAt = CURRENT_TIMESTAMP
            WHERE v.id = :videoId
              AND v.status = :processingStatus
              AND v.processingEventId = :eventId
            """)
    int markFailed(
            @Param("videoId") UUID videoId,
            @Param("eventId") UUID eventId,
            @Param("processingStatus") VideoStatus processingStatus,
            @Param("failedStatus") VideoStatus failedStatus
    );
}