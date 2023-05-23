package com.example.c_compiler.service;

import com.example.c_compiler.domain.entity.*;
import com.example.c_compiler.utils.CustomException;
import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompileService {

    private Integer TreeIndex = 0;
    public Map<Integer, String> SymbolTable;
    public List<Pair<String, Integer>> TokenList;
    public Map<Integer, Closure> LR1;
    public Map<Integer, List<ActionItem>> ACTION;
    public Map<Integer, List<Integer>> GOTO;
    public List<List<List<String>>> derivation;
    public TreeNode reductionTreeRoot;
    public List<TASitem> intermediate_code;
    public List<TASitem> optimized_code;
    public double optimize_rate;
    public List<MessageTableItem> messageTableHistory;
    public List<AnalysisHistoryItem> analysisHistory;
    public String mips_code;

    public String getSymbolTable() {
        String ret = "符号表：\n";
        ret += "序号" + "\t" + "标识符\n";
        for (Map.Entry<Integer, String> entry : SymbolTable.entrySet()) {
            ret += entry.getKey() + "\t" + entry.getValue() + "\n";
        }
        return ret;
    }

    public String getTokenList() {
        String ret = "词法单元序列：\n";
        ret += "序号" + "\t" + "Token\n";
        for (Pair<String, Integer> item : TokenList) {
            ret += item.getValue() + "\t" + item.getKey() + "\n";
        }
        return ret;
    }

    public String getLR1() {
        String ret = "LR1项目集簇：\n";
        for (Map.Entry<Integer, Closure> p : LR1.entrySet()) {
            ret += "(" + p.getKey() + ")\n";
            ret += p.getValue().showCLOSURE();
        }
        return ret;
    }

    public String getDerivation() {
        String ret = "推导过程：\n";
        for (int i = 0; i < derivation.size(); i++) {
            ret += "----------------" + "第" + i + "步" + "-------------------\n";
            for (int j = 0; j < derivation.get(i).size(); j++) {
                for (int k = 0; k < derivation.get(i).get(j).size(); k++) {
                    ret += derivation.get(i).get(j).get(k) + " ";
                }
                ret += "\n";
            }
            ret += "-----------------------------------------\n";
        }
        return ret;
    }

    public SyntaxTreeNode dfs(TreeNode nownode) {
        SyntaxTreeNode ret = new SyntaxTreeNode();
        ret.setId(TreeIndex++);
        ret.setLabel(nownode.content.getKey());
        for (TreeNode child : nownode.children) {
            ret.getChildren().add(dfs(child));
        }
        return ret;
    }
    //只获取id，label和child属性，其他不要
    public SyntaxTreeNode getSyntaxTree() {
        return dfs(reductionTreeRoot);
    }

    public String getIntermediateCode() {
        String ret = "中间代码：\n";
        Integer cnt = 1;
        for (TASitem item : intermediate_code) {
            ret += "(" + cnt++ + ")" + item.toString() + "\n";
        }
        return ret;
    }

    public String getOptimizedCode() {
        String ret = "优化后的中间代码：\n";
        Integer cnt = 1;
        for (TASitem item : optimized_code) {
            ret += "(" + cnt++ + ")" + item.toString() + "\n";
        }
        return ret;
    }
    public String getMessageTableHistory() {
        String ret = "寄存器分配：\n";
        for (int ino = 0; ino < analysisHistory.size(); ino++) {
            AnalysisHistoryItem e = analysisHistory.get(ino);
            ret += "(" + ino + ")";
            ret += e.TAS.op + "\t" + e.TAS.arg1 + "\t" + e.TAS.arg2 + "\t" + e.TAS.result + "\n";
            ret += "********OBJECT_CODE********\n";
            for (int i = 0; i < e.object_codes.size(); i++) {
                ret += e.object_codes.get(i) + '\t';
            }
            ret += "\n********RVALUE********\n";
            for (Map.Entry<String, List<Pair<String, Integer>>> entry : e.RVALUE.entrySet()) {
                String key = entry.getKey();
                List<Pair<String, Integer>> values = entry.getValue();
                ret += key + "\t";
                for (int i = 0; i < values.size(); i++) {
                    Pair<String, Integer> pair = values.get(i);
                    ret += pair.getKey() + ":" + pair.getValue() + "\t";
                }
                ret += "\n";
            }
            ret += "********AVALUE********\n";
            for (Map.Entry<String, List<String>> entry : e.AVALUE.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                ret += key + "  ";
                for (int i = 0; i < values.size(); i++) {
                    ret += values.get(i) + "  ";
                }
            }
            ret += "\n\n";
        }
        return ret;
    }

    public String show_tag(Pair<Integer,Boolean> tag) {
        if (tag.getValue()) {
            if(tag.getKey() == 2147483647) {
                return "(^,y)";
            } else {
                return "("+ tag.getKey().toString() +",y)";
            }
        } else {
            return "(^,^)";
        }

    }

    public String getAnalysisHistory() {
        String ret = "待用活跃信息表：\n";
        for (int tno = 0; tno < messageTableHistory.size(); tno++) {
            MessageTableItem message_table = messageTableHistory.get(tno);
            ret += "(" + tno + ") " + message_table.TAS.op + '\t' + message_table.TAS.arg1 + '\t' + message_table.TAS.arg2 + '\t' + message_table.TAS.result;
            ret += show_tag(message_table.result_tag) + '\t' + show_tag(message_table.arg1_tag) + '\t' + show_tag(message_table.arg2_tag) + '\n';
        }
        return ret;
    }

    public String getMipsCode() {
        return mips_code;
    }

    public double getOptimizeRate() {
        return optimize_rate;
    }
    public String compile(String code) {
        try {
            // 中间代码生成
            System.out.println("Generate intermediate code ...");
            SyntaxAnalysis syntax = new SyntaxAnalysis();
            syntax.initializeLR1();
            syntax.getInput(code);
            syntax.analysis();

            //获取符号表
            SymbolTable = syntax.L.nameTable;

            //获取token表
            TokenList = syntax.L.history;

            //获取LR1分析表
            LR1 = syntax.G.getCollection();

            //获取action表
            ACTION = syntax.G.ACTION;

            //获取goto表
            GOTO = syntax.G.GOTO;

            //获取推导过程
            derivation = syntax.history;

            //获取语法树根
            reductionTreeRoot = syntax.reductionTreeRoot;

            // 中间代码优化
            System.out.println("Optimize intermediate code ...");
            OptimizerAnalysis optimizer = new OptimizerAnalysis(syntax.L.nameTable, syntax.S.global_table, syntax.S.intermediate_code);
            double opt_rate = optimizer.analysis();

            //获取中间代码
            intermediate_code = optimizer.original_code;

            //获取优化后的中间代码
            optimized_code = optimizer.intermediate_code;

            //获取优化率
            optimize_rate = optimized_code.size() * 100.0 / intermediate_code.size();


            // 目标代码生成
            System.out.println("Optimize code");
            ObjectCodeGenerator mips_generator = new ObjectCodeGenerator(optimizer.intermediate_code, optimizer.block_group, globalUtils.stack_size);
            mips_generator.getObjectCode();

            //获取待用活跃信息表
            messageTableHistory = mips_generator.messageTableHistory;

            //获取寄存器分配表
            analysisHistory = mips_generator.analysisHistory;

            // 生成可执行文件
            String mips_content = "";
            for (int i = 0; i < mips_generator.object_code.size(); i++) {
                mips_content += mips_generator.object_code.get(i) + "\n";
            }

            //获取mips代码
            mips_code = mips_content;

            return mips_content;
        } catch (CustomException e) {
            return e.getMessage();
        }
    }
}
