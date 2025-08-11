// strategy/impl/FailFastStrategy.java
package com.project.cache.strategy.impl;

import com.project.cache.exception.CacheException;
import com.project.cache.helper.CacheHelper;
import com.project.cache.model.Student;
import com.project.cache.repository.StudentRepository;
import com.project.cache.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("failFastStrategy")
@RequiredArgsConstructor
@Slf4j
public class FailFastStrategy implements CacheStrategy {

    private final StudentRepository studentRepository;
    private final CacheHelper cacheHelper;

    @Override
    @Transactional
    public Student saveStudent(Student student) {
        log.debug("Saving student using Fail-Fast strategy");

        // Pre-check cache availability before any operations
        if (!isStrategyAvailable()) {
            log.error("Cache is not available - Fail-Fast strategy cannot proceed for student: {}", student.getEmail());
            throw new RuntimeException("Cache is not available - Fail-Fast strategy cannot proceed");
        }

        try {
            // 1. Save to database (within transaction, not committed yet)
            Student savedStudent = studentRepository.save(student);
            log.debug("Student saved to database with ID: {}", savedStudent.getId());

            // 2. Cache operations must succeed using strict methods
            cacheHelper.cacheStudentStrict(savedStudent);
            log.debug("Student cached successfully with ID: {}", savedStudent.getId());

            // 3. Invalidate list cache
            cacheHelper.invalidateAllStudentsCacheStrict();
            log.debug("All students cache invalidated successfully");

            log.info("Student saved successfully using Fail-Fast strategy with ID: {}", savedStudent.getId());
            return savedStudent;

        } catch (CacheException e) {
            log.error("Fail-Fast strategy failed due to cache error - transaction will rollback for student: {}",
                    student.getEmail(), e);
            throw new RuntimeException("Fail-Fast strategy failed: Cache operation error", e);
        } catch (Exception e) {
            log.error("Fail-Fast strategy failed for student: {}", student.getEmail(), e);
            throw new RuntimeException("Fail-Fast strategy failed: Database operation error", e);
        }
    }

    @Override
    @Transactional
    public Student updateStudent(Student student) {
        log.debug("Updating student using Fail-Fast strategy for ID: {}", student.getId());

        // Pre-check cache availability
        if (!isStrategyAvailable()) {
            log.error("Cache is not available - Fail-Fast strategy cannot proceed for student ID: {}", student.getId());
            throw new RuntimeException("Cache is not available - Fail-Fast strategy cannot proceed");
        }

        try {
            // 1. Update in database (within transaction)
            Student updatedStudent = studentRepository.save(student);
            log.debug("Student updated in database with ID: {}", updatedStudent.getId());

            // 2. Cache operations must succeed
            cacheHelper.updateStudentInCacheStrict(updatedStudent);
            log.debug("Student updated in cache with ID: {}", updatedStudent.getId());

            // 3. Invalidate list cache
            cacheHelper.invalidateAllStudentsCacheStrict();
            log.debug("All students cache invalidated successfully");

            log.info("Student updated successfully using Fail-Fast strategy with ID: {}", updatedStudent.getId());
            return updatedStudent;

        } catch (CacheException e) {
            log.error("Fail-Fast strategy failed during update due to cache error - transaction will rollback for student ID: {}",
                    student.getId(), e);
            throw new RuntimeException("Fail-Fast strategy failed: Cache operation error", e);
        } catch (Exception e) {
            log.error("Fail-Fast strategy failed during update for student ID: {}", student.getId(), e);
            throw new RuntimeException("Fail-Fast strategy failed: Database operation error", e);
        }
    }

    @Override
    @Transactional
    public void deleteStudent(Long studentId) {
        log.debug("Deleting student using Fail-Fast strategy for ID: {}", studentId);

        // Pre-check cache availability
        if (!isStrategyAvailable()) {
            log.error("Cache is not available - Fail-Fast strategy cannot proceed for student ID: {}", studentId);
            throw new RuntimeException("Cache is not available - Fail-Fast strategy cannot proceed");
        }

        try {
            // 1. Delete from database (within transaction)
            studentRepository.deleteById(studentId);
            log.debug("Student deleted from database with ID: {}", studentId);

            // 2. Remove from cache - must succeed
            cacheHelper.removeStudentFromCacheStrict(studentId);
            log.debug("Student removed from cache with ID: {}", studentId);

            // 3. Invalidate list cache
            cacheHelper.invalidateAllStudentsCacheStrict();
            log.debug("All students cache invalidated successfully");

            log.info("Student deleted successfully using Fail-Fast strategy with ID: {}", studentId);

        } catch (CacheException e) {
            log.error("Fail-Fast strategy failed during deletion due to cache error - transaction will rollback for student ID: {}",
                    studentId, e);
            throw new RuntimeException("Fail-Fast strategy failed: Cache operation error", e);
        } catch (Exception e) {
            log.error("Fail-Fast strategy failed during deletion for student ID: {}", studentId, e);
            throw new RuntimeException("Fail-Fast strategy failed: Database operation error", e);
        }
    }

    @Override
    public String getStrategyName() {
        return "FAIL_FAST";
    }

    @Override
    public boolean isStrategyAvailable() {
        try {
            boolean available = cacheHelper.isCacheAvailable();
            log.debug("Fail-Fast strategy availability check: {}", available);
            return available;
        } catch (Exception e) {
            log.error("Cache availability check failed for Fail-Fast strategy", e);
            return false;
        }
    }
}
