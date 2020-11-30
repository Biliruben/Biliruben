package sailpoint.rest;


public class TreyRestApplication extends SailPointRestApplication {
    
    public TreyRestApplication () {
        super();
        register(TKResource.class);
    }
}
