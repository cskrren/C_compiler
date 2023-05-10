package com.example.c_compiler.domain.entity;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Canonical {
    public Pair<String, List<String>> p;
    public int dot;
    public List<String> expect;

    public Canonical() {
        this.p = new Pair<String, List<String>>("", null);
        this.dot = 0;
        this.expect = new ArrayList<>();
    }

    public boolean equals(Canonical c) {
        if (this.p.equals(c.p) && this.dot == c.dot && this.expect.equals(c.expect)) {
            return true;
        } else {
            return false;
        }
    }
}