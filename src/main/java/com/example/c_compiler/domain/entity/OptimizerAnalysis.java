package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
public class OptimizerAnalysis {
    private Map<Integer, String> name_table;
    private SymbolTable global_table;
    private Map<Integer, String> label_map;
    private int temp_counter;
    private List<List<DAGitem>> DAG_group;
    private List<TASitem> unoptimized_code;
    private List<BlockItem> unoptimized_block;
    public List<TASitem> intermediate_code;
    public List<TASitem> original_code;
    public List<BlockItem> block_group;

    public OptimizerAnalysis() {
        this.name_table = new LinkedHashMap<>();
        this.global_table = new SymbolTable();
        this.label_map = new LinkedHashMap<>();
        this.temp_counter = 0;
        this.DAG_group = new ArrayList<>();
        this.unoptimized_code = new ArrayList<>();
        this.unoptimized_block = new ArrayList<>();
        this.intermediate_code = new ArrayList<>();
        this.block_group = new ArrayList<>();
    }

    public OptimizerAnalysis(LinkedHashMap<Integer, String> nt, SymbolTable gt, List<TASitem> ic) {
        this.name_table = nt;
        this.global_table = gt;
        this.label_map = new LinkedHashMap<>();
        this.temp_counter = 0;
        this.DAG_group = new ArrayList<>();
        this.unoptimized_code = new ArrayList<>();
        this.unoptimized_block = new ArrayList<>();
        this.intermediate_code = ic;
        this.block_group = new ArrayList<>();
    }

    private boolean preOptimize() {
        int main_id = -1;
        int main_offset = 0;
        for (Map.Entry<Integer, String> entry : name_table.entrySet()) {
            if (entry.getValue().equals("main") && main_id == -1) {
                main_id = entry.getKey();
            } else if (entry.getValue().equals("main") && main_id != -1) {
                String err = "ERROR: 中间代码分块出错";
                globalUtils.errorLog(err);
            }
        }
        if (main_id == -1) {
            String err = "ERROR: 优化出错";
            globalUtils.errorLog(err);
        }
        for (int i = 0; i < global_table.table.size(); i++) {
            if (global_table.table.get(i).id == main_id) {
                main_offset = global_table.table.get(i).offset;
                break;
            }
        }
        label_map.put(main_offset, "Fmain");
        int normal_label_count = 0;
        int function_label_count = 0;
        for (int i = 0; i < intermediate_code.size(); i++) {
            TASitem e = intermediate_code.get(i);
            if (e.op.equals("jal")) {
                if (!label_map.containsKey(Integer.parseInt(e.result))) {
                    label_map.put(Integer.parseInt(e.result), "F" + function_label_count++);
                }
                e.result = label_map.get(Integer.parseInt(e.result));
            } else if (globalUtils.getChar(e.op, 0) == 'j') {
                if (!label_map.containsKey(Integer.parseInt(e.result))) {
                    label_map.put(Integer.parseInt(e.result), "L" + normal_label_count++);
                }
                e.result = label_map.get(Integer.parseInt(e.result));
            }
            intermediate_code.set(i, e);
        }

        List<TASitem> newcode = new ArrayList<>();
        for (int i = 0; i < intermediate_code.size(); i++) {
            TASitem e = intermediate_code.get(i);
            if (label_map.containsKey(i)) {
                TASitem label = new TASitem(label_map.get(i), "", "", "");
                newcode.add(label);
            }
            newcode.add(e);
        }
        intermediate_code = newcode;
        return true;
    }

    public void partition() {
        BlockItem block = new BlockItem();
        for (int i = 0; i < intermediate_code.size(); i++) {
            TASitem e = intermediate_code.get(i);
            boolean jmp_flag = (i - 1 >= 0 && (globalUtils.getChar(intermediate_code.get(i - 1).op, 0) == 'j' || intermediate_code.get(i - 1).op.equals("ret")));
            if (i == 0 || globalUtils.getChar(e.op, 0) == 'L' || globalUtils.getChar(e.op, 0) == 'F' || jmp_flag) {
                block.begin = i;
                block.wait_variable = new ArrayList<>();
                block.active_variable = new ArrayList<>();
            }
            if ((globalUtils.getChar(e.result, 0) == 'V' || globalUtils.getChar(e.result, 0) == 'T') && !block.wait_variable.contains(e.result)) {
                block.wait_variable.add(e.result);
            }
            if ((globalUtils.getChar(e.arg1, 0) == 'V' || globalUtils.getChar(e.arg1, 0) == 'T') && !block.wait_variable.contains(e.arg1)) {
                block.wait_variable.add(e.arg1);
            }
            if ((globalUtils.getChar(e.arg2, 0) == 'V' || globalUtils.getChar(e.arg2, 0) == 'T') && !block.wait_variable.contains(e.arg2)) {
                block.wait_variable.add(e.arg2);
            }
            if ((globalUtils.getChar(e.arg1, 0) == 'V' || globalUtils.getChar(e.arg1, 0) == 'T') && !block.active_variable.contains(e.arg1)) {
                block.active_variable.add(e.arg1);
            }
            if ((globalUtils.getChar(e.arg2, 0) == 'V' || globalUtils.getChar(e.arg2, 0) == 'T') && !block.active_variable.contains(e.arg2)) {
                block.active_variable.add(e.arg2);
            }
            boolean enter_flag = ((i + 1 < intermediate_code.size() && (globalUtils.getChar(intermediate_code.get(i + 1).op, 0) == 'L' || globalUtils.getChar(intermediate_code.get(i + 1).op, 0) == 'F')) || globalUtils.getChar(e.op, 0) == 'j' || e.op.equals("ret") || e.op.equals("break"));
            if (enter_flag) {
                block.end = i;
                block_group.add(block);
                block = new BlockItem();
            }
        }

        Map<String, Integer> label_loc = new LinkedHashMap<>();
        for (int pos = 0; pos < intermediate_code.size(); pos++) {
            if (globalUtils.getChar(intermediate_code.get(pos).op, 0) == 'L') {
                label_loc.put(intermediate_code.get(pos).op, pos);
            }
        }

        for (int i = 0; i < block_group.size(); i++) {
            BlockItem e = block_group.get(i);
            if (intermediate_code.get(e.end).op.equals("ret")) {
                e.useless_variable = e.wait_variable;
                e.wait_variable = new ArrayList<>();
            } else {
                List<String> real_wait_variable = new ArrayList<>();

                int pos = block_group.get(i + 1).begin;
                int prepos = pos - 1;
                while (prepos < intermediate_code.size()) {
                    if (label_loc.containsKey(intermediate_code.get(prepos).result) && label_loc.get(intermediate_code.get(prepos).result) < pos) {
                        pos = label_loc.get(intermediate_code.get(prepos).result);
                        prepos = label_loc.get(intermediate_code.get(prepos).result);
                    }
                    prepos++;
                }
                while (pos < intermediate_code.size() && globalUtils.getChar(intermediate_code.get(pos).op, 0) != 'F') {
                    if (e.wait_variable.contains(intermediate_code.get(pos).arg1) && !real_wait_variable.contains(intermediate_code.get(pos).arg1)) {
                        real_wait_variable.add(intermediate_code.get(pos).arg1);
                    }
                    if (e.wait_variable.contains(intermediate_code.get(pos).arg2) && !real_wait_variable.contains(intermediate_code.get(pos).arg2)) {
                        real_wait_variable.add(intermediate_code.get(pos).arg2);
                    }
                    pos++;
                }
                for (int j = 0; j < e.wait_variable.size(); j++) {
                    if (!real_wait_variable.contains(e.wait_variable.get(j))) {
                        e.useless_variable.add(e.wait_variable.get(j));
                    }
                }
                e.wait_variable = real_wait_variable;
            }
        }
    }

    public List<DAGitem> geneDAG(int block_no) {
        List<DAGitem> DAG = new ArrayList<>();
        BlockItem block = block_group.get(block_no);
        for (int pos = block.begin; pos <= block.end; pos++) {
            String op = intermediate_code.get(pos).op;
            String B = intermediate_code.get(pos).arg1;
            String C = intermediate_code.get(pos).arg2;
            String A = intermediate_code.get(pos).result;
            int element_count;
            if (op.equals("nop") || globalUtils.getChar(op, 0) == 'F' || globalUtils.getChar(op, 0) == 'L')
                element_count = -1;
            else if (globalUtils.getChar(A, 0) == '$' || A.equals("[$sp]"))
                element_count = -1;
            else if (op.equals(":="))
                element_count = 0;
            else if (op.equals("=[]"))
                element_count = 2;
            else if (op.equals("[]="))
                element_count = 3;
            else if (op.equals("j<") || op.equals("j<=") || op.equals("j>") || op.equals("j>=") || op.equals("j==") || op.equals("j!="))
                element_count = -1;
            else if (op.equals("jnz"))
                element_count = -1;
            else if (op.equals("j") || op.equals("jal") || op.equals("break") || op.equals("ret"))
                element_count = -1;
            else
                element_count = 2;

            if (element_count == -1) {
                DAGitem newDAG = new DAGitem();
                newDAG.isremain = true;
                newDAG.code = intermediate_code.get(pos);
                DAG.add(newDAG);
                if (globalUtils.getChar(A, 0) == '$' || A.equals("[$sp]")) {
                    for (int i = 0; i < DAG.size(); i++) {
                        if (DAG.get(i).isleaf && DAG.get(i).value.equals(A)) {
                            DAG.get(i).value = "-" + A;
                            break;
                        }
                    }
                }
                continue;
            }

            int state = 1;
            int n = 0;
            int A_no = -1;
            boolean new_A = true;
            int B_no = -1;
            boolean new_B = true;
            int C_no = -1;
            boolean new_C = true;
            while (state > 0) {
                switch (state) {
                    case 1: {
                        B_no = -1;
                        for (int i = 0; i < DAG.size(); i++) {
                            if ((DAG.get(i).isleaf && DAG.get(i).value.equals(B)) || DAG.get(i).label.contains(B)) {
                                B_no = i;
                                new_B = false;
                                break;
                            }
                        }
                        if (B_no == -1) {
                            DAGitem newDAG = new DAGitem();
                            newDAG.isleaf = true;
                            newDAG.value = B;
                            B_no = DAG.size();
                            new_B = true;
                            DAG.add(newDAG);
                        }
                        if (element_count == 0) {
                            n = B_no;
                            state = 4;
                        } else if (element_count == 1) {
                            state = 21;
                        } else if (element_count == 2) {
                            C_no = -1;
                            for (int i = 0; i < DAG.size(); i++) {
                                if ((DAG.get(i).isleaf && DAG.get(i).value.equals(C)) || DAG.get(i).label.contains(C)) {
                                    C_no = i;
                                    new_C = false;
                                    break;
                                }
                            }
                            if (C_no == -1) {
                                DAGitem newDAG = new DAGitem();
                                newDAG.isleaf = true;
                                newDAG.value = C;
                                C_no = DAG.size();
                                new_C = true;
                                DAG.add(newDAG);
                            }
                            state = 22;
                        } else if (element_count == 3) {
                            C_no = -1;
                            for (int i = 0; i < DAG.size(); i++) {
                                if ((DAG.get(i).isleaf && DAG.get(i).value.equals(C)) || DAG.get(i).label.contains(C)) {
                                    C_no = i;
                                    new_C = false;
                                    break;
                                }
                            }
                            if(C_no == -1) {
                                DAGitem newDAG = new DAGitem();
                                newDAG.isleaf = true;
                                newDAG.value = C;
                                C_no = DAG.size();
                                new_C = true;
                                DAG.add(newDAG);
                            }
                            A_no = -1;
                            for (int i = 0; i < DAG.size(); i++) {
                                if ((DAG.get(i).isleaf && DAG.get(i).value.equals(A)) || DAG.get(i).label.contains(A)) {
                                    A_no = i;
                                    new_A = false;
                                    break;
                                }
                            }
                            if(A_no == -1) {
                                DAGitem newDAG = new DAGitem();
                                newDAG.isleaf = true;
                                newDAG.value = A;
                                A_no = DAG.size();
                                new_A = true;
                                DAG.add(newDAG);
                            }
                            DAGitem newDAG = new DAGitem();
                            newDAG.isleaf = false;
                            newDAG.op = op;
                            newDAG.left_child = B_no;
                            newDAG.right_child = C_no;
                            newDAG.tri_child = A_no;
                            n = DAG.size();
                            DAG.add(newDAG);
                            DAG.get(B_no).parent = n;
                            DAG.get(C_no).parent = n;
                            DAG.get(A_no).parent = n;
                            for(int i = 0; i < DAG.size(); i++) {
                                if(DAG.get(i).isremain && DAG.get(i).code.result.equals(A)) {
                                    DAG.get(i).code.result = "-" + A;
                                }
                            }
                            state = -1;
                        }
                        else
                            state = -1;
                        break;
                    }
                    case 21: {
                        if(DAG.get(B_no).isleaf && globalUtils.isNum(DAG.get(B_no).value)) {
                            state = 23;
                        }
                        else {
                            state = 31;
                        }
                        break;
                    }
                    case 22:{
                        if ((DAG.get(B_no).isleaf && globalUtils.isNum(DAG.get(B_no).value)) && (DAG.get(C_no).isleaf && globalUtils.isNum(DAG.get(C_no).value)))
                        {
                            state = 24;
                        }
                        else
                        {
                            state = 32;
                        }
                        break;
                    }
                    case 23:{
                        state = -1;
                        break;
                    }
                    case 24:{
                        int _B = Integer.parseInt(DAG.get(B_no).value);
                        int _C = Integer.parseInt(DAG.get(C_no).value);
                        int P = 0;

                        if (op.equals("+")) {
                            P = _B + _C;
                        } else if (op.equals("-")) {
                            P = _B - _C;
                        } else if (op.equals("&")) {
                            P = _B & _C;
                        } else if (op.equals("|")) {
                            P = _B | _C;
                        } else if (op.equals("^")) {
                            P = _B ^ _C;
                        } else if (op.equals("*")) {
                            P = _B * _C;
                        } else if (op.equals("/")) {
                            P = _B / _C;
                        }

                        DAGitem tmpB = DAG.get(B_no);
                        DAGitem tmpC = DAG.get(C_no);

                        if (new_B) {
                            DAG.remove(tmpB);
                        }

                        if (new_C) {
                            DAG.remove(tmpC);
                        }

                        n = -1;
                        for (int i = 0; i < DAG.size(); i++) {
                            if ((DAG.get(i).isleaf && DAG.get(i).value.equals(Integer.toString(P))) || DAG.get(i).label.contains(Integer.toString(P))) {
                                n = i;
                                break;
                            }
                        }

                        if (n == -1) {
                            DAGitem newDAG = new DAGitem();
                            newDAG.isleaf = true;
                            newDAG.value = String.valueOf(P);
                            n = DAG.size();
                            DAG.add(newDAG);
                        }

                        state = 4;
                        break;
                    }
                    case 31: {
                        n = -1;
                        for(int i = 0; i < DAG.size(); i++) {
                            if(DAG.get(i).isleaf && DAG.get(i).left_child == B_no && DAG.get(i).op.equals(op)) {
                                n = i;
                                break;
                            }
                        }
                        if(n == -1) {
                            DAGitem newDAG = new DAGitem();
                            newDAG.isleaf = false;
                            newDAG.op = op;
                            newDAG.left_child = B_no;
                            n = DAG.size();
                            DAG.add(newDAG);
                            DAG.get(B_no).parent = n;
                        }
                        state = 4;
                        break;
                    }
                    case 32:{
                        n=-1;
                        for(int i = 0; i < DAG.size(); i++) {
                            if(DAG.get(i).isleaf && DAG.get(i).left_child == B_no && DAG.get(i).right_child == C_no && DAG.get(i).op.equals(op)) {
                                n = i;
                                break;
                            }
                        }
                        if(n == -1) {
                            DAGitem newDAG = new DAGitem();
                            newDAG.isleaf = false;
                            newDAG.op = op;
                            newDAG.left_child = B_no;
                            newDAG.right_child = C_no;
                            n = DAG.size();
                            DAG.add(newDAG);
                            DAG.get(B_no).parent = n;
                            DAG.get(C_no).parent = n;
                        }
                        state = 4;
                        break;
                    }
                    case 4:{
                        for(int i = 0; i < DAG.size(); i++) {
                            if(DAG.get(i).isleaf && DAG.get(i).value.equals(A)) {
                                DAG.get(i).value = "-" + A;
                                break;
                            } else if (DAG.get(i).label.contains(A)) {
                                DAG.get(i).label.remove(A);
                            }
                        }
                        DAG.get(n).label.add(A);
                        state = -1;
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return DAG;
    }

    private void utilizeChildren(List<DAGitem> DAG, int now) {
        DAG.get(now).useful = true;
        if (!DAG.get(now).isleaf) {
            if (DAG.get(now).right_child != -1)
                utilizeChildren(DAG, DAG.get(now).right_child);
            if (DAG.get(now).left_child != -1)
                utilizeChildren(DAG, DAG.get(now).left_child);
            if (DAG.get(now).tri_child != -1)
                utilizeChildren(DAG, DAG.get(now).tri_child);
        }
    }

    private String newtemp() {
        return "S" + String.valueOf(temp_counter++);
    }



    private void optimize() {
        List<TASitem> optimized_code = new ArrayList<>();
        for (int block_no = 0; block_no < block_group.size(); block_no++) {
            List<DAGitem> DAG = geneDAG(block_no);
            DAG_group.add(DAG);
            BlockItem newblock = new BlockItem();
            newblock.begin = optimized_code.size();
            BlockItem block = block_group.get(block_no);
            List<String> wait_variable = new ArrayList<>(block.wait_variable);
            wait_variable.add("$gp");
            wait_variable.add("$sp");
            wait_variable.add("$fp");
            wait_variable.add("$v0");
            wait_variable.add("$t0");
            wait_variable.add("$t1");
            wait_variable.add("$t2");
            wait_variable.add("$t3");
            wait_variable.add("$t4");
            wait_variable.add("$t5");
            wait_variable.add("$t6");
            wait_variable.add("$t7");
            wait_variable.add("[$sp]");

            for (int i = 0; i < DAG.size(); i++) {
                if (DAG.get(i).isremain) {
                    if (!DAG.get(i).code.arg1.equals("") && !wait_variable.contains(DAG.get(i).code.arg1))
                        wait_variable.add(DAG.get(i).code.arg1);
                    if (!DAG.get(i).code.arg2.equals("") && !wait_variable.contains(DAG.get(i).code.arg2))
                        wait_variable.add(DAG.get(i).code.arg2);
                }
            }

            for (int i = 0; i < DAG.size(); i++) {
                if (!DAG.get(i).isremain) {
                    if (DAG.get(i).tri_child == -1) {
                        List<String> new_label = new ArrayList<>();
                        for (String label : DAG.get(i).getLabel()) {
                            if (globalUtils.getChar(label, 0) == 'G' || wait_variable.contains(label)) {
                                new_label.add(label);
                                DAG.get(i).setUseful(true);
                            }
                        }
                        DAG.get(i).setLabel(new_label);
                        if (DAG.get(i).isUseful())
                            utilizeChildren(DAG, i);
                        if (!DAG.get(i).isleaf && DAG.get(i).getLabel().isEmpty())
                            DAG.get(i).label.add(newtemp());
                    } else {
                        DAG.get(i).setUseful(true);
                        utilizeChildren(DAG, i);
                    }
                }
            }

            for (int i = 0; i < DAG.size(); i++) {
                if (DAG.get(i).isremain)
                    optimized_code.add(DAG.get(i).getCode());
                else {
                    if (DAG.get(i).isleaf) {
                        for (String label : DAG.get(i).getLabel()) {
                            String v;
                            if (globalUtils.getChar(DAG.get(i).getValue(),0) == '-')
                                v = DAG.get(i).getValue().substring(1);
                            else
                                v = DAG.get(i).getValue();
                            TASitem newTAS = new TASitem(":=", v, "", label);
                            optimized_code.add(newTAS);
                        }
                    } else {
                        String lv;
                        if (DAG.get(DAG.get(i).left_child).isleaf) {
                            if (globalUtils.getChar(DAG.get(DAG.get(i).left_child).getValue(),0) == '-')
                                lv = DAG.get(DAG.get(i).left_child).getValue().substring(1);
                            else
                                lv = DAG.get(DAG.get(i).left_child).getValue();
                        } else {
                            lv = DAG.get(DAG.get(i).left_child).getLabel().get(0);
                        }
                        String rv;
                        if (DAG.get(DAG.get(i).right_child).isleaf) {
                            if (globalUtils.getChar(DAG.get(DAG.get(i).right_child).getValue(),0) == '-')
                                rv = DAG.get(DAG.get(i).right_child).getValue().substring(1);
                            else
                                rv = DAG.get(DAG.get(i).right_child).getValue();
                        } else {
                            rv = DAG.get(DAG.get(i).right_child).getLabel().get(0);
                        }

                        if (DAG.get(i).tri_child != -1) {
                            String tri_v;
                            if (DAG.get(DAG.get(i).tri_child).isleaf) {
                                if (globalUtils.getChar(DAG.get(DAG.get(i).tri_child).getValue(),0) == '-')
                                    tri_v = DAG.get(DAG.get(i).tri_child).getValue().substring(1);
                                else
                                    tri_v = DAG.get(DAG.get(i).tri_child).getValue();
                            } else {
                                tri_v = DAG.get(DAG.get(i).tri_child).getLabel().get(0);
                            }
                            TASitem newTAS = new TASitem(DAG.get(i).getOp(), lv, rv, tri_v);
                            optimized_code.add(newTAS);
                        } else {
                            TASitem newTAS = new TASitem(DAG.get(i).getOp(), lv, rv, DAG.get(i).getLabel().get(0));
                            optimized_code.add(newTAS);
                            for (int label_no = 1; label_no < DAG.get(i).getLabel().size(); label_no++) {
                                newTAS = new TASitem(":=", DAG.get(i).getLabel().get(0), "", DAG.get(i).getLabel().get(label_no));
                                optimized_code.add(newTAS);
                            }
                        }
                    }
                }
            }

            for (int i = newblock.begin; i < optimized_code.size(); i++) {
                TASitem e = optimized_code.get(i);
                if (e.getOp().equals("+") && e.getArg1().equals("$sp") && globalUtils.isNum(e.getArg2()) && e.getResult().equals("$sp")) {
                    int sum = Integer.parseInt(e.getArg2());
                    while (i + 1 < optimized_code.size() && optimized_code.get(i + 1).getOp().equals("+") &&
                            optimized_code.get(i + 1).getArg1().equals("$sp") && globalUtils.isNum(optimized_code.get(i + 1).getArg2()) &&
                            optimized_code.get(i + 1).getResult().equals("$sp")) {
                        sum += Integer.parseInt(optimized_code.get(i + 1).getArg2());
                        optimized_code.remove(i + 1);
                    }
                    optimized_code.get(i).setArg2(Integer.toString(sum));
                }
            }

            newblock.end = optimized_code.size() - 1;
        }

        LinkedHashMap<String, String> tmpV_map = new LinkedHashMap<>();
        int newtemp_counter = 0;
        for (int pos = 0; pos < optimized_code.size(); pos++) {
            TASitem TAS = optimized_code.get(pos);
            if ((globalUtils.getChar(TAS.getArg1(),0) == 'T' || globalUtils.getChar(TAS.getArg1(),0) == 'S') && !tmpV_map.containsKey(TAS.getArg1()))
                tmpV_map.put(TAS.getArg1(), "T" + newtemp_counter++);
            if ((globalUtils.getChar(TAS.getArg2(),0) == 'T' || globalUtils.getChar(TAS.getArg2(),0) == 'S') && !tmpV_map.containsKey(TAS.getArg2()))
                tmpV_map.put(TAS.getArg2(), "T" + newtemp_counter++);
            if ((globalUtils.getChar(TAS.getResult(),0) == 'T' || globalUtils.getChar(TAS.getResult(),0) == 'S') && !tmpV_map.containsKey(TAS.getResult()))
                tmpV_map.put(TAS.getResult(), "T" + newtemp_counter++);
        }

        for (int pos = 0; pos < optimized_code.size(); pos++) {
            TASitem pTAS = optimized_code.get(pos);
            if (tmpV_map.containsKey(pTAS.getArg1()))
                pTAS.setArg1(tmpV_map.get(pTAS.getArg1()));
            if (tmpV_map.containsKey(pTAS.getArg2()))
                pTAS.setArg2(tmpV_map.get(pTAS.getArg2()));
            if (tmpV_map.containsKey(pTAS.getResult()))
                pTAS.setResult(tmpV_map.get(pTAS.getResult()));
        }

        unoptimized_block = new ArrayList<>(block_group);
        unoptimized_code = new ArrayList<>(intermediate_code);
        block_group = new ArrayList<>();
        intermediate_code = new ArrayList<>(optimized_code);
        partition();
    }

    public void showIntermediateCode() {
        System.out.println("Intermediate Code");
        for (int i = 0; i < intermediate_code.size(); i++) {
            System.out.println("(" + i + ")\t" + intermediate_code.get(i).op + "\t" + intermediate_code.get(i).arg1 + "\t" + intermediate_code.get(i).arg2 + "\t" + intermediate_code.get(i).result);
        }
        System.out.println("*********************************");
    }

    public double analysis() {
        preOptimize();
        //showIntermediateCode();
        partition();
        int route = 0;
        int original_size = intermediate_code.size();
        original_code = new ArrayList<>(intermediate_code);
        int optimize_size = 0;
        do {
            optimize();
            route++;
        } while (unoptimized_code.size() != intermediate_code.size());
        optimize_size = intermediate_code.size();
        //showIntermediateCode();
        return 100.0 * (double) optimize_size / (double) original_size;
    }
}

