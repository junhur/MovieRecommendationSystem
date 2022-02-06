package com.example.favor8.dao.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Table(name = "watching")
@Entity
public class WatchingPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "movie_title")
    private String movieTitle;

    @Column(name = "minute")
    private Integer minute;

    @Column(name = "watched_at")
    private LocalDateTime watchedAt;
}
