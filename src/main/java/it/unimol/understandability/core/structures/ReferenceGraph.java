package it.unimol.understandability.core.structures;

import com.intellij.psi.PsiElement;
import org.jgrapht.alg.scoring.PageRank;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by simone on 09/12/16.
 */
public class ReferenceGraph extends SimpleDirectedGraph<PsiElement, DefaultEdge> {
    private Map<PsiElement, Double> ranks;
    private Map<PsiElement, Double> normalizedRanks;

    public ReferenceGraph(Class<? extends DefaultEdge> edgeClass) {
        super(edgeClass);
    }

    public Map<PsiElement, Double> getPageRank() {
        if (this.ranks == null) {
            PageRank<PsiElement, DefaultEdge> ranker = new PageRank<>(this, PageRank.DAMPING_FACTOR_DEFAULT, this.edgeSet().size());
            this.ranks = ranker.getScores();
        }

        return ranks;
    }

    public Map<PsiElement, Double> getNormalizedPageRank() {
        if (this.normalizedRanks == null) {
            this.normalizedRanks = new HashMap<>();

            double max = 0.0;
            for (Double value : this.getPageRank().values()) {
                if (value > max)
                    max = value;
            }

            for (Map.Entry<PsiElement, Double> entry : this.getPageRank().entrySet()) {
                this.normalizedRanks.put(entry.getKey(), entry.getValue() / max);
            }
        }

        return this.normalizedRanks;
    }

    public ReferenceGraph getSubgraph(List<PsiElement> allowedElements) {
        ReferenceGraph copy = (ReferenceGraph) this.clone();
        Set<PsiElement> toRemove = this.vertexSet();
        toRemove.removeAll(allowedElements);

        copy.removeAllVertices(toRemove);

        return copy;
    }

    @Override
    public boolean addEdge(PsiElement sourceVertex, PsiElement targetVertex, DefaultEdge defaultEdge) {
        this.ranks = null;

        return super.addEdge(sourceVertex, targetVertex, defaultEdge);
    }

    @Override
    public DefaultEdge addEdge(PsiElement sourceVertex, PsiElement targetVertex) {
        this.ranks = null;

        return super.addEdge(sourceVertex, targetVertex);
    }

    @Override
    public boolean addVertex(PsiElement s) {
        this.ranks = null;

        return super.addVertex(s);
    }

    @Override
    public boolean removeEdge(DefaultEdge defaultEdge) {
        this.ranks = null;

        return super.removeEdge(defaultEdge);
    }

    @Override
    public boolean removeVertex(PsiElement s) {
        this.ranks = null;

        return super.removeVertex(s);
    }

    public void registerReference(PsiElement source, PsiElement destination) {
        if (source == destination)
            return;

        if (!this.containsVertex(source))
            this.addVertex(source);

        if (!this.containsVertex(destination)) {
            this.addVertex(destination);
        }

        if (!this.containsEdge(source, destination)) {
            this.addEdge(source, destination);
        }
    }
}
