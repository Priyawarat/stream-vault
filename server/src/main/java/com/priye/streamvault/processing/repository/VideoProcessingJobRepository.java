package com.priye.streamvault.processing.repository;

import com.priye.streamvault.processing.entity.VideoProcessingJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoProcessingJobRepository extends JpaRepository<VideoProcessingJob, UUID> {

    List<VideoProcessingJob> findByVideoIdOrderByStartedAtAsc(UUID videoId);
}