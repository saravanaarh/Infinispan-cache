package com.project.cache.services.impl;

import com.project.cache.model.Subject;
import com.project.cache.model.dto.SubjectDTO;
import com.project.cache.repository.SubjectRepository;
import com.project.cache.services.SubjectServiceI;

import java.util.List;

public class SubjectServiceImpl implements SubjectServiceI {

    private final SubjectRepository subjectRepository;

    public SubjectServiceImpl(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

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
