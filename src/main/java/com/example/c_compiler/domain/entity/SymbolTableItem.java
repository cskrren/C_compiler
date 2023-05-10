package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SymbolTableItem {
    public int id;
    public globalUtils.type t;
    public globalUtils.kind k;
    public int offset;
    public List<Integer> dimension;
    public SymbolTable proctable;

    public SymbolTableItem() {
        this.id = 0;
        this.t = globalUtils.type.INT;
        this.k = globalUtils.kind.VAR;
        this.offset = 0;
        this.dimension = new ArrayList<>();
        this.proctable = new SymbolTable();
    }
}