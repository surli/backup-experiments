package io.swagger.jaxrs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Api
@Path("/classParent")
public class Test2ClassParentResource implements Test2InterfaceChildResource {

    @GET
    @Path("/testValue")
    @ApiOperation("Returns a test value")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = String.class)})
    public Response getTestValue() {
        return Response.ok("Success").build();
    }
}
