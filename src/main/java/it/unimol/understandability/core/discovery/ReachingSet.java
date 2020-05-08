package it.unimol.understandability.core.discovery;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by simone on 03/01/17.
 */
public class ReachingSet implements Comparable<ReachingSet> {
    private PsiElement element;
    private final Map<Integer, Set<PsiElement>> reachable;
    private final int maxLevel;
    private double score;

    public ReachingSet(PsiElement e, int maxLevel) {
        this.setElement(e);
        this.maxLevel = maxLevel;
        this.reachable = new HashMap<>();
    }

    public PsiElement getElement() {
        return element;
    }

    public void setElement(PsiElement element) {
        this.element = element;
    }

    public Set<PsiElement> getAllReachable() {
        Set<PsiElement> allReachable = new HashSet<>();

        for (Set<PsiElement> set : reachable.values())
            allReachable.addAll(set);

        return allReachable;
    }

    public void setReachable(int level, Set<PsiElement> reachable) {
        this.reachable.put(level, reachable);
    }

    public Set<PsiElement> getDirectlyReachable() {
        return this.reachable.get(1);
    }

    public void setDirectlyReachable(Set<PsiElement> directlyReachable) {
        this.setReachable(1, directlyReachable);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void updateScore(Map<PsiElement, Double> pageRank) {
        this.updateScore(pageRank, new HashSet<PsiElement>());
    }

    public void updateScore(Map<PsiElement, Double> pageRank, Set<PsiElement> toIgnore) {
        double score = 0;
        Queue<PsiElement> queue = new LinkedList<>();
        int currentLevel = 1;

        Set<PsiElement> alreadyAnalyzed = new HashSet<>(toIgnore);
        while (currentLevel < maxLevel) {
            queue.addAll(this.reachable.get(currentLevel));
            double multiplier = Math.pow(2, -currentLevel);

            queue.removeAll(alreadyAnalyzed);
            while (!queue.isEmpty()) {
                PsiElement current = queue.poll();

                alreadyAnalyzed.add(current);

                score += multiplier * pageRank.get(current);
            }

            currentLevel++;
        }

        this.score = score;
    }

    public double getUpdatedScore(Map<PsiElement, Double> pageRank, Set<PsiElement> toIgnore) {
        this.updateScore(pageRank, toIgnore);

        return this.score;
    }

    @Override
    public int compareTo(@NotNull ReachingSet reachingSet) {
        return new Double(this.getScore()).compareTo(reachingSet.getScore());
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
