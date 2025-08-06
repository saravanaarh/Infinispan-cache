package com.project.cache.repository;

import com.project.cache.model.Student;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.subjects")
    List<Student> findAllWithSubjects();
}
