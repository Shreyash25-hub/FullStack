package com.placements.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leetcode_problems")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeetCodeProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(nullable = false, length = 20)
    private String difficulty;

    @Column(name = "paid_only", nullable = false)
    private boolean paidOnly;

    @Column(name = "primary_tag", nullable = false, length = 100)
    private String primaryTag;

    @Column(name = "all_tags", length = 1000)
    private String allTags;
}
