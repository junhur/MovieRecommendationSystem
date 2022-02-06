package com.example.favor8.dao.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@Data
@Table(name = "movies")
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class MoviePo {
    @Id
    @Column(name = "title")
    private String title;

    @Type(type = "jsonb")
    @Column(name = "info")
    private Object info;
}
