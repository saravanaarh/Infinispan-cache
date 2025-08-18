package com.project.cache.helper.impl;

import com.project.cache.config.InfinispanConfig;
import com.project.cache.exception.CacheException;
import com.project.cache.helper.CacheHelper;
import com.project.cache.model.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.project.cache.helper.Constants.ALL_STUDENTS_KEY;
import static com.project.cache.helper.Constants.STUDENT_KEY_PREFIX;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheHelperImpl implements CacheHelper {

    private final InfinispanConfig infinispanConfig;

    // ===============================================
    // BEST-EFFORT METHODS (for Cache-Aside and Async)
    // ===============================================

    @Override
    public void cacheStudent(Student student) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + student.getId();
            cache.put(key, student);
            log.debug("Cached student with id: {}", student.getId());
        } catch (Exception e) {
            log.warn("Failed to cache student with id: {} - continuing gracefully", student.getId(), e);
            // Don't throw - best effort for Cache-Aside and Async strategies
        }
    }

    @Override
    public void updateStudentInCache(Student student) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + student.getId();
            cache.put(key, student);
            log.debug("Updated student in cache with id: {}", student.getId());
        } catch (Exception e) {
            log.warn("Failed to update student in cache with id: {} - continuing gracefully", student.getId(), e);
        }
    }

    @Override
    public Student getStudentFromCacheById(Long id) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + id;
            Student student = (Student) cache.get(key);
            if (student != null) {
                log.debug("Retrieved student from cache with id: {}", id);
            }
            return student;
        } catch (Exception e) {
            log.warn("Failed to retrieve student from cache with id: {} - returning null", id, e);
            return null;
        }
    }

    @Override
    public void removeStudentFromCache(Long id) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + id;
            cache.remove(key);
            log.debug("Removed student from cache with id: {}", id);
        } catch (Exception e) {
            log.warn("Failed to remove student from cache with id: {} - continuing gracefully", id, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Student> getAllStudentsFromCache() {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            List<Student> students = (List<Student>) cache.get(ALL_STUDENTS_KEY);
            if (students == null) {
                log.debug("No students found in cache, returning empty list");
                return new ArrayList<>();
            }
            log.debug("Retrieved {} students from cache", students.size());
            return students;
        } catch (Exception e) {
            log.warn("Failed to retrieve all students from cache - returning empty list", e);
            return new ArrayList<>();
        }
    }

    @Override
    public void addStudentsToCache(List<Student> students) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();

            // Cache individual students
            for (Student student : students) {
                String key = STUDENT_KEY_PREFIX + student.getId();
                cache.put(key, student);
            }

            // Cache the complete list
            cache.put(ALL_STUDENTS_KEY, students);
            log.debug("Cached all students with size: {}", students.size());
        } catch (Exception e) {
            log.warn("Failed to cache students list with size: {} - continuing gracefully", students.size(), e);
        }
    }

    @Override
    public void invalidateAllStudentsCache() {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            cache.remove(ALL_STUDENTS_KEY);
            log.debug("Invalidated all students cache");
        } catch (Exception e) {
            log.warn("Failed to invalidate all students cache - continuing gracefully", e);
        }
    }

    // ===============================================
    // STRICT METHODS (for WriteThrough and FailFast)
    // ===============================================

    @Override
    public void cacheStudentStrict(Student student) throws CacheException {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + student.getId();
            cache.put(key, student);
            log.debug("Cached student with id: {} (strict mode)", student.getId());
        } catch (Exception e) {
            log.error("Failed to cache student with id: {} (strict mode)", student.getId(), e);
            throw new CacheException("Failed to cache student with id: " + student.getId(), e);
        }
    }

    @Override
    public void updateStudentInCacheStrict(Student student) throws CacheException {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + student.getId();
            cache.put(key, student);
            log.debug("Updated student in cache with id: {} (strict mode)", student.getId());
        } catch (Exception e) {
            log.error("Failed to update student in cache with id: {} (strict mode)", student.getId(), e);
            throw new CacheException("Failed to update student in cache with id: " + student.getId(), e);
        }
    }

    @Override
    public void removeStudentFromCacheStrict(Long id) throws CacheException {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + id;
            cache.remove(key);
            log.debug("Removed student from cache with id: {} (strict mode)", id);
        } catch (Exception e) {
            log.error("Failed to remove student from cache with id: {} (strict mode)", id, e);
            throw new CacheException("Failed to remove student from cache with id: " + id, e);
        }
    }

    @Override
    public void invalidateAllStudentsCacheStrict() throws CacheException {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            cache.remove(ALL_STUDENTS_KEY);
            log.debug("Invalidated all students cache (strict mode)");
        } catch (Exception e) {
            log.error("Failed to invalidate all students cache (strict mode)", e);
            throw new CacheException("Failed to invalidate all students cache", e);
        }
    }

    // ===============================================
    // UTILITY METHODS
    // ===============================================

    @Override
    public boolean isCacheAvailable() {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();

            // More robust health check with actual read/write test
            String testKey = "health_test_" + Thread.currentThread().getId() + "_" + System.currentTimeMillis();
            cache.put(testKey, "test_value", 5, TimeUnit.SECONDS);
            boolean exists = cache.containsKey(testKey);
            cache.remove(testKey); // Immediate cleanup

            log.debug("Cache health check completed successfully");
            return exists;

        } catch (Exception e) {
            log.error("Cache is not available: {}", e.getMessage());
            return false;
        }
    }
}
