package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.locks.Condition;

public class ObjectCodeGenerator {
    private List<TASitem> intermediate_code;
    private List<BlockItem> block_group;
    private Map<String, List<Pair<String, Integer>>> RVALUE;
    private Map<String, List<String>> AVALUE;
    private List<String> new_code;
    public int stack_buf_size;
    public int data_buf_size;
    public int temp_buf_size;
    public List<String> object_code;
    public List<MessageTableItem> messageTableHistory;
    public List<AnalysisHistoryItem> analysisHistory;

    public ObjectCodeGenerator() {
        this.intermediate_code = new ArrayList<>();
        this.block_group = new ArrayList<>();
        this.RVALUE = new LinkedHashMap<>();
        this.AVALUE = new LinkedHashMap<>();
        this.new_code = new ArrayList<>();
        this.stack_buf_size = 4 * 1024 * 32;
        this.data_buf_size = 4 * 1024 * 32;
        this.temp_buf_size = 4 * 1024 * 32;
        this.object_code = new ArrayList<>();
        this.messageTableHistory = new ArrayList<>();
        this.analysisHistory = new ArrayList<>();

        // Initialize RVALUE map with default values
        RVALUE.put("$t0", new ArrayList<>());
        RVALUE.put("$t1", new ArrayList<>());
        RVALUE.put("$t2", new ArrayList<>());
        RVALUE.put("$t3", new ArrayList<>());
        RVALUE.put("$t4", new ArrayList<>());
        RVALUE.put("$t5", new ArrayList<>());
        RVALUE.put("$t6", new ArrayList<>());
        RVALUE.put("$t7", new ArrayList<>());
    }

    public ObjectCodeGenerator(List<TASitem> ic, List<BlockItem> bg, int stack_size)
    {
        intermediate_code = ic;
        block_group = bg;
        RVALUE = new LinkedHashMap<>();
        AVALUE = new LinkedHashMap<>();
        new_code = new ArrayList<>();
        object_code = new ArrayList<>();
        messageTableHistory = new ArrayList<>();
        analysisHistory = new ArrayList<>();
        stack_buf_size = stack_size * 1024;
        data_buf_size = stack_size * 1024;
        temp_buf_size = stack_size * 1024;

        // Initialize RVALUE map with default values
        RVALUE.put("$t0", new ArrayList<>());
        RVALUE.put("$t1", new ArrayList<>());
        RVALUE.put("$t2", new ArrayList<>());
        RVALUE.put("$t3", new ArrayList<>());
        RVALUE.put("$t4", new ArrayList<>());
        RVALUE.put("$t5", new ArrayList<>());
        RVALUE.put("$t6", new ArrayList<>());
        RVALUE.put("$t7", new ArrayList<>());
    }

    private List<MessageTableItem> getMessageTable(int block_no) {
        List<MessageTableItem> message_table = new ArrayList<>();
        Map<String, Pair<Integer, Boolean>> message_link = new LinkedHashMap<>();

        for (int pos = block_group.get(block_no).end; pos >= block_group.get(block_no).begin; pos--) {
            TASitem TAS = intermediate_code.get(pos);
            MessageTableItem new_table_item = new MessageTableItem();
            new_table_item.no = pos;
            new_table_item.TAS = TAS;

            if (globalUtils.getChar(TAS.arg1, 0) == 'G' || globalUtils.getChar(TAS.arg1, 0) == 'V' || globalUtils.getChar(TAS.arg1, 0) == 'T') {
                if (!message_link.containsKey(TAS.arg1)) {
                    if (globalUtils.getChar(TAS.arg1, 0) == 'G' || block_group.get(block_no).wait_variable.contains(TAS.arg1)) {
                        message_link.put(TAS.arg1, new Pair<>(Integer.MAX_VALUE, true));
                    } else {
                        message_link.put(TAS.arg1, new Pair<>(0, false));
                    }
                }
                new_table_item.arg1_tag = message_link.get(TAS.arg1);
                message_link.put(TAS.arg1, new Pair<>(pos, true));
            }

            if (globalUtils.getChar(TAS.arg2, 0) == 'G' || globalUtils.getChar(TAS.arg2, 0) == 'V' || globalUtils.getChar(TAS.arg2, 0) == 'T') {
                if (!message_link.containsKey(TAS.arg2)) {
                    if (globalUtils.getChar(TAS.arg2, 0) == 'G' || block_group.get(block_no).wait_variable.contains(TAS.arg2)) {
                        message_link.put(TAS.arg2, new Pair<>(Integer.MAX_VALUE, true));
                    } else {
                        message_link.put(TAS.arg2, new Pair<>(0, false));
                    }
                }
                new_table_item.arg2_tag = message_link.get(TAS.arg2);
                message_link.put(TAS.arg2, new Pair<>(pos, true));
            }

            if (globalUtils.getChar(TAS.result, 0) == 'G' || globalUtils.getChar(TAS.result, 0) == 'V' || globalUtils.getChar(TAS.result, 0) == 'T') {
                if (!message_link.containsKey(TAS.result)) {
                    if (globalUtils.getChar(TAS.result, 0) == 'G' || block_group.get(block_no).wait_variable.contains(TAS.result)) {
                        message_link.put(TAS.result, new Pair<>(Integer.MAX_VALUE, true));
                    } else {
                        message_link.put(TAS.result, new Pair<>(0, false));
                    }
                }
                new_table_item.result_tag = message_link.get(TAS.result);
                message_link.put(TAS.result, new Pair<>(0, false));
            }

            message_table.add(new_table_item);
            messageTableHistory.add(new_table_item);
        }
        Collections.reverse(message_table);
        return message_table;
    }

    private String getRegment(String result) {
        String R = "";
        boolean has_R = false;

        if (AVALUE.containsKey(result) && AVALUE.get(result).size() > 0) {
            for (int i = 0; i < AVALUE.get(result).size(); i++) {
                if (!AVALUE.get(result).get(i).equals("M")) {
                    R = AVALUE.get(result).get(i);
                    has_R = true;
                    break;
                }
            }
        }

        if (!has_R) {
            for (Map.Entry<String, List<Pair<String, Integer>>> entry : RVALUE.entrySet()) {
                if (entry.getValue().size() == 0) {
                    R = entry.getKey();
                    return R;
                }
            }

            int farthest_R = -1;

            for (Map.Entry<String, List<Pair<String, Integer>>> entry : RVALUE.entrySet()) {
                int closest_V = Integer.MAX_VALUE;

                for (int i = 0; i < entry.getValue().size(); i++) {
                    if (entry.getValue().get(i).getValue() < closest_V) {
                        closest_V = entry.getValue().get(i).getValue();
                    }
                }

                if (closest_V > farthest_R) {
                    farthest_R = closest_V;
                    R = entry.getKey();
                }
            }
        }

        for (int i = 0; i < RVALUE.get(R).size(); i++) {
            String V = RVALUE.get(R).get(i).getKey();

            if (AVALUE.get(V).size() == 1 && AVALUE.get(V).get(0).equals(R)) {
                // save variable V
                if (globalUtils.getChar(V, 0) == 'G') {
                    emit("sw " + R + "," + globalUtils.DATA + "+" + Integer.parseInt(V.substring(1)));
                } else if (globalUtils.getChar(V, 0) == 'V') {
                    emit("sw " + R + "," + globalUtils.STACK + "+" + (4 + Integer.parseInt(V.substring(1))) + "($fp)");
                } else if (globalUtils.getChar(V, 0) == 'T') {
                    emit("sw " + R + "," + globalUtils.TEMP + "+" + (4 * Integer.parseInt(V.substring(1))));
                } else {
                    String err = "ERROR: AVALUE中变量名错误" + V + "\n";
                    globalUtils.errorLog(err);
                }
            }

            AVALUE.get(V).remove(R);

            if (!AVALUE.get(V).contains("M")) {
                AVALUE.get(V).add("M");
            }
        }

        RVALUE.put(R, new ArrayList<Pair<String, Integer>>());
        return R;
    }

    private void fresh(Pair<Integer, Boolean> tag, String R, String V, boolean value_changed) {
        if (value_changed || !tag.getValue()) {
            if (AVALUE.get(V) != null) {
                for (int i = 0; i < AVALUE.get(V).size(); i++) {
                    if (RVALUE.containsKey(AVALUE.get(V).get(i))) {
                        String opR = AVALUE.get(V).get(i);

                        for (int j = 0; j < RVALUE.get(opR).size(); j++) {
                            if (RVALUE.get(opR).get(j).getKey().equals(V)) {
                                RVALUE.get(opR).remove(j);
                                break;
                            }
                        }
                    }
                }
            }

            if (tag.getValue()) {
                AVALUE.put(V, new ArrayList<String>(Collections.singletonList(R)));
                RVALUE.get(R).add(new Pair<>(V, tag.getKey()));
            } else {
                AVALUE.remove(V);
            }
        } else {
            boolean is_find = false;

            for (int i = 0; i < RVALUE.get(R).size(); i++) {
                if (RVALUE.get(R).get(i).getKey().equals(V)) {
                    is_find = true;
                    List<Pair<String, Integer>> temp = RVALUE.get(R);
                    temp.set(i, new Pair<>(V, tag.getKey()));
                    break;
                }
            }

            if (!is_find) {
                RVALUE.get(R).add(new Pair<>(V, tag.getKey()));
            }

            if (!AVALUE.containsKey(V)) {
                AVALUE.put(V, new ArrayList<String>(Collections.singletonList(R)));
            } else {
                if (!AVALUE.get(V).contains(R)) {
                    AVALUE.get(V).add(R);
                }
            }
        }
    }

    private void emit(String code)
    {
        object_code.add(code);
        new_code.add(code);
    }

    public void endBlock() {
        for (Map.Entry<String, List<String>> entry : AVALUE.entrySet()) {
            String V = entry.getKey();
            List<String> values = entry.getValue();
            if (!values.contains("M")) {
                String R = "";
                for (String value : values) {
                    if (!value.equals("M")) {
                        R = value;
                        break;
                    }
                }
                if (globalUtils.getChar(V, 0) == 'G')
                    emit("sw " + R + "," + globalUtils.DATA + "+" + Integer.parseInt(V.substring(1)));
                else if (globalUtils.getChar(V, 0) == 'V')
                    emit("sw " + R + "," + globalUtils.STACK + "+" + (4 + Integer.parseInt(V.substring(1))) + "($fp)");
                else if (globalUtils.getChar(V, 0) == 'T')
                    emit("sw " + R + "," + globalUtils.TEMP + "+" + (4 * Integer.parseInt(V.substring(1))));
                else {
                    String err = "ERROR: AVALUE中变量名错误" + V + "\n";
                    globalUtils.errorLog(err);
                }
            }
        }
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : RVALUE.entrySet()) {
            String R = entry.getKey();
            RVALUE.put(R, new ArrayList<Pair<String, Integer>>());
        }
        AVALUE = new HashMap<>();
    }

    public void getObjectCode()
    {
        emit(".data");
        emit(globalUtils.DATA + ":.space " + Integer.toString(data_buf_size));
        emit(globalUtils.STACK + ":.space " + Integer.toString(stack_buf_size));
        emit(globalUtils.TEMP + ":.space " + Integer.toString(temp_buf_size));
        emit(".text");
        emit("nop");
        emit("addi $gp,$zero,0");
        emit("addi $fp,$zero,0");
        emit("addi $sp,$zero,4");
        emit("jal Fmain");

        for (int block_no = 0; block_no < block_group.size(); block_no++)
        {
            List<MessageTableItem> MessageTable = getMessageTable(block_no);
            boolean j_end = false;
            for (int i = 0; i < MessageTable.size(); i++)
            {
                new_code = new ArrayList();
                TASitem TAS = MessageTable.get(i).TAS;
                String Reg_arg1 = "", Reg_arg2 = "";
                if (TAS.arg1 == "" || TAS.op == "=[]")
                    Reg_arg1 = "";
                else if (globalUtils.getChar(TAS.arg1, 0) == '$')
                    Reg_arg1 = TAS.arg1;
                else if (TAS.arg1 == "[$sp]")
                    Reg_arg1 = globalUtils.STACK + "($sp)";
                else if (globalUtils.isNum(TAS.arg1))
                {
                    if (TAS.op == "+")
                        Reg_arg1 = TAS.arg1;
                    else
                    {
                        emit("addi $t8,$zero," + TAS.arg1);
                        Reg_arg1 = "$t8";
                    }
                }
                else if (globalUtils.getChar(TAS.arg1, 0) == 'G')
                {
                    if (!AVALUE.containsKey(TAS.arg1))
                    {
                        ArrayList<String> temp = new ArrayList<>();
                        temp.add("M");
                        AVALUE.put(TAS.arg1,temp);
                    }
                    else if (AVALUE.get(TAS.arg1).size() == 0)
                    {
                        String err = "ERROR: 找不到" + TAS.arg1 + "的地址\n";
                        globalUtils.errorLog(err);
                    }

                    if (AVALUE.get(TAS.arg1).size() == 1 && AVALUE.get(TAS.arg1).get(0) == "M")
                    {
                        Integer result = Integer.parseInt(TAS.arg1.substring(1));
                        emit("lw $t8," + globalUtils.DATA + "+" + Integer.toString(result));
                        Reg_arg1 = "$t8";
                    }
                    else
                    {
                        for (int k = 0; k < AVALUE.get(TAS.arg1).size(); k++)
                        {
                            if (AVALUE.get(TAS.arg1).get(k) != "M")
                                Reg_arg1 = AVALUE.get(TAS.arg1).get(k);
                        }
                    }
                }
                else if (globalUtils.getChar(TAS.arg1, 0) == 'V')
                {
                    if (!AVALUE.containsKey(TAS.arg1))
                    {
                        List<String> temp = new ArrayList<>();
                        temp.add("M");
                        AVALUE.put(TAS.arg1,temp); 
                    }
                    else if (AVALUE.get(TAS.arg1).size() == 0)
                    {
                        String err = "ERROR: 找不到" + TAS.arg1 + "的地址\n";
                        globalUtils.errorLog(err);
                    }

                    if (AVALUE.get(TAS.arg1).size() == 1 && AVALUE.get(TAS.arg1).get(0) == "M")
                    {
                        Integer result = Integer.parseInt(TAS.arg1.substring(1));
                        emit("lw $t8," + globalUtils.STACK + "+" + Integer.toString(4 + result) + "($fp)");
                        Reg_arg1 = "$t8";
                    }
                    else
                    {
                        for (int k = 0; k < AVALUE.get(TAS.arg1).size(); k++)
                        {
                            if (AVALUE.get(TAS.arg1).get(k) != "M")
                                Reg_arg1 = AVALUE.get(TAS.arg1).get(k);
                        }
                    }
                }
                else if (globalUtils.getChar(TAS.arg1, 0) == 'T')
                {
                    if (!AVALUE.containsKey(TAS.arg1))
                    {
                        List<String> temp = new ArrayList<>();
                        temp.add("M");
                        AVALUE.put(TAS.arg1,temp);
                    }
                    else if (AVALUE.get(TAS.arg1).size() == 0)
                    {
                        String err = "ERROR: 找不到" + TAS.arg1 + "的地址\n";
                        globalUtils.errorLog(err);
                    }

                    if (AVALUE.get(TAS.arg1).size() == 1 && AVALUE.get(TAS.arg1).get(0) == "M")
                    {
                        Integer result = Integer.parseInt(TAS.arg1.substring(1));
                        emit("lw $t8," + globalUtils.TEMP + "+" + Integer.toString(4 * result));
                        Reg_arg1 = "$t8";
                    }
                    else
                    {
                        for (int k = 0; k < AVALUE.get(TAS.arg1).size(); k++)
                        {
                            if (AVALUE.get(TAS.arg1).get(k) != "M")
                                Reg_arg1 = AVALUE.get(TAS.arg1).get(k);
                        }
                    }
                }

                if (TAS.arg2 == "")
                    Reg_arg2 = "";
                else if (globalUtils.getChar(TAS.arg2, 0) == '$')
                    Reg_arg2 = TAS.arg2;
                else if (TAS.arg2 == "[$sp]")
                    Reg_arg2 = globalUtils.STACK + "($sp)";
                else if (globalUtils.isNum(TAS.arg2))
                {
                    if (TAS.op == "+" && !globalUtils.isNum(Reg_arg1))
                        Reg_arg2 = TAS.arg2;
                    else
                    {
                        emit("addi $t9,$zero," + TAS.arg2);
                        Reg_arg2 = "$t9";
                    }
                }
                else if (globalUtils.getChar(TAS.arg2, 0) == 'G')
                {
                    if (!AVALUE.containsKey(TAS.arg2))
                    {
                        List<String> temp = new ArrayList<>();
                        temp.add("M");
                        AVALUE.put(TAS.arg2,temp);
                    }
                    else if (AVALUE.get(TAS.arg2).size() == 0)
                    {
                        String err = "ERROR: 找不到" + TAS.arg2 + "的地址\n";
                        globalUtils.errorLog(err);
                    }
                    if (AVALUE.get(TAS.arg2).size() == 1 && AVALUE.get(TAS.arg2).get(0) == "M")
                    {
                        Integer result = Integer.parseInt(TAS.arg2.substring(1));
                        emit("lw $t9," + globalUtils.DATA + "+" + Integer.toString(result));
                        Reg_arg2 = "$t9";
                    }
                    else
                    {
                        for (int k = 0; k < AVALUE.get(TAS.arg2).size(); k++)
                        {
                            if (AVALUE.get(TAS.arg2).get(k) != "M")
                                Reg_arg2 = AVALUE.get(TAS.arg2).get(k);
                        }
                    }
                }
                else if (globalUtils.getChar(TAS.arg2, 0) == 'V')
                {
                    if (!AVALUE.containsKey(TAS.arg2))
                    {
                        List<String> temp = new ArrayList<>();
                        temp.add("M");
                        AVALUE.put(TAS.arg2,temp);
                    }
                    else if (AVALUE.get(TAS.arg2).size() == 0)
                    {
                        String err = "ERROR: 找不到" + TAS.arg2 + "的地址\n";
                        globalUtils.errorLog(err);
                    }
                    if (AVALUE.get(TAS.arg2).size() == 1 && AVALUE.get(TAS.arg2).get(0) == "M")
                    {
                        Integer result = Integer.parseInt(TAS.arg2.substring(1));
                        emit("lw $t9," + globalUtils.STACK + "+" + Integer.toString(4 + result) + "($fp)");
                        Reg_arg2 = "$t9";
                    }
                    else
                    {
                        for (int k = 0; k < AVALUE.get(TAS.arg2).size(); k++)
                        {
                            if (AVALUE.get(TAS.arg2).get(k) != "M")
                                Reg_arg2 = AVALUE.get(TAS.arg2).get(k);
                        }
                    }
                }
                else if (globalUtils.getChar(TAS.arg2, 0) == 'T')
                {
                    if (!AVALUE.containsKey(TAS.arg2))
                    {
                        List<String> temp = new ArrayList<>();
                        temp.add("M");
                        AVALUE.put(TAS.arg2,temp);
                    }
                    else if (AVALUE.get(TAS.arg2).size() == 0)
                    {
                        String err = "ERROR: 找不到" + TAS.arg2 + "的地址\n";
                        globalUtils.errorLog(err);
                    }
                    if (AVALUE.get(TAS.arg2).size() == 1 && AVALUE.get(TAS.arg2).get(0) == "M")
                    {
                        Integer result = Integer.parseInt(TAS.arg2.substring(1));
                        emit("lw $t9," +globalUtils.TEMP+ "+" + Integer.toString(4 * result));
                        Reg_arg2 = "$t9";
                    }
                    else
                    {
                        for (int k = 0; k < AVALUE.get(TAS.arg2).size(); k++)
                        {
                            if (AVALUE.get(TAS.arg2).get(k) != "M")
                                Reg_arg2 =AVALUE.get(TAS.arg2).get(k);
                        }
                    }
                }

                if (globalUtils.getChar(TAS.op, 0) == 'F' || globalUtils.getChar(TAS.op, 0) == 'L')
                {
                    emit(TAS.op + ":");
                }
                else if (TAS.op == "nop")
                {
                    emit("nop");
                }
                else if (TAS.op == "j")
                {
                    j_end = true;
                    endBlock();
                    emit("j " + TAS.result);
                }
                else if (TAS.op == "jal")
                {
                    j_end = true;
                    //endBlock();
                    emit("jal " + TAS.result);
                }
                else if (TAS.op == "break")
                {
                    j_end = true;
                    endBlock();
                    emit("break");
                }
                else if (TAS.op == "ret")
                {
                    j_end = true;
                    endBlock();
                    emit("jr $ra");
                }
                else if (TAS.op == "jnz")
                {
                    j_end = true;
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    endBlock();
                    emit("bne " + Reg_arg1 + ",$zero," + TAS.result);
                }
                else if (TAS.op == "j<")
                {
                    j_end = true;
                    emit("addi $t8," + Reg_arg1 + ",1");
                    emit("sub $t9," + Reg_arg2 + ",$t8");
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                    endBlock();
                    emit("bgez $t9," + TAS.result);
                }
                else if (TAS.op == "j<=")
                {
                    j_end = true;
                    emit("sub $t9," + Reg_arg2 + "," + Reg_arg1);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                    endBlock();
                    emit("bgez $t9," + TAS.result);
                }
                else if (TAS.op == "j>")
                {
                    j_end = true;
                    emit("addi $t9," + Reg_arg2 + ",1");
                    emit("sub  $t8," + Reg_arg1 + ",$t9");
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                    endBlock();
                    emit("bgez $t8," + TAS.result);
                }
                else if (TAS.op == "j>=")
                {
                    j_end = true;
                    emit("sub $t8," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                    endBlock();
                    emit("bgez $t8," + TAS.result);
                }
                else if (TAS.op == "j==")
                {
                    j_end = true;
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                    endBlock();
                    emit("beq " + Reg_arg1 + "," + Reg_arg2 + "," + TAS.result);
                }
                else if (TAS.op == "j!=")
                {
                    j_end = true;
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                    endBlock();
                    emit("bne " + Reg_arg1 + "," + Reg_arg2 + "," + TAS.result);
                }
                else if (TAS.op == ":=")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                    {
                        R = TAS.result;
                        if (TAS.arg1 == "[$sp]")
                        {
                            emit("lw " + R + "," + Reg_arg1);
                        }
                        else
                        {
                            emit("add " + R + ",$zero," + Reg_arg1);
                        }
                    }
                    else if (TAS.result == "[$sp]")
                    {
                        R = globalUtils.STACK + "($sp)";
                        if (TAS.arg1 == "[$sp]")
                        {
                            String err = "ERROR: 发生从[$sp]到[$sp]的赋值\n";
                            globalUtils.errorLog(err);
                        }
                        else
                        {
                            emit("sw " + Reg_arg1 + "," + R);
                        }
                    }
                    else
                    {
                        R = getRegment(TAS.result);
                        if (TAS.arg1 == "[$sp]")
                        {
                            emit("lw " + R + "," + Reg_arg1);
                        }
                        else
                        {
                            emit("add " + R + ",$zero," + Reg_arg1);
                        }
                    }
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                }
                else if (TAS.op == "[]=")
                {
                    String base = TAS.result;
                    if (globalUtils.getChar(TAS.result, 0) == 'G')
                    {
                        emit("sll $t9," + Reg_arg2 + ",2");
                        emit("addi $t9,$t9," + base.substring(1));
                        emit("sw " + Reg_arg1 + "," + globalUtils.DATA + "($t9)");
                    }
                    else if (globalUtils.getChar(TAS.result, 0) == 'V')
                    {
                        emit("sll $t9," + Reg_arg2 + ",2");
                        emit("addi $t9,$t9," + base.substring(1));
                        emit("addi $t9,$t9,4");
                        emit("add $t9,$t9,$fp");
                        emit("sw " + Reg_arg1 + "," + globalUtils.STACK + "($t9)");
                    }
                    else if (globalUtils.getChar(TAS.result, 0) == 'T')
                    {
                        emit("addi $t9," + Reg_arg2 + "," + base.substring(1));
                        emit("sll $t9,$t9,2");
                        emit("sw " + Reg_arg1 + "," + globalUtils.TEMP + "($t9)");
                    }
                    else
                    {
                        String err = "ERROR: [] = 的左部result标识符不合法\n";
                        globalUtils.errorLog(err);
                    }
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "=[]")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    if (globalUtils.getChar(TAS.arg1, 0) == 'G')
                    {
                        emit("sll $t9," + Reg_arg2 + ",2");
                        emit("addi $t9,$t9," + TAS.arg1.substring(1));
                        emit("lw " + R + "," + globalUtils.DATA + "($t9)");
                    }
                    else if (globalUtils.getChar(TAS.arg1, 0) == 'V')
                    {
                        emit("sll $t9," + Reg_arg2 + ",2");
                        emit("addi $t9,$t9," + TAS.arg1.substring(1));
                        emit("addi $t9,$t9,4");
                        emit("add $t9,$t9,$fp");
                        emit("lw " + R + "," + globalUtils.STACK + "($t9)");
                    }
                    else if (globalUtils.getChar(TAS.arg1, 0) == 'T')
                    {
                        emit("addi $t9," + Reg_arg2 + "," + TAS.arg1.substring(1));
                        emit("sll $t9,$t9,2");
                        emit("lw " + R + "," +globalUtils.TEMP+ "($t9)");
                    }
                    else
                    {
                        String err = "ERROR: = []的右部arg1标识符不合法\n";
                        globalUtils.errorLog(err);
                    }
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "+")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);

                    if (globalUtils.isNum(Reg_arg1))
                        emit("addi " + R + "," + Reg_arg2 + "," + Reg_arg1);
                    else if (globalUtils.isNum(Reg_arg2))
                        emit("addi " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    else
                        emit("add " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "-")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    emit("sub " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "&")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    emit("and " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "|")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    emit("or " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "^")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    emit("xor " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "*")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    emit("mul " + R + "," + Reg_arg1 + "," + Reg_arg2);
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else if (TAS.op == "/")
                {
                    String R;
                    if (globalUtils.getChar(TAS.result, 0) == '$')
                        R = TAS.result;
                    else if (TAS.result == "[$sp]")
                        R = globalUtils.STACK + "($sp)";
                    else
                        R = getRegment(TAS.result);
                    emit("div " + Reg_arg1 + "," + Reg_arg2);
                    emit("mflo " + R);//Quotient in $lo
                    if (RVALUE.containsKey(R))
                        fresh(MessageTable.get(i).result_tag, R, TAS.result, true);
                    if (RVALUE.containsKey(Reg_arg1))
                        fresh(MessageTable.get(i).arg1_tag, Reg_arg1, TAS.arg1, false);
                    if (RVALUE.containsKey(Reg_arg2))
                        fresh(MessageTable.get(i).arg2_tag, Reg_arg2, TAS.arg2, false);
                }
                else
                {
                    String err = "ERROR: 中间代码非法\n";
                    globalUtils.errorLog(err);
                }

                AnalysisHistoryItem tmp = new AnalysisHistoryItem(TAS, new_code, RVALUE, AVALUE);
                analysisHistory.add(tmp);
            }
            if (!j_end)
                endBlock();
        }
//        showAnalysisHistory();
//        showObjectCode();
    }

    public void showMessageTableHistory() {
        for (int tno = 0; tno < messageTableHistory.size(); tno++) {
            MessageTableItem message_table = messageTableHistory.get(tno);
            System.out.print("(" + message_table.no + ")\t" + message_table.TAS.op + ' ' + message_table.TAS.arg1 + ' ' + message_table.TAS.arg2 + ' ' + message_table.TAS.result);
            System.out.print("\t(" + message_table.arg1_tag.getKey() + ',' + message_table.arg1_tag.getValue() + ")");
            System.out.print("\t(" + message_table.arg2_tag.getKey() + ',' + message_table.arg2_tag.getValue() + ")");
            System.out.println("\t(" + message_table.result_tag.getKey() + ',' + message_table.result_tag.getValue() + ")");
        }
    }

    public void showAnalysisHistory() {
        for (int ino = 0; ino < analysisHistory.size(); ino++) {
            AnalysisHistoryItem e = analysisHistory.get(ino);

            System.out.println("\n****************(" + e.TAS.op + "," + e.TAS.arg1 + "," + e.TAS.arg2 + "," + e.TAS.result + ")****************");
            for (int i = 0; i < e.object_codes.size(); i++) {
                System.out.print(e.object_codes.get(i) + '\t');
            }
            System.out.println("\n********RVALUE********");
            for (Map.Entry<String, List<Pair<String, Integer>>> entry : e.RVALUE.entrySet()) {
                String key = entry.getKey();
                List<Pair<String, Integer>> values = entry.getValue();
                System.out.print(key + "\t");
                for (int i = 0; i < values.size(); i++) {
                    Pair<String, Integer> pair = values.get(i);
                    System.out.print(pair.getKey() + ":" + pair.getValue() + "\t");
                }
                System.out.println();
            }
            System.out.println("**********************");
            System.out.println("\n******** AVALUE ********");
            for (Map.Entry<String, List<String>> entry : e.AVALUE.entrySet()) {
                String key = entry.getKey();
                List<String> values = entry.getValue();
                System.out.print(key + "\t");
                for (int i = 0; i < values.size(); i++) {
                    System.out.print(values.get(i) + "\t");
                }
                System.out.println();
            }
            System.out.println("**********************");
        }
    }

    public void showObjectCode() {
        for (int i = 0; i < object_code.size(); i++) {
            System.out.println("(" + i + ")\t" + object_code.get(i));
        }
    }


}
