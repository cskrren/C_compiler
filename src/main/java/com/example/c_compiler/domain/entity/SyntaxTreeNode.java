package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SyntaxTreeNode {
    Integer id;
    String label;
    List<SyntaxTreeNode> children;

    public SyntaxTreeNode() {
        this.id = -1;
        this.label = "";
        this.children = new ArrayList<>();
    }
}
