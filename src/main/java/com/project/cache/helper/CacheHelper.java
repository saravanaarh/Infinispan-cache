// helper/CacheHelper.java
package com.project.cache.helper;

import com.project.cache.exception.CacheException;
import com.project.cache.model.Student;
import java.util.List;

public interface CacheHelper {

    // Best-effort methods (for Cache-Aside and Async strategies)
    void cacheStudent(Student student);
    void updateStudentInCache(Student student);
    Student getStudentFromCacheById(Long id);
    void removeStudentFromCache(Long id);
    List<Student> getAllStudentsFromCache();
    void addStudentsToCache(List<Student> students);
    void invalidateAllStudentsCache();

    // Strict methods (for WriteThrough and FailFast strategies)
    void cacheStudentStrict(Student student) throws CacheException;
    void updateStudentInCacheStrict(Student student) throws CacheException;
    void removeStudentFromCacheStrict(Long id) throws CacheException;
    void invalidateAllStudentsCacheStrict() throws CacheException;

    // Utility methods
    boolean isCacheAvailable();
}
