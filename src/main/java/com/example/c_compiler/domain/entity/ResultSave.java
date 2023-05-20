package com.example.c_compiler.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultSave {

    private Integer id;
    private String label;
    private Integer fileId;
    private String type;
    private String fileContent;
}
