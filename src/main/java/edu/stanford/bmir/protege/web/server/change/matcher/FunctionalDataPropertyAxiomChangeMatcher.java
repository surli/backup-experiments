package edu.stanford.bmir.protege.web.server.change.matcher;

import com.google.common.reflect.TypeToken;
import edu.stanford.bmir.protege.web.server.owlapi.OWLObjectStringFormatter;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 16/03/16
 */
public class FunctionalDataPropertyAxiomChangeMatcher extends AbstractAxiomMatcher<OWLFunctionalDataPropertyAxiom> {

    private final OWLObjectStringFormatter formatter;

    @Inject
    public FunctionalDataPropertyAxiomChangeMatcher(OWLObjectStringFormatter formatter) {
        super(new TypeToken<OWLFunctionalDataPropertyAxiom>(){});
        this.formatter = formatter;
    }

    @Override
    protected Optional<String> getDescriptionForAddAxiomChange(OWLFunctionalDataPropertyAxiom axiom) {
        return formatter.format("Made property %s functional", axiom.getProperty());
    }

    @Override
    protected Optional<String> getDescriptionForRemoveAxiomChange(OWLFunctionalDataPropertyAxiom axiom) {
        return formatter.format("Removed the functional property characteristic from %s", axiom.getProperty());
    }
}
