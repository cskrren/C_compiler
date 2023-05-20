package com.example.c_compiler;

import com.example.c_compiler.domain.entity.ObjectCodeGenerator;
import com.example.c_compiler.domain.entity.OptimizerAnalysis;
import com.example.c_compiler.domain.entity.SyntaxAnalysis;
import com.example.c_compiler.utils.globalUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;

@SpringBootApplication
@MapperScan("com.example.c_compiler.domain.mapper")
public class CCompilerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CCompilerApplication.class, args);
    }

}