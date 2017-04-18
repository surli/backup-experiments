package edu.stanford.bmir.protege.web.shared.entity;

import edu.stanford.bmir.protege.web.shared.HasLexicalForm;
import edu.stanford.bmir.protege.web.shared.PrimitiveType;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntityVisitorEx;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;
import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 28/11/2012
 */
public final class OWLLiteralData extends OWLPrimitiveData implements HasLexicalForm {

    private final OWLLiteral literal;

    public OWLLiteralData(OWLLiteral object) {
        super(object);
        this.literal = object;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.LITERAL;
    }

    @Override
    public OWLLiteral getObject() {
        return literal;
    }

    public OWLLiteral getLiteral() {
        return getObject();
    }

    @Override
    public String getBrowserText() {
        OWLLiteral literal = getLiteral();
        return literal.getLiteral();
    }

    @Override
    public String getUnquotedBrowserText() {
        return getBrowserText();
    }

    @Override
    public String getLexicalForm() {
        return getLiteral().getLiteral();
    }

    public boolean hasLang() {
        return getLiteral().hasLang();
    }

    @Nonnull
    public String getLang() {
        return getLiteral().getLang();
    }


    @Override
    public String toString() {
        return toStringHelper("OWLLiteralData" )
                .addValue(getLiteral())
                .toString();
    }

    @Override
    public <R, E extends Throwable> R accept(OWLPrimitiveDataVisitor<R, E> visitor) throws E {
        return visitor.visit(this);
    }

    @Override
    public <R> R accept(OWLEntityVisitorEx<R> visitor, R defaultValue) {
        return defaultValue;
    }

    @Override
    public int hashCode() {
        return "OWLLiteralData".hashCode() + getLiteral().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof OWLLiteralData)) {
            return false;
        }
        OWLLiteralData other = (OWLLiteralData) obj;
        return this.getLiteral().equals(other.getLiteral());
    }

    @Override
    public Optional<OWLAnnotationValue> asAnnotationValue() {
        return Optional.of(getLiteral());
    }
}
