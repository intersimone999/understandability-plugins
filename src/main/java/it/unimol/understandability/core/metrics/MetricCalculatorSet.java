package it.unimol.understandability.core.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.ase.NestedBlockDepthCalculator;
import it.unimol.understandability.core.metrics.ase.NumberOfParametersCalculator;
import it.unimol.understandability.core.metrics.ase.NumberOfStatementsCalculator;
import it.unimol.understandability.core.metrics.parts.*;
import it.unimol.understandability.core.metrics.readability.ReadabilityMetricAdapter;

import java.util.*;

/**
 * Created by simone on 12/12/16.
 */
public class MetricCalculatorSet {
    public enum Option {
        MAX_DEPTH
    }

    private static final String READABILITY_ADAPTERS = "__READABILITY_ADAPTERS";

    public static List<String> getMetricNames() {
        List<String> names = new ArrayList<>();

        names.add(NameQualityCalculator.NAME);
        names.add(InternalDocumentationQualityCalculator.NAME);
        names.add(ExternalDocumentationQualityCalculator.NAME);
//        names.add(APIInternalPopularityCalculator.NAME);
        names.add(APIExternalPopularityCalculator.NAME);
//        names.add(SourceUnderstandabilityCalculator.NAME);
        names.add(SourceBaseUnderstandabilityCalculator.NAME);
        names.add(ReadabilityCalculator.NAME);
        names.add(TaskEasinessCalculator.NAME);
//        names.add(SourceReadabilityCalculator.NAME);

        names.add(READABILITY_ADAPTERS);

        names.add(NestedBlockDepthCalculator.NAME);
        names.add(NumberOfParametersCalculator.NAME);
        names.add(NumberOfStatementsCalculator.NAME);


        return names;
    }

    public static MetricCalculatorSet getInstance(MultiElementMetricCalculator.Mode mode) {
        List<String> metricNames = MetricCalculatorSet.getMetricNames();
        MetricCalculatorSet metrics = new MetricCalculatorSet();

        if (mode.equals(MultiElementMetricCalculator.Mode.ABSOLUTE)) {
            if (metricNames.contains(ReadabilityCalculator.NAME))
                metrics.addMetric(new ReadabilityCalculator());

            if (metricNames.contains(TaskEasinessCalculator.NAME))
                metrics.addMetric(new TaskEasinessCalculator());

            if (metricNames.contains(READABILITY_ADAPTERS))
                metrics.addAllMetrics(ReadabilityMetricAdapter.getAllMetricCalculators());

            if (metricNames.contains(NestedBlockDepthCalculator.NAME))
                metrics.addMetric(new NestedBlockDepthCalculator());

            if (metricNames.contains(NumberOfParametersCalculator.NAME))
                metrics.addMetric(new NumberOfParametersCalculator());

            if (metricNames.contains(NumberOfStatementsCalculator.NAME))
                metrics.addMetric(new NumberOfStatementsCalculator());
        } else {
            if (metricNames.contains(NameQualityCalculator.NAME))
                metrics.addMetric(new NameQualityCalculator(mode));

            if (metricNames.contains(InternalDocumentationQualityCalculator.NAME))
                metrics.addMetric(new InternalDocumentationQualityCalculator(mode));

            if (metricNames.contains(ExternalDocumentationQualityCalculator.NAME))
                metrics.addMetric(new ExternalDocumentationQualityCalculator(mode));

            if (metricNames.contains(APIInternalPopularityCalculator.NAME))
                metrics.addMetric(new APIInternalPopularityCalculator(mode));

            if (metricNames.contains(APIExternalPopularityCalculator.NAME))
                metrics.addMetric(new APIExternalPopularityCalculator(mode));

            if (metricNames.contains(SourceUnderstandabilityCalculator.NAME))
                metrics.addMetric(new SourceUnderstandabilityCalculator(mode));

            if (metricNames.contains(SourceBaseUnderstandabilityCalculator.NAME))
                metrics.addMetric(new SourceBaseUnderstandabilityCalculator(mode));

            if (metricNames.contains(SourceReadabilityCalculator.NAME))
                metrics.addMetric(new SourceReadabilityCalculator(mode));
        }

        return metrics;
    }

    public void initialize(Project project) {
        for (MetricCalculator calculator : this.calculators)
            calculator.initialize(project);
    }

    private final List<MetricCalculator> calculators;

    public MetricCalculatorSet() {
        this.calculators = new ArrayList<>();
    }

    public void addMetric(MetricCalculator calculator) {
        this.calculators.add(calculator);
    }

    public void addAllMetrics(Collection<MetricCalculator> calculators) {
        for (MetricCalculator calculator : calculators) {
            this.addMetric(calculator);
        }
    }

    public List<MetricCalculator> getMetricCalculators() {
        return this.calculators;
    }

    public Map<String, Double> calculateMetrics(PsiMethod method) {
        Map<String, Double> metrics = new HashMap<>();
        for (MetricCalculator calculator : this.calculators) {
            metrics.put(calculator.getName(), calculator.compute(method));
        }

        return metrics;
    }

    public void setIntOption(Option name, int value) {
        for (MetricCalculator calculator : this.calculators) {
            calculator.setIntOption(name, value);
        }
    }
}
