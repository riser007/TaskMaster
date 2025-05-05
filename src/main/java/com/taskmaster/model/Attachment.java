package com.taskmaster.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "attachments")
@EntityListeners(AuditingEntityListener.class) // Needed for @CreatedDate if not extending Auditable
public class Attachment { // Doesn't extend Auditable to only have createdAt

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Size(max = 100)
    @Column(name = "file_type") // e.g., application/pdf, image/jpeg
    private String fileType;

    @NotBlank
    @Column(name = "file_path", nullable = false) // Path on server or key in cloud storage
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize; // Size in bytes

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploader_id", nullable = false)
    private User uploader;

    @CreatedDate // Automatically set on creation
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 31;
    }
}