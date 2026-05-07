package test.openai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
@Entity
@Table(name = "models")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name",nullable = false, unique = true)
    private String modelName;

    @Column(name = "display_name")
    private String displayname;

    @Column(name = "description",length = 500)
    private String description;

    @Column(name = "owner_id", nullable = true)
    private Long ownerid;

    @Column(name = "is_Active")
    private Boolean isActive = true;

    @Column(name = "max_Tokens")
    private Integer maxTokens;

    @Column(name = "supports_streaming")
    private Boolean supportsStreaming;

    @CreationTimestamp
    @Column(name = "created_At")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
