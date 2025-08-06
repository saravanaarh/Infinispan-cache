package com.project.cache.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    private String name;
    private String department;

    @JsonManagedReference
    @ManyToMany(mappedBy = "students", fetch = FetchType.LAZY)
    private List<Subject> subjects;

}
