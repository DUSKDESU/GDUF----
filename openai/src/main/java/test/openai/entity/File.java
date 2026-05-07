package test.openai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true)
    private String fileId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "filename", nullable = false)
    private String filename;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "bytes")
    private Long bytes;

    @Column(name = "status")
    private String status = "uploaded";

    @Column(name = "status_details")
    private String statusDetails;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
