package com.project.cache.model.dto;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {
    private String name;
    private Integer chapters;
}
