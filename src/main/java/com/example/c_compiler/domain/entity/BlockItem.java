package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class BlockItem {
    public int begin;
    public int end;
    public List<String> wait_variable;
    public List<String> useless_variable;
    public List<String> active_variable;

    public BlockItem() {
        this.begin = 0;
        this.end = 0;
        this.wait_variable = new ArrayList<>();
        this.useless_variable = new ArrayList<>();
        this.active_variable = new ArrayList<>();
    }
}