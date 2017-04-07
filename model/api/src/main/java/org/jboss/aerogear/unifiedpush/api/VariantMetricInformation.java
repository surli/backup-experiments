/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.api;

import javax.validation.constraints.NotNull;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Contains metadata about a variant, of the push message request, such as successful delivery to the push network
 * or involved client devices.
 */
public class VariantMetricInformation extends BaseModel {

    private static final long serialVersionUID = 8704836533294404718L;

    @NotNull
    private String variantID;
    private Long receivers = 0L;
    private Boolean deliveryStatus = Boolean.FALSE;
    private String reason;
    private Long variantOpenCounter = 0L;
    private Integer servedBatches = 0;
    private Integer totalBatches = 0;

    @JsonIgnore
    private PushMessageInformation pushMessageInformation;

    public VariantMetricInformation() {
    }
    
    /**
     * The ID of the involved variant
     *
     * @return variant ID
     */
    public String getVariantID() {
        return variantID;
    }

    public void setVariantID(String variantID) {
        this.variantID = variantID;
    }

    /**
     * Number of receivers for this variant that were found for the submitted push request
     *
     * @return number of receivers
     */
    public Long getReceivers() {
        return receivers;
    }

    public void setReceivers(Long receivers) {
        this.receivers = receivers;
    }

    /**
     * Indicator if the request to the actual push network, for the related variant, was successful or not.
     *
     * @return status of the delivery
     */
    public Boolean getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(Boolean deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    /**
     * In case of an error (deliveryStatus:false), there is most likely a reason which may give some more insights.
     *
     * @return error details
     */
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public PushMessageInformation getPushMessageInformation() {
        return pushMessageInformation;
    }

    public void setPushMessageInformation(PushMessageInformation pushMessageInformation) {
        this.pushMessageInformation = pushMessageInformation;
    }

    /**
     * To track how many time this variant has been opened after a Push Notification
     *
     * @return long , the times this variant has been opened after a Push Notification
     */
    public Long getVariantOpenCounter() {
        return variantOpenCounter;
    }

    public void setVariantOpenCounter(Long variantOpenCounter) {
        this.variantOpenCounter = variantOpenCounter;
    }

    public void incrementVariantOpenCounter() {
        this.setVariantOpenCounter(this.getVariantOpenCounter() + 1);
    }

    /**
     * The number of device token batches that were fully NotificationDispatcher.
     *
     * Note: The value is updated asynchronously in MetricsCollector.
     *
     * @return number of device token batches that were fully processed.
     */
    public Integer getServedBatches() {
        return servedBatches;
    }

    public void setServedBatches(Integer servedBatches) {
        this.servedBatches = servedBatches;
    }

    /**
     * The total number of device token batches that were loaded by the TokenLoader.
     *
     * Note: The value is updated asynchronously in MetricsCollector.
     *
     * @return total number of device token batches that were loaded by the TokenLoader.
     */
    public Integer getTotalBatches() {
        return totalBatches;
    }

    public void setTotalBatches(Integer totalBatches) {
        this.totalBatches = totalBatches;
    }
}
