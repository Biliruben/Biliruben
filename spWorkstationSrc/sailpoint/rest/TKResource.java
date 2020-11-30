package sailpoint.rest;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import sailpoint.authorization.RightAuthorizer;
import sailpoint.object.SPRight;
import sailpoint.tools.GeneralException;

@Path("trey")
public class TKResource extends BaseResource {

    @GET
    @Path("ping/{delay}")
    public String ping(
            @DefaultValue("5000") @PathParam("delay") long delay) throws GeneralException, InterruptedException {
        // Same as IIQResource#ping, just takes (configurably) longer
        // Ping, then wait. Ping so that it will do something meanial and work.
        // Wait, because we're using this to test concurrent connections.
        String result = "IIQ TK JAX-RS is better than dead";
        authorize(new RightAuthorizer(SPRight.WebServices));
        if (delay > 0) {
            Thread.sleep(delay);
        }
        return result;
    }
    
}
