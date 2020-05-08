package it.unimol.understandability.core.discovery;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.filters.FilterSystem;
import it.unimol.understandability.core.structures.ReferenceGraph;

import java.util.*;

/**
 * This class allows to simplify program discovering process
 * Created by simone on 22/12/16.
 */
public class ProgramDiscoverer {
    private final Project project;
    private ReferenceGraph graph;

    public ProgramDiscoverer(Project project, ReferenceGraph graph) {
        this.graph = graph;
        this.project = project;
    }

    public Set<PsiElement> discover(double percentageCovered) {
        int targetSize = (int)(this.graph.vertexSet().size() * percentageCovered);
        return discover(targetSize, Integer.MAX_VALUE);
    }

    public Set<PsiElement> discover(int maxResultElements) {
        return discover(Integer.MAX_VALUE, maxResultElements);
    }

    private Set<PsiElement> discover(int targetReached, int targetOpened) {
        LongestReachablePath lrp = new LongestReachablePath(this.project, this.graph);
        Map<PsiElement, Set<PsiElement>> reachingSets = lrp.getReachingSets();

        Set<PsiElement> goldenSet = new HashSet<>();
        Set<PsiElement> reachableByGoldenSet = new HashSet<>();

        while (reachableByGoldenSet.size() < targetReached && goldenSet.size() < targetOpened) {
            Map.Entry<PsiElement, Set<PsiElement>> longestReachingSet = null;
            int maxAdditional = 0;

            for (Map.Entry<PsiElement, Set<PsiElement>> entry : reachingSets.entrySet()) {
                Set<PsiElement> additional = new HashSet<>(entry.getValue());
                additional.removeAll(reachableByGoldenSet);

                if (longestReachingSet == null || additional.size() > maxAdditional) {
                    longestReachingSet = entry;
                    maxAdditional = additional.size();
                }
            }

            goldenSet.add(longestReachingSet.getKey());
            reachableByGoldenSet.addAll(longestReachingSet.getValue());

            if (goldenSet.size() == 1) {
                PsiUtils.logWarning(this.getClass(), goldenSet.toString() + " can discover:");
                for (PsiElement element : reachableByGoldenSet) {
                    PsiUtils.logWarning(this.getClass(), "\t" + element.toString());
                }
            }
            //PsiUtils.logWarning(this.getClass(), "Discovered " + reachableByGoldenSet.size() + "/" + targetSize);
        }

        return goldenSet;
    }

    public List<PsiElement> getCentralPoints(int targetNumber) {
        Map<PsiElement, Double> pageRank = this.graph.getPageRank();

        List<Map.Entry<PsiElement, Double>> sorted = new ArrayList<>();
        for (Map.Entry<PsiElement, Double> entry : pageRank.entrySet()) {
            sorted.add(entry);
        }

        sorted.sort((t0, t1) -> {
            if (t0 == null)
                return 1;
            else if (t1 == null)
                return -1;

            return t0.getValue().compareTo(t1.getValue());
        });

        Collections.reverse(sorted);

        List<PsiElement> topElements = new ArrayList<>();

        for (Map.Entry<PsiElement, Double> entry : sorted) {
            if (!FilterSystem.getStandardInstance().pass(entry.getKey()))
                continue;

            topElements.add(entry.getKey());

            if (topElements.size() > targetNumber)
                break;
        }

        return topElements;
    }

    public List<PsiElement> getStartingPointsByPopularity(int targetNumber) {
        LongestReachablePath lrp = new LongestReachablePath(this.project, this.graph);
        Map<PsiElement, ReachingSet> reachingSets = lrp.getCompleteReachingSets(4);
        Map<PsiElement, Double> pageRank = this.graph.getPageRank();

        ReachingSet maxReachingSet;

        List<PsiElement> goldenSet = new ArrayList<>();
        Set<PsiElement> toIgnore = new HashSet<>();

        if (reachingSets.size() > 0) {
            while (goldenSet.size() < targetNumber && toIgnore.size() < reachingSets.size()) {
                maxReachingSet = null;
                for (ReachingSet set : reachingSets.values()) {
                    if (toIgnore.contains(set.getElement()))
                        continue;

                    set.updateScore(pageRank, toIgnore);
                    if (maxReachingSet == null || set.getScore() > maxReachingSet.getScore())
                        maxReachingSet = set;
                }

                if (maxReachingSet != null) {
                    goldenSet.add(maxReachingSet.getElement());
                    toIgnore.addAll(maxReachingSet.getAllReachable());
                    toIgnore.add(maxReachingSet.getElement());
                    PsiUtils.logWarning(this.getClass(), "Golden set of " + goldenSet.size() + "classes (" + toIgnore.size() + " covered)");
                }
            }
        }

        return goldenSet;
    }
}
