package biliruben.games.ninjawarz.object;

import java.io.IOException;

import biliruben.games.ninjawarz.NinjaBotException;

public abstract class BossTrigger extends AbstractTrigger implements Timer {

    public enum BossName {
        Genbu("genbu"),
        Girl("girl"),
        SmallGirl("small_girl"),
        MechaGenbu("mechagenbu");

        private String _name;

        private BossName(String name) {
            _name = name;
        }

        public String getName() {
            return _name;
        }
    }
    protected String _bossName;

    public BossTrigger(String bossName) {
        super();
        _bossName = bossName;
    }

    public BossTrigger(BossName bossName) {
        this(bossName.getName());
    }

    @Override
    protected boolean internalEvaluate() throws NinjaBotException {
        try {
            Stage stage = getBot().getStage();
            BossCollection bosses = stage.getBosses();
            if (bosses != null) {
                for (Boss boss : bosses) {
                    if (boss != null && _bossName.equalsIgnoreCase(boss.getInternal_name())) {
                        setMatchedObject(boss);
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            getBot().logError(e);
        }
        return false;
    }

}
