package macrobase.analysis.summary;

import macrobase.analysis.summary.itemset.FPGrowthEmerging;
import macrobase.analysis.summary.itemset.ItemsetEncoder;
import macrobase.analysis.summary.itemset.result.EncodedItemsetResult;
import macrobase.analysis.summary.itemset.result.ItemsetResult;
import macrobase.datamodel.DataFrame;
import macrobase.datamodel.Schema;
import macrobase.operator.Operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.DoublePredicate;

public class BatchSummarizer implements Operator<DataFrame, Summary> {
    // Parameters
    public String outlierColumn = "_OUTLIER";
    public double minOutlierSupport = 0.1;
    public double minIORatio = 3;
    public boolean useAttributeCombinations = true;
    public List<String> attributes = new ArrayList<>();
    public DoublePredicate predicate = d -> d != 0.0;

    // Output
    private Summary summary = null;
    // Encoder
    private ItemsetEncoder encoder = new ItemsetEncoder();
    private List<Set<Integer>> inlierItemsets, outlierItemsets;
    private FPGrowthEmerging fpg = new FPGrowthEmerging();

    // Setter and constructor
    public BatchSummarizer() { }
    public BatchSummarizer setUseAttributeCombinations(boolean flag) {
        this.useAttributeCombinations = flag;
        fpg.setCombinationsEnabled(flag);
        return this;
    }
    public BatchSummarizer setMinSupport(double minSupport) {
        this.minOutlierSupport = minSupport;
        return this;
    }
    public BatchSummarizer setMinIORatio(double minIORatio) {
        this.minIORatio = minIORatio;
        return this;
    }
    public BatchSummarizer setOutlierPredicate(DoublePredicate predicate) {
        this.predicate = predicate;
        return this;
    }
    public BatchSummarizer setAttributes(List<String> attributes) {
        this.attributes = attributes;
        this.encoder.setColumnNames(attributes);
        return this;
    }
    public BatchSummarizer setOutlierColumn(String outlierColumn) {
        this.outlierColumn = outlierColumn;
        return this;
    }

    @Override
    public void process(DataFrame df) {
        // Filter inliers and outliers
        DataFrame outlierDF = df.filterDoubleByName(outlierColumn, predicate);
        DataFrame inlierDF = df.filterDoubleByName(outlierColumn, predicate.negate());

        // Encode inlier and outlier attribute columns
        if (attributes.isEmpty()) {
            encoder.setColumnNames(df.getSchema().getColumnNamesByType(Schema.ColType.STRING));
            inlierItemsets = encoder.encodeColumns(inlierDF.getStringCols());
            outlierItemsets = encoder.encodeColumns(outlierDF.getStringCols());
        } else {
            encoder.setColumnNames(attributes);
            inlierItemsets = encoder.encodeColumns(inlierDF.getStringColsByName(attributes));
            outlierItemsets = encoder.encodeColumns(outlierDF.getStringColsByName(attributes));
        }

        long startTime = System.currentTimeMillis();
        List<EncodedItemsetResult> encodedItemsetResults = fpg.getEmergingItemsetsWithMinSupport(
            inlierItemsets,
            outlierItemsets,
            minOutlierSupport,
            minIORatio);
        // Decode results
        List<ItemsetResult> isr = new ArrayList<>();
        encodedItemsetResults.forEach(i -> isr.add(new ItemsetResult(i, encoder)));
        long elapsed = System.currentTimeMillis() - startTime;

        summary = new Summary(isr,
                inlierItemsets.size(),
                outlierItemsets.size(),
                elapsed);
    }

    @Override
    public Summary getResults() {
        return summary;
    }
}
