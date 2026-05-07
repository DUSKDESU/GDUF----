package test.openai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.openai.entity.File;

import java.util.List;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    Optional<File> findByFileId(String fileId);//根据文件ID查询文件

    List<File> findByUserId(Long userId);//根据用户ID查询文件

    void deleteByFileId(String fileId);//删除文件
}
