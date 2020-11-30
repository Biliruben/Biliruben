package biliruben.games.ninjawarz.object;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tournament extends JSONObject {
    
    public class Data extends JSONObject {
        private Clan[] opponents;
        private int[] matches;
        private Item prize;
        private long tid;
        
        
        public Clan[] getOpponents() {
            return opponents;
        }
        public void setOpponents(Clan[] opponents) {
            this.opponents = opponents;
        }
        public int[] getMatches() {
            return matches;
        }
        public void setMatches(int[] matches) {
            this.matches = matches;
        }
        public Item getPrize() {
            return prize;
        }
        public void setPrize(Item prize) {
            this.prize = prize;
        }
        public long getTid() {
            return tid;
        }
        public void setTid(long tid) {
            this.tid = tid;
        }
    }
    
    private Data data;
    
    public enum TournamentType {
        SameLevel("same%5Flevel"),
        OpenLevel("open");
        
        private String _type;

        TournamentType(String type) {
            _type = type;
        }
        
        public String getType() {
            return _type;
        }
    }

    public Tournament() {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public String getId() {
        return String.valueOf(getTid());
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
    
    public int[] getMatches() {
        return this.data.getMatches();
    }
    
    public Clan[] getOpponents() {
        return this.data.getOpponents();
    }
    
    public Item getPrize() {
        return this.data.getPrize();
    }
    
    public long getTid() {
        return this.data.getTid();
    }
    
    public Clan[] getMatchedClans() {
        int[] matches = data.getMatches();
        int size = matches.length;
        Clan[] clans = new Clan[size];
        for (int i = 0; i < size; i++) {
            clans[i] = data.getOpponents()[matches[i]];
        }
        return clans;
    }

}
