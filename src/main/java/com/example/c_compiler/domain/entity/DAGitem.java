package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
public class DAGitem {
    public boolean useful;
    public boolean isleaf;
    public String value;
    public String op;
    public List<String> label;
    public int parent;
    public int left_child;
    public int right_child;
    public int tri_child;
    public boolean isremain;
    public TASitem code;

    public DAGitem() {
        this.useful = false;
        this.isleaf = true;
        this.value = "";
        this.op = "";
        this.label = new ArrayList<>();
        this.parent = -1;
        this.left_child = -1;
        this.right_child = -1;
        this.tri_child = -1;
        this.isremain = false;
        this.code = new TASitem();
    }


    public boolean equals(DAGitem b) {
        boolean f1 = this.isleaf == b.isleaf;
        boolean f2 = Objects.equals(this.value, b.value);
        boolean f3 = Objects.equals(this.op, b.op);
        boolean f4 = this.label.size() == b.label.size();
        boolean f5 = this.parent == b.parent;
        boolean f6 = this.left_child == b.left_child;
        boolean f7 = this.right_child == b.right_child;
        boolean f8 = true;
        for (int i = 0; i < this.label.size() && i < b.label.size(); i++) {
            if (!Objects.equals(this.label.get(i), b.label.get(i))) {
                f8 = false;
                break;
            }
        }
        return f1 && f2 && f3 && f4 && f5 && f6 && f7 && f8;
    }
}
