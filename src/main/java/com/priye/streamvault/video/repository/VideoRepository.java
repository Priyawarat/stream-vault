package com.priye.streamvault.video.repository;

import com.priye.streamvault.common.enums.VideoStatus;
import com.priye.streamvault.video.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VideoRepository extends JpaRepository<Video, UUID> {

    List<Video> findByStatus(VideoStatus status);

}