package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class TreeNode {
    public int level;
    public TreeNode parent;
    public List<TreeNode> children;
    public Pair<String, Integer> content;
    public globalUtils.type t;
    public globalUtils.kind k;
    public int n;
    public int width;
    public List<Integer> dimension;
    public List<String> params;
    public String place;
    int quad;
    int true_list;
    int false_list;
    int x;
    int y;

    public TreeNode() {
        this.level = -1;
        this.children = new ArrayList<>();
        this.content = new Pair<>("", -1);
        this.t = globalUtils.type.INT;
        this.k = globalUtils.kind.VAR;
        this.n = 0;
        this.width = 0;
        this.dimension = new ArrayList<>();
        this.params = new ArrayList<>();
        this.place = "";
        this.quad = 0;
        this.true_list = 0;
        this.false_list = 0;
        this.x = -1;
        this.y = -1;
        this.parent = null;
    }

    public void clear() {
        level = -1;
        children = new ArrayList<>();
        content = new Pair<String, Integer>("", -1);
        parent.clear();
    }

    public String toString() {
        return "kkk";
    }
}
