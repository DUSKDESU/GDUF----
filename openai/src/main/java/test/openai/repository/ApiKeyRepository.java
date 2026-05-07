package test.openai.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.openai.entity.ApiKey;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {


     //根据API密钥值查找

    Optional<ApiKey> findByApiKey(String apiKey);



   //只查找用户启用中的 API Key
    List<ApiKey> findByUserIdAndIsActiveTrue(Long userId);


    @Modifying
    @Query("UPDATE ApiKey SET usedCount = usedCount + 1 WHERE id = :id")
    void incrementUsageCount(@Param("id") Long id);


    boolean existsByApiKeyAndIsActiveTrue(String apiKey);
}
