package com.mkrdeveloper.devkhmeroauth2server.entity;

import jakarta.persistence.*;
import lombok.*;


@Builder
@Setter
@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "authorities")
public class Authority{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(unique = true)
    public String name;
}
