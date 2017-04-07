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
package org.jboss.aerogear.unifiedpush.rest.registry.applications;

import com.qmino.miredot.annotations.ReturnType;
import org.jboss.aerogear.unifiedpush.api.AndroidVariant;
import org.jboss.aerogear.unifiedpush.api.PushApplication;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

@Path("/applications/{pushAppID}/android")
public class AndroidVariantEndpoint extends AbstractVariantEndpoint {

    /**
     * Add Android Variant
     *
     * @param androidVariant    new {@link AndroidVariant}
     * @param pushApplicationID id of {@link PushApplication}
     * @param uriInfo           the uri
     * @return                  created {@link AndroidVariant}
     *
     * @statuscode 201 The Android Variant created successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested PushApplication resource does not exist
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("org.jboss.aerogear.unifiedpush.api.AndroidVariant")
    public Response registerAndroidVariant(
            AndroidVariant androidVariant,
            @PathParam("pushAppID") String pushApplicationID,
            @Context UriInfo uriInfo) {

        // find the root push app
        PushApplication pushApp = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);

        if (pushApp == null) {
            return Response.status(Status.NOT_FOUND).entity("Could not find requested PushApplicationEntity").build();
        }

        // some validation
        try {
            validateModelClass(androidVariant);
        } catch (ConstraintViolationException cve) {

            // Build and return the 400 (Bad Request) response
            ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

            return builder.build();
        }

        // store the Android variant:
        variantService.addVariant(androidVariant);
        // add iOS variant, and merge:
        pushAppService.addVariant(pushApp, androidVariant);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(String.valueOf(androidVariant.getVariantID())).build()).entity(androidVariant).build();
    }

    /**
     * List Android Variants for Push Application
     *
     * @param pushApplicationID id of {@link PushApplication}
     * @return                  list of {@link AndroidVariant}s
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.util.Set<org.jboss.aerogear.unifiedpush.api.AndroidVariant>")
    public Response listAllAndroidVariationsForPushApp(@PathParam("pushAppID") String pushApplicationID) {
        final PushApplication application = getSearch().findByPushApplicationIDForDeveloper(pushApplicationID);
        return Response.ok(getVariantsByType(application, AndroidVariant.class)).build();
    }

    /**
     * Update Android Variant
     *
     * @param id                        id of {@link PushApplication}
     * @param androidID                 id of {@link AndroidVariant}
     * @param updatedAndroidApplication new info of {@link AndroidVariant}
     *
     * @return                  updated {@link AndroidVariant}
     *
     * @statuscode 200 The Android Variant updated successfully
     * @statuscode 400 The format of the client request was incorrect
     * @statuscode 404 The requested Android Variant resource does not exist
     */
    @PUT
    @Path("/{androidID}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ReturnType("java.lang.Void")
    public Response updateAndroidVariation(
            @PathParam("pushAppID") String id,
            @PathParam("androidID") String androidID,
            AndroidVariant updatedAndroidApplication) {

        AndroidVariant androidVariant = (AndroidVariant) variantService.findByVariantID(androidID);
        if (androidVariant != null) {

            // some validation
            try {
                validateModelClass(updatedAndroidApplication);
            } catch (ConstraintViolationException cve) {

                // Build and return the 400 (Bad Request) response
                ResponseBuilder builder = createBadRequestResponse(cve.getConstraintViolations());

                return builder.build();
            }

            // apply updated data:
            androidVariant.setGoogleKey(updatedAndroidApplication.getGoogleKey());
            androidVariant.setProjectNumber(updatedAndroidApplication.getProjectNumber());
            androidVariant.setName(updatedAndroidApplication.getName());
            androidVariant.setDescription(updatedAndroidApplication.getDescription());
            variantService.updateVariant(androidVariant);
            return Response.ok(androidVariant).build();
        }

        return Response.status(Status.NOT_FOUND).entity("Could not find requested Variant").build();
    }
}
