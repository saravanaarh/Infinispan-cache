// strategy/impl/CacheAsideStrategy.java
package com.project.cache.strategy.impl;

import com.project.cache.helper.CacheHelper;
import com.project.cache.model.Student;
import com.project.cache.repository.StudentRepository;
import com.project.cache.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component("cacheAsideStrategy")
@RequiredArgsConstructor
@Slf4j
public class CacheAsideStrategy implements CacheStrategy {

    private final StudentRepository studentRepository;
    private final CacheHelper cacheHelper;

    @Override
    public Student saveStudent(Student student) {
        // 1. Save to database first
        Student savedStudent = studentRepository.save(student);

        // 2. Cache operations (best-effort, non-blocking)
        try {
            cacheHelper.cacheStudent(savedStudent);
            cacheHelper.invalidateAllStudentsCache();
            log.debug("Student cached successfully using Cache-Aside strategy");
        } catch (Exception e) {
            log.warn("Cache operation failed for student ID: {} - continuing without cache",
                    savedStudent.getId(), e);
        }

        return savedStudent;
    }

    @Override
    public Student updateStudent(Student student) {
        Student updatedStudent = studentRepository.save(student);

        try {
            cacheHelper.cacheStudent(updatedStudent);
            cacheHelper.invalidateAllStudentsCache();
        } catch (Exception e) {
            log.warn("Cache update failed for student ID: {}", updatedStudent.getId(), e);
        }

        return updatedStudent;
    }

    @Override
    public void deleteStudent(Long studentId) {
        studentRepository.deleteById(studentId);

        try {
            cacheHelper.removeStudentFromCache(studentId);
            cacheHelper.invalidateAllStudentsCache();
        } catch (Exception e) {
            log.warn("Cache deletion failed for student ID: {}", studentId, e);
        }
    }

    @Override
    public String getStrategyName() {
        return "CACHE_ASIDE";
    }

    @Override
    public boolean isStrategyAvailable() {
        return true; // Always available as it's fault-tolerant
    }
}
