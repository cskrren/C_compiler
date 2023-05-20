package com.example.c_compiler.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemTree {
    /**
     * 目录项id
     */
    @TableId(type = IdType.AUTO)
    @TableField("id")
    private Integer id;
    /**
     * 目录项名称
     */
    @TableField("label")
    private String label;
    /**
     * 目录项父id
     */
    @TableField("father_id")
    private Integer fatherId;
    /**
     * 目录项属性
     */
    @TableField("type")
    private String type;
    /**
     * 文件内容
     */
    @TableField("file_content")
    private String fileContent;
}
