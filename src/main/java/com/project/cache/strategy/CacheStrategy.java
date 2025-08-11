package com.project.cache.strategy;

import com.project.cache.model.Student;

public interface CacheStrategy {

    /**
     * Save student with the specific caching strategy
     */
    Student saveStudent(Student student);

    /**
     * Update student with the specific caching strategy
     */
    Student updateStudent(Student student);

    /**
     * Delete student with the specific caching strategy
     */
    void deleteStudent(Long studentId);

    /**
     * Get strategy name for identification
     */
    String getStrategyName();

    /**
     * Check if strategy is available (cache is working)
     */
    boolean isStrategyAvailable();
}

