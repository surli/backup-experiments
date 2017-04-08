/*
 * Copyright (c) 2010-2015 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.repo.sql.data.common.container;

import com.evolveum.midpoint.prism.PrismContainerValue;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.data.common.RAccessCertificationCampaign;
import com.evolveum.midpoint.repo.sql.data.common.RObject;
import com.evolveum.midpoint.repo.sql.data.common.embedded.RActivation;
import com.evolveum.midpoint.repo.sql.data.common.embedded.REmbeddedReference;
import com.evolveum.midpoint.repo.sql.data.common.enums.RAccessCertificationResponse;
import com.evolveum.midpoint.repo.sql.data.common.id.RContainerId;
import com.evolveum.midpoint.repo.sql.data.common.other.RCReferenceOwner;
import com.evolveum.midpoint.repo.sql.query.definition.JaxbName;
import com.evolveum.midpoint.repo.sql.query.definition.JaxbType;
import com.evolveum.midpoint.repo.sql.query.definition.OwnerGetter;
import com.evolveum.midpoint.repo.sql.query.definition.OwnerIdGetter;
import com.evolveum.midpoint.repo.sql.query2.definition.IdQueryProperty;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.MidPointSingleTablePersister;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.schema.SchemaConstantsGenerated;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCaseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationDecisionType;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Persister;
import org.hibernate.annotations.Where;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lazyman
 * @author mederly
 */

@JaxbType(type = AccessCertificationCaseType.class)
@Entity
@IdClass(RContainerId.class)
@Table(name = "m_acc_cert_case", indexes = {
        @Index(name = "iCaseObjectRefTargetOid", columnList = "objectRef_targetOid"),
        @Index(name = "iCaseTargetRefTargetOid", columnList = "targetRef_targetOid"),
        @Index(name = "iCaseTenantRefTargetOid", columnList = "tenantRef_targetOid"),
        @Index(name = "iCaseOrgRefTargetOid", columnList = "orgRef_targetOid")
})
@Persister(impl = MidPointSingleTablePersister.class)
public class RAccessCertificationCase implements Container {

    private static final Trace LOGGER = TraceManager.getTrace(RAccessCertificationCase.class);

    public static final String F_OWNER = "owner";

    private Boolean trans;

    private byte[] fullObject;

    private RObject owner;
    private String ownerOid;
    private Integer id;

    private Set<RCertCaseReference> reviewerRef;
    private REmbeddedReference objectRef;
    private REmbeddedReference targetRef;
    private REmbeddedReference tenantRef;
    private REmbeddedReference orgRef;
    private RActivation activation;                 // we need mainly validFrom + validTo + maybe adminStatus; for simplicity we added whole ActivationType here

    private XMLGregorianCalendar reviewRequestedTimestamp;
    private XMLGregorianCalendar reviewDeadline;
    private XMLGregorianCalendar remediedTimestamp;
    private Set<RAccessCertificationDecision> decisions;
    private RAccessCertificationResponse currentStageOutcome;
    private Integer currentStageNumber;
    private RAccessCertificationResponse overallOutcome;

    public RAccessCertificationCase() {
        this(null);
    }

    public RAccessCertificationCase(RObject owner) {
        this.setOwner(owner);
    }

    @Id
    @org.hibernate.annotations.ForeignKey(name = "fk_acc_cert_case_owner")
    @MapsId("owner")
    @ManyToOne(fetch = FetchType.LAZY)
    @OwnerGetter(ownerClass = RAccessCertificationCampaign.class)
    public RObject getOwner() {
        return owner;
    }

    @Column(name = "owner_oid", length = RUtil.COLUMN_LENGTH_OID, nullable = false)
    @OwnerIdGetter()
    public String getOwnerOid() {
        if (owner != null && ownerOid == null) {
            ownerOid = owner.getOid();
        }
        return ownerOid;
    }

    @Id
    @GeneratedValue(generator = "ContainerIdGenerator")
    @GenericGenerator(name = "ContainerIdGenerator", strategy = "com.evolveum.midpoint.repo.sql.util.ContainerIdGenerator")
    @Column(name = "id")
    @IdQueryProperty
    public Integer getId() {
        return id;
    }

    @JaxbName(localPart = "currentReviewerRef")
    @Where(clause = RCertCaseReference.REFERENCE_TYPE + "= 2")
    @OneToMany(mappedBy = "owner", orphanRemoval = true)
    @org.hibernate.annotations.ForeignKey(name = "none")
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public Set<RCertCaseReference> getReviewerRef() {
        if (reviewerRef == null) {
            reviewerRef = new HashSet<>();
        }
        return reviewerRef;
    }

    public void setReviewerRef(Set<RCertCaseReference> reviewerRef) {
        this.reviewerRef = reviewerRef;
    }

    @Embedded
    public REmbeddedReference getTargetRef() {
        return targetRef;
    }

    @Embedded
    public REmbeddedReference getObjectRef() {
        return objectRef;
    }

    @Embedded
    public REmbeddedReference getTenantRef() {
        return tenantRef;
    }

    @Embedded
    public REmbeddedReference getOrgRef() {
        return orgRef;
    }

    @Embedded
    public RActivation getActivation() {
        return activation;
    }

    @JaxbName(localPart = "currentReviewRequestedTimestamp")
    public XMLGregorianCalendar getReviewRequestedTimestamp() {
        return reviewRequestedTimestamp;
    }

    @JaxbName(localPart = "currentReviewDeadline")
    public XMLGregorianCalendar getReviewDeadline() {
        return reviewDeadline;
    }

    public XMLGregorianCalendar getRemediedTimestamp() {
        return remediedTimestamp;
    }

    @OneToMany(mappedBy = RAccessCertificationDecision.F_OWNER, orphanRemoval = true)
    @ForeignKey(name = "none")
    @Cascade({org.hibernate.annotations.CascadeType.ALL})
    public Set<RAccessCertificationDecision> getDecision() {
        if (decisions == null) {
            decisions = new HashSet<>();
        }
        return decisions;
    }

    public RAccessCertificationResponse getCurrentStageOutcome() {
        return currentStageOutcome;
    }

    public Integer getCurrentStageNumber() {
        return currentStageNumber;
    }

    public RAccessCertificationResponse getOverallOutcome() {
        return overallOutcome;
    }

    public void setOwner(RObject owner) {
        this.owner = owner;
    }

    public void setOwnerOid(String ownerOid) {
        this.ownerOid = ownerOid;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTargetRef(REmbeddedReference targetRef) {
        this.targetRef = targetRef;
    }

    public void setObjectRef(REmbeddedReference objectRef) {
        this.objectRef = objectRef;
    }

    public void setTenantRef(REmbeddedReference tenantRef) {
        this.tenantRef = tenantRef;
    }

    public void setOrgRef(REmbeddedReference orgRef) {
        this.orgRef = orgRef;
    }

    public void setActivation(RActivation activation) {
        this.activation = activation;
    }

    public void setReviewRequestedTimestamp(XMLGregorianCalendar reviewRequestedTimestamp) {
        this.reviewRequestedTimestamp = reviewRequestedTimestamp;
    }

    public void setReviewDeadline(XMLGregorianCalendar reviewDeadline) {
        this.reviewDeadline = reviewDeadline;
    }

    public void setRemediedTimestamp(XMLGregorianCalendar remediedTimestamp) {
        this.remediedTimestamp = remediedTimestamp;
    }

    public void setDecision(Set<RAccessCertificationDecision> decisions) {
        this.decisions = decisions;
    }

    public void setCurrentStageOutcome(RAccessCertificationResponse currentStageOutcome) {
        this.currentStageOutcome = currentStageOutcome;
    }

    public void setCurrentStageNumber(Integer currentStageNumber) {
        this.currentStageNumber = currentStageNumber;
    }

    public void setOverallOutcome(RAccessCertificationResponse overallOutcome) {
        this.overallOutcome = overallOutcome;
    }

    @Lob
    public byte[] getFullObject() {
        return fullObject;
    }

    public void setFullObject(byte[] fullObject) {
        this.fullObject = fullObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RAccessCertificationCase)) return false;

        RAccessCertificationCase that = (RAccessCertificationCase) o;

        if (!Arrays.equals(fullObject, that.fullObject)) return false;
        if (ownerOid != null ? !ownerOid.equals(that.ownerOid) : that.ownerOid != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (reviewerRef != null ? !reviewerRef.equals(that.reviewerRef) : that.reviewerRef != null) return false;
        if (objectRef != null ? !objectRef.equals(that.objectRef) : that.objectRef != null) return false;
        if (targetRef != null ? !targetRef.equals(that.targetRef) : that.targetRef != null) return false;
        if (tenantRef != null ? !tenantRef.equals(that.tenantRef) : that.tenantRef != null) return false;
        if (orgRef != null ? !orgRef.equals(that.orgRef) : that.orgRef != null) return false;
        if (activation != null ? !activation.equals(that.activation) : that.activation != null) return false;
        if (reviewRequestedTimestamp != null ? !reviewRequestedTimestamp.equals(that.reviewRequestedTimestamp) : that.reviewRequestedTimestamp != null)
            return false;
        if (reviewDeadline != null ? !reviewDeadline.equals(that.reviewDeadline) : that.reviewDeadline != null)
            return false;
        if (remediedTimestamp != null ? !remediedTimestamp.equals(that.remediedTimestamp) : that.remediedTimestamp != null)
            return false;
        if (decisions != null ? !decisions.equals(that.decisions) : that.decisions != null) return false;
        if (currentStageOutcome != that.currentStageOutcome) return false;
        return !(currentStageNumber != null ? !currentStageNumber.equals(that.currentStageNumber) : that.currentStageNumber != null);

    }

    @Override
    public int hashCode() {
        int result = ownerOid != null ? ownerOid.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (reviewerRef != null ? reviewerRef.hashCode() : 0);
        result = 31 * result + (objectRef != null ? objectRef.hashCode() : 0);
        result = 31 * result + (targetRef != null ? targetRef.hashCode() : 0);
        result = 31 * result + (reviewRequestedTimestamp != null ? reviewRequestedTimestamp.hashCode() : 0);
        result = 31 * result + (reviewDeadline != null ? reviewDeadline.hashCode() : 0);
        result = 31 * result + (remediedTimestamp != null ? remediedTimestamp.hashCode() : 0);
        result = 31 * result + (currentStageOutcome != null ? currentStageOutcome.hashCode() : 0);
        result = 31 * result + (currentStageNumber != null ? currentStageNumber.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RAccessCertificationCase{" +
                "id=" + id +
                ", ownerOid='" + ownerOid + '\'' +
                ", owner=" + owner +
                ", targetRef=" + targetRef +
                ", objectRef=" + objectRef +
                '}';
    }

    @Override
    @Transient
    public Boolean isTransient() {
        return trans;
    }

    @Override
    public void setTransient(Boolean trans) {
        this.trans = trans;
    }

    public static RAccessCertificationCase toRepo(RAccessCertificationCampaign owner, AccessCertificationCaseType case1, PrismContext prismContext) throws DtoTranslationException {
        RAccessCertificationCase rCase = toRepo(case1, prismContext);
        rCase.setOwner(owner);
        return rCase;
    }

    public static RAccessCertificationCase toRepo(String ownerOid, AccessCertificationCaseType case1, PrismContext prismContext) throws DtoTranslationException {
        RAccessCertificationCase rCase = toRepo(case1, prismContext);
        rCase.setOwnerOid(ownerOid);
        return rCase;
    }

    private static RAccessCertificationCase toRepo(AccessCertificationCaseType case1, PrismContext prismContext) throws DtoTranslationException {
        RAccessCertificationCase rCase = new RAccessCertificationCase();
        rCase.setTransient(null);       // we don't try to advise hibernate - let it do its work, even if it would cost some SELECTs
        rCase.setId(RUtil.toInteger(case1.getId()));
        rCase.setObjectRef(RUtil.jaxbRefToEmbeddedRepoRef(case1.getObjectRef(), prismContext));
        rCase.setTargetRef(RUtil.jaxbRefToEmbeddedRepoRef(case1.getTargetRef(), prismContext));
        rCase.setTenantRef(RUtil.jaxbRefToEmbeddedRepoRef(case1.getTenantRef(), prismContext));
        rCase.setOrgRef(RUtil.jaxbRefToEmbeddedRepoRef(case1.getOrgRef(), prismContext));
        if (case1.getActivation() != null) {
            RActivation activation = new RActivation();
            RActivation.copyFromJAXB(case1.getActivation(), activation, prismContext);
            rCase.setActivation(activation);
        }
        rCase.getReviewerRef().addAll(RCertCaseReference.safeListReferenceToSet(
                case1.getCurrentReviewerRef(), prismContext, rCase, RCReferenceOwner.CASE_REVIEWER));
        rCase.setReviewRequestedTimestamp(case1.getCurrentReviewRequestedTimestamp());
        rCase.setReviewDeadline(case1.getCurrentReviewDeadline());
        rCase.setRemediedTimestamp(case1.getRemediedTimestamp());
        rCase.setCurrentStageOutcome(RUtil.getRepoEnumValue(case1.getCurrentStageOutcome(), RAccessCertificationResponse.class));
        rCase.setCurrentStageNumber(case1.getCurrentStageNumber());
        rCase.setOverallOutcome(RUtil.getRepoEnumValue(case1.getOverallOutcome(), RAccessCertificationResponse.class));
        for (AccessCertificationDecisionType decision : case1.getDecision()) {
            RAccessCertificationDecision rDecision = RAccessCertificationDecision.toRepo(rCase, decision, prismContext);
            rCase.getDecision().add(rDecision);
        }

        PrismContainerValue<AccessCertificationCaseType> cvalue = case1.asPrismContainerValue();
        String xml;
        try {
            xml = prismContext.xmlSerializer().serialize(cvalue, SchemaConstantsGenerated.C_VALUE);
        } catch (SchemaException e) {
            throw new IllegalStateException("Couldn't serialize certification case to string", e);
        }
        LOGGER.trace("RAccessCertificationCase full object\n{}", xml);
        byte[] fullObject = RUtil.getByteArrayFromXml(xml, false);
        rCase.setFullObject(fullObject);

        return rCase;
    }

    public AccessCertificationCaseType toJAXB(PrismContext prismContext) throws SchemaException {
        return createJaxb(fullObject, prismContext, true);
    }

    // TODO find appropriate name
    public static AccessCertificationCaseType createJaxb(byte[] fullObject, PrismContext prismContext, boolean removeCampaignRef) throws SchemaException {
        String xml = RUtil.getXmlFromByteArray(fullObject, false);
        LOGGER.trace("RAccessCertificationCase full object to be parsed\n{}", xml);
        try {
            return prismContext.parserFor(xml).xml().compat().parseRealValue(AccessCertificationCaseType.class);
        } catch (SchemaException e) {
            LOGGER.debug("Couldn't parse certification case because of schema exception ({}):\nData: {}", e, xml);
            throw e;
        } catch (RuntimeException e) {
            LOGGER.debug("Couldn't parse certification case because of unexpected exception ({}):\nData: {}", e, xml);
            throw e;
        }
        //aCase.asPrismContainerValue().removeReference(AccessCertificationCaseType.F_CAMPAIGN_REF);
    }
}
