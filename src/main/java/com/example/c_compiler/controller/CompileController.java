package com.example.c_compiler.controller;

import com.example.c_compiler.domain.AjaxResult;
import com.example.c_compiler.domain.entity.*;
import com.example.c_compiler.service.CompileService;
import com.example.c_compiler.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CompileController {
    @Autowired
    private CompileService compileService;

    @Autowired
    private SystemService systemService;

    @PostMapping("/compile")
    public AjaxResult compile(@RequestBody CompileInput input) {
        String code = input.getCode();
        String mips_code = compileService.compile(code);
        if (mips_code.substring(0, 5).equals("ERROR")) {
            return AjaxResult.error(mips_code);
        }
        return AjaxResult.success(mips_code);
    }

    @PostMapping("/saveresult")
    public AjaxResult saveResult(@RequestBody ResultSave result) {
        Integer fileId = result.getFileId();
        SystemTree bro = systemService.getNodeById(fileId);
        SystemTree node = new SystemTree();
        node.setId(result.getId());
        node.setFatherId(bro.getFatherId());
        node.setLabel(result.getLabel());
        node.setType(result.getType());
        node.setFileContent(result.getFileContent());
        systemService.insert(node);
        return AjaxResult.success();
    }

    @GetMapping("/getSymbolTable")
    public AjaxResult getSymbolTable() {
        String TableContent = compileService.getSymbolTable();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getTokenList")
    public AjaxResult getTokenList() {
        String TableContent = compileService.getTokenList();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getLR1")
    public AjaxResult getLR1() {
        String TableContent = compileService.getLR1();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getSyntaxTree")
    public AjaxResult getSyntaxTree() {
        SyntaxTreeNode root= compileService.getSyntaxTree();
        return AjaxResult.success(root);
    }

    @GetMapping("/getIntermediateCode")
    public AjaxResult getIntermediateCode() {
        String TableContent = compileService.getIntermediateCode();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getOptimizedCode")
    public AjaxResult getOptimizedCode() {
        String TableContent = compileService.getOptimizedCode();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getMessageTableHistory")
    public AjaxResult getMessageTableHistory() {
        String TableContent = compileService.getMessageTableHistory();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getAnalysisHistory")
    public AjaxResult getAnalysisHistory() {
        String TableContent = compileService.getAnalysisHistory();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getMipsCode")
    public AjaxResult getMipsCode() {
        String TableContent = compileService.getMipsCode();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getDerivation")
    public AjaxResult getDerivation() {
        String TableContent = compileService.getDerivation();
        return AjaxResult.success(TableContent);
    }

    @GetMapping("/getOptimizeRate")
    public AjaxResult getOptimizeRate() {
        double ratio = compileService.getOptimizeRate();
        return AjaxResult.success(ratio);
    }
}
