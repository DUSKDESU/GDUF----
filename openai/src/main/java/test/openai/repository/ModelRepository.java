package test.openai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import test.openai.entity.Model;

public interface ModelRepository extends JpaRepository<Model, Long>
{


    Model findByModelName(String modelName);// 根据模型名称查询模型



}
