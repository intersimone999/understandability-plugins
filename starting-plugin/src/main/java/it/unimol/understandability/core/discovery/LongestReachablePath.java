package it.unimol.understandability.core.discovery;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.structures.ReferenceGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

/**
 * Created by simone on 22/12/16.
 */
public class LongestReachablePath {
    private final Project project;
    private ReferenceGraph graph;
    private Map<PsiElement, Set<PsiElement>> reachingSets;
    private Map<PsiElement, Set<PsiElement>> directReachingSets;

    public LongestReachablePath(Project project, ReferenceGraph graph) {
        this.graph = graph;
        this.project = project;
    }

    public Map<PsiElement, Set<PsiElement>> getReachingSets() {
        if (this.reachingSets == null) {
            this.reachingSets = computeReachingSets();
        }

        return this.reachingSets;
    }

    private Map<PsiElement, Set<PsiElement>> getDirectReachingSets() {
        if (this.directReachingSets == null) {
            this.directReachingSets = computeDirectReachingSets();
        }

        return this.directReachingSets;
    }

    public Map<PsiElement, ReachingSet> getCompleteReachingSets(int maxLevel) {
        Map<PsiElement, ReachingSet> result = new HashMap<>();

        for (Map.Entry<PsiElement, Set<PsiElement>> entry : this.getDirectReachingSets().entrySet()) {
            ReachingSet set = new ReachingSet(entry.getKey(), maxLevel);

            this.computeReachableAtLevels(set);

            result.put(entry.getKey(), set);
        }

        return result;
    }

    private void computeReachableAtLevels(ReachingSet set) {
        Map<PsiElement, Double> pageRank = this.graph.getPageRank();

        Queue<PsiElement> nextLevelQueue;
        Queue<PsiElement> currentLevelQueue = new LinkedList<>();
        int currentLevel = 1;

        currentLevelQueue.addAll(this.getDirectReachingSets().get(set.getElement()));
        Set<PsiElement> alreadyAnalyzed = new HashSet<>();
        while (currentLevel < set.getMaxLevel()) {
            nextLevelQueue = new LinkedList<>();

            currentLevelQueue.removeAll(alreadyAnalyzed);
            set.setReachable(currentLevel, new HashSet<>(currentLevelQueue));
            while (!currentLevelQueue.isEmpty()) {
                PsiElement current = currentLevelQueue.poll();

                alreadyAnalyzed.add(current);

                nextLevelQueue.addAll(this.directReachingSets.get(current));
            }

            currentLevelQueue = nextLevelQueue;
            currentLevel++;
        }

        set.updateScore(pageRank);
    }
//
//    public PsiElement getLongestReachingElement() {
//        PsiElement longest = null;
//        int longestSize = 0;
//
//        for (Map.Entry<PsiElement, Set<PsiElement>> entry : this.getReachingSets().entrySet()) {
//            if (entry.getValue().size() > longestSize) {
//                longestSize = entry.getValue().size();
//                longest = entry.getKey();
//            }
//        }
//
//        return longest;
//    }

    private Map<PsiElement, Set<PsiElement>> computeDirectReachingSets() {
        Map<PsiElement, Set<PsiElement>> result = new HashMap<>();

        for (PsiElement vertex : this.graph.vertexSet()) {
            Set<PsiElement> directReferences = new HashSet<>();

            for (DefaultEdge edge : this.graph.outgoingEdgesOf(vertex)) {
                PsiElement element = this.graph.getEdgeTarget(edge);
                //Only keeps into account in-project artifacts
                if (element.getContainingFile().getVirtualFile().getCanonicalPath().startsWith(this.project.getBaseDir().getCanonicalPath()))
                    directReferences.add(element);
            }

            result.put(vertex, directReferences);
        }

        return result;
    }

    private Map<PsiElement, Set<PsiElement>> computeReachingSets() {
        Map<PsiElement, Set<PsiElement>> result = new HashMap<>();

        result.putAll(this.getDirectReachingSets());

        boolean stable;

        do {
            stable = true;
            int unstables = 0;

            for (Map.Entry<PsiElement, Set<PsiElement>> entry: result.entrySet()) {
                Set<PsiElement> copySet = new HashSet<>(entry.getValue());
                for (PsiElement indirect : copySet) {
                    for (PsiElement toAdd : result.get(indirect)) {
                        if (!entry.getValue().contains(toAdd)) {
                            entry.getValue().add(toAdd);
                            stable = false;
                            unstables++;
                        }
                    }
                }
            }

            PsiUtils.logWarning(this.getClass(), "Unstables: " + unstables);
        } while (!stable);

        return result;
    }
}
