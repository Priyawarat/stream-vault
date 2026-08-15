package com.priye.streamvault.video.repository;

import com.priye.streamvault.video.entity.VideoVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoVariantRepository extends JpaRepository<VideoVariant, UUID> {

    List<VideoVariant> findByVideoId(UUID videoId);

    List<VideoVariant> findByVideoIdOrderByBitrateAsc(UUID videoId);

    Optional<VideoVariant> findByVideoIdAndResolution(UUID videoId, String resolution);
}