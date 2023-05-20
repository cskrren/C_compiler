package com.example.c_compiler.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class Closure {

    public List<Canonical> set;
    public Map<String, Integer> next;

    public Closure() {
        this.set = new ArrayList<>();
        this.next = new LinkedHashMap<>();
    }

    public String showCLOSURE(){
        String res = "";
        res += "----------------CLOSURE_SET-------------------\n";
        for (int i = 0; i < set.size(); i++)
        {
            res += set.get(i).p.getKey() + "->";
            for (int j = 0; j < set.get(i).p.getValue().size(); j++)
            {
                if (j == set.get(i).dot)
                    res += "·";
                res += set.get(i).p.getValue().get(j) + ' ';
            }
            if (set.get(i).dot == set.get(i).p.getValue().size())
                res += "·";
            res += "----";
            for (int j = 0; j < set.get(i).expect.size(); j++)
            {
                res += set.get(i).expect.get(j) + ' ';
            }
            res += '\n';
        }
        res += "----------------CLOSURE_SET-------------------\n";
        for (Map.Entry<String, Integer> entry : next.entrySet()) {
            res += entry.getKey() + " " + entry.getValue() + '\n';
        }
        res+='\n';
        return res;
    }
}