package com.priye.streamvault.video.repository;

import com.priye.streamvault.video.entity.VideoStatusTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.UUID;

@EnableJpaRepositories
public interface VideoStatusTransitionRepository extends JpaRepository<VideoStatusTransition, UUID> {

    List<VideoStatusTransition> findByVideoIdOrderByCreatedAtAsc(UUID videoId);

}