package Exploration.console;

import java.util.*;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;

import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Pair;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import Exploration.scripts.Utils;
import Exploration.scripts.UtilClasses.SolarSystem;
import Exploration.scripts.UtilClasses.Planet;
import Exploration.scripts.ExplorationModPlugin;

// TODO IMPORTANT https://fractalsoftworks.com/forum/index.php?topic=17103.msg320152#msg320152
//  * Cryosleeper in range of planet
//  * Habitable Planet
//  * Low Hazard
//  * Very rich planet
//  * Lots of planets in the system
// TODO kod Explore ubaciti Moon u header?

public class Explore implements BaseCommand {

    protected static boolean foundAdequateSystem = true;
    protected static boolean wildcardMode = false;

    // String - constellation name, SolarSystem[] - objects
    protected static HashMap<String, List<SolarSystem>> AdequateSolarSystems = new HashMap<String, List<SolarSystem>>();

    // How it looks
    //  |Constellation:|-------------------------NAME-------------------------------------------| - STRING_FORMAT_CONSTELLATION
    //  |System:-|----------------------------NAME-------------------------------|Gate:-|--YES--| - STRING_FORMAT_SYSTEM_AND_GATE
    //  |Star(s):|---RED-GIANT/BLUE-GIANT---|-Jump-points:---------|-2-|-Stable-locations:|--3--| - STRING_FORMAT_STARS + STRING_FORMAT_JUMPPOINT_AND_STABLELOC
    //  |--No.---|------PLANET-NAME---------|-----------FACTION--------|-----TYPE--------|HAZARD| - STRING_FORMAT_PLANET_INFO
    //  |----------CONDITIONS-------------------------------------------------------------------| - STRING_FORMAT_PLANET_CONDITIONS
    //  |----------CONDITIONS-------------------------------------------------------------------| - STRING_FORMAT_PLANET_CONDITIONS

    // max width between first and last vertical bar is 108
    // each number represents the max space available after deducting the vertical bars in the middle and preexisting text
    //protected static final int MAX_WIDTH = 108;
    protected static final int CONSTELLATION_CELL_SIZE = 92;

    protected static final int SYSTEM_CELL_SIZE = 85;// was 80
    protected static final int TYPE_CELL_SIZE = 11;

    protected static final int STARS_CELL_SIZE = 41; // deducted -14
    protected static final int JUMPPOINTS_CELL_SIZE = 3;
    protected static final int STABLELOC_CELL_SIZE = 6; // was 3
    protected static final int GATE_CELL_SIZE = 6; // was

    protected static final int PLANET_NUMBER_CELL_SIZE = 8; // -10-
    protected static final int PLANET_NAME_CELL_SIZE = 41;
    protected static final int PLANET_FACTION_CELL_SIZE = 20;
    protected static final int PLANET_TYPE_CELL_SIZE = 29;
    protected static final int PLANET_HAZARD_CELL_SIZE = 6; // -150%-

    protected static final int PLANET_CONDITIONS_CELL_SIZE = ExplorationModPlugin.MAX_TABLE_WIDTH - 2; // 2 are reserved for spaces

    protected static final String SEPARATOR = "--------------------------------------------------------------------------------------------------------------";
    protected static final String STRING_FORMAT_CONSTELLATION = "|Constellation: |%s|";
    protected static final String STRING_FORMAT_SYSTEM_AND_GATE = "|System: |%s|Gate: |%s|";
    protected static final String STRING_FORMAT_STARS = "|Star(s):|%s|";
    protected static final String STRING_FORMAT_JUMPPOINT_AND_STABLELOC = " Jump points:   |%s| Stable locations:           |%s|";
    protected static final String STRING_FORMAT_PLANET_INFO = "|%s|%s|%s|%s|%s|";
    protected static final String STRING_FORMAT_PLANET_CONDITIONS = "| %-" + PLANET_CONDITIONS_CELL_SIZE + "s |"; // possibly two rows, separate printing

    protected static final String STRING_FORMAT_SYSTEM_ENTRY = STRING_FORMAT_SYSTEM_AND_GATE + "\n" + SEPARATOR + "\n"
            + STRING_FORMAT_STARS + STRING_FORMAT_JUMPPOINT_AND_STABLELOC;

    protected static final String STRING_FORMAT_PLANET_ENTRY = STRING_FORMAT_PLANET_INFO + "\n" + SEPARATOR;


    protected static void explore(StarSystemAPI starSystem, HashMap<String, Integer> playerConditions)//, boolean playerSurveyFull, int playerPlanetCount, String[] playerConditions)
    {

        SolarSystem localSolarSystem = new SolarSystem();
        int nrOfStars = 0;
        String pulsar_blackhole = "";

        boolean nebula = false; // TODO filter by nebula?

//        Console.showMessage("Trying getStringTypeOfSystemAndStarNumber");
        Pair<String, Integer> result = Utils.getStringTypeOfSystemAndStarNumber(starSystem.getType());
        nrOfStars = result.two;
        int celestialBodiesInSystem = starSystem.getPlanets().size();
        int planets = celestialBodiesInSystem - nrOfStars;

//        Console.showMessage("Checking planet count");
        if (planets < ExplorationModPlugin.playerPlanetCount) {
            // basic criterion not met
            return;
        }
//        Console.showMessage("Gathering star info");
        switch (nrOfStars) {
            case 1:
                localSolarSystem.setStarTypes(starSystem.getStar().getTypeNameWithWorld());
                break;
            case 2:
                localSolarSystem.setStarTypes(starSystem.getStar().getTypeNameWithWorld());
                localSolarSystem.setStarTypes(starSystem.getSecondary().getTypeNameWithWorld());
                break;
            case 3:
                localSolarSystem.setStarTypes(starSystem.getStar().getTypeNameWithWorld());
                localSolarSystem.setStarTypes(starSystem.getSecondary().getTypeNameWithWorld());
                localSolarSystem.setStarTypes(starSystem.getTertiary().getTypeNameWithWorld());
                break;
            default:
                localSolarSystem.setStarTypes("/");
        }

//        Console.showMessage("Setting nr stars, system type and planet count");
        localSolarSystem.setNrOfStars(nrOfStars);
        localSolarSystem.setStarInfo(result.one);
        localSolarSystem.setPlanetCount(planets);

//        Console.showMessage("Setting pulsar and blackhole");
        if (starSystem.hasPulsar()) pulsar_blackhole += "P";
        if (starSystem.hasBlackHole()) pulsar_blackhole += " B";
        localSolarSystem.setPulsarBlackhole(pulsar_blackhole);

//        Console.showMessage("Checking gate tag");
        if (!starSystem.getEntitiesWithTag(Tags.GATE).isEmpty()) {
            localSolarSystem.setGate("YES"); //default is NO
        } else {
            if (ExplorationModPlugin.hasGate) {
                // hasGate is true, but we didn't find a gate
                return;
            }
        }

//        Console.showMessage("Checking constellation name");
        try {
            localSolarSystem.setConstellation(starSystem.getConstellation().getNameWithLowercaseType());
        } catch (Exception e) {
            localSolarSystem.setConstellation("Core world");
            if (ExplorationModPlugin.excludeClaimedSystems) {
                // skips further iteration if the system is in the core world
                return;
            }
        }

//        Console.showMessage("Setting system name, jump point and stable loc data");
        localSolarSystem.setSystemName(starSystem.getName());

        int _jumpPoints = starSystem.getEntities(JumpPointAPI.class).size();
        int _stableLocs = starSystem.getEntitiesWithTag("stable_location").size();

        if (_jumpPoints < ExplorationModPlugin.playerJumpPointsCount) {
            return;
        }
        localSolarSystem.setJumpPointCount(_jumpPoints);

        if (_stableLocs < ExplorationModPlugin.playerStableLocCount) {
            return;
        }
        localSolarSystem.setStableLocCount(_stableLocs);

        // TODO Distinguish moons and planets and sort accordingly
//        Console.showMessage("Iterating over planets");
        for (PlanetAPI planet : starSystem.getPlanets()) {
            if (!planet.isStar() && !planet.isNormalStar()) {
                Planet localPlanet = new Planet();

//                Console.showMessage("Setting planet name and type");
                localPlanet.setName(planet.getName());
                localPlanet.setType(planet.getTypeNameWithLowerCaseWorld());

//                Console.showMessage("Getting and setting planet market info");
                MarketAPI planetMarket = planet.getMarket();
                localPlanet.setFaction(planetMarket.getFaction().getDisplayName());
                if (!localPlanet.getFaction().equalsIgnoreCase("neutral") && ExplorationModPlugin.excludeClaimedSystems) {
                    // neutral = not claimed
                    return;
                }
//                Console.showMessage("Setting hazard level");
                int _hazard = (int) (planetMarket.getHazardValue() * 100);
                if (ExplorationModPlugin.filterByHazard) {
                    if (_hazard > ExplorationModPlugin.maxHazard) {
                        return;
                    }
                }
                localPlanet.setHazard(_hazard);

                // TODO unused
                MarketAPI.SurveyLevel planetSurveyLevel = planetMarket.getSurveyLevel();
                // set survey lvl to FULL if the player specified
                if (ExplorationModPlugin.playerSurveyAll) planetMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
                // get all planet conditions
                try {
                    // quest planet fails this check
//                    Console.showMessage("Getting and setting planetary conditions");
                    for (MarketConditionAPI condition : planetMarket.getConditions()) {
                        if (condition.isPlanetary()) {
                            // gets only conditions that apply to the planet itself, not the market (e.g. pirate raid, goods shortages, etc.)
                            if (ExplorationModPlugin.playerSurveyAll) {
                                // if true, shows all conditions, whether surveyed or not
                                // and mark each condition as surveyed
                                condition.setSurveyed(true);
                                localPlanet.addCondition(condition.getId());// ore_sparse
                            } else {
                                if (condition.isSurveyed()) {
                                    localPlanet.addCondition(condition.getId());// ore_sparse
                                }
                            }
                        }
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    localPlanet.addCondition("no_conditions");
                }
                localSolarSystem.setPlanets(localPlanet);
            }
        }

//        Console.showMessage("Creating all conditions within localSolarSystem");
        localSolarSystem.fillAllPlanetConditions();
//        Console.showMessage(localSolarSystem.allPalnetConditions);

//        Console.showMessage("Checking player condition criteria");
        for (Map.Entry<String, Integer> condition : playerConditions.entrySet()) {
//            Console.showMessage("Player condition - " + condition.toString());
            String key = condition.getKey();
            int value = condition.getValue();
            if (localSolarSystem.allPalnetConditions.containsKey(key)) {
//                Console.showMessage("planetary condition - " + localSolarSystem.allPalnetConditions.get(condition.getKey()));
                if (localSolarSystem.allPalnetConditions.get(key) < value) {
                    // if the player has specified a larger number of conditions than there are in the system, break the loop
                    foundAdequateSystem = false;
                    break;
                }
            } else if (key.startsWith("*")) {
                int counter = value; // how many times a similar condition needs to appear
                // go through every condition of the planet and check whether they match
                for (Map.Entry<String,Integer> planetCondition : localSolarSystem.allPalnetConditions.entrySet()){
                    if (planetCondition.getKey().endsWith(key.substring(1))){
                        // decrement to signify a similarity has been found
                        counter--;
                    }
                }
                if (counter > 0){
                    // if the counter doesn't reach zero, not enough conditions matched the wildcard
                    foundAdequateSystem = false;
                    break;
                }
            } else if (key.endsWith("*")) {
                // wildcard used> ore_*=3 (all types of ore)
                int counter = value; // how many times a similar condition needs to appear
                // go through every condition of the planet and check whether they match
                for (Map.Entry<String,Integer> planetCondition : localSolarSystem.allPalnetConditions.entrySet()){
                    if (planetCondition.getKey().startsWith(key.substring(0,key.length()-1))){
                        // decrement to signify a similarity has been found
                        counter--;
                    }
                }
                if (counter > 0){
                    // if the counter doesn't reach zero, not enough conditions matched the wildcard
                    foundAdequateSystem = false;
                    break;
                }
            } else {
                foundAdequateSystem = false;
                break;
            }
        }

        if (foundAdequateSystem) {
//            Console.showMessage("Found adequate system");
            if (AdequateSolarSystems.get(localSolarSystem.getConstellation()) == null) {
                AdequateSolarSystems.put(localSolarSystem.getConstellation(), new ArrayList<SolarSystem>() {
                });
            }
            AdequateSolarSystems.get(localSolarSystem.getConstellation()).add(localSolarSystem);
        }
        // reset value, I always forget this...
        foundAdequateSystem = true;
    }


    public void sortAndPrintOutput(HashMap<String, List<SolarSystem>> map) {
        Map<String, List<SolarSystem>> sortedMapByConstellation = new TreeMap<String, List<SolarSystem>>(map);
        Map<String, SolarSystem> sortedSubMapByStarSystem = new TreeMap<String, SolarSystem>();
        Map<String, Map<String, SolarSystem>> completeSort = new TreeMap<String, Map<String, SolarSystem>>();

        // sort by star system names
        for (Map.Entry<String, List<SolarSystem>> _entry : sortedMapByConstellation.entrySet()) {
            for (SolarSystem _ss : _entry.getValue()) {
                sortedSubMapByStarSystem.put(_ss.getSystemName(), _ss);
            }
            completeSort.put(_entry.getKey(), new TreeMap<String, SolarSystem>(sortedSubMapByStarSystem));
            sortedSubMapByStarSystem.clear();
        }

        for (Map.Entry<String, Map<String, SolarSystem>> _entry : completeSort.entrySet()) {
            // Constellation header
            Console.showMessage("\n"); // add a gap between constellations
            Console.showMessage(SEPARATOR);
            Console.showMessage(String.format(STRING_FORMAT_CONSTELLATION, Utils.centerText(_entry.getKey(), CONSTELLATION_CELL_SIZE)));

            for (Map.Entry<String, SolarSystem> entry : _entry.getValue().entrySet()) {
                SolarSystem _system = entry.getValue();
                // Star system header
                Console.showMessage(SEPARATOR);
                Console.showMessage(SEPARATOR);
                Console.showMessage(
                        String.format(STRING_FORMAT_SYSTEM_ENTRY,
                                Utils.centerText(_system.getSystemName(), SYSTEM_CELL_SIZE),
                                //Utils.centerText(_system.getStarInfo(), TYPE_CELL_SIZE),
                                Utils.centerText(_system.getGate(), GATE_CELL_SIZE),
                                Utils.centerText(_system.getStarTypes().substring(1), STARS_CELL_SIZE), // remove leading backslash
                                Utils.centerText(String.valueOf(_system.getJumpPointCount()), JUMPPOINTS_CELL_SIZE),
                                Utils.centerText(String.valueOf(_system.getStableLocCount()), STABLELOC_CELL_SIZE)
                        )
                );

                int counterForPlanetNumnbers = 1;

                for (Planet p : _system.getPlanets()) {
                    // Individual planets
                    Console.showMessage(SEPARATOR);
                    Console.showMessage(
                            String.format(STRING_FORMAT_PLANET_ENTRY,
                                    Utils.centerText(String.valueOf(counterForPlanetNumnbers), PLANET_NUMBER_CELL_SIZE),
                                    Utils.centerText(p.getName(), PLANET_NAME_CELL_SIZE),
                                    Utils.centerText(p.getFaction(), PLANET_FACTION_CELL_SIZE),
                                    Utils.centerText(p.getType(), PLANET_TYPE_CELL_SIZE),
                                    Utils.centerText(p.getHazardAsString(), PLANET_HAZARD_CELL_SIZE)
                            )
                    );
                    counterForPlanetNumnbers++;

                    String[] _rows = splitIntoRows(p.getConditionsAsString());
                    if (!_rows[1].isEmpty()) {
                        Console.showMessage(_rows[0]);
                        Console.showMessage(_rows[1]);
                    } else {
                        Console.showMessage(_rows[0]);
                    }
                }
            }
            Console.showMessage(SEPARATOR); // closing separator between tables
        }
    }


    public static String[] splitIntoRows(String conditions) {

        // If text won't fit withing the cell size, make a second row
        String firstRowConditions = "";
        String secondRowConditions = "";
        String firstRow = "";
        String secondRow = "";

        if (conditions.length() > PLANET_CONDITIONS_CELL_SIZE) {
            // TODO check of the two last characters before the split are a white space, so that we dont leave a signel character in the first row
//            if (_constellation.substring(CONSTELLATION_CELL_SIZE - 1, CONSTELLATION_CELL_SIZE).equalsIgnoreCase(" ") ||
//                    _constellation.substring(CONSTELLATION_CELL_SIZE - 2, CONSTELLATION_CELL_SIZE -1).equalsIgnoreCase(" ")){
//                addDash = "-";
//            }
            String addDash = conditions.substring(PLANET_CONDITIONS_CELL_SIZE - 1, PLANET_CONDITIONS_CELL_SIZE).equalsIgnoreCase(" ") ? "" : "-";
            // index from 1 to length()-1 because I want to cut off [] when converting from array to string
            firstRowConditions = conditions.substring(1, PLANET_CONDITIONS_CELL_SIZE - 1) + addDash;
            secondRowConditions = conditions.substring(PLANET_CONDITIONS_CELL_SIZE - 1, conditions.length() - 1);
        } else {
            firstRowConditions = conditions.substring(1, conditions.length() - 1);
        }

        if (!secondRowConditions.isEmpty()) {
            firstRow = String.format(STRING_FORMAT_PLANET_CONDITIONS, firstRowConditions);
            secondRow = String.format(STRING_FORMAT_PLANET_CONDITIONS, secondRowConditions);
        } else {
            firstRow = String.format(STRING_FORMAT_PLANET_CONDITIONS, firstRowConditions);
        }
        return new String[]{firstRow, secondRow};
    }


    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        // Known error if you want to modify the code yourself:
        //  1) Program doesn't work if you leave <> in HashMap, or any other complex type, empty; although it shouldn't be required since Java 7

        HashMap<String, Integer> playerConditions = new HashMap<String, Integer>(); // planetary conditions

        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

//        Console.showMessage("Max tabel width: " + ExplorationModPlugin.MAX_TABLE_WIDTH);
//        Console.showMessage("excludeClaimedSystems: " + ExplorationModPlugin.excludeClaimedSystems);
//        Console.showMessage("playerSurveyAll: " + ExplorationModPlugin.playerSurveyAll);
//        Console.showMessage("playerPlanetCount: " + ExplorationModPlugin.playerPlanetCount);
//        Console.showMessage("hasGate: " + ExplorationModPlugin.hasGate);
//        Console.showMessage("playerStableLocCount: " + ExplorationModPlugin.playerStableLocCount);
//        Console.showMessage("playerJumpPointsCount: " + ExplorationModPlugin.playerJumpPointsCount);
//        Console.showMessage("filterByHazard: " + ExplorationModPlugin.filterByHazard);
//        Console.showMessage("maxHazard: " + ExplorationModPlugin.maxHazard);

        if (!args.isEmpty()) {
            final String[] tmp = args.toLowerCase().split(" ");
            // Console.showMessage("Arguments: " + Arrays.toString(tmp));
            for (String t : tmp) {
                if (t.equalsIgnoreCase("all")) {
                    ExplorationModPlugin.playerSurveyAll = true;
                } else if (t.equalsIgnoreCase("gate")) {
                    ExplorationModPlugin.hasGate = true;
                } else if (t.equalsIgnoreCase("exclude")) {
                    ExplorationModPlugin.excludeClaimedSystems = true;
                } else if (t.startsWith("stableloc")) {
                    String[] _str = t.split("=");
                    try {
                        ExplorationModPlugin.playerStableLocCount = Integer.parseInt(_str[1]);
                    } catch (Exception e) {
                        Console.showMessage("You must enter a number after stableloc: " + _str[1]);
                        return CommandResult.BAD_SYNTAX;
                    }
                } else if (t.startsWith("jump")) {
                    String[] _str = t.split("=");
                    try {
                        ExplorationModPlugin.playerJumpPointsCount = Integer.parseInt(_str[1]);
                    } catch (Exception e) {
                        Console.showMessage("You must enter a number after jumppoint: " + _str[1]);
                        return CommandResult.BAD_SYNTAX;
                    }
                } else if (t.startsWith("hazard")) {
                    String[] _str = t.split("=");
                    try {
                        ExplorationModPlugin.maxHazard = Integer.parseInt(_str[1]);
                        ExplorationModPlugin.filterByHazard = true;
                    } catch (Exception e) {
                        Console.showMessage("You must enter a number after maxhazard: " + _str[1]);
                        return CommandResult.BAD_SYNTAX;
                    }
                } else if (t.startsWith("planets")) {
                    String[] _str = t.split("=");
                    try {
                        ExplorationModPlugin.playerPlanetCount = Integer.parseInt(_str[1]);
                    } catch (Exception e) {
                        Console.showMessage("You must enter a number after planets: " + _str[1]);
                        return CommandResult.BAD_SYNTAX;
                    }
                } else if (t.equalsIgnoreCase("include")) {
                    ExplorationModPlugin.excludeClaimedSystems = false;
                } else if (t.charAt(0) == '[') {
                    try {
                        String[] tmp_t = t.substring(1, t.length() - 1).split(","); // ["habitable=2",...]
                        // passing in [] results in a single empty string
                        if (tmp_t.length != 0 && !tmp_t[0].isEmpty()) {
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
//                        Console.showMessage("Player conditions: " + (playerConditions.size() > 0 ? playerConditions.toString() : "empty"));
                    } catch (Exception e) {
                        Console.showMessage("Bad conditions syntax. Needs to be [condition1=amount,condition2=amount,...]." +
                                "\nUse 'list conditions' to see all available conditions.");
                        return CommandResult.BAD_SYNTAX;
                    }
                } else {
                    Console.showMessage("Explore - Invalid argument." +
                            "\nYou typed: " + t +
                            "\nType 'help Explore' for more information.");
                    return CommandResult.BAD_SYNTAX;
                }
            }
        }
        for (
                StarSystemAPI starSystem : Global.getSector().

                getStarSystems()) {
            explore(starSystem, playerConditions);
        }

        if (AdequateSolarSystems.isEmpty()) {
            String defaultArguments = "all="+ExplorationModPlugin.playerSurveyAll
                    +", include="+ExplorationModPlugin.excludeClaimedSystems
                    + ", planets="+ExplorationModPlugin.playerPlanetCount
                    +", gate="+ExplorationModPlugin.hasGate
                    +", stableloc="+ExplorationModPlugin.playerStableLocCount
                    +", jumppoint="+ExplorationModPlugin.playerJumpPointsCount+",";
            String defaultArguments2 =
                    "filterbyhazard="+ExplorationModPlugin.filterByHazard
                    +", hazard="+ExplorationModPlugin.maxHazard;
            Console.showMessage(SEPARATOR);
            Console.showMessage(String.format("| Default arguments: %-"+(ExplorationModPlugin.MAX_TABLE_WIDTH-21)+"s |", defaultArguments));
            Console.showMessage(String.format("| %-"+(ExplorationModPlugin.MAX_TABLE_WIDTH-2)+"s |", defaultArguments2));
            Console.showMessage(String.format("| Player defined arguments: %-"+(ExplorationModPlugin.MAX_TABLE_WIDTH-28)+"s |", args));
            Console.showMessage(String.format("|%" + ExplorationModPlugin.MAX_TABLE_WIDTH + "s|",
                    Utils.centerText("Couldn't find any system matching the requirements. Try with different conditions or planet count.",
                            ExplorationModPlugin.MAX_TABLE_WIDTH)));
            Console.showMessage(SEPARATOR);
        } else {
            sortAndPrintOutput(AdequateSolarSystems);
        }
        // TODO make a local copy?
        Console.showMessage(String.format("|%" + ExplorationModPlugin.MAX_TABLE_WIDTH + "s|", Utils.centerText(" ", ExplorationModPlugin.MAX_TABLE_WIDTH)));
        Console.showMessage(String.format("|%" + ExplorationModPlugin.MAX_TABLE_WIDTH + "s|", Utils.centerText("END OF QUERY", ExplorationModPlugin.MAX_TABLE_WIDTH)));
        Console.showMessage(String.format("|%" + ExplorationModPlugin.MAX_TABLE_WIDTH + "s|", Utils.centerText(" ", ExplorationModPlugin.MAX_TABLE_WIDTH)));
        Console.showMessage(SEPARATOR + "\n");

        // reset value with each command
        // TODO make a local copy?
        ExplorationModPlugin.playerSurveyAll = false;
        ExplorationModPlugin.playerPlanetCount = 1;
        ExplorationModPlugin.excludeClaimedSystems = false;
        ExplorationModPlugin.hasGate = false;
        ExplorationModPlugin.playerStableLocCount = 1;
        ExplorationModPlugin.playerJumpPointsCount = 1;
        ExplorationModPlugin.filterByHazard = false;
        ExplorationModPlugin.maxHazard = 250;

        foundAdequateSystem = true;
        AdequateSolarSystems.clear();

        return CommandResult.SUCCESS;
    }
}