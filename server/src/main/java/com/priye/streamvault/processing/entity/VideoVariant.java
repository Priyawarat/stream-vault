package com.priye.streamvault.processing.entity;

import com.priye.streamvault.common.entity.BaseEntity;
import com.priye.streamvault.common.enums.VideoVariantStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "video_variants")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoVariant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID videoId;

    @Column(nullable = false, length = 20)
    private String resolution;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false)
    private Long fileSize;

    @Column
    private Long bitrate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VideoVariantStatus status;
}