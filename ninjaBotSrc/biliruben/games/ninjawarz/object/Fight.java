package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/*
 * "fights":[
 {"my_ninja":19,
"opp_ninja":16,
"combat":[[0,53,149,0],[1,27,367,1],[0,63,86,0],[1,14,353,0],[0,77,9,0],[1,13,340,0],[0,53,-44,0]],
"winner":0},..]

 * 
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Fight extends JSONObject {

    private int my_ninja;
    private int opp_ninja;
    private int[][] combat;
    private int winner;
    
    
    public int getMy_ninja() {
        return my_ninja;
    }
    public void setMy_ninja(int my_ninja) {
        this.my_ninja = my_ninja;
    }
    public int getOpp_ninja() {
        return opp_ninja;
    }
    public void setOpp_ninja(int opp_ninja) {
        this.opp_ninja = opp_ninja;
    }
    public int[][] getCombat() {
        return combat;
    }
    public void setCombat(int[][] combat) {
        this.combat = combat;
    }
    public int getWinner() {
        return winner;
    }
    public void setWinner(int winner) {
        this.winner = winner;
    }
}
