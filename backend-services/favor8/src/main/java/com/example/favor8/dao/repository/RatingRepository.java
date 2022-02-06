package com.example.favor8.dao.repository;

import com.example.favor8.dao.entity.RatingPo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<RatingPo, Integer> {
}
