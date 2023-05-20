package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@AllArgsConstructor
public class SemanticAnalysis {
    private List<SymbolTable> table_stack;
    private List<Integer> offset_stack;
    private int temp_counter;
    public SymbolTable last_table;
    public SymbolTable global_table;
    public List<TASitem> intermediate_code;

    public SemanticAnalysis() {
        temp_counter = -1;
        table_stack = new ArrayList<SymbolTable>();
        offset_stack = new ArrayList<Integer>();
        last_table = new SymbolTable();
        global_table = new SymbolTable();
        intermediate_code = new ArrayList<TASitem>();
    }

    private String newtemp() {
        String temp_name = "V" + offset_stack.get(offset_stack.size() - 1);
        table_stack.get(table_stack.size() - 1).enter(temp_counter--, globalUtils.type.INT, globalUtils.kind.VAR, offset_stack.get(offset_stack.size() - 1));
        offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + 4);

        intermediate_code.add(new TASitem("+", "$sp", "4", "$sp"));
        return temp_name;
    }

    public String lookup(int id) {
        SymbolTable tp = table_stack.get(table_stack.size() - 1);
        int offset;
        while (tp != null) {
            for (int i = 0; i < tp.table.size(); i++) {
                if (tp.table.get(i).id == id) {
                    offset = tp.table.get(i).offset;
                    if (tp.table.get(i).k == globalUtils.kind.VAR || tp.table.get(i).k == globalUtils.kind.ARRAY) {
                        if (tp.parent != null)
                            return "V" + Integer.toString(offset);
                        else
                            return "G" + Integer.toString(offset);
                    }
                }
            }
            tp = tp.parent;
        }
        return "";
    }

    public SymbolTableItem find(int id) {
        SymbolTable tp = table_stack.get(table_stack.size() - 1);
        while (tp != null) {
            for (int i = 0; i < tp.table.size(); i++) {
                if (tp.table.get(i).id == id) {
                    return tp.table.get(i);
                }
            }
            tp = tp.parent;
        }
        return null;
    }

    public void emit(String op, String arg1, String arg2, String result) {
        TASitem tas = new TASitem(op, arg1, arg2, result);
        intermediate_code.add(tas);
    }

    public Integer nextstat() {
        return intermediate_code.size();
    }

    public void change(int i, String result) {
        TASitem temp = new TASitem(intermediate_code.get(i));
        temp.result = result;
        intermediate_code.set(i, temp);
    }
    public void analysis(String token, TreeNode root, LinkedHashMap<Integer, String> nameTable) {
        if (token.equals("<PROGRAM>::=<M><ASSERTIONS>")) {
            global_table = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            global_table.width = offset_stack.get(offset_stack.size() - 1);
            offset_stack.remove(offset_stack.size() - 1);
        } else if (token.equals("<M>::='epsilon'")) {
            root.quad = nextstat();
            SymbolTable t = new SymbolTable();
            if (last_table != null) {
                last_table.next = t;
                t.previous = last_table;
            }
            if (!table_stack.isEmpty()) {
                t.parent = table_stack.get(table_stack.size() - 1);
            }
            last_table = t;
            table_stack.add(t);
            offset_stack.add(0);
        } else if (token.equals("<ASSERTIONS>::=<ASSERTION>")) {
        } else if (token.equals("<ASSERTIONS>::=<ASSERTION><ASSERTIONS>")) {
        } else if (token.equals("<ASSERTION>::='INT''ID'<ASSERTIONTYPE>'DEL'")) {
            root.t = globalUtils.type.INT;
            root.k = root.children.get(2).k;
            root.n = root.children.get(2).n;
            root.width = 4 * root.n;
            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, offset_stack.get(offset_stack.size() - 1));
            if (root.k == globalUtils.kind.ARRAY) {
                root.dimension = new ArrayList<>(root.children.get(2).dimension);
                table_stack.get(table_stack.size() - 1).enterdimension(root.children.get(1).content.getValue(), root.dimension);
            }
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + root.width);
        } else if (token.equals("<ASSERTION>::=<FUNCASSERTION><SENBLOCK>")) {
            root.t = root.children.get(0).t;
            root.k = root.children.get(0).k;
            root.n = root.children.get(0).n;
            root.width = root.children.get(0).width;
            SymbolTable t = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            t.width = t.table.isEmpty() ? 0 : offset_stack.get(offset_stack.size() - 1) - t.table.get(0).offset;
            offset_stack.remove(offset_stack.size() - 1);
        } else if (token.equals("<ASSERTIONTYPE>::='epsilon'")) {
            root.k = globalUtils.kind.VAR;
            root.n = 1;
        } else if (token.equals("<ASSERTIONTYPE>::=<ARRAYASSERTION>")) {
            root.k = globalUtils.kind.ARRAY;
            root.n = root.children.get(0).n;
            root.dimension = new ArrayList<>(root.children.get(0).dimension);
            Collections.reverse(root.dimension);
        } else if (token.equals("<FUNCASSERTION>::='VOID''ID'<M>'LP'<FORMALPARAM>'RP'")) {
            root.t = globalUtils.type.VOID;
            root.k = globalUtils.kind.FUNC;
            root.n = 1;
            root.width = -1 * root.n;

            SymbolTable new_table = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            int new_offset = offset_stack.get(offset_stack.size() - 1);
            offset_stack.remove(offset_stack.size() - 1);

            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, root.children.get(2).quad);
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + 0);
            table_stack.get(table_stack.size() - 1).enterproc(root.children.get(1).content.getValue(), new_table);
            table_stack.get(table_stack.size() - 1).enterdimension(root.children.get(1).content.getValue(), root.children.get(4).dimension);

            table_stack.add(new_table);
            offset_stack.add(new_offset);
        } else if (token.equals("<FUNCASSERTION>::='INT''ID'<M>'LP'<FORMALPARAM>'RP'")) {
            root.t = globalUtils.type.INT;
            root.k = globalUtils.kind.FUNC;
            root.n = 1;
            root.width = -1 * root.n;

            SymbolTable new_table = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            int new_offset = offset_stack.get(offset_stack.size() - 1);
            offset_stack.remove(offset_stack.size() - 1);

            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, root.children.get(2).quad);
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + 0);
            table_stack.get(table_stack.size() - 1).enterproc(root.children.get(1).content.getValue(), new_table);
            table_stack.get(table_stack.size() - 1).enterdimension(root.children.get(1).content.getValue(), root.children.get(4).dimension);

            table_stack.add(new_table);
            offset_stack.add(new_offset);
        } else if (token.equals("<ARRAYASSERTION>::='LS''NUM''RS'")) {
            root.k = globalUtils.kind.ARRAY;
            root.n = root.children.get(1).content.getValue();
            root.dimension.add(root.children.get(1).content.getValue());
        } else if (token.equals("<ARRAYASSERTION>::='LS''NUM''RS'<ARRAYASSERTION>")) {
            root.k = globalUtils.kind.ARRAY;
            root.n = root.children.get(1).content.getValue() * root.children.get(3).n;
            root.dimension = new ArrayList<>(root.children.get(3).dimension);
            root.dimension.add(root.children.get(1).content.getValue());
        } else if (token.equals("<FORMALPARAM>::=<FORMALPARAMLIST>")) {
            root.dimension = new ArrayList<>(root.children.get(0).dimension);
        } else if (token.equals("<FORMALPARAM>::='VOID'")) {
            root.dimension.add(0);
        } else if (token.equals("<FORMALPARAM>::='epsilon'")) {
            root.dimension.add(0);
        } else if (token.equals("<FORMALPARAMLIST>::='INT''ID'")) {
            root.t = globalUtils.type.INT;
            root.k = globalUtils.kind.VAR;
            root.n = 1;
            root.width = 4 * root.n;
            root.dimension.add(1);
            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, offset_stack.get(offset_stack.size() - 1));
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + root.width);
        } else if (token.equals("<FORMALPARAMLIST>::='INT''ID''SEP'<FORMALPARAMLIST>")) {
            root.t = globalUtils.type.INT;
            root.k = globalUtils.kind.VAR;
            root.n = 1;
            root.width = 4 * root.n;
            root.dimension = new ArrayList<>(root.children.get(3).dimension);
            root.dimension.set(0, root.dimension.get(0) + 1);
            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, offset_stack.get(offset_stack.size() - 1));
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + root.width);
        } else if (token.equals("<SENBLOCK>::='LB'<INNERASSERTION><SENSEQ>'RB'")) {
        } else if (token.equals("<INNERASSERTION>::=<INNERVARIDEF>'DEL'<INNERASSERTION>")) {
        } else if (token.equals("<INNERASSERTION>::='epsilon'")) {
        } else if (token.equals("<INNERVARIDEF>::='INT''ID'")) {
            root.t = globalUtils.type.INT;
            root.k = globalUtils.kind.VAR;
            root.n = 1;
            root.width = 4 * root.n;
            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, offset_stack.get(offset_stack.size() - 1));
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + root.width);
            emit("+", "$sp", Integer.toString(root.width), "$sp");
        } else if (token.equals("<INNERVARIDEF>::='INT''ID'<ARRAYASSERTION>")) {
            root.t = globalUtils.type.INT;
            root.k = globalUtils.kind.ARRAY;
            root.n = root.children.get(2).n;
            root.width = 4 * root.n;
            root.dimension = new ArrayList<>(root.children.get(2).dimension);
            Collections.reverse(root.dimension);
            table_stack.get(table_stack.size() - 1).enter(root.children.get(1).content.getValue(), root.t, root.k, offset_stack.get(offset_stack.size() - 1));
            table_stack.get(table_stack.size() - 1).enterdimension(root.children.get(1).content.getValue(), root.dimension);
            offset_stack.set(offset_stack.size() - 1, offset_stack.get(offset_stack.size() - 1) + root.width);
            emit("+", "$sp", Integer.toString(root.width), "$sp");
        } else if (token.equals("<SENSEQ>::=<SENTENCE>")) {
        } else if (token.equals("<SENSEQ>::=<SENTENCE><SENSEQ>")) {
        } else if (token.equals("<SENTENCE>::=<IFSEN>")) {
        } else if (token.equals("<SENTENCE>::=<WHILESEN>")) {
        } else if (token.equals("<SENTENCE>::=<RETURNSEN>'DEL'")) {
        } else if (token.equals("<SENTENCE>::=<ASSIGNMENT>'DEL'")) {
        } else if (token.equals("<ASSIGNMENT>::='ID''ASSIGN'<EXPRESSION>")) {
            String p = lookup(root.children.get(0).content.getValue());
            if (p.equals("")) {
                String err = "ERROR: 未定义" + nameTable.get(root.children.get(0).content.getValue()) + "\n";
                globalUtils.errorLog(err);
            } else {
                emit(":=", root.children.get(2).place, "", p);
                root.place = newtemp();
                emit(":=", root.children.get(2).place, "", root.place);
            }
        } else if (token.equals("<ASSIGNMENT>::=<ARRAY>'ASSIGN'<EXPRESSION>")) {
            if (root.children.get(0).dimension.size() != 1) {
                String err = "ERROR: 数组索引不完整\n";
                globalUtils.errorLog(err);
            }
            String p = lookup(root.children.get(0).content.getValue());
            if (p.equals("")) {
                String err = "ERROR: 未定义" + nameTable.get(root.children.get(0).content.getValue()) + "\n";
                globalUtils.errorLog(err);
            } else {
                emit("[]=", root.children.get(2).place, root.children.get(0).place, p);
                root.place = newtemp();
                emit(":=", root.children.get(2).place, "", root.place);
            }
        } else if (token.equals("<RETURNSEN>::='RETURN'<EXPRESSION>")) {
            emit(":=", root.children.get(1).place, "", "$v0");
            emit("ret", "", "", "");
        } else if (token.equals("<RETURNSEN>::='RETURN'")) {
            emit(":=", Integer.toString(0), "", "$v0");
            emit("ret", "", "", "");
        } else if (token.equals("<WHILESEN>::=<B>'WHILE''LP'<CTRL>'RP'<T><SENBLOCK>")) {
            SymbolTable t = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            t.width = t.table.isEmpty() ? 0 : offset_stack.get(offset_stack.size() - 1) - t.table.get(0).offset;
            offset_stack.remove(offset_stack.size() - 1);

            emit("-", "$sp", Integer.toString(t.width), "$sp");

            emit("j", "", "", Integer.toString(root.children.get(0).quad));
            change(root.children.get(3).true_list, Integer.toString(root.children.get(5).quad));
            change(root.children.get(3).false_list, Integer.toString(nextstat()));

        } else if (token.equals("<B>::='epsilon'")) {
            root.quad = nextstat();
        } else if (token.equals("<IFSEN>::='IF''LP'<CTRL>'RP'<T><SENBLOCK>")) {
            SymbolTable t = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            t.width = t.table.isEmpty() ? 0 : offset_stack.get(offset_stack.size() - 1) - t.table.get(0).offset;
            offset_stack.remove(offset_stack.size() - 1);

            emit("-", "$sp", Integer.toString(t.width), "$sp");

            change(root.children.get(2).true_list, Integer.toString(root.children.get(4).quad));
            change(root.children.get(2).false_list, Integer.toString(nextstat()));
        } else if (token.equals("<IFSEN>::='IF''LP'<CTRL>'RP'<T><SENBLOCK>'ELSE'<N><SENBLOCK>")) {
            SymbolTable t = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            t.width = t.table.isEmpty() ? 0 : offset_stack.get(offset_stack.size() - 1) - t.table.get(0).offset;
            offset_stack.remove(offset_stack.size() - 1);

            emit("-", "$sp", Integer.toString(t.width), "$sp");


            change(root.children.get(2).true_list, Integer.toString(root.children.get(4).quad));
            change(root.children.get(2).false_list, Integer.toString(root.children.get(7).quad));
            change(root.children.get(7).true_list, Integer.toString(nextstat()));
        } else if (token.equals("<CTRL>::=<EXPRESSION>")) {
            root.true_list = nextstat();
            emit("jnz", root.children.get(0).place, "", Integer.toString(0));
            root.false_list = nextstat();
            emit("j", "", "", Integer.toString(0));
        } else if (token.equals("<T>::='epsilon'")) {
            root.quad = nextstat();
            SymbolTable t = new SymbolTable();
            if (last_table != null) {
                last_table.next = t;
                t.previous = last_table;
            }
            if (!table_stack.isEmpty()) {
                t.parent = table_stack.get(table_stack.size() - 1);
            }
            last_table = t;
            table_stack.add(t);
            if (offset_stack.isEmpty()) {
                offset_stack.add(0);
            } else {
                int back_offset = offset_stack.get(offset_stack.size() - 1);
                offset_stack.add(back_offset);
            }
        } else if (token.equals("<N>::='epsilon'")) {
            SymbolTable t = table_stack.get(table_stack.size() - 1);
            table_stack.remove(table_stack.size() - 1);
            t.width = t.table.isEmpty() ? 0 : offset_stack.get(offset_stack.size() - 1) - t.table.get(0).offset;
            offset_stack.remove(offset_stack.size() - 1);

            emit("-", "$sp", Integer.toString(t.width), "$sp");

            t = new SymbolTable();
            if (last_table != null) {
                last_table.next = t;
                t.previous = last_table;
            }
            if (!table_stack.isEmpty()) {
                t.parent = table_stack.get(table_stack.size() - 1);
            }
            last_table = t;
            table_stack.add(t);
            if (offset_stack.isEmpty()) {
                offset_stack.add(0);
            } else {
                int back_offset = offset_stack.get(offset_stack.size() - 1);
                offset_stack.add(back_offset);
            }
            root.true_list = nextstat();
            emit("j", "", "", Integer.toString(0));
            root.quad = nextstat();
        } else if (token.equals("<EXPRESSION>::=<BOOLAND>")) {
            root.place = root.children.get(0).place;
        } else if (token.equals("<EXPRESSION>::=<BOOLAND>'OR'<EXPRESSION>")) {
            root.place = newtemp();
            emit("jnz", root.children.get(0).place, "", Integer.toString(nextstat() + 4));
            emit("jnz", root.children.get(2).place, "", Integer.toString(nextstat() + 3));
            emit(":=", Integer.toString(0), "", root.place);
            emit("j", "", "", Integer.toString(nextstat() + 2));
            emit(":=", Integer.toString(1), "", root.place);
        } else if (token.equals("<BOOLAND>::=<BOOLNOT>")) {
            root.place = root.children.get(0).place;
        } else if (token.equals("<BOOLAND>::=<BOOLNOT>'AND'<BOOLAND>")) {
            root.place = newtemp();
            emit("jnz", root.children.get(0).place, "", Integer.toString(nextstat() + 2));
            emit("j", "", "", Integer.toString(nextstat() + 2));
            emit("jnz", root.children.get(2).place, "", Integer.toString(nextstat() + 3));
            emit(":=", Integer.toString(0), "", root.place);
            emit("j", "", "", Integer.toString(nextstat() + 2));
            emit(":=", Integer.toString(1), "", root.place);
        } else if (token.equals("<BOOLNOT>::=<COMP>")) {
            root.place = root.children.get(0).place;
        } else if (token.equals("<BOOLNOT>::='NOT'<COMP>")) {
            root.place = newtemp();
            emit("jnz", root.children.get(1).place, "", Integer.toString(nextstat() + 3));
            emit(":=", Integer.toString(1), "", root.place);
            emit("j", "", "", Integer.toString(nextstat() + 2));
            emit(":=", Integer.toString(0), "", root.place);
        } else if (token.equals("<COMP>::=<PLUSEX>")) {
            root.place = root.children.get(0).place;
        } else if (token.equals("<COMP>::=<PLUSEX>'RELOP'<COMP>")) {
            root.place = newtemp();
            switch (root.children.get(1).content.getValue()) {
                case 0:
                    emit("j<", root.children.get(0).place, root.children.get(2).place, Integer.toString(nextstat() + 3));
                    break;
                case 1:
                    emit("j<=", root.children.get(0).place, root.children.get(2).place, Integer.toString(nextstat() + 3));
                    break;
                case 2:
                    emit("j>", root.children.get(0).place, root.children.get(2).place, Integer.toString(nextstat() + 3));
                    break;
                case 3:
                    emit("j>=", root.children.get(0).place, root.children.get(2).place, Integer.toString(nextstat() + 3));
                    break;
                case 4:
                    emit("j==", root.children.get(0).place, root.children.get(2).place, Integer.toString(nextstat() + 3));
                    break;
                case 5:
                    emit("j!=", root.children.get(0).place, root.children.get(2).place, Integer.toString(nextstat() + 3));
                    break;
            }
            emit(":=", Integer.toString(0), "", root.place);
            emit("j", "", "", Integer.toString(nextstat() + 2));
            emit(":=", Integer.toString(1), "", root.place);
        } else if (token.equals("<PLUSEX>::=<TERM>")) {
            root.place = root.children.get(0).place;
        } else if (token.equals("<PLUSEX>::=<TERM>'OP1'<PLUSEX>")) {
            root.place = newtemp();
            switch (root.children.get(1).content.getValue()) {
                case 0:
                    emit("+", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
                case 1:
                    emit("-", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
                case 2:
                    emit("&", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
                case 3:
                    emit("|", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
                case 4:
                    emit("^", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
            }
        } else if (token.equals("<TERM>::=<FACTOR>")) {
            root.place = root.children.get(0).place;
        } else if (token.equals("<TERM>::=<FACTOR>'OP2'<TERM>")) {
            root.place = newtemp();
            switch (root.children.get(1).content.getValue()) {
                case 0:
                    emit("*", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
                case 1:
                    emit("/", root.children.get(0).place, root.children.get(2).place, root.place);
                    break;
            }
        } else if (token.equals("<FACTOR>::='NUM'")) {
            root.place = newtemp();
            emit(":=", Integer.toString(root.children.get(0).content.getValue()), "", root.place);
        } else if (token.equals("<FACTOR>::='LP'<EXPRESSION>'RP'")) {
            root.place = root.children.get(1).place;
        } else if (token.equals("<FACTOR>::='ID'")) {
            String p = lookup(root.children.get(0).content.getValue());
            if (p.equals("")) {
                String err = "ERROR: 未定义" + nameTable.get(root.children.get(0).content.getValue()) + "\n";
                globalUtils.errorLog(err);
            } else {
                root.place = p;
            }
        } else if (token.equals("<FACTOR>::=<ARRAY>")) {
            if (root.children.get(0).dimension.size() != 1) {
                String err = "ERROR: 遇到不完整的数组索引\n";
                globalUtils.errorLog(err);
            }
            String p = lookup(root.children.get(0).content.getValue());
            if (p.equals("")) {
                String err = "ERROR: 未定义:" + nameTable.get(root.children.get(0).content.getValue()) + "\n";
                globalUtils.errorLog(err);
            } else {
                root.place = newtemp();
                emit("=[]", p, root.children.get(0).place, root.place);
            }
        } else if (token.equals("<FACTOR>::='ID'<CALL>")) {
            SymbolTableItem f = find(root.children.get(0).content.getValue());
            if (f == null) {
                String err = "ERROR: 未定义:" + nameTable.get(root.children.get(0).content.getValue()) + "\n";
                globalUtils.errorLog(err);
            }
            if (f.dimension.get(0) != root.children.get(1).params.size()) {
                String err = "ERROR: 实参数量错误\n";
                globalUtils.errorLog(err);
            }

            emit(":=", "$ra", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t0", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t1", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t2", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t3", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t4", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t5", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t6", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$t7", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");

            emit(":=", "$sp", "", "$s0");
            emit(":=", "$fp", "", "[$sp]");
            emit("+", "$sp", "4", "$sp");
            emit(":=", "$s0", "", "$fp");

            for (int i = 0; i < root.children.get(1).params.size(); i++) {
                emit(":=", root.children.get(1).params.get(i), "", "[$sp]");
                emit("+", "$sp", "4", "$sp");
            }
            emit("jal", "", "", Integer.toString(f.offset));
            emit(":=", "$fp", "", "$sp");
            emit(":=", "[$sp]", "", "$fp");

            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t7");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t6");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t5");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t4");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t3");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t2");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t1");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$t0");
            emit("-", "$sp", "4", "$sp");
            emit(":=", "[$sp]", "", "$ra");

            root.place = newtemp();
            emit(":=", "$v0", "", root.place);

        } else if (token.equals("<FACTOR>::='LP'<ASSIGNMENT>'RP'")) {
            root.place = root.children.get(1).place;
        } else if (token.equals("<CALL>::='LP'<ACTUALPARAM>'RP'")) {
            root.params = root.children.get(1).params;
        } else if (token.equals("<ARRAY>::='ID''LS'<EXPRESSION>'RS'")) {
            SymbolTableItem e = find(root.children.get(0).content.getValue());
            if (e == null) {
                String err = "ERROR: 未定义:" + nameTable.get(root.children.get(0).content.getValue()) + "\n";
                globalUtils.errorLog(err);
            }
            root.content = root.children.get(0).content;
            root.k = globalUtils.kind.ARRAY;
            root.dimension = new ArrayList<>(e.dimension);

            if (root.dimension.size() == 0) {
                String err = "ERROR: 数组下标错误";
                globalUtils.errorLog(err);
            } else if (root.dimension.size() == 1) {
                root.place = root.children.get(2).place;
            } else {
                int dim_len = root.dimension.get(1);
                for (int i = 2; i < root.dimension.size(); i++) {
                    dim_len *= root.dimension.get(i);
                }
                String p = newtemp();
                emit(":=", Integer.toString(dim_len), "", p);
                root.place = newtemp();
                emit("*", p, root.children.get(2).place, root.place);
            }

        } else if (token.equals("<ARRAY>::=<ARRAY>'LS'<EXPRESSION>'RS'")) {

            root.content = root.children.get(0).content;
            root.k = globalUtils.kind.ARRAY;
            root.dimension = new ArrayList<>(root.children.get(0).dimension);
            root.dimension.remove(0);
            if(root.dimension.size() == 0) {
                String err = "ERROR: 数组下标错误";
                globalUtils.errorLog(err);
            } else if (root.dimension.size() == 1) {
                root.place = newtemp();
                emit("+", root.children.get(0).place, root.children.get(2).place, root.place);
            } else {
                int dim_len = root.dimension.get(1);
                for (int i = 2; i < root.dimension.size(); i++) {
                    dim_len *= root.dimension.get(i);
                }
                String p1 = newtemp();
                emit(":=", Integer.toString(dim_len), "", p1);
                String p2 = newtemp();
                emit("*", p1, root.children.get(2).place, p2);
                root.place = newtemp();
                emit("+", root.children.get(0).place, p2, root.place);
            }
        } else if (token.equals("<ACTUALPARAM>::=<ACTUALPARAMLIST>")) {
            root.params = root.children.get(0).params;
        } else if (token.equals("<ACTUALPARAM>::='epsilon'")) {
        } else if (token.equals("<ACTUALPARAMLIST>::=<EXPRESSION>")) {
            root.params.add(root.children.get(0).place);
        } else if (token.equals("<ACTUALPARAMLIST>::=<EXPRESSION>'SEP'<ACTUALPARAMLIST>")) {
            root.params = root.children.get(2).params;
            root.params.add(root.children.get(0).place);
        } else {
            String err = "ERROR: 语义分析器错误:找不到产生式 " + token + " 的语义分析子程序";
            globalUtils.errorLog(err);
        }
    }
}

