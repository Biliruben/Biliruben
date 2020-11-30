package biliruben.tools;

import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;

public class Base64Util {

    private static final String OPT_VALUE = "value";
    private static final String OPT_DECODE = Command.decode.toString();
    private static final String OPT_ENCODE = Command.encode.toString();
    private static final String OPT_COMMAND = "command";
    private static GetOpts _opts;

    private enum Command {
        decode,
        encode
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        init(args);
        List<String> values = _opts.getList(OPT_VALUE);
        Command command = Command.valueOf(_opts.getStr(OPT_COMMAND));
        switch (command) {
        case decode: 
            decode(values); break;
        case encode:
            encode(values); break;
        }
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(Base64Util.class);
        
        OptionLegend legend = new OptionLegend(OPT_COMMAND, "The thing you want to do");
        legend.setAllowedValues(new String[]{OPT_ENCODE, OPT_DECODE});
        legend.setRequired(true);
        _opts.addLegend(legend);
        
        legend = new OptionLegend(OPT_VALUE, "A value to apply to the command");
        legend.setMulti(true);
        legend.setRequired(false);
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);
    }
    
    private static void decode(List<String> toDecode) {
        for (String decode : toDecode) {
            byte[] decodedBytes = Base64.decodeBase64(decode);
            System.out.println("decodedBytes " + new String(decodedBytes));
        }
    }
    
    private static void encode(List<String> toEncode) {
        for (String encode : toEncode) {
            byte[] encodedBytes = Base64.encodeBase64(encode.getBytes());
            System.out.println(encode + " ==> " + new String(encodedBytes));
        }
    }

}
