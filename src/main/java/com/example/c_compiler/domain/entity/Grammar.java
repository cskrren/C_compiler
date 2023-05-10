package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class Grammar {
    private String S;
    private Map<String, List<String>> FIRST;
    private Map<Integer, Closure> collection;
    public List<String> VN;
    public List<String> VT;
    public Map<String, List<List<String>>> P;
    public Map<Integer, List<ActionItem>> ACTION;
    public Map<Integer, List<Integer>> GOTO;

    public Grammar() {
        this.S = "";
        this.FIRST = new LinkedHashMap<>();
        this.collection = new LinkedHashMap<>();
        this.VN = new ArrayList<>();
        this.VT = new ArrayList<>();
        this.P = new LinkedHashMap<>();
        this.ACTION = new LinkedHashMap<>();
        this.GOTO = new LinkedHashMap<>();
    }
    private Closure genNext(Closure c, String A) {
        Closure ret = new Closure();
        for (int i = 0; i < c.set.size(); i++) {
            Canonical can = c.set.get(i);
            if (can.dot < can.p.getValue().size() && can.p.getValue().get(can.dot).equals(A)) {
                Canonical newcan = new Canonical(can.p,can.dot,can.expect);
                newcan.dot++;
                ret.set.add(newcan);
            }
        }
        return ret;
    }

    private List<String> genX(Closure c) {
        List<String> ret = new ArrayList<>();
        for (int i = 0; i < c.set.size(); i++) {
            Canonical can = c.set.get(i);
            if (can.dot < can.p.getValue().size()) {
                ret.add(can.p.getValue().get(can.dot));
            }
        }
        List<String> uniqueList = ret.stream().distinct().collect(Collectors.toList());
        return uniqueList;
    }

    private boolean canonicalSetSame(List<Canonical> a, List<Canonical> b) {
        if (a.size() != b.size()) {
            return false;
        } else {
            for (int i = 0; i < a.size(); i++) {
                boolean flag = false;
                for (int j = 0; j < b.size(); j++) {
                    if (a.get(i).equals(b.get(j))) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    return false;
                }
            }
            return true;
        }
    }

    public void readGrammar() {
        try {
            BufferedReader f = new BufferedReader(new FileReader(globalUtils.GRAMMARFILE));
            String buf, tmpV;
            Pair<String, List<List<String>>> tmpP;
            List<String> branch = new ArrayList<>();
            boolean flag = false;
            while ((buf = f.readLine()) != null) {
                flag = false;
                tmpP = new Pair<>("", new ArrayList<>());
                branch = new ArrayList<>();
                for (int i = 0; i < buf.length(); i++) {
                    if(globalUtils.getChar(buf,i) == '<') {
                        tmpV = "";
                        i++;
                        while(globalUtils.getChar(buf,i) != '>') {
                            tmpV += globalUtils.getChar(buf,i);
                            i++;
                        }
                        VN.add(tmpV);
                        if (!flag) {
                            List<List<String>> tmp = tmpP.getValue();
                            tmpP = new Pair<>(tmpV, tmp);
                        } else {
                            branch.add(tmpV);
                        }
                    } else if (globalUtils.getChar(buf,i)  == '\'') {
                        tmpV = "";
                        i++;
                        while (globalUtils.getChar(buf,i)  != '\'') {
                            tmpV += globalUtils.getChar(buf,i);
                            i++;
                        }
                        VT.add(tmpV);
                        branch.add(tmpV);
                    } else if (globalUtils.getChar(buf,i) == ':') {
                        while (globalUtils.getChar(buf,i) != '=') {
                            i++;
                        }
                        flag = true;
                    } else if (globalUtils.getChar(buf,i) == '|') {
                        tmpP.getValue().add(branch);
                        branch = new ArrayList<>();
                    }
                }
                tmpP.getValue().add(branch);
                P.put(tmpP.getKey(), tmpP.getValue());
                tmpP = new Pair<>("", new ArrayList<>());
                branch = new ArrayList<>();
            }
            f.close();
            S = VN.get(0);
            globalUtils.removeDuplicates(VN);
            globalUtils.removeDuplicates(VT);
            VT.add("#");
            if (VN.size() != P.size()) {
                String err = "grammar产生式与非终结符数目不对应";
                globalUtils.errorLog(err);
            }
        } catch (IOException e) {
            String err = "ERROR: grammar文件丢失\n";
            globalUtils.errorLog(err);
        }
    }

    public void showGrammar() {
        System.out.println("**************** Grammar ****************");
        System.out.println("**************** VN ****************");
        for (int i = 0; i < VN.size(); i++)
            System.out.print("(" + (i + 1) + ") " + VN.get(i) + " ");
        System.out.println("\n**************** VT ****************");
        for (int i = 0; i < VT.size(); i++)
            System.out.print("(" + (i + 1) + ") " + VT.get(i) + " ");
        System.out.println("\n**************** S ****************");
        System.out.println(S);
        System.out.println("**************** P ****************");
        int cnt = 1;
        for (Map.Entry<String, List<List<String>>> entry : P.entrySet()) {
            String key = entry.getKey();
            List<List<String>> value = entry.getValue();
            System.out.print("(" + cnt + ") " + key + "->");
            for (List<String> v : value) {
                for (String s : v) {
                    System.out.print(s + " ");
                }
                System.out.print("|");
            }
            System.out.println();
            cnt++;
        }
        System.out.println("**************** End ****************");
    }

    public void genFIRST() {
        for (int i = 0; i < VT.size(); i++) {
            FIRST.put(VT.get(i), new ArrayList<>(Arrays.asList(VT.get(i))));
        }
        boolean ischange = true;
        while (ischange) {
            ischange = false;
            for (Map.Entry<String, List<List<String>>> entry : P.entrySet()) {
                String nonterm = entry.getKey();
                if (!FIRST.containsKey(nonterm))
                    FIRST.put(nonterm, new ArrayList<>());
                for (List<String> prod : entry.getValue()) {
                    if(FIRST.containsKey(prod.get(0))) {
                        if (prod.size() > 0){
                            for (String f : FIRST.get(prod.get(0))) {
                                if (!FIRST.get(nonterm).contains(f)) {
                                    FIRST.get(nonterm).add(f);
                                    ischange = true;
                                }
                            }
                        } else {
                            String err = "ERROR: 产生式右侧为空\n";
                            globalUtils.errorLog(err);
                        }
                    }
                }
            }
        }
    }

    public void showFIRST() {
        System.out.println("**************** FIRST ****************");
        for (String i : FIRST.keySet()) {
            System.out.print("FIRST[" + i + "]={");
            for (String j : FIRST.get(i)) {
                System.out.print(j + ",");
            }
            System.out.println("}");
        }
        System.out.println("**************** End ****************");
    }

    public void genCLOSURE(Closure c) {
        for (int i = 0; i < c.set.size(); i++) {
            if (c.set.get(i).dot >= c.set.get(i).p.getValue().size())
                continue;
            String dotV = c.set.get(i).p.getValue().get(c.set.get(i).dot);
            if (VN.contains(dotV)) {
                for (int j = 0; j < P.get(dotV).size(); j++) {
                    Canonical newcan = new Canonical();
                    newcan.p = new Pair<>(dotV, P.get(dotV).get(j));
                    newcan.dot = 0;
                    int k = c.set.get(i).dot + 1;
                    if (k == c.set.get(i).p.getValue().size()) {
                        newcan.expect.addAll(c.set.get(i).expect);
                    } else {
                        String v = c.set.get(i).p.getValue().get(k);
                        for (int r = 0; r < FIRST.get(v).size(); r++) {
                            newcan.expect.add(FIRST.get(v).get(r));
                        }
                    }
                    boolean isrepeat = false;
                    for (int t = 0; t < c.set.size(); t++) {
                        if (c.set.get(t).p.equals(newcan.p) && c.set.get(t).dot == newcan.dot) {
                            isrepeat = true;
                            c.set.get(t).expect.addAll(newcan.expect);
                            Set<String> tempSet = new HashSet<>(c.set.get(t).expect);
                            c.set.get(t).expect = new ArrayList<>(tempSet);
                            Collections.sort(c.set.get(t).expect);
                            break;
                        }
                    }
                    if (!isrepeat) {
                        Collections.sort(newcan.expect);
                        c.set.add(newcan);
                    }
                }
            }
        }
    }

    public void showCLOSURE(Closure c) {
        System.out.println("**************** CLOSURE_SET ****************");
        for (int i = 0; i < c.set.size(); i++) {
            System.out.print(c.set.get(i).p.getKey() + "->");
            for (int j = 0; j < c.set.get(i).p.getValue().size(); j++) {
                if (j == c.set.get(i).dot)
                    System.out.print("·");
                System.out.print(c.set.get(i).p.getValue().get(j) + " ");
            }
            if (c.set.get(i).dot == c.set.get(i).p.getValue().size())
                System.out.print("·");
            System.out.print(" --- ");
            for (int j = 0; j < c.set.get(i).expect.size(); j++)
                System.out.print(c.set.get(i).expect.get(j) + " ");
            System.out.println();
        }
        System.out.println("**************** CLOSURE_NEXT ****************");
        Iterator<Map.Entry<String, Integer>> it = c.next.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            System.out.println(entry.getKey() + "->" + entry.getValue());
        }
        System.out.println("**************** End ****************");
    }

    public void analysisLR1() {
        int count = 0;
        Stack<Integer> wait = new Stack<>();
        Closure I = new Closure(new ArrayList<>(), new LinkedHashMap<>());
        Canonical start = new Canonical();
        start.p = new Pair<>("S'", new ArrayList<>(Arrays.asList(S)));
        start.dot = 0;
        start.expect.add("#");
        I.set.add(start);
        genCLOSURE(I);
        collection.put(count, I);
        wait.push(count);
        count++;
        while (!wait.empty()) {
            int now = wait.pop();
            List<String> XList = genX(collection.get(now));
            for (int i = 0; i < XList.size(); i++) {
                Closure newclo = genNext(collection.get(now), XList.get(i));
                genCLOSURE(newclo);
                int exist = -1;
                for (int key : collection.keySet()) {
                    if (canonicalSetSame(collection.get(key).set, newclo.set)) {
                        exist = key;
                        break;
                    }
                }
                if (exist == -1) {
                    collection.get(now).next.put(XList.get(i), count);
                    collection.put(count, newclo);
                    wait.push(count);
                    count++;
                } else {
                    collection.get(now).next.put(XList.get(i), exist);
                }
            }
        }
    }


    public void showLR1() {
        for (Map.Entry<Integer, Closure> entry : collection.entrySet()) {
            System.out.println("**********************************************");
            System.out.println("No:" + entry.getKey());
            showCLOSURE(entry.getValue());
            System.out.println("**********************************************");
        }
    }


    public void genLR1Table() {
        for (Map.Entry<Integer, Closure> entry : collection.entrySet()) {
            int p = entry.getKey();
            Closure clo = entry.getValue();
            List<ActionItem> newaction = new ArrayList<>();
            List<Integer> newgoto = new ArrayList<>();
            for (int i = 0; i < VT.size(); i++) {
                newaction.add(new ActionItem());
            }
            for (int i = 0; i < VN.size(); i++) {
                newgoto.add(-1);
            }
            List<Canonical> set = clo.set;
            for (Map.Entry<String, Integer> q : clo.next.entrySet()) {
                int pos = -1;
                if ((pos = VN.indexOf(q.getKey())) != -1) {
                    newgoto.set(pos, q.getValue());
                } else if ((pos = VT.indexOf(q.getKey())) != -1) {
                    newaction.get(pos).status = globalUtils.actionStatus.ACTION_STATE;
                    newaction.get(pos).nextState = q.getValue();
                } else {
                    String err = "ERROR: 生成ACTION/GOTO表时遇到问题\n";
                    globalUtils.errorLog(err);
                }
            }
            for (int i = 0; i < set.size(); i++) {
                if (set.get(i).dot == set.get(i).p.getValue().size()) {
                    if (set.get(i).p.getKey().equals("S'")) {
                        newaction.get(newaction.size() - 1).status = globalUtils.actionStatus.ACTION_ACC;
                    } else {
                        Canonical tmpcan = set.get(i);
                        for (int j = 0; j < tmpcan.expect.size(); j++) {
                            int pos = VT.indexOf(tmpcan.expect.get(j));
                            newaction.get(pos).status = globalUtils.actionStatus.ACTION_REDUCTION;
                            newaction.get(pos).p = tmpcan.p;
                        }
                    }
                }
            }
            ACTION.put(p, newaction);
            GOTO.put(p, newgoto);
        }
    }

    public void showTable() {
        if (ACTION.size() != GOTO.size()) {
            String err = "ERROR: ACTION表和GOTO表长度不同\n";
            globalUtils.errorLog(err);
        }
        for (Map.Entry<Integer, List<ActionItem>> entry : ACTION.entrySet()) {
            int state = entry.getKey();
            List<ActionItem> actionList = entry.getValue();
            System.out.print(state + "\t");
            for (int i = 0; i < actionList.size(); i++) {
                ActionItem item = actionList.get(i);
                switch (item.status) {
                    case ACTION_ACC:
                        System.out.print("ACC\t");
                        break;
                    case ACTION_STATE:
                        System.out.print("s" + item.nextState + "\t");
                        break;
                    case ACTION_REDUCTION:
                        System.out.print("r:" + item.p.getKey() + "->");
                        for (int j = 0; j < item.p.getValue().size(); j++) {
                            System.out.print(item.p.getValue().get(j) + " ");
                        }
                        break;
                    case ACTION_ERROR:
                        System.out.print("\t");
                        break;
                    default:
                        break;
                }
                System.out.print("\t");
            }
            System.out.print("\t");
            List<Integer> gotoList = GOTO.get(state);
            for (int i = 0; i < gotoList.size(); i++) {
                System.out.print(gotoList.get(i) + " ");
            }
            System.out.println();
        }
    }
}