package test.openai.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.openai.entity.Completion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CompletionRepository extends JpaRepository<Completion, Long> {


    Optional<Completion> findByCompletionId(String completionId);// 根据 completionId 查询

    @Query("SELECT COUNT(c) FROM Completion c WHERE c.userId = :userId AND c.createdAt BETWEEN :startTime AND :endTime")
    long countByUserIdAndCreatedAtBetween(
            @Param("userId") Long userId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
