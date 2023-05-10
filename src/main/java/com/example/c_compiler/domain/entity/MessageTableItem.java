package com.example.c_compiler.domain.entity;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class MessageTableItem {
    public int no = 0;
    public TASitem TAS;
    public Pair<Integer, Boolean> arg1_tag;
    public Pair<Integer, Boolean> arg2_tag;
    public Pair<Integer, Boolean> result_tag;

    public MessageTableItem() {
        this.TAS = new TASitem();
        this.arg1_tag = new Pair<Integer, Boolean>(0, false);
        this.arg2_tag = new Pair<Integer, Boolean>(0, false);
        this.result_tag = new Pair<Integer, Boolean>(0, false);
    }
}
