// strategy/impl/AsyncCacheStrategy.java
package com.project.cache.strategy.impl;

import com.project.cache.helper.CacheHelper;
import com.project.cache.model.Student;
import com.project.cache.repository.StudentRepository;
import com.project.cache.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component("asyncCacheStrategy")
@RequiredArgsConstructor
@Slf4j
public class AsyncCacheStrategy implements CacheStrategy {

    private final StudentRepository studentRepository;
    private final CacheHelper cacheHelper;

    @Override
    public Student saveStudent(Student student) {
        // 1. Save to database first
        Student savedStudent = studentRepository.save(student);

        // 2. Asynchronous cache operations
        performAsyncCacheOperations(savedStudent, "save");

        return savedStudent;
    }

    @Override
    public Student updateStudent(Student student) {
        Student updatedStudent = studentRepository.save(student);
        performAsyncCacheOperations(updatedStudent, "update");
        return updatedStudent;
    }

    @Override
    public void deleteStudent(Long studentId) {
        studentRepository.deleteById(studentId);
        performAsyncCacheDeletion(studentId);
    }

    @Async
    public void performAsyncCacheOperations(Student student, String operation) {
        CompletableFuture.runAsync(() -> {
            try {
                cacheHelper.cacheStudent(student);
                cacheHelper.invalidateAllStudentsCache();
                log.debug("Async cache {} completed for student ID: {}", operation, student.getId());
            } catch (Exception e) {
                log.warn("Async cache {} failed for student ID: {}", operation, student.getId(), e);
            }
        });
    }

    @Async
    public void performAsyncCacheDeletion(Long studentId) {
        CompletableFuture.runAsync(() -> {
            try {
                cacheHelper.removeStudentFromCache(studentId);
                cacheHelper.invalidateAllStudentsCache();
                log.debug("Async cache deletion completed for student ID: {}", studentId);
            } catch (Exception e) {
                log.warn("Async cache deletion failed for student ID: {}", studentId, e);
            }
        });
    }

    @Override
    public String getStrategyName() {
        return "ASYNC_CACHE";
    }

    @Override
    public boolean isStrategyAvailable() {
        return true; // Always available as cache operations are async
    }
}
