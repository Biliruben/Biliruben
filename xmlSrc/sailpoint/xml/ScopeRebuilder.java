package sailpoint.xml;

/**
 * Given an input XML of an export of Scopes from IdentityIQ:
 * - Store the rendered data in its natural hierarchy
 *  - Use sailpoint API to parse XML and build proper object (can I leverage XML factory and POJO)? Or do I need a spring starter?
 * - Export the rendered data in hierarchy order from top to bottom (parent-first)
 *  - If I have a SailPointContext, mebe I can go straight to dB with this?
 *  - At this piont, why aren't I extending TKConsole instead of this shit?
 * @author trey.kirk
 *
 */
public class ScopeRebuilder {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
    
    
    

}
