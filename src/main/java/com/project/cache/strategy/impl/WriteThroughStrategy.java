// strategy/impl/WriteThroughStrategy.java
package com.project.cache.strategy.impl;

import com.project.cache.exception.CacheException;
import com.project.cache.helper.CacheHelper;
import com.project.cache.model.Student;
import com.project.cache.repository.StudentRepository;
import com.project.cache.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component("writeThroughStrategy")
@RequiredArgsConstructor
@Slf4j
public class WriteThroughStrategy implements CacheStrategy {

    private final StudentRepository studentRepository;
    private final CacheHelper cacheHelper;

    @Override
    public Student saveStudent(Student student) {
        log.debug("Saving student using Write-Through strategy");

        try {
            // 1. Save to database
            Student savedStudent = studentRepository.save(student);
            log.debug("Student saved to database with ID: {}", savedStudent.getId());

            // 2. Update cache using strict methods
            cacheHelper.cacheStudentStrict(savedStudent);
            log.debug("Student cached successfully with ID: {}", savedStudent.getId());

            // 3. Invalidate list cache
            cacheHelper.invalidateAllStudentsCacheStrict();
            log.debug("All students cache invalidated successfully");

            log.info("Student saved successfully using Write-Through strategy with ID: {}", savedStudent.getId());
            return savedStudent;

        } catch (CacheException e) {
            log.error("Write-Through strategy failed due to cache error - transaction will rollback for student: {}",
                    student.getEmail(), e);
            throw new RuntimeException("Failed to save student with Write-Through strategy: Cache operation failed", e);
        } catch (Exception e) {
            log.error("Write-Through strategy failed due to database error for student: {}", student.getEmail(), e);
            throw new RuntimeException("Failed to save student with Write-Through strategy: Database operation failed", e);
        }
    }

    @Override
    public Student updateStudent(Student student) {
        log.debug("Updating student using Write-Through strategy for ID: {}", student.getId());

        try {
            // 1. Update in database (within transaction)
            Student updatedStudent = studentRepository.save(student);
            log.debug("Student updated in database with ID: {}", updatedStudent.getId());

            // 2. Update cache using strict methods
            cacheHelper.updateStudentInCacheStrict(updatedStudent);
            log.debug("Student updated in cache with ID: {}", updatedStudent.getId());

            // 3. Invalidate list cache
            cacheHelper.invalidateAllStudentsCacheStrict();
            log.debug("All students cache invalidated successfully");

            log.info("Student updated successfully using Write-Through strategy with ID: {}", updatedStudent.getId());
            return updatedStudent;

        } catch (CacheException e) {
            log.error("Write-Through strategy failed during update due to cache error - transaction will rollback for student ID: {}",
                    student.getId(), e);
            throw new RuntimeException("Failed to update student with Write-Through strategy: Cache operation failed", e);
        } catch (Exception e) {
            log.error("Write-Through strategy failed during update for student ID: {}", student.getId(), e);
            throw new RuntimeException("Failed to update student with Write-Through strategy: Database operation failed", e);
        }
    }

    @Override
    public void deleteStudent(Long studentId) {
        log.debug("Deleting student using Write-Through strategy for ID: {}", studentId);

        try {
            // 1. Delete from database (within transaction)
            studentRepository.deleteById(studentId);
            log.debug("Student deleted from database with ID: {}", studentId);

            // 2. Remove from cache using strict methods
            cacheHelper.removeStudentFromCacheStrict(studentId);
            log.debug("Student removed from cache with ID: {}", studentId);

            // 3. Invalidate list cache
            cacheHelper.invalidateAllStudentsCacheStrict();
            log.debug("All students cache invalidated successfully");

            log.info("Student deleted successfully using Write-Through strategy with ID: {}", studentId);

        } catch (CacheException e) {
            log.error("Write-Through strategy failed during deletion due to cache error - transaction will rollback for student ID: {}",
                    studentId, e);
            throw new RuntimeException("Failed to delete student with Write-Through strategy: Cache operation failed", e);
        } catch (Exception e) {
            log.error("Write-Through strategy failed during deletion for student ID: {}", studentId, e);
            throw new RuntimeException("Failed to delete student with Write-Through strategy: Database operation failed", e);
        }
    }

    @Override
    public String getStrategyName() {
        return "WRITE_THROUGH";
    }

    @Override
    public boolean isStrategyAvailable() {
        boolean available = cacheHelper.isCacheAvailable();
        log.debug("Write-Through strategy availability check: {}", available);
        return available;
    }
}
