package com.priye.streamvault.video.repository;

import com.priye.streamvault.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {
}