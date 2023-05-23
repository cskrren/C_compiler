package com.example.c_compiler.utils;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class globalUtils {

    public static final String GRAMMARFILE = "grammar.txt";
    public static final String STACK = "stack";
    public static final String DATA = "data";
    public static final String TEMP = "temp";
    public static final int stack_size = 128;

    public enum state {
        INIT,
        SINGLEANNOTATION,
        MULTIANNOTATION,
        NUMBER,
        OPERATOR,
        STRING,
        ERROR
    }

    public enum actionStatus {
        ACTION_ERROR,
        ACTION_STATE,
        ACTION_REDUCTION,
        ACTION_ACC
    }

    public enum type {
        INT,
        VOID
    }

    public enum kind {
        VAR,
        FUNC,
        ARRAY
    }

    public static void errorLog(String err) {
        try {
            BufferedWriter fout = new BufferedWriter(new FileWriter("compile_log", true));
            fout.write(err);
            fout.close();
            throw new CustomException(err);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeDuplicates(List<String> v) {
        Collections.sort(v);
        List<String> uniqueList = v.stream().distinct().collect(Collectors.toList());
        v.clear();
        v.addAll(uniqueList);
    }

    public static boolean isNum(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static char getChar(String s, int pos) {
        if(s.length() <= pos) {
            return '\0';
        }
        return s.charAt(pos);
    }

}
