package org.grobid.core.tokenization;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.grobid.core.GrobidModel;
import org.grobid.core.engines.label.TaggingLabels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Triple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zholudev on 11/01/16.
 * Synchronize tagging result and layout tokens
 */
public class TaggingTokenSynchronizer implements Iterator<LabeledTokensContainer>, Iterable<LabeledTokensContainer> {
    private final GrobidModel grobidModel;
    private final Iterator<Triple<String, String, String>> tokensAndLabelsIt;
    private final PeekingIterator<LayoutToken> tokenizationsIt;
    private int tokensAndLabelsPtr;
    private int tokenizationsPtr;
    private List<Triple<String, String, String>> tokensAndLabels;
    private List<LayoutToken> tokenizations;

    public TaggingTokenSynchronizer(GrobidModel grobidModel, String result, List<LayoutToken> tokenizations) {
        this(grobidModel, result, tokenizations, false);
    }

    public TaggingTokenSynchronizer(GrobidModel grobidModel, String result, List<LayoutToken> tokenizations,
                                    boolean addFeatureStrings) {
        this.grobidModel = grobidModel;
        tokensAndLabels = GenericTaggerUtils.getTokensWithLabelsAndFeatures(result, addFeatureStrings);
        tokensAndLabelsIt = tokensAndLabels.iterator();
        this.tokenizations = tokenizations;
        tokenizationsIt = Iterators.peekingIterator(this.tokenizations.iterator());
    }

    @Override
    public boolean hasNext() {
        return tokensAndLabelsIt.hasNext();
    }

    @Override
    //null value indicates an empty line in a tagging result
    public LabeledTokensContainer next() {
        Triple<String, String, String> p = tokensAndLabelsIt.next();

        if (p == null) {
            return null;
        }

        String resultToken = p.getA();
        String label = p.getB();
        String featureString = p.getC();

        List<LayoutToken> layoutTokenBuffer = new ArrayList<>();
        boolean stop = false;
        boolean addSpace = false;
        boolean newLine = false;
        int preTokenizationPtr = tokenizationsPtr;

        while ((!stop) && (tokenizationsIt.hasNext())) {
            LayoutToken layoutToken = tokenizationsIt.next();

            layoutTokenBuffer.add(layoutToken);
            String tokOriginal = layoutToken.t();
            if (LayoutTokensUtil.spaceyToken(tokOriginal)) {
                addSpace = true;
            } else if (LayoutTokensUtil.newLineToken(tokOriginal)) {
                newLine = true;
            } else if (tokOriginal.trim().equals(resultToken)) {
                stop = true;
            } else if (tokOriginal.isEmpty()) {
              // no op
            } else {
                int limit = 5;
                StringBuilder sb = new StringBuilder();
                for (int i = Math.max(0, tokensAndLabelsPtr - limit); i < Math.min(tokensAndLabelsPtr + limit, tokensAndLabels.size()); i++) {
                    Triple<String, String, String> s = tokensAndLabels.get(i);
                    String str = i == tokensAndLabelsPtr ? "-->\t'" + s.getA() + "'" : "\t'" + s.getA() + "'";
                    sb.append(str).append("\n");
                }

                StringBuilder sb2 = new StringBuilder();
                for (int i = Math.max(0, preTokenizationPtr - limit * 2); i < Math.min(preTokenizationPtr + limit * 2, tokenizations.size()); i++) {
                    LayoutToken s = tokenizations.get(i);
                    String str = i == preTokenizationPtr ? "-->\t'" + s.t() + "'" : "\t'" + s.t() + "'";
                    sb2.append(str).append("\n");
                }

                throw new IllegalStateException("IMPLEMENTATION ERROR: " +
                        "tokens (at pos: " + tokensAndLabelsPtr + ") got dissynchronized with tokenizations (at pos: "
                        + tokenizationsPtr + " )\n" +
                    "labelsAndTokens +-: \n" + sb.toString() +
                        "\n" + "tokenizations +-: " + sb2
                );
            }
            tokenizationsPtr++;
        }


        //filling spaces to the end, instead of appending spaces to the next container
        while (tokenizationsIt.hasNext()) {
            if (LayoutTokensUtil.spaceyToken(tokenizationsIt.peek().t())) {
                layoutTokenBuffer.add(tokenizationsIt.next());
                tokenizationsPtr++;
            } else {
                break;
            }
        }

        resultToken = LayoutTokensUtil.removeSpecialVariables(resultToken);

        tokensAndLabelsPtr++;
        LabeledTokensContainer labeledTokensContainer =
                new LabeledTokensContainer(layoutTokenBuffer, resultToken, TaggingLabels.labelFor(grobidModel, label),
                GenericTaggerUtils.isBeginningOfEntity(label));

        labeledTokensContainer.setFeatureString(featureString);

        labeledTokensContainer.setSpacePreceding(addSpace);
        labeledTokensContainer.setNewLinePreceding(newLine);

        return labeledTokensContainer;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<LabeledTokensContainer> iterator() {
        return this;
    }
}
