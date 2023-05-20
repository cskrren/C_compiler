package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.*;

@Data
@AllArgsConstructor
public class LexicalAnalysis {

    private final List<String> skip = new ArrayList<String>() {{
        add(" ");
        add("\t");
        add("\r");
    }};
    private final List<String> keyword = new ArrayList<String>() {{
        add("int");
        add("void");
        add("while");
        add("if");
        add("else");
        add("return");
    }};
    private final List<String> unary_operator = new ArrayList<String>() {{
        add("+");
        add("-");
        add("&");
        add("|");
        add("^");
        add("*");
        add("/");
        add("<");
        add(">");
        add("=");
        add(";");
        add(",");
        add("(");
        add(")");
        add("[");
        add("]");
        add("{");
        add("}");
        add("!");
    }};
    private final List<String> binary_operator = new ArrayList<String>() {{
        add("<=");
        add("!=");
        add("==");
        add(">=");
        add("&&");
        add("||");
    }};
    private final Map<String, Pair<String, Integer>> strToken = new LinkedHashMap<String, Pair<String, Integer>>() {{
        put("int", new Pair<String, Integer>("INT", -1));
        put("void", new Pair<String, Integer>("VOID", -1));
        put("id", new Pair<String, Integer>("ID", -1));
        put("(", new Pair<String, Integer>("LP", -1));
        put(")", new Pair<String, Integer>("RP", -1));
        put("[", new Pair<String, Integer>("LS", -1));
        put("]", new Pair<String, Integer>("RS", -1));
        put("{", new Pair<String, Integer>("LB", -1));
        put("}", new Pair<String, Integer>("RB", -1));
        put("!", new Pair<String, Integer>("NOT", -1));
        put("while", new Pair<String, Integer>("WHILE", -1));
        put("if", new Pair<String, Integer>("IF", -1));
        put("else", new Pair<String, Integer>("ELSE", -1));
        put("return", new Pair<String, Integer>("RETURN", -1));
        put("=", new Pair<String, Integer>("ASSIGN", -1));
        put("+", new Pair<String, Integer>("OP1", 0));
        put("-", new Pair<String, Integer>("OP1", 1));
        put("&", new Pair<String, Integer>("OP1", 2));
        put("|", new Pair<String, Integer>("OP1", 3));
        put("^", new Pair<String, Integer>("OP1", 4));
        put("*", new Pair<String, Integer>("OP2", 0));
        put("/", new Pair<String, Integer>("OP2", 1));
        put("<", new Pair<String, Integer>("RELOP", 0));
        put("<=", new Pair<String, Integer>("RELOP", 1));
        put(">", new Pair<String, Integer>("RELOP", 2));
        put(">=", new Pair<String, Integer>("RELOP", 3));
        put("==", new Pair<String, Integer>("RELOP", 4));
        put("!=", new Pair<String, Integer>("RELOP", 5));
        put("||", new Pair<String, Integer>("OR", -1));
        put("&&", new Pair<String, Integer>("AND", -1));
        put(";", new Pair<String, Integer>("DEL", -1));
        put(",", new Pair<String, Integer>("SEP", -1));
        put("\n", new Pair<String, Integer>("NL", -1));
    }};

    private String sourceFile;
    private int pos;
    private globalUtils.state S;
    private int symbolCount;

    public int retCode;
    public LinkedHashMap<Integer, String> nameTable;
    public List<Pair<String, Integer>> history;

    public LexicalAnalysis() {
        this.sourceFile = "";
        this.pos = 0;
        this.S = globalUtils.state.INIT;
        this.symbolCount = 0;
        this.retCode = 1;
        this.nameTable = new LinkedHashMap<Integer, String>();
        this.history = new ArrayList<Pair<String, Integer>>();
    }

    private boolean is_skip(String str) {
        for (String i : skip) {
            if (i.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean is_keyword(String str) {
        for (String i : keyword) {
            if (i.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean is_unary_operator(String str) {
        for (String i : unary_operator) {
            if (i.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean is_binary_operator(String str) {
        for (String i : binary_operator) {
            if (i.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean is_number(String str) {
        if (globalUtils.getChar(str, 0) >= '0' && globalUtils.getChar(str, 0) <= '9') {
            return true;
        } else {
            return false;
        }
    }

    public boolean is_letter(String str) {
        if (globalUtils.getChar(str, 0) >= 'a' && globalUtils.getChar(str, 0) <= 'z' || globalUtils.getChar(str, 0) >= 'A' && globalUtils.getChar(str, 0) <= 'Z') {
            return true;
        } else {
            return false;
        }
    }

    public void setFileString(String fstring) {
        sourceFile = fstring;
    }

    public Pair<String, Integer> getLexic() {
        String buf = "";
        while (pos < sourceFile.length()) {
            String first_sym = Character.toString(globalUtils.getChar(sourceFile, pos));
            String second_sym = Character.toString(globalUtils.getChar(sourceFile, pos + 1));
            String double_sym = first_sym + second_sym;
            switch (S) {
                case INIT:
                    if (is_skip(first_sym)) {
                        pos++;
                        S = globalUtils.state.INIT;
                    } else if (first_sym.equals("\n")) {
                        pos++;
                        S = globalUtils.state.INIT;
                        retCode++;
                        history.add(strToken.get(first_sym));
                        return strToken.get(first_sym);
                    } else if (first_sym.equals("/")) {
                        if (second_sym.equals("/")) {
                            pos += 2;
                            S = globalUtils.state.SINGLEANNOTATION;
                        } else if (second_sym.equals("*")) {
                            pos += 2;
                            S = globalUtils.state.MULTIANNOTATION;
                        } else {
                            S = globalUtils.state.OPERATOR;
                        }
                    } else if (is_unary_operator(first_sym)) {
                        S = globalUtils.state.OPERATOR;
                    } else if (is_number(first_sym) || (is_letter(first_sym) || first_sym.equals("_"))) {
                        pos++;
                        if (is_number(first_sym)) {
                            S = globalUtils.state.NUMBER;
                        } else {
                            S = globalUtils.state.STRING;
                        }
                        buf = first_sym;
                    } else {
                        S = globalUtils.state.ERROR;
                    }
                    break;
                case NUMBER:
                    if (is_number(first_sym)) {
                        pos++;
                        buf += first_sym;
                        S = globalUtils.state.NUMBER;
                    } else {
                        S = globalUtils.state.INIT;
                        history.add(new Pair<>("NUM", Integer.parseInt(buf.toString())));
                        return new Pair<>("NUM", Integer.parseInt(buf.toString()));
                    }
                    break;
                case OPERATOR:
                    if (is_binary_operator(double_sym)) {
                        pos += 2;
                        S = globalUtils.state.INIT;
                        history.add(strToken.get(double_sym));
                        return strToken.get(double_sym);
                    } else {
                        pos++;
                        S = globalUtils.state.INIT;
                        history.add(strToken.get(first_sym));
                        return strToken.get(first_sym);
                    }
                case STRING:
                    if (is_letter(first_sym) || first_sym.equals("_") || is_number(first_sym)) {
                        pos++;
                        buf += first_sym;
                        S = globalUtils.state.STRING;
                    } else {
                        if (is_keyword(buf)) {
                            S = globalUtils.state.INIT;
                            history.add(strToken.get(buf));
                            return strToken.get(buf);
                        } else {
                            S = globalUtils.state.INIT;
                            boolean flag = true;
                            int id = 0;
                            for (Map.Entry<Integer, String> entry : nameTable.entrySet()) {
                                if (entry.getValue().equals(buf)) {
                                    flag = false;
                                    id = entry.getKey();
                                    break;
                                }
                            }
                            if (flag) {
                                nameTable.put(symbolCount, buf);
                                history.add(new Pair<>("ID", symbolCount));
                                return new Pair<>("ID", symbolCount++);
                            } else {
                                history.add(new Pair<>("ID", id));
                                return new Pair<>("ID", id);
                            }
                        }
                    }
                    break;
                case SINGLEANNOTATION:
                    if (!first_sym.equals("\n")) {
                        pos++;
                        S = globalUtils.state.SINGLEANNOTATION;
                    } else {
                        pos++;
                        S = globalUtils.state.INIT;
                        retCode++;
                        history.add(strToken.get(first_sym));
                        return strToken.get(first_sym);
                    }
                    break;
                case MULTIANNOTATION:
                    if (first_sym.equals("*") && second_sym.equals("/")) {
                        pos += 2;
                        S = globalUtils.state.INIT;
                    } else if (first_sym.equals("\n")) {
                        pos++;
                        S = globalUtils.state.MULTIANNOTATION;
                        retCode++;
                        history.add(strToken.get(first_sym));
                        return strToken.get(first_sym);
                    } else {
                        pos++;
                        S = globalUtils.state.MULTIANNOTATION;
                    }
                    break;
                case ERROR:
                    String err = "ERROR: 出错位置在第" + retCode + "行";
                    globalUtils.errorLog(err);
                    history.add(new Pair<>("globalUtils.state.ERROR", -1));
                    return new Pair<>("globalUtils.state.ERROR", -1);
            }
        }
        history.add(new Pair<>("#", -1));
        return new Pair<>("#", -1);
    }
}
