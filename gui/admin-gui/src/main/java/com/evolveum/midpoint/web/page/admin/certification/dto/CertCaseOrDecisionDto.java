/*
 * Copyright (c) 2010-2016 Evolveum
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

package com.evolveum.midpoint.web.page.admin.certification.dto;

import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.prism.xml.XmlTypeConverter;
import com.evolveum.midpoint.schema.util.CertCampaignTypeUtil;
import com.evolveum.midpoint.web.component.util.Selectable;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCampaignType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationCaseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.AccessCertificationStageType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang3.Validate;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.Date;

/**
 * A common superclass for CertCaseDto + CertDecisionDto.
 *
 * TODO cleanup a bit
 *
 * @author mederly
 */
public class CertCaseOrDecisionDto extends Selectable {

    public static final String F_OBJECT_NAME = "objectName";
    public static final String F_TARGET_NAME = "targetName";
    public static final String F_TARGET_TYPE = "targetType";
    public static final String F_CAMPAIGN_NAME = "campaignName";
    public static final String F_REVIEW_REQUESTED = "reviewRequested";
    public static final String F_DEADLINE_AS_STRING = "deadlineAsString";

    private AccessCertificationCaseType certCase;
    private String objectName;
    private String targetName;
    private String deadlineAsString;

    public CertCaseOrDecisionDto(AccessCertificationCaseType _case, PageBase page) {
        Validate.notNull(_case);

        this.certCase = _case;
        this.objectName = getName(_case.getObjectRef());
        this.targetName = getName(_case.getTargetRef());
        this.deadlineAsString = computeDeadlineAsString(page);
    }

    // ugly hack (for now) - we extract the name from serialization metadata
    private String getName(ObjectReferenceType ref) {
        if (ref == null) {
            return null;
        }
        String name = ref.getTargetName() != null ? ref.getTargetName().getOrig() : null;
        if (name == null) {
            return "(" + ref.getOid() + ")";
        } else {
            return name.trim();
        }
    }

    public String getObjectName() {
        return objectName;
    }

	public QName getObjectType() {
		return certCase.getObjectRef().getType();
	}

    public String getTargetName() {
        return targetName;
    }

    public QName getTargetType() {
        return certCase.getTargetRef().getType();
    }

    public ObjectReferenceType getCampaignRef() {
        return certCase.getCampaignRef();
    }

    public Long getCaseId() {
        return certCase.asPrismContainerValue().getId();
    }

    public AccessCertificationCaseType getCertCase() {
        return certCase;
    }

    public AccessCertificationCampaignType getCampaign() {
        try {
            return (AccessCertificationCampaignType) certCase.getCampaignRef().asReferenceValue().getObject().asObjectable();
        } catch (NullPointerException e) {
            return null;      // TODO fix this really crude hack
        }
    }

    public String getCampaignName() {
        AccessCertificationCampaignType campaign = getCampaign();
        return campaign != null ? campaign.getName().getOrig() : "";
    }

    public Integer getCampaignStageNumber() {
        AccessCertificationCampaignType campaign = getCampaign();
        return campaign != null ? campaign.getStageNumber() : null;      // numbers after # of stages should not occur, as there are no cases in these stages
    }

    public Integer getCampaignStageCount() {
        AccessCertificationCampaignType campaign = getCampaign();
        return CertCampaignTypeUtil.getNumberOfStages(campaign);
    }

    public Date getReviewRequested() {
        XMLGregorianCalendar date = certCase.getCurrentReviewRequestedTimestamp();
        return XmlTypeConverter.toDate(date);
    }

    public Date getStageStarted() {
        AccessCertificationCampaignType campaign = getCampaign();
        if (campaign == null) {
            return null;
        }
        int stageNumber = campaign.getStageNumber();
        if (stageNumber <= 0 || stageNumber > CertCampaignTypeUtil.getNumberOfStages(campaign)) {
            return null;
        }
        AccessCertificationStageType stage = CertCampaignTypeUtil.findStage(campaign, stageNumber);
        return XmlTypeConverter.toDate(stage.getStart());
    }

    public String getCurrentStageName() {
        AccessCertificationCampaignType campaign = getCampaign();
        if (campaign == null) {
            return null;
        }
        AccessCertificationStageType stage = CertCampaignTypeUtil.getCurrentStage(campaign);
        if (stage == null) {
            return null;
        }
        return stage.getName();
    }

    public String getHandlerUri() {
        AccessCertificationCampaignType campaign = getCampaign();
        return campaign != null ? campaign.getHandlerUri() : null;
    }

    private String computeDeadlineAsString(PageBase page) {
        XMLGregorianCalendar deadline = certCase.getCurrentReviewDeadline();

        if (deadline == null) {
            return "";
        } else {
            long delta = XmlTypeConverter.toMillis(deadline) - System.currentTimeMillis();

            // round to hours; we always round down
            long precision = 3600000L;      // 1 hour
            if (Math.abs(delta) > precision) {
                delta = (delta / precision) * precision;
            }

            //todo i18n
            if (delta > 0) {
            	return PageBase.createStringResourceStatic(page, "PageCert.in", DurationFormatUtils.formatDurationWords(delta, true, true)).getString();
            } else if (delta < 0) {
            	return PageBase.createStringResourceStatic(page, "PageCert.ago", DurationFormatUtils.formatDurationWords(-delta, true, true)).getString();
            } else {
                return page.getString("PageCert.now");
            }
        }
    }

    public String getDeadlineAsString() {
        return deadlineAsString;
    }
}
