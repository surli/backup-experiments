/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.message;

import static org.mockito.Mockito.mock;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;

import org.jboss.aerogear.unifiedpush.dao.PushMessageInformationDao;
import org.jboss.aerogear.unifiedpush.dao.VariantMetricInformationDao;
import org.jboss.aerogear.unifiedpush.service.ClientInstallationService;
import org.jboss.aerogear.unifiedpush.service.GenericVariantService;

@RequestScoped
public class MockProviders {

    private PushMessageInformationDao pushMessageInformationDao = mock(PushMessageInformationDao.class);
    private GenericVariantService genericVariantService = mock(GenericVariantService.class);
    private VariantMetricInformationDao variantMetricInformationDao = mock(VariantMetricInformationDao.class);
    private ClientInstallationService clientInstallationService = mock(ClientInstallationService.class);

    @Produces
    public PushMessageInformationDao getPushMessageInformationDao() {
        return pushMessageInformationDao;
    }

    @Produces
    public GenericVariantService getGenericVariantService() {
        return genericVariantService;
    }

    @Produces
    public VariantMetricInformationDao getVariantMetricInformationDao() {
        return variantMetricInformationDao;
    }

    @Produces
    public ClientInstallationService getClientInstallationService() {
        return clientInstallationService;
    }
}
