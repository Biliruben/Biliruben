package biliruben.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

/**
 * Don't get excited, this is just something to get me through Monday:
 * 
 * Find all the sctrings that match the REGEX for an ID and just spitoon those out. Do me
 * a solid and throw them in a Set for filtering reasons
 * @author trey.kirk
 *
 */
public class BetterThanSed {

    private static GetOpts _opts;


    public BetterThanSed() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws IOException {
        //String testString = "2019-04-26 07:41:02,492 TRACE QuartzScheduler_Worker-2 sailpoint.api.Certificationer:122 - Entering getEntitlizer(cert = sailpoint.object.Certification@752d12e4[id=8ab2a2276a47c393016a59a9c10163fc,name=Key Contact Security Review for Joseph Santhanasamy-04/22/19 09:27:49 PM CDT])";
        init(args);
        Set<String> ids = new HashSet<String>();
        
        Pattern p = Pattern.compile("^.*([0-91-f]{32}).*$");
        String fileName = _opts.getStr("file");
        File f = new File(fileName);
        FileReader fr = new FileReader(f);
        BufferedReader b = new BufferedReader(fr);
        String line = null;
        do {
            line = b.ready() ? b.readLine() : null;
            if (line != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    ids.add(m.group(1));
                }
            }
        } while (line != null);
        b.close();
        
        for (String id : ids) {
            System.out.println(id);
        }
    }
    
    
    private static void init(String[] args) {
        _opts = new GetOpts(BetterThanSed.class);
        OptionLegend legend = new OptionLegend("file");
        legend.setRequired(true);
        _opts.addLegend(legend);
        
        _opts.setOpts(args);
    }

}
