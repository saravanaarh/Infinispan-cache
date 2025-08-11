package com.project.cache.services.impl;

import com.project.cache.helper.CacheHelper;
import com.project.cache.model.Student;
import com.project.cache.model.Subject;
import com.project.cache.model.dto.StudentDTO;
import com.project.cache.repository.StudentRepository;
import com.project.cache.repository.SubjectRepository;
import com.project.cache.services.StudentService;
import com.project.cache.strategy.CacheStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final CacheHelper cacheHelper;

    @Lazy
    private final CacheStrategy primaryCacheStrategy; // Injected bean from configuration

    @Override
    @Transactional
    public Student createStudent(StudentDTO studentDTO) {
        log.info("Creating student with email: {} using strategy: {}",
                studentDTO.getEmail(), primaryCacheStrategy.getStrategyName());

        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new IllegalArgumentException("Student with email " + studentDTO.getEmail() + " already exists");
        }

        Student student = buildStudentFromDTO(studentDTO);

        Student savedStudent = primaryCacheStrategy.saveStudent(student);

        log.info("Student created successfully with ID: {} using strategy: {}",
                savedStudent.getId(), primaryCacheStrategy.getStrategyName());
        return savedStudent;
    }

    @Override
    @Transactional
    public Student updateStudent(Long id, StudentDTO dto) {
        log.debug("Updating student with id: {} using strategy: {}", id, primaryCacheStrategy.getStrategyName());

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Update fields with validation
        updateStudentFields(student, dto);

        // Execute using configured strategy
        Student updatedStudent = primaryCacheStrategy.updateStudent(student);

        log.debug("Student updated successfully with id: {} using strategy: {}",
                id, primaryCacheStrategy.getStrategyName());
        return updatedStudent;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Student> findAllStudents() {
        // Read operations use cache-aside pattern
        try {
            List<Student> cached = cacheHelper.getAllStudentsFromCache();
            if (cached != null && !cached.isEmpty()) {
                log.debug("Retrieved {} students from cache", cached.size());
                return cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve students from cache", e);
        }

        // Fetch from database
        List<Student> students = studentRepository.findAllWithSubjects();

        // Cache the result (best-effort)
        try {
            cacheHelper.addStudentsToCache(students);
        } catch (Exception e) {
            log.warn("Failed to cache students list", e);
        }

        log.debug("Retrieved {} students from database", students.size());
        return students;
    }

    @Override
    @Transactional(readOnly = true)
    public Student findStudentById(Long id) {
        // Read operations use cache-aside pattern
        try {
            Student student = cacheHelper.getStudentFromCacheById(id);
            if (student != null) {
                log.debug("Retrieved student from cache with id: {}", id);
                return student;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve student from cache with id: {}", id, e);
        }

        // Fetch from database
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Cache the result (best-effort)
        try {
            cacheHelper.updateStudentInCache(student);
        } catch (Exception e) {
            log.warn("Failed to cache student with id: {}", id, e);
        }

        return student;
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Deleting student with id: {} using strategy: {}", id, primaryCacheStrategy.getStrategyName());

        // Verify student exists
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found");
        }

        // Execute using configured strategy
        primaryCacheStrategy.deleteStudent(id);

        log.info("Student deleted successfully with id: {} using strategy: {}",
                id, primaryCacheStrategy.getStrategyName());
    }

    private Student buildStudentFromDTO(StudentDTO dto) {
        Student student = new Student();
        student.setEmail(dto.getEmail());
        student.setName(dto.getName());
        student.setDepartment(dto.getDepartment());

        // Handle subjects
        if (dto.getSubjectIds() != null && !dto.getSubjectIds().isEmpty()) {
            List<Subject> subjects = subjectRepository.findAllById(dto.getSubjectIds());
            if (subjects.size() != dto.getSubjectIds().size()) {
                throw new IllegalArgumentException("Some subject IDs are invalid");
            }

            student.setSubjects(subjects);
            for (Subject subject : subjects) {
                if (subject.getStudents() == null) {
                    subject.setStudents(new ArrayList<>());
                }
                subject.getStudents().add(student);
            }
        }

        return student;
    }

    private void updateStudentFields(Student student, StudentDTO dto) {
        if (dto.getName() != null) {
            student.setName(dto.getName());
        }

        // Email validation
        if (dto.getEmail() != null) {
            if (!dto.getEmail().equals(student.getEmail())) {
                if (studentRepository.existsByEmail(dto.getEmail())) {
                    throw new IllegalArgumentException("Email '" + dto.getEmail() + "' is already in use");
                }
                student.setEmail(dto.getEmail());
            }
        }

        if (dto.getDepartment() != null) {
            student.setDepartment(dto.getDepartment());
        }

        // Handle subject updates
        if (dto.getSubjectIds() != null) {
            // Clear existing relationships
            for (Subject subject : student.getSubjects()) {
                subject.getStudents().remove(student);
            }

            List<Subject> subjects = subjectRepository.findAllById(dto.getSubjectIds());
            student.setSubjects(subjects);

            for (Subject subject : subjects) {
                if (!subject.getStudents().contains(student)) {
                    subject.getStudents().add(student);
                }
            }
        }
    }
}
