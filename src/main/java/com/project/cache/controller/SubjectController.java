package com.project.cache.controller;

import com.project.cache.model.dto.SubjectDTO;
import com.project.cache.model.Subject;
import com.project.cache.services.impl.SubjectServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/subjects")
public class SubjectController {

    private SubjectServiceImpl subjectService;

    public SubjectController(SubjectServiceImpl subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping
    public Subject createSubject(@RequestBody SubjectDTO dto) {
        return subjectService.createSubject(dto);
    }

    @GetMapping
    public List<Subject> getSubjects() {
        return subjectService.findAll();
    }
}
