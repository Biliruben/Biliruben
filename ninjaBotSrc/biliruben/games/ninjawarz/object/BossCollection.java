package biliruben.games.ninjawarz.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BossCollection extends JSONObject implements Iterable<Boss> {
    
    private Boss genbu;
    private Boss mechagenbu;
    private Boss girl;
    private Boss small_girl;
    
    public Boss getGenbu() {
        return genbu;
    }
    public void setGenbu(Boss genbu) {
        this.genbu = genbu;
    }
    public Boss getMechagenbu() {
        return mechagenbu;
    }
    public void setMechagenbu(Boss mechagenbu) {
        this.mechagenbu = mechagenbu;
    }
    public Boss getGirl() {
        return girl;
    }
    public void setGirl(Boss girl) {
        this.girl = girl;
    }
    public Boss getSmall_girl() {
        return small_girl;
    }
    public void setSmall_girl(Boss small_girl) {
        this.small_girl = small_girl;
    }
    @Override
    public Iterator<Boss> iterator() {
        List<Boss> bosses = new ArrayList<Boss>();
        if (genbu != null) {
            bosses.add(genbu);
        }
        if (mechagenbu != null) {
            bosses.add(mechagenbu);
        }
        if (girl != null) {
            bosses.add(girl);
        }
        if (small_girl != null) {
            bosses.add(small_girl);
        }
        return bosses.iterator();
    }
    public boolean isEmpty() {
        return genbu == null && girl == null && mechagenbu == null && small_girl == null;
    }

}
