package com.project.cache.helper;

import com.project.cache.config.InfinispanConfig;
import com.project.cache.model.Student;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.project.cache.helper.Constants.ALL_STUDENTS_KEY;
import static com.project.cache.helper.Constants.STUDENT_KEY_PREFIX;

@Component
@Slf4j
public class CacheHelper {

    @Autowired
    private InfinispanConfig infinispanConfig;

    public void cacheStudent(Student student) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + student.getId();
            cache.put(key, student);
            log.debug("Cached student with id: {}", student.getId());
        } catch (Exception e) {
            log.warn("Failed to cache student with id: {}", student.getId(), e);
        }
    }

    public void removeStudentFromCache(Long id) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + id;
            cache.remove(key);
            log.debug("Removed student from cache with id: {}", id);
        } catch (Exception e) {
            log.warn("Failed to remove student from cache with id: {}", id, e);
        }
    }

    public void invalidateAllStudentsCache() {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            cache.remove(ALL_STUDENTS_KEY);
            log.debug("Invalidated all students cache");
        } catch (Exception e) {
            log.warn("Failed to invalidate all students cache", e);
        }
    }

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
            log.warn("Failed to retrieve all students from cache", e);
            return new ArrayList<>();
        }
    }

    public void addStudentsToCache(List<Student> students) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            for (Student student : students) {
                String key = STUDENT_KEY_PREFIX + student.getId();
                cache.put(key, student);
                log.debug("Cached student with id: {}", student.getId());
            }
            cache.put(ALL_STUDENTS_KEY, students);
            log.debug("Cached all students with size: {}", students.size());
        } catch (Exception e) {
            log.warn("Failed to cache students", e);
        }
    }

    public Student getStudentFromCacheById(Long id) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + id;
            Student student = (Student) cache.get(key);
            if (student != null) {
                log.debug("Retrieved student from cache with id: {}", id);
                return student;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve student from cache with id: {}", id, e);
        }
        return null;
    }

    public void updateStudentInCache(Student student) {
        try {
            Cache<String, Object> cache = infinispanConfig.getCache();
            String key = STUDENT_KEY_PREFIX + student.getId();
            cache.put(key, student);
            log.debug("Updated student in cache with id: {}", student.getId());
        } catch (Exception e) {
            log.warn("Failed to update student in cache with id: {}", student.getId(), e);
        }
    }
}
