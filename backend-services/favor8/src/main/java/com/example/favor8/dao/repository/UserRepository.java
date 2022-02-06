package com.example.favor8.dao.repository;

import com.example.favor8.dao.entity.UserPo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserPo, Integer> {
}
