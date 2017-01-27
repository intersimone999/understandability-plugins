/*
 * (C) Copyright 2016-2016, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.alg.scoring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.WeightedGraph;
import org.jgrapht.alg.interfaces.VertexScoringAlgorithm;

/**
 * PageRank implementation.
 *
 * <p>
 * The <a href="https://en.wikipedia.org/wiki/PageRank">wikipedia</a> article
 * contains a nice description of PageRank. The method can be found on the
 * article: Sergey Brin and Larry Page: The Anatomy of a Large-Scale
 * Hypertextual Web Search Engine. Proceedings of the 7th World-Wide Web
 * Conference, Brisbane, Australia, April 1998. See also the following
 * <a href="http://infolab.stanford.edu/~backrub/google.html">page</a>.
 * </p>
 *
 * <p>
 * This is a simple iterative implementation of PageRank which stops after a
 * given number of iterations or if the PageRank values between two iterations
 * do not change more than a predefined value. The implementation uses the
 * variant which divides by the number of nodes, thus forming a probability
 * distribution over graph nodes.
 * </p>
 *
 * <p>
 * Each iteration of the algorithm runs in linear time O(n+m) when n is the
 * number of nodes and m the number of edges of the graph. The maximum number of
 * iterations can be adjusted by the caller. The default value is
 * {@link PageRank#MAX_ITERATIONS_DEFAULT}.
 * </p>
 *
 * <p>
 * If the graph is a weighted graph, a weighted variant is used where the
 * probability of following an edge e out of node v is equal to the weight of e
 * over the sum of weights of all outgoing edges of v.
 * </p>
 *
 * @param <V> the graph vertex type
 * @param <E> the graph edge type
 *
 * @author Dimitrios Michail
 * @since August 2016
 */
public final class PageRank<V, E>
        implements VertexScoringAlgorithm<V, Double>
{
    /**
     * Default number of maximum iterations.
     */
    public static final int MAX_ITERATIONS_DEFAULT = 100;

    /**
     * Default value for the tolerance. The calculation will stop if the
     * difference of PageRank values between iterations change less than this
     * value.
     */
    public static final double TOLERANCE_DEFAULT = 0.0001;

    /**
     * Damping factor default value.
     */
    public static final double DAMPING_FACTOR_DEFAULT = 0.85d;

    private final Graph<V, E> g;
    private Map<V, Double> scores;

    /**
     * Create and execute an instance of PageRank.
     *
     * @param g the input graph
     */
    public PageRank(Graph<V, E> g)
    {
        this(
                g,
                DAMPING_FACTOR_DEFAULT,
                MAX_ITERATIONS_DEFAULT,
                TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     */
    public PageRank(Graph<V, E> g, double dampingFactor)
    {
        this(g, dampingFactor, MAX_ITERATIONS_DEFAULT, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     */
    public PageRank(Graph<V, E> g, double dampingFactor, int maxIterations)
    {
        this(g, dampingFactor, maxIterations, TOLERANCE_DEFAULT);
    }

    /**
     * Create and execute an instance of PageRank.
     *
     * @param g the input graph
     * @param dampingFactor the damping factor
     * @param maxIterations the maximum number of iterations to perform
     * @param tolerance the calculation will stop if the difference of PageRank
     *        values between iterations change less than this value
     */
    public PageRank(
            Graph<V, E> g,
            double dampingFactor,
            int maxIterations,
            double tolerance)
    {
        this.g = g;
        this.scores = new HashMap<>();

        if (maxIterations <= 0) {
            throw new IllegalArgumentException(
                    "Maximum iterations must be positive");
        }

        if (dampingFactor < 0.0 || dampingFactor > 1.0) {
            throw new IllegalArgumentException("Damping factor not valid");
        }

        if (tolerance <= 0.0) {
            throw new IllegalArgumentException(
                    "Tolerance not valid, must be positive");
        }

        run(dampingFactor, maxIterations, tolerance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<V, Double> getScores()
    {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double getVertexScore(V v)
    {
        if (!g.containsVertex(v)) {
            throw new IllegalArgumentException(
                    "Cannot return score of unknown vertex");
        }
        return scores.get(v);
    }

    private void run(double dampingFactor, int maxIterations, double tolerance)
    {
        // initialization
        Specifics specifics;
        if (g instanceof DirectedGraph<?, ?>) {
            specifics = new DirectedSpecifics((DirectedGraph<V, E>) g);
        } else {
            specifics = new UndirectedSpecifics(g);
        }

        int totalVertices = g.vertexSet().size();
        boolean weighted = (g instanceof WeightedGraph<?, ?>);
        Map<V, Double> weights;
        if (weighted) {
            weights = new HashMap<>(totalVertices);
        } else {
            weights = Collections.emptyMap();
        }

        double initScore = 1.0d / totalVertices;
        for (V v : g.vertexSet()) {
            scores.put(v, initScore);
            if (weighted) {
                double sum = 0;
                for (E e : specifics.outgoingEdgesOf(v)) {
                    sum += g.getEdgeWeight(e);
                }
                weights.put(v, sum);
            }
        }

        // run PageRank
        Map<V, Double> nextScores = new HashMap<>();
        double maxChange = tolerance;

        while (maxIterations > 0 && maxChange >= tolerance) {
            // compute next iteration scores
            double r = 0d;
            for (V v : g.vertexSet()) {
                if (specifics.outgoingEdgesOf(v).size() > 0) {
                    r += (1d - dampingFactor) * scores.get(v);
                } else {
                    r += scores.get(v);
                }
            }
            r /= totalVertices;

            maxChange = 0d;
            for (V v : g.vertexSet()) {
                double contribution = 0d;

                if (weighted) {
                    for (E e : specifics.incomingEdgesOf(v)) {
                        V w = Graphs.getOppositeVertex(g, e, v);
                        contribution += dampingFactor * scores.get(w)
                                * g.getEdgeWeight(e) / weights.get(w);
                    }
                } else {
                    for (E e : specifics.incomingEdgesOf(v)) {
                        V w = Graphs.getOppositeVertex(g, e, v);
                        contribution += dampingFactor * scores.get(w)
                                / specifics.outgoingEdgesOf(w).size();
                    }
                }

                double vOldValue = scores.get(v);
                double vNewValue = r + contribution;
                maxChange = Math
                        .max(maxChange, Math.abs(vNewValue - vOldValue));
                nextScores.put(v, vNewValue);
            }

            // swap scores
            Map<V, Double> tmp = scores;
            scores = nextScores;
            nextScores = tmp;

            // progress
            maxIterations--;
        }

    }

    abstract class Specifics
    {
        public abstract Set<? extends E> outgoingEdgesOf(V vertex);

        public abstract Set<? extends E> incomingEdgesOf(V vertex);
    }

    class DirectedSpecifics
            extends Specifics
    {
        private DirectedGraph<V, E> graph;

        public DirectedSpecifics(DirectedGraph<V, E> g)
        {
            graph = g;
        }

        @Override
        public Set<? extends E> outgoingEdgesOf(V vertex)
        {
            return graph.outgoingEdgesOf(vertex);
        }

        @Override
        public Set<? extends E> incomingEdgesOf(V vertex)
        {
            return graph.incomingEdgesOf(vertex);
        }
    }

    class UndirectedSpecifics
            extends Specifics
    {
        private Graph<V, E> graph;

        public UndirectedSpecifics(Graph<V, E> g)
        {
            graph = g;
        }

        @Override
        public Set<E> outgoingEdgesOf(V vertex)
        {
            return graph.edgesOf(vertex);
        }

        @Override
        public Set<E> incomingEdgesOf(V vertex)
        {
            return graph.edgesOf(vertex);
        }
    }

}