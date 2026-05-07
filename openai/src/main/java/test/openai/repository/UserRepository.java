package test.openai.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import test.openai.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


     // 根据用户名查找用户

    Optional<User> findByUsername(String username);




     //检查用户名是否存在

    boolean existsByUsername(String username);


      //检查邮箱是否存在

    boolean existsByEmail(String email);


     // 根据用户名或邮箱查找用户

    @Query("SELECT u FROM User u WHERE u.username = :identifier OR u.email = :identifier")
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);
}
