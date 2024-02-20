package org.saur.disjointset;

import lombok.Data;

@Data
public class DisjointSetInfo {
    private Integer parentNode;
    private int rank;
    DisjointSetInfo(Integer parent) {
        setParentNode(parent);
        setRank(0);
    }
}
