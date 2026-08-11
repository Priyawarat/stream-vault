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
      AND (
            v.status = :uploadedStatus
            OR ( v.status = :processingStatus
                AND v.processingEventId = :eventId
               )
           )
    """)
    int claimProcessing(
            @Param("videoId") UUID videoId,
            @Param("eventId") UUID eventId,
            @Param("uploadedStatus") VideoStatus uploadedStatus,
            @Param("processingStatus") VideoStatus processingStatus
    );

}