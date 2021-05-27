package data.console.commands;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;


// TODO IMPORTANT https://fractalsoftworks.com/forum/index.php?topic=17103.msg320152#msg320152

public class Explore implements BaseCommand {
    static boolean playerSurveyFull = false; // if true, survey even unsurveyed planets
    static int playerPlanetCount = 1; // minimum number of planets in a system
    static boolean foundAdequateSystem = false;

    protected static void explore(LocationAPI loc, HashMap<String, Integer> playerConditions)//, boolean playerSurveyFull, int playerPlanetCount, String[] playerConditions)
    {
        /*
            location/system nema
            main star/black hole + type
            [additional stars/black holes + types]
            planet name + type + conditions
            moons + type + conditions
        */

        // TODO filter by active gate and maybe more
        final StringBuilder sb = new StringBuilder();
        String constellationName = "";

        // LOCAL VARIABLES
        // System
        try {
            constellationName = loc.getConstellation().getName();
        } catch (Exception e) {
            // TODO skip all planets in the core world
            constellationName = "Core world";
        }

        sb.append("Constellation `").append(constellationName).append("`\n");
        sb.append("System `").append(loc.getName()).append("` ");

        boolean activeGate = false;
        int jumpPointCount = 0;
        int stableLocationCount = 0;
        int planetsInSystem = 0; // also used as a counter variable
        boolean nebula = false; // TODO filter by nebula?

        // Stars
        // FullName, TypeNameWithWorld
        HashMap<String, String> stars = new HashMap<String, String>();

        // Planets and conditions
        String[] planetNames = new String[20];
        String[] planetTypes = new String[20];
        int[] planetHazardValues = new int[20];

        int numberOfPlayerConditions = 0;
        int numberOfConditionMet = 0;
        // [[habitable,...],[extreme weather,...]]
        String[][] planetConditions = new String[20][10]; // up to 20 planets with 10 conditions each
        // condition1=2, condition2=5...
        HashMap<String, Integer> allConditions = new HashMap<String, Integer>();
        // LOCAL VARIABLES END

        // add all values
        for (Integer v : playerConditions.values()) {
            numberOfPlayerConditions = numberOfPlayerConditions + v;
        }

        jumpPointCount = loc.getEntities(JumpPointAPI.class).size();
        stableLocationCount = loc.getEntitiesWithTag("stable_location").size();

        for (SectorEntityToken token : loc.getAllEntities()) {
            // TODO find a way to discard looping if token isn't what we want
            if (token.hasTag("gate")) {
                // TODO gate as an argument
                activeGate = true;
            }
            try {
                // all entities are even fleets, derelict ships and more
                StarSystemAPI system = token.getStarSystem();

                PlanetAPI primary = system.getStar();
                PlanetAPI secondary = system.getSecondary();
                PlanetAPI tertiary = system.getTertiary();
                if (primary != null) {
                    // Galatia=Yellow star OR Gamma Nautilus=Nebula (Young) OR Something=Black hole
                    stars.put(system.getStar().getFullName(), system.getStar().getTypeNameWithWorld());
                    if (secondary != null)
                        stars.put(system.getSecondary().getFullName(), system.getSecondary().getTypeNameWithWorld());
                    if (tertiary != null)
                        stars.put(system.getTertiary().getFullName(), system.getTertiary().getTypeNameWithWorld());
                }
            } catch (Exception e) {
                // hidden location, feel, jump point, gravity well etc.
                continue;
            }
        }

        sb.append(stars).append(", jump points: ").append(jumpPointCount).append(", stable locations: ").append(stableLocationCount);
        if (activeGate) {
            sb.append(", has Inactive gate\n");
        } else {
            sb.append("\n");
        }

        // TODO Distinguish moons and planets and sort accordingly
        for (PlanetAPI planet : loc.getPlanets()) {
            StringBuilder planetsAndConditions = new StringBuilder("");
            if (!planet.isStar() & !planet.isNormalStar()) {
                planetNames[planetsInSystem] = planet.getName();
                planetTypes[planetsInSystem] = planet.getTypeNameWithWorld();
                MarketAPI planetMarket = planet.getMarket();
                String factionName = planetMarket.getFaction().getDisplayName();
                planetHazardValues[planetsInSystem] = (int) (planetMarket.getHazardValue() * 100);
                MarketAPI.SurveyLevel planetSurveyLevel = planetMarket.getSurveyLevel();
                // set survey lvl to FULL if the player specified
                if (playerSurveyFull) planetMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

                String f = String.format("%-25s %20s", planetNames[planetsInSystem].toString(), "(" + factionName + ")");
                String d = " [" + planetTypes[planetsInSystem].toString() + ", " + planetHazardValues[planetsInSystem] + "%" + ", Survey lvl: " + planetSurveyLevel.toString() + "]: ";
                String f1 = String.format("%-53s", d);
                planetsAndConditions.append("\t-").append(f).append(f1);

                // get all planet conditions
                int counter = 0; // counts the number of planetary conditions per planet
                try {
                    // quest planet fails this check
                    for (MarketConditionAPI condition : planetMarket.getConditions()) {
                        if (condition.isPlanetary()) {
                            // gets only conditions that apply to the planet itself, not the market (e.g. pirate raid, goods shortages, etc.)
                            String condName = "";
                            if (playerSurveyFull) {
                                // if true, shows all conditions, whether surveyed or not
                                // and mark each condition as surveyed
                                condName = condition.getId(); // ore_sparse
                                condition.setSurveyed(true);
                            } else {
                                if (condition.isSurveyed()) {
                                    condName = condition.getId(); // ore_sparse
                                }
                            }

                            planetConditions[planetsInSystem][counter] = condName;
                            // Check whether a condition exists and increment it's count
                            // Could be replaced with shorter syntax, but for compatibility reasons with Java 7 shouldn't
                            // int value = allConditions.containsKey(condName) ? allConditions.get(condName) : 0;
                            if (allConditions.containsKey(condName)) {
                                int value = (Integer) allConditions.get(condName);
                                value++;
                                allConditions.put(condName, value);
                            } else {
                                allConditions.put(condName, 1);
                            }

                            planetsAndConditions.append(condName).append(", ");
                            // increment the planetary condition counter
                            counter++;
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
//                    Console.showMessage("No market conditions on planet: " + planetNames[planetsInSystem]);
                    planetsAndConditions.append("no_conditions");
                }
                // increment the planet counter
                planetsInSystem++;
            }
            String f2 = String.format("%20s", planetsAndConditions + "\n");
            sb.append(f2);
            // Console.showMessage(planetsAndConditions);
        }

        // Checks whether the planet count criterion is met
        if (planetsInSystem >= playerPlanetCount) {
            int value = 0;
            // Check if the condition criterion is met
            for (Map.Entry<String, Integer> condition : playerConditions.entrySet()) {
                // Could be replaced with shorter syntax, but for compatibility reasons with Java 7 shouldn't
                for (int i = (Integer) condition.getValue(); i > 0; i--) {
                    if (allConditions.containsKey(condition.getKey())) {
                        value = (Integer) allConditions.get(condition.getKey());
                    } else {
                        // break when check fails
                        break;
                    }
                    //int value = allConditions.containsKey(condition) ? allConditions.get(condition) : 0;
                    if (value > 0) {
                        value--;
                        allConditions.put(condition.getKey(), value);
                        numberOfConditionMet++;
                    } else {
                        // break when check fails
                        break;
                    }
                }
            }
            if (numberOfConditionMet >= numberOfPlayerConditions) {
                // set global variable to true
                foundAdequateSystem = true;
                // Console.showMessage("All requests met.");
                Console.showMessage("Planets in system: " + planetsInSystem);
                Console.showMessage(sb + "\n");
            }
        }
    }

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        // args:
        //  full: bool - search even planets that you haven't surveyed
        //  PlanetNumber: int - minimum number of planets that need to be present in a system
        //  Conditions: array - what conditions need to be present
        //              nr of repeated conditions equals the nr of planets that must satisfy them

        // Known error if you want to modify the code yourself:
        //  1) Program doesn't work if you leave <> in HashMap, or any other complex type, empty; although it shouldn't be required since Java 7

        HashMap<String, Integer> playerConditions = new HashMap<String, Integer>(); // planetary conditions

        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (!args.isEmpty()) {
            final String[] tmp = args.toLowerCase().split(" ");
            // Console.showMessage("Arguments: " + Arrays.toString(tmp));
            for (String t : tmp) {
                if (t.equalsIgnoreCase("true")) {
                    playerSurveyFull = true;
                } else if (t.equalsIgnoreCase("false")) {
                    playerSurveyFull = false;
                } else if (t.charAt(0) == '[') {
                    try {
                        String[] tmp_t = t.substring(1, t.length() - 1).split(","); // ["habitable=2",...]
                        // passing in [] results in a single empty string
                        if (tmp_t.length != 0 & !tmp_t[0].isEmpty()) {
                            for (String cond : tmp_t) {
                                String[] tmp_cond = cond.split("=");
                                try {
                                    playerConditions.put(tmp_cond[0], Integer.parseInt(tmp_cond[1]));
                                } catch (NumberFormatException e) {
                                    Console.showMessage("You must enter a number after the condition name: " + tmp_cond[1]);
                                    return CommandResult.BAD_SYNTAX;
                                }
                            }
                        }
                        Console.showMessage("Player conditions: " + (playerConditions.size() > 0 ? playerConditions.toString() : "empty"));
                    } catch (Exception e) {
                        Console.showMessage("Bad conditions syntax. Needs to be [condition1=amount,condition2=amount,...]." +
                                "\nUse 'list conditions' to see all available conditions.");
                        return CommandResult.BAD_SYNTAX;
                    }
                } else {
                    try {
                        playerPlanetCount = Integer.parseInt(t);
                    } catch (Exception e) {
                        Console.showMessage("Couldn't parse planet count integer or invalid argument." +
                                "\nYou typed: " + t +
                                "\nType 'help Explore' for more information.");
                        return CommandResult.BAD_SYNTAX;
                    }
                }
            }
        }

        for (LocationAPI loc : Global.getSector().getAllLocations()) {
            explore(loc, playerConditions);
        }

        if (!foundAdequateSystem) {
            Console.showMessage("Couldn't find any system matching the requirements. Try with different conditions or planet count.");
        }

        return CommandResult.SUCCESS;
    }
}
