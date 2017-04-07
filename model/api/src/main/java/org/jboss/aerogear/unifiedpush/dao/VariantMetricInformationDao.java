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
package org.jboss.aerogear.unifiedpush.dao;
import org.jboss.aerogear.unifiedpush.api.VariantMetricInformation;


public interface VariantMetricInformationDao extends GenericBaseDao<VariantMetricInformation, String>{

    /**
     * Finds a VariantMetricInformation instance by the related variantID
     *
     * @param variantID that is associated with the VariantMetricInformation instance
     * @param pushMessageinformationId of the @link{{org.jboss.aerogear.unifiedpush.api.PushMessageInformation}} that owns this instance
     * @return a VariantMetricInformation instance
     */
    VariantMetricInformation findVariantMetricInformationByVariantID(String variantID, String pushMessageinformationId);

}

