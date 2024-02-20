package org.saur.disjointset;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DisjointSets {
    @Getter
    private List<DisjointSetInfo> nodes;

    // ToDo: сделать статический креатор
    public void initDisjointSets(int totalNodes) {
        nodes = new ArrayList<>(totalNodes);
        for (int i = 0; i < totalNodes; i++) {
            nodes.add(new DisjointSetInfo(i));
        }
    }

    public Integer find(Integer node) {
        DisjointSetInfo setInfo = nodes.get(node);
        Integer parent = setInfo.getParentNode();
        if (parent.equals(node)) {
            return node;
        } else {
            Integer parentNode = find(parent);
            setInfo.setParentNode(parentNode);
            return parentNode;
        }
    }

    public void union(int rootU, int rootV) {
        if (rootU == rootV) return;
        DisjointSetInfo setInfoU = nodes.get(rootU);
        DisjointSetInfo setInfoV = nodes.get(rootV);
        int rankU = setInfoU.getRank();
        int rankV = setInfoV.getRank();
        if (rankU < rankV) {
            setInfoU.setParentNode(rootV);
        } else {
            setInfoV.setParentNode(rootU);
            if (rankU == rankV) {
                setInfoU.setRank(rankU + 1);
            }
        }
    }

    public void unionAll(int rootU, Set<Integer> rootsV) {
        rootsV.forEach(rootV -> union(rootU, rootV));
    }
}
