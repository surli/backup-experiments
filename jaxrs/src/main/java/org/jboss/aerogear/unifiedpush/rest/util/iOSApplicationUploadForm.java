/**
 * JBoss, Home of Professional Open Source
 * Copyright Red Hat, Inc., and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.aerogear.unifiedpush.rest.util;

import org.jboss.aerogear.crypto.util.PKCS12;
import org.jboss.aerogear.unifiedpush.api.iOSVariant;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.validation.constraints.AssertTrue;
import javax.ws.rs.FormParam;

/**
 * Helper class to read values from the multipart request
 * that is performed when creating (or updating) an iOS variant.
 */
public class iOSApplicationUploadForm {

    public iOSApplicationUploadForm() {
    }

    private Boolean production; // RestEasy needs 'Boolean' here
    private String passphrase;
    private byte[] certificate;
    private String name;
    private String description;

    public Boolean getProduction() {
        return production;
    }

    /**
     * Reads the boolean flag from the multipart request,
     * which indicates if the iOS variant is a 'production' variant or not.
     *
     * The {@link iOSVariant} model differentiates between production and test
     * in order to establish connections to different APNs Servers.
     *
     * @param production flag if producution or not.
     */
    @FormParam("production")
    public void setProduction(Boolean production) {
        this.production = production;
    }

    public String getName() {
        return name;
    }

    /**
     * Reads the name field from the multipart request.
     *
     * @param name the name value
     */
    @FormParam("name")
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Reads the description field from the multipart request.
     *
     * @param description the description value
     */
    @FormParam("description")
    public void setDescription(String description) {
        this.description = description;
    }

    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Reads the passphrase field from the multipart request.
     *
     * @param passphrase the passphrase value
     */
    @FormParam("passphrase")
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public byte[] getCertificate() {
        return certificate;
    }

    /**
     * Reads the certificate file upload from the multipart request.

     * @param data the cert value
     */
    @FormParam("certificate")
    @PartType("application/octet-stream")
    public void setCertificate(byte[] data) {
        this.certificate = data;
    }

    /**
     * Validates whether the certificate/passphrase pair
     * is valid, and does not contain any bogus content.
     *
     * @return true if valid, otherwise false
     */
    @AssertTrue(message = "the provided certificate passphrase does not match with the uploaded certificate")
    public boolean isCertificatePassphraseValid() {
        try {
            PKCS12.validate(certificate, passphrase);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}