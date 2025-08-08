package com.project.cache.services;

import com.project.cache.helper.CacheHelper;
import com.project.cache.model.dto.StudentDTO;
import com.project.cache.model.Student;
import com.project.cache.model.Subject;
import com.project.cache.repository.StudentRepository;
import com.project.cache.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Slf4j
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    private final SubjectRepository subjectRepository;

    private final CacheHelper cacheHelper;

    @Transactional
    public Student createStudent(StudentDTO studentDTO) {
        log.info("Creating student with email: {}", studentDTO.getEmail());

        Student student = new Student();
        student.setEmail(studentDTO.getEmail());
        student.setName(studentDTO.getName());
        student.setDepartment(studentDTO.getDepartment());

        List<Subject> subjects = subjectRepository.findAllById(studentDTO.getSubjectIds());
        student.setSubjects(subjects);

        for (Subject subject : subjects) {
            if (subject.getStudents() == null) {
                subject.setStudents(new ArrayList<>());
            }
            subject.getStudents().add(student);
        }

        student = studentRepository.save(student);

        // Cache with consistent key pattern
        cacheHelper.cacheStudent(student);
        cacheHelper.invalidateAllStudentsCache();

        return student;
    }

    @Transactional
    public Student updateStudent(Long id, StudentDTO dto) {
        log.debug("Updating student with id: {}", id);

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (dto.getName() != null) student.setName(dto.getName());
        if (dto.getEmail() != null) student.setEmail(dto.getEmail());
        if (dto.getDepartment() != null) student.setDepartment(dto.getDepartment());

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

        student = studentRepository.save(student);

        // Update cache
        cacheHelper.cacheStudent(student);
        cacheHelper.invalidateAllStudentsCache();

        return student;

    }

    public List<Student> findAllStudents() {

        try {
            List<Student> cached = cacheHelper.getAllStudentsFromCache();
            if (!cached.isEmpty()) {
                log.debug("Students list from cache is Empty");
                return cached;
            }
        } catch (Exception e) {
            log.warn("Failed to retrieve students from cache", e);
        }

        List<Student> students = studentRepository.findAllWithSubjects();
        cacheHelper.addStudentsToCache(students);
        return students;
    }

    public Student findStudentByStudentId(Long id) {
        Student student = cacheHelper.getStudentFromCacheById(id);
        if (student != null) return student;
        student =  studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        cacheHelper.updateStudentInCache(student);
        return student;
    }
}
