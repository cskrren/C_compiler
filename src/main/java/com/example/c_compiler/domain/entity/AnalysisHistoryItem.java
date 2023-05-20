package com.example.c_compiler.domain.entity;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class AnalysisHistoryItem {
    public TASitem TAS;
    public List<String> object_codes;
    public Map<String, List<Pair<String, Integer>>> RVALUE;
    public Map<String, List<String>> AVALUE;

    public AnalysisHistoryItem() {
        this.TAS = new TASitem();
        this.object_codes = new ArrayList<>();
        this.RVALUE = new LinkedHashMap<>();
        this.AVALUE = new LinkedHashMap<>();
    }

    public AnalysisHistoryItem(TASitem tas, List<String> code, Map<String, List<Pair<String, Integer>>> rvalue, Map<String, List<String>> avalue) {
        this.TAS = new TASitem(tas);
        this.object_codes = new ArrayList<>(code);
        this.RVALUE = new LinkedHashMap<>(rvalue);
        this.AVALUE = new LinkedHashMap<>(avalue);
    }
}
