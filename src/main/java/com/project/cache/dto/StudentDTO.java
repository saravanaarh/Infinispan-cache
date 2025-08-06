package com.project.cache.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {
    public String department;
    private String name;
    private String email;
    private List<Long> subjectIds;
}
