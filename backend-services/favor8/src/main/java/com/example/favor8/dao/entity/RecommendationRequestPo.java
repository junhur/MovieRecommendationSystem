package com.example.favor8.dao.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Data
@Table(name = "recommendation_request")
@Entity
@TypeDef(name="jsonb", typeClass= JsonBinaryType.class)
public class RecommendationRequestPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "result")
    @Type(type= "jsonb")
    private Object result;

    @Column(name = "response_time")
    private Integer responseTime; // In ms

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
}
