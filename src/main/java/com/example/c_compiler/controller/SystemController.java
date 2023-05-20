package com.example.c_compiler.controller;

import com.example.c_compiler.domain.entity.SystemTree;
import com.example.c_compiler.service.SystemService;
import com.example.c_compiler.domain.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/system")
public class SystemController {
    @Autowired
    private SystemService systemService;

    /**
     * 获取文件树信息
     * @return AjaxResult
     */
    @GetMapping("/nodelist")
    public AjaxResult getNodeList(){
        try {
            return AjaxResult.success(systemService.list());
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 获取表项个数
     * @return AjaxResult
     */
    @GetMapping("/nodecount")
    public AjaxResult getNodeCount(){
        try {
            return AjaxResult.success(systemService.count());
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 插入新表项
     */
    @PostMapping("/nodeinsert")
    public AjaxResult insertNode(@RequestBody SystemTree newnode){
        try {
            systemService.insert(newnode);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/nodedelete")
    public AjaxResult deleteNode(@RequestBody SystemTree node){
        try {
            Integer id = node.getId();
            systemService.delete(id);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/noderename")
    public AjaxResult renameNode(@RequestBody SystemTree node){
        try {
            Integer id = node.getId();
            String label = node.getLabel();
            systemService.rename(id, label);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PostMapping("/savefile")
    public AjaxResult saveFile(@RequestBody SystemTree node){
        try {
            Integer id = node.getId();
            String fileContent = node.getFileContent();
            systemService.updateFileContent(id, fileContent);
            return AjaxResult.success();
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }
}
