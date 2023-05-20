package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemNode {
    private Integer id;
    private String label;
    private String type;
    private List<SystemNode> children;
    private String fileContent;
}
