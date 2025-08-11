// service/StudentService.java
package com.project.cache.services;

import com.project.cache.model.Student;
import com.project.cache.model.dto.StudentDTO;
import java.util.List;

public interface StudentService {

    /**
     * Create a new student
     */
    Student createStudent(StudentDTO studentDTO);

    /**
     * Update existing student
     */
    Student updateStudent(Long id, StudentDTO dto);

    /**
     * Find all students
     */
    List<Student> findAllStudents();

    /**
     * Find student by ID
     */
    Student findStudentById(Long id);

    /**
     * Delete student by ID
     */
    void deleteStudent(Long id);
}
