package com.project.cache.services;

import com.project.cache.dto.StudentDTO;
import com.project.cache.model.Student;
import com.project.cache.model.Subject;
import com.project.cache.repository.StudentRepository;
import com.project.cache.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    private final SubjectRepository subjectRepository;

    public Student createStudent(StudentDTO studentDTO) {
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

        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, StudentDTO dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (dto.getName() != null) student.setName(dto.getName());
        if (dto.getSubjectIds() != null) {
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

        return studentRepository.save(student);
    }

    public List<Student> findAllStudents() {
        return studentRepository.findAllWithSubjects();
    }

    public Student findStudentByStudentId(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
    }
}
