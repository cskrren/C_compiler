package com.example.c_compiler.domain.entity;

import com.example.c_compiler.utils.globalUtils;
import lombok.*;

import java.util.*;

@Setter
@Getter
@AllArgsConstructor
public class SymbolTable {
    public SymbolTable previous;
    public SymbolTable next;
    public SymbolTable parent;
//    public Map<String, SymbolTable> pointer;
    public int width;
    public List<SymbolTableItem> table;

    public SymbolTable() {
        this.previous = null;
        this.next = null;
        this.parent = null;
        this.width = 0;
        this.table = new ArrayList<>();
    }

    public void clear() {
        width = 0;
        table = new ArrayList<>();
        previous.clear();
        next.clear();
    }

    public void enter(int id, globalUtils.type t, globalUtils.kind k, int offset) {
        SymbolTableItem e = new SymbolTableItem();
        e.id = id;
        e.t = t;
        e.k = k;
        e.offset = offset;
        table.add(e);
    }

    public void enterdimension(int id, List<Integer> dimension) {
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).id == id && ((table.get(i).k == globalUtils.kind.ARRAY) || (table.get(i).k == globalUtils.kind.FUNC))) {
                table.get(i).dimension = dimension;
                break;
            }
        }
    }

    public void enterproc(int id, SymbolTable newtable) {
        for (int i = 0; i < table.size(); i++) {
            if (table.get(i).id == id && table.get(i).k == globalUtils.kind.FUNC) {
                table.get(i).proctable = newtable;
                break;
            }
        }
    }
}
