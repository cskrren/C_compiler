package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class TASitem {
    public String op;
    public String arg1;
    public String arg2;
    public String result;

    public TASitem() {
        this.op = "";
        this.arg1 = "";
        this.arg2 = "";
        this.result = "";
    }

    public TASitem(TASitem item) {
        this.op = item.op;
        this.arg1 = item.arg1;
        this.arg2 = item.arg2;
        this.result = item.result;
    }
}
