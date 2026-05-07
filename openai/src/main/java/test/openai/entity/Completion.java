package test.openai.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "completions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"user", "apiKey"})
public class Completion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "completion_id", nullable = false, unique = true)
    private String completionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "api_key_id", nullable = false)
    private Long apiKeyId;

    @Column(nullable = false)
    private String model;

    @Column(name = "request_messages", columnDefinition = "json", nullable = false)
    private String requestMessages;

    @Column(name = "response_content", columnDefinition = "text")
    private String responseContent;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private StatusEnum status = StatusEnum.pending;

    @Column(precision = 3, scale = 2)
    private BigDecimal temperature = new BigDecimal("1.0");

    @Column(nullable = false)
    private Boolean stream = false;

    @Column(name = "finish_reason", length = 20)
    private String finishReason = "stop";

    @Column(name = "usage_tokens")
    private Integer usageTokens = 0;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs = 0;

    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", insertable = false, updatable = false)
    private ApiKey apiKey;

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        if (value == null) return null;
        for (StatusEnum status : StatusEnum.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }

    public enum StatusEnum {
        pending, processing, completed, failed, cancelled
    }
}
