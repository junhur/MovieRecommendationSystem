package com.example.favor8.dao.entity;

import com.vladmihalcea.hibernate.type.array.ListArrayType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@Data
@Table(name = "recommendation_request")
@Entity
@TypeDef(name="list-array", typeClass = ListArrayType.class)
public class RecommendationRequestPo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "status")
    private Integer status;

    @Type(type= "list-array")
    @Column(name = "results", columnDefinition = "text []")
    private List<String> results;

    @Column(name = "response_time")
    private Integer responseTime; // In ms

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;
}
