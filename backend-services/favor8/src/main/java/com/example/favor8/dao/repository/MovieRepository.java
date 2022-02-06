package com.example.favor8.dao.repository;

import com.example.favor8.dao.entity.MoviePo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<MoviePo, String> {
}
