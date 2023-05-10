package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Closure {

    public List<Canonical> set;
    public Map<String, Integer> next;

    public Closure() {
        this.set = new ArrayList<>();
        this.next = new LinkedHashMap<>();
    }

}