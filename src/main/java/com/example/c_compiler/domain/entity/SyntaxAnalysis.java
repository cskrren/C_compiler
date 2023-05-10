package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;


@Data
@AllArgsConstructor
public class SyntaxAnalysis {
    private int retcode;
    public Grammar G;
    public LexicalAnalysis L;
    public SemanticAnalysis S;
    public List<List<List<String>>> history;
    public TreeNode reductionTreeRoot;
    public int maxTreeLevel;
    public int leafNum;

    public SyntaxAnalysis() {
        this.retcode = 0;
        this.G = new Grammar();
        this.L = new LexicalAnalysis();
        this.S = new SemanticAnalysis();
        this.history = new ArrayList<>();
        this.reductionTreeRoot = new TreeNode();
        this.maxTreeLevel = 0;
        this.leafNum = 0;
    }

    private void _genTreeLevel(TreeNode nownode, Integer nowlevel) {
        if (nowlevel > maxTreeLevel) {
            maxTreeLevel = nowlevel;
        }
        if (nownode.children.size() == 0) {
            leafNum++;
        }
        nownode.level = nowlevel;
        for (int i = 0; i < nownode.children.size(); i++) {
            _genTreeLevel(nownode.children.get(i), nowlevel + 1);
        }
    }

    private void _showTree2(TreeNode nownode) {
        for (int i = 0; i < nownode.level; i++)
            System.out.print("|--");
        System.out.print("< " + nownode.content.getKey() + ", ");
        if (nownode.content.getKey().equals("ID")) {
            System.out.print(L.nameTable.get(nownode.content.getValue()));
        } else {
            System.out.print(nownode.content.getValue());
        }
        System.out.println(" >");
        for (int i = 0; i < nownode.children.size(); i++)
            _showTree2(nownode.children.get(i));
    }

    public void initializeLR1()
    {
        G.readGrammar();
        //G.showGrammar();
        G.genFIRST();
        G.showFIRST();
        G.analysisLR1();
        //G.showLR1();
        G.genLR1Table();
        //G.showTable();
    }

    public void getInput(String input) {
        L.setFileString(input);
    }

    public void analysis() {
        List<Integer> state = new ArrayList<>();
        List<Pair<String, Integer>> symbol = new ArrayList<Pair<String,Integer>>();
        state.add(0);
        symbol.add(new Pair<>("#", -1));
        Pair<String, Integer> epsilon_lexis = new Pair<>("epsilon", -1);
        Pair<String, Integer> epsilon_next_lexis = new Pair<>("", 0);
        Pair<String, Integer> lexis = L.getLexic();
        retcode = 1;
        Stack<TreeNode> treeNodeStack = new Stack<>();
        TreeNode tp;
        List<String> strstate = new ArrayList<>();
        List<String> strsymbol = new ArrayList<>();
        List<String> input = new ArrayList<>();
        do {
            if (lexis.getKey().equals("NL")) {
                retcode++;
                lexis = L.getLexic();
                System.out.println(lexis);
                continue;
            }

            if (!G.VT.contains(lexis.getKey())) {
                String err = "ERROR: " + lexis.getKey() + " is not a terminal symbol\n";
                globalUtils.errorLog(err);
            }

            ActionItem item = G.ACTION.get(state.get(state.size() - 1)).get(G.VT.indexOf(lexis.getKey()));
            switch (item.getStatus()) {
                case ACTION_ACC:
                    _genTreeLevel(reductionTreeRoot, 0);
                    return;
                case ACTION_ERROR:
                    if (!lexis.equals(epsilon_lexis)) {
                        epsilon_next_lexis = lexis;
                        lexis = epsilon_lexis;
                    } else {
                        String err = "ERROR: Syntax error on line " + retcode + "\n";
                        globalUtils.errorLog(err);
                    }
                    break;
                case ACTION_STATE:
                    tp = new TreeNode();
                    tp.setContent(lexis);
                    treeNodeStack.push(tp);

                    state.add(item.getNextState());
                    symbol.add(lexis);

                    if (lexis.equals(epsilon_lexis)) {
                        lexis = epsilon_next_lexis;
                    } else {
                        lexis = L.getLexic();
                    }
                    System.out.println(lexis);
                    break;
                case ACTION_REDUCTION:
                    List<String> prod = item.p.getValue();
                    for (int i = prod.size() - 1; i >= 0; i--) {
                        if (symbol.get(symbol.size() - 1).getKey().equals(prod.get(i))) {
                            symbol.remove(symbol.size() - 1);
                            state.remove(state.size() - 1);
                        } else {
                            String err = "ERROR: Syntax error on line " + retcode + "\n";
                            globalUtils.errorLog(err);
                        }
                    }
                    tp = new TreeNode();
                    tp.setContent(new Pair<>(item.p.getKey(), -1));
                    for (String s : prod) {
                        treeNodeStack.peek().setParent(tp);
                        tp.getChildren().add(treeNodeStack.peek());
                        treeNodeStack.pop();
                    }
                    Collections.reverse(tp.children);
                    reductionTreeRoot = tp;
                    treeNodeStack.push(tp);

                    String token = "<" + item.p.getKey() + ">::=";
                    for (String s : item.p.getValue()) {
                        if (G.getVN().contains(s))
                            token += "<" + s + ">";
                        else
                            token += "'" + s + "'";
                    }
                    try {
                        System.out.println(token);
                        S.analysis(token, tp, L.nameTable);
                    } catch (Exception e) {
                        String err = "ERROR: 在第" + retcode + "行出现语法错误\n";
                        globalUtils.errorLog(err);
                    }

                    symbol.add(new Pair<>(item.p.getKey(), -1));
                    state.add(G.GOTO.get(state.get(state.size() - 1)).get(G.getVN().indexOf(symbol.get(symbol.size() - 1).getKey())));

                    strstate = new ArrayList<>();
                    strsymbol = new ArrayList<>();
                    input = new ArrayList<>();
                    for (int i = 0; i < state.size(); i++) {
                        strstate.add(String.valueOf(state.get(i)));
                    }
                    for (int i = 0; i < symbol.size(); i++) {
                        strsymbol.add(symbol.get(i).getKey());
                    }
                    input.add(lexis.getKey());
                    List<List<String>> newList = new ArrayList<>();
                    newList.add(strstate);
                    newList.add(strsymbol);
                    newList.add(input);
                    history.add(newList);
            }
        }
        while (!lexis.getKey().equals("ERROR")) ;
    }

    public void showHistory() {
        System.out.println("**************** History ****************");
        for (int i = 0; i < history.size(); i++) {
            System.out.print("---Step:" + i + "---");
            for (int j = 0; j < history.get(i).size(); j++) {
                for (int k = 0; k < history.get(i).get(j).size(); k++) {
                    System.out.print(history.get(i).get(j).get(k) + " ");
                }
                System.out.print("\t\t\t");
            }
            System.out.println();
        }
        System.out.println("**************** End ****************");
    }

    public void showTree() {
        System.out.println("**************** ReductionTree ****************");
        List<TreeNode> idstack = new ArrayList<>();
        int nowlevel = reductionTreeRoot.getLevel();
        idstack.add(reductionTreeRoot);
        while (!idstack.isEmpty()) {
            TreeNode nownode = idstack.get(0);
            idstack.remove(0);
            if (nownode.getLevel() == nowlevel) {
                System.out.print(nownode.getLevel() + " ");
            } else {
                System.out.print("\n" + nownode.getLevel() + " ");
                nowlevel++;
            }
            for (TreeNode child : nownode.getChildren()) {
                idstack.add(child);
            }
        }
        System.out.println("**************** End ****************");
    }

    public void showTree2() {
        System.out.println("**************** ReductionTree2 ****************");
        _showTree2(reductionTreeRoot);
        System.out.println("**************** End ****************");
    }

}