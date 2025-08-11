package com.project.cache.services;

import com.project.cache.model.Subject;
import com.project.cache.model.dto.SubjectDTO;

import java.util.List;

public interface SubjectServiceI {

    Subject createSubject(SubjectDTO dto);

    List<Subject> findAll();

}
