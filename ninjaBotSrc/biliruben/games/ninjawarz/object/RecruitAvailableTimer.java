package biliruben.games.ninjawarz.object;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import biliruben.games.ninjawarz.NinjaBotException;

/*
 * This one is pretty hairy, maybe for v3
 */
public class RecruitAvailableTimer extends AbstractTrigger implements Timer {

    public enum Criteria {
        Gender("Gender"),
        Age("Age"),
        Blood("Blood Type"),
        Power("Power"),
        Health("Health"),
        Balanced("Balanced"),
        Superior("Superior"),

        AgeLessThan18("Less than 18"),
        AgeMoreThan35("More than 35"),

        BloodABPos("AB+"),
        BloodABNeg("AB-"),
        BloodAPos("A+"),
        BloodANeg("A-"),
        BloodBPos("B+"),
        BloodBNeg("B-"),
        BloodOPos("O+"),
        BloodONeg("O-"),

        GenderMale("Male"),
        GenderFemale("Femail"),

        PowerGreaterThan("Power: Greater Than"),
        PowerLessThan("Power: Less Than"),

        HealthGreaterThan("Health: Greater Than"),
        HealthLessThan("Health: Less Than"),

        End("End"); // not a real condition, but used in the selection lists

        private String _display;

        private Criteria(String displayValue) {
            _display = displayValue;
        }

        public String getDisplayValue() {
            return _display;
        }

    }

    private String _prompt;

    private Criteria[] GENERAL_CRITERIA = {Criteria.Gender, Criteria.Age, Criteria.Blood, Criteria.Power, Criteria.Health, Criteria.Balanced, Criteria.Superior, Criteria.End};
    private Criteria[] AGE_CRITERIA = {Criteria.AgeLessThan18, Criteria.AgeMoreThan35};
    private Criteria[] BLOOD_CRITERIA = {Criteria.BloodOPos, Criteria.BloodONeg, Criteria.BloodAPos, Criteria.BloodANeg ,Criteria.BloodBPos,
            Criteria.BloodBNeg, Criteria.BloodABPos, Criteria.BloodABNeg};
    private Criteria[] GENDER_CRITERIA = {Criteria.GenderMale, Criteria.GenderFemale};
    private Criteria[] POWER_CRITERIA = {Criteria.PowerGreaterThan, Criteria.PowerLessThan};
    private Criteria[] HEALTH_CRITERIA = {Criteria.HealthGreaterThan, Criteria.HealthLessThan};


    @Override
    protected boolean internalEvaluate() throws NinjaBotException {

        // for every recruit available, send them through a series of conditional checks.  Return true on the first match
        try {
            // get recruits
            List<Recruit> recruits = getBot().getRecruits();

            // iterate recruit list
            for (Recruit recruit : recruits) {
                boolean matched = false;
                for (String criteriaKey : getTriggerData().keySet()) {
                    Criteria crit = Criteria.valueOf(criteriaKey);
                    matched = testRecruit(crit, recruit);
                    if (!matched) {
                        break; // criteria for loop
                    }
                }
                if (matched) {
                    setMatchedObject(recruit);
                    return true;
                }
            }
            // for each recruit, match against all asked for conditions
        } catch (IOException e) {
            getBot().logError(e);
        }
        return false;
    }

    private boolean testRecruit(Criteria crit, Ninja recruit) {
        // to avoid a crazy complicated switch, we'll do some divide 'n conquire
        switch(crit) {
        case AgeLessThan18:
        case AgeMoreThan35:
            return testAge(crit, recruit);

        case BloodABNeg:
        case BloodABPos:
        case BloodANeg:
        case BloodAPos:
        case BloodBNeg:
        case BloodBPos:
        case BloodONeg:
        case BloodOPos:
            return testBlood(crit, recruit);

        case GenderFemale:
        case GenderMale:
            return testGender(crit, recruit);

        case HealthGreaterThan:
        case HealthLessThan:
            return testHealth(crit, recruit);

        case PowerGreaterThan:
        case PowerLessThan:
            return testPower(crit, recruit);

        case Balanced:
            return testBalanced(recruit);

        case Superior:
            return testSuperior(recruit);
        }

        return false;
    }

    private boolean testSuperior(Ninja recruit) {
        // a superior ninja's power + health will add up to over 90.  Note, elites will add up to 100 and we don't ever want
        // an elite to pass our filter
        int total = recruit.getPower() + recruit.getHealth();
        return (total > 90 && !recruit.getElite());
    }

    private boolean testBalanced(Ninja recruit) {
        // Balanced ninjas have stats within 2 of each other
        int delta = Math.abs(recruit.getPower() - recruit.getHealth());
        return delta <= 2;
    }

    private boolean testPower(Criteria crit, Ninja recruit) {
        int testPower = recruit.getPower();
        int threshold = (Integer) getTriggerData().get(crit.toString());
        if (crit == Criteria.PowerGreaterThan) {
            return (testPower > threshold);
        } else if (crit == Criteria.PowerLessThan) {
            return (testPower < threshold);
        }
        // if we got here, the programmer did something wrong
        return false;
    }

    private boolean testHealth(Criteria crit, Ninja recruit) {
        int testHealth = recruit.getHealth();
        int threshold = (Integer) getTriggerData().get(crit.toString());
        if (crit == Criteria.HealthGreaterThan) {
            return (testHealth > threshold);
        } else if (crit == Criteria.HealthLessThan) {
            return (testHealth < threshold);
        }
        // if we got here, the programmer did something wrong
        return false;
    }

    private boolean testGender(Criteria crit, Ninja recruit) {
        int gender = recruit.getGender();
        if (crit == Criteria.GenderFemale) {
            return gender != 0; 
        } else if (crit == Criteria.GenderMale) {
            return gender == 0;
        }
        // if we got here, the programmer did something wrong
        return false;
    }

    private boolean testBlood(Criteria crit, Ninja recruit) {
        String bloodType = recruit.getBlood_type();
        switch(crit) {
        case BloodABNeg:
            return ("AB-".equals(bloodType));
        case BloodABPos:
            return ("AB+".equals(bloodType));
        case BloodANeg:
            return ("A-".equals(bloodType));
        case BloodAPos:
            return ("A+".equals(bloodType));
        case BloodBNeg:
            return ("B-".equals(bloodType));
        case BloodBPos:
            return ("B+".equals(bloodType));
        case BloodONeg:
            return ("O-".equals(bloodType));
        case BloodOPos:
            return ("O+".equals(bloodType));
        }
        
        // we should never get here
        getBot().logError(new NinjaBotException("Unexpected blood type: " + bloodType));
        return false;
    }

    private boolean testAge(Criteria crit, Ninja recruit) {
        // sample birthday{"for_mysql":"1990-09-05","pretty":"Sep 5, 1390"}
        // so we'll parse the mysql date and get three tokens: year, month, day.  With that, we'll calculate
        // how old they are from today's date
        String mysqlDate = recruit.getBirthdate().getFor_mysql();
        String[] dateTokens = mysqlDate.split("-");
        int year = Integer.valueOf(dateTokens[0]);
        int month = Integer.valueOf(dateTokens[1]);
        int day = Integer.valueOf(dateTokens[2]);
        Calendar today = GregorianCalendar.getInstance();
        // set it to midnight
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0); 
        today.set(Calendar.SECOND, 00);
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(year, month, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0); 
        cal.set(Calendar.SECOND, 00);
        
        if (crit == Criteria.AgeLessThan18) {
            // set today's date to 18 years ago, see if cal's date is after today
            today.set(Calendar.YEAR, today.get(Calendar.YEAR) - 18);
            if (today.before(cal)) {
                // younger!
                return true;
            }
        } else if (crit == Criteria.AgeMoreThan35) {
            today.set(Calendar.YEAR, today.get(Calendar.YEAR) - 35);
            if (today.after(cal)) {
                // older!
                return true;
            }
        }
        
        return false;
    }

    private static Criteria selectFromList(String prompt, List<Criteria> values, PrintStream output, Scanner fromInput) {
        Criteria choice = null;
        if (values == null || values.size() == 0) {
            return choice;
        }
        boolean validSelection = false;
        while (!validSelection) {
            int selectColumn = + 5;
            int valueColumn = 30;
            for (int i = 0; i < values.size(); i++) {

                output.printf("%-" + selectColumn + "s", "[" + i + "]: ");
                Criteria criteria = values.get(i);
                output.printf("%-" + valueColumn + "s", criteria.getDisplayValue());
                output.println();
            }
            output.print(prompt);
            String rawChoice = fromInput.nextLine();
            try {
                int index = Integer.valueOf(rawChoice);
                if (index >= values.size() || index < 0) {
                    output.println("Sorry, " + index + " is not a valid selection.  Please select using one of the numbers list to the left.");
                } else {
                    choice = values.get(index);
                    validSelection = true;
                }
            } catch (NumberFormatException e) {
                output.println("Sorry, " + rawChoice + " is not a valid selection.  Please select using one of the numbers list to the left.");
            }
        }
        return choice;
    }

    @Override
    public Map<String, Object> createTriggerData(Scanner fromInput) {
        Map<String, Object> recruitFilters = new HashMap<String, Object>();

        Criteria choice = null;
        List<Criteria> values = Arrays.asList(GENERAL_CRITERIA);
        getBot().getOutput().println("Select a criteria or select end to finish:");
        do {
            choice = selectFromList(getPrompt(recruitFilters), values, getBot().getOutput(), fromInput);
            switch (choice) {
            case Age:
                createSubCriteria(recruitFilters, Arrays.asList(AGE_CRITERIA), fromInput);
                break;
            case Balanced:
                recruitFilters.put(Criteria.Balanced.toString(), true);
                break;
            case Blood:
                createSubCriteria(recruitFilters, Arrays.asList(BLOOD_CRITERIA), fromInput);
                break;
            case Gender:
                createSubCriteria(recruitFilters, Arrays.asList(GENDER_CRITERIA), fromInput);
                break;
            case Health:
                createHealthCriteria(recruitFilters, fromInput);
                break;
            case Power:
                createPowerCriteria(recruitFilters, fromInput);
                break;
            case Superior:
                recruitFilters.put(Criteria.Superior.toString(), true);
                break;
            }
        } while (choice != Criteria.End);

        return recruitFilters;
    }

    private static String getPrompt(Map currentCriteria) {
        if (currentCriteria == null || currentCriteria.size() == 0) {
            return "(recruitTimer)$ ";
        } else {
            return "(recruitTimer:" + currentCriteria + ")$ ";
        }
    }

    private void createPowerCriteria(Map<String, Object> recruitFilters, Scanner fromInput) {
        Criteria powerCriteria = selectFromList(getPrompt(recruitFilters), Arrays.asList(POWER_CRITERIA), getBot().getOutput(), fromInput);
        boolean validSelection = false;
        while (!validSelection) {
            getBot().getOutput().println("Input the power limit (exclusive): ");
            getBot().getOutput().print(getPrompt(recruitFilters));
            String rawAnswer = fromInput.nextLine();
            try {
                int threshold = Integer.valueOf(rawAnswer);
                recruitFilters.put(powerCriteria.toString(), threshold);
                validSelection = true;
            } catch (NumberFormatException e) {
                getBot().getOutput().println("An integer value must be supplied");
            }
        }
    }

    private void createHealthCriteria(Map<String, Object> recruitFilters,
            Scanner fromInput) {
        Criteria healthCriteria = selectFromList(getPrompt(recruitFilters), Arrays.asList(HEALTH_CRITERIA), getBot().getOutput(), fromInput);
        boolean validSelection = false;
        while (!validSelection) {
            getBot().getOutput().println("Input the health limit (exclusive): ");
            getBot().getOutput().print(getPrompt(recruitFilters));
            String rawAnswer = fromInput.nextLine();
            try {
                int threshold = Integer.valueOf(rawAnswer);
                recruitFilters.put(healthCriteria.toString(), threshold);
                validSelection = true;
            } catch (NumberFormatException e) {
                getBot().getOutput().println("An integer value must be supplied");
            }
        }
    }

    private void createSubCriteria(Map<String, Object> criteriaMap, List<Criteria> subCriteria, Scanner fromInput) {
        Criteria ageCriteria = selectFromList(getPrompt(criteriaMap), subCriteria, getBot().getOutput(), fromInput);
        criteriaMap.put(ageCriteria.toString(), true);
    }

    @Override
    public String getRecommendedFrequency() {
        return "00:10:00";
    }

}
