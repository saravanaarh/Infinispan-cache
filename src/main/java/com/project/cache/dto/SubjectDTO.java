package com.project.cache.dto;

import lombok.*;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {
    private String name;
    private Integer chapters;
}
