package com.example.c_compiler;

import com.example.c_compiler.domain.entity.ObjectCodeGenerator;
import com.example.c_compiler.domain.entity.OptimizerAnalysis;
import com.example.c_compiler.domain.entity.SyntaxAnalysis;
import com.example.c_compiler.utils.globalUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;

@SpringBootApplication
public class
CCompilerApplication {

    public static void main(String[] args) {
        try {
            String c_filepath, c_code;
            System.out.println("Please input the C file:");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            c_filepath = reader.readLine();
            FileReader fileReader = new FileReader(c_filepath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringWriter stringWriter = new StringWriter();
            BufferedWriter bufferedWriter = new BufferedWriter(stringWriter);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedReader.close();
            bufferedWriter.close();
            c_code = stringWriter.toString();
            System.out.println(c_code);

            // 中间代码生成
            System.out.println("Generate intermediate code ...");
            SyntaxAnalysis syntax = new SyntaxAnalysis();
            syntax.initializeLR1();
            syntax.getInput(c_code);
            syntax.analysis();

            // 中间代码优化
            System.out.println("Optimize intermediate code ...");
            OptimizerAnalysis optimizer = new OptimizerAnalysis(syntax.L.nameTable, syntax.S.global_table, syntax.S.intermediate_code);
            double opt_rate = optimizer.analysis();

            // 目标代码生成
            System.out.println("Optimize code");
            ObjectCodeGenerator mips_generator = new ObjectCodeGenerator(optimizer.intermediate_code, optimizer.block_group, globalUtils.stack_size);
            mips_generator.getObjectCode();

            // 生成可执行文件
            System.out.println("Generate an executable file");
            String mips_filepath = c_filepath.substring(0, c_filepath.lastIndexOf("."));
            mips_filepath += ".s";
            FileWriter fileWriter = new FileWriter(mips_filepath);
            BufferedWriter writer = new BufferedWriter(fileWriter);
            for (int i = 0; i < mips_generator.object_code.size(); i++) {
                writer.write(mips_generator.object_code.get(i));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //SpringApplication.run(CCompilerApplication.class, args);
    }

}
