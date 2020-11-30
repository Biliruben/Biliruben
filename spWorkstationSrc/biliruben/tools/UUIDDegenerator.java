package biliruben.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;


public class UUIDDegenerator {

    private static final String UUID = "uuid";
    private static GetOpts _opts;

    public UUIDDegenerator() {
        // TODO Auto-generated constructor stub
    }

    public static void main(String[] args) throws UnknownHostException {
        init(args);
        
        String uuid = _opts.getStr(UUID);
        if (uuid == null || "".equals(uuid.trim())) {
            System.out.println("Nothing to do");
            return;
        } else if (uuid.length() != 32) {
            System.out.println(uuid + " is not a valid 32 character UUID");
            return;
        }
        
        String ipAddy = uuid.substring(0, 8);
        String sysTime = uuid.substring(8,16);
        String hiTime = uuid.substring(16,20);
        String loTime = uuid.substring(20, 28);
        String counter = uuid.substring(28, 32);
        
        System.out.println("Hex values: ");
        System.out.println("ipAddy: " + ipAddy);
        System.out.println("sysTime: " + sysTime);
        System.out.println("hiTime: " + hiTime);
        System.out.println("loTime: " + loTime);
        System.out.println("counter: " + counter + " (" + Integer.parseInt(counter, 16) + ")");
        
        System.out.println("IP Address: " + getIp(ipAddy));
        
    }
    
    private static String getIp(String fromHex) {
        StringBuilder ipAddressBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int pos = i * 2;
            String octetHex = fromHex.substring(pos, pos + 2);
            String high = null;
            String low = null;
            if (octetHex.length() > 2) {
                high = "0";
                low = octetHex;
            } else {
                high = octetHex.substring(0,1);
                low = octetHex.substring(1,2);
            }
            int decHigh = Integer.parseInt(high, 16);
            int rotated = 0xF - 0x7 + decHigh;
            if (rotated > 0xF) {
                rotated = Math.abs(rotated - 0x10);
            }
            high = Integer.toHexString(rotated);

            int octet=Integer.parseInt(high + low,16);
            ipAddressBuilder.append(octet).append(" ");
        }
        
        return ipAddressBuilder.toString();

        
    }
    
    private static void init(String[] args) {
        _opts = new GetOpts(UUIDDegenerator.class);

        OptionLegend legend = new OptionLegend(UUID);
        legend.setRequired(true);
        legend.setDescription("UUID to deconstruct");
        _opts.addLegend(legend);
        
        _opts.parseOpts(args);

    }

}
