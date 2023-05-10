package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ActionItem {

    public globalUtils.actionStatus status;
    public int nextState;
    public Pair<String, List<String>> p;

    public ActionItem() {
        this.status = globalUtils.actionStatus.ACTION_ERROR;
        this.nextState = -1;
        this.p = new Pair<String, List<String>>("", new ArrayList<String>());
    }
}