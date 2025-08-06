package com.project.cache.services;

import com.project.cache.dto.SubjectDTO;
import com.project.cache.model.Student;
import com.project.cache.model.Subject;
import com.project.cache.repository.StudentRepository;
import com.project.cache.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    private final StudentRepository studentRepository;

    public Subject createSubject(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setName(dto.getName());
        subject.setChapters(dto.getChapters());

        return subjectRepository.save(subject);
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }
}
