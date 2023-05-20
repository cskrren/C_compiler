package com.example.c_compiler.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.c_compiler.domain.entity.SystemTree;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SystemTreeMapper extends BaseMapper<SystemTree> {
    @Insert("insert into system_tree (id, label, father_id, type, file_content) values (#{id}, #{label}, #{fatherId}, #{type}, #{fileContent})")
    public void insertNode(Integer id, String label, Integer fatherId, String type, String fileContent);

    @Update("update system_tree set label=#{label}, father_id=#{fatherId}, type=#{type}, file_content=#{fileContent} where id=#{id}")
    public void updateNodeById(Integer id, String label, Integer fatherId, String type, String fileContent);

    @Update("update system_tree set label=#{label} where id=#{id}")
    public void updateLabelById(Integer id, String label);

    @Delete("delete from system_tree where id=#{id}")
    public void deleteNodeById(Integer id);

    @Update("update system_tree set file_content=#{fileContent} where id=#{id}")
    public void updateFileContentById(Integer id, String fileContent);

    @Select("select * from system_tree where id=#{id}")
    public SystemTree selectNodeById(Integer id);
}
