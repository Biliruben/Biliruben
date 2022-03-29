package biliruben.tools;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import com.biliruben.util.GetOpts;
import com.biliruben.util.OptionLegend;


public class UUIDDegenerator {

    private static final String UUID = "uuid";
    private static GetOpts _opts;
    private String uuid;
    private String ipAddyHex;
    private String sysTime;
    private String hiTime;
    private String loTime;
    private String counter;

    public UUIDDegenerator(String uuid) {
        this.uuid = uuid;
        deconstruct();
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
        
        UUIDDegenerator deGenner = new UUIDDegenerator(uuid);
        String ipAddy = deGenner.getIpHex();
        String sysTime = deGenner.getSysTime();
        String hiTime = deGenner.getHiTime();
        String loTime = deGenner.getLoTime();
        String counter = deGenner.getCounter();
        
        System.out.println("Hex values");
        System.out.println("ipAddy: " + ipAddy);
        System.out.println("sysTime: " + sysTime);
        System.out.println("hiTime: " + hiTime);
        System.out.println("loTime: " + loTime);
        System.out.println("counter: " + counter + " (" + Integer.parseInt(counter, 16) + ")");
        
        System.out.println("IP Address: " + deGenner.getIp());
    }
    

    private void deconstruct() {
        ipAddyHex = uuid.substring(0, 8);
        sysTime = uuid.substring(8,16);
        hiTime = uuid.substring(16,20);
        loTime = uuid.substring(20, 28);
        counter = uuid.substring(28, 32);
    }

    public String getIpHex() {
        return this.ipAddyHex;
    }
    
    public String getSysTime() {
        return this.sysTime;
    }
    
    public String getHiTime() {
        return this.hiTime;
    }
    
    public String getLoTime() {
        return this.loTime;
    }

    public String getCounter() {
        return this.counter;
    }
    
    public String getIp() {
        StringBuilder ipAddressBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int pos = i * 2;
            String octetHex = this.ipAddyHex.substring(pos, pos + 2);
            int octet = Integer.parseInt(octetHex, 16);
            ipAddressBuilder.append(octet).append(".");
        }
        int length = ipAddressBuilder.length();
        ipAddressBuilder.delete(length - 1, length);
        return ipAddressBuilder.toString();
    }

    public String getIp_rotated() {
        StringBuilder ipAddressBuilder = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            int pos = i * 2;
            String octetHex = this.ipAddyHex.substring(pos, pos + 2);
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
        ipAddressBuilder.delete(ipAddressBuilder.length() - 1, ipAddressBuilder.length());
        
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
