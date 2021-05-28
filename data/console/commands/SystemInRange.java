package data.console.commands;


import java.text.DecimalFormat;
import java.util.*;
import java.lang.Math;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;

import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

import org.lwjgl.util.vector.Vector2f;

//import data.console.auxiliary_classes.CustomMessageIntel2;

// TODO IMPORTANT https://fractalsoftworks.com/forum/index.php?topic=17103.msg320152#msg320152

public class SystemInRange implements BaseCommand {

    static String[] searchableObjects = {Tags.CRYOSLEEPER, Tags.CORONAL_TAP, Tags.GATE};
    static String objectToSearch = "";
    static int playerLightYearDistance = 10;
    static HashMap<String, Vector2f> systems = new HashMap<String, Vector2f>();
    static HashMap<String, Vector2f>  objectCoordinates = new HashMap<String, Vector2f>();
    // static HashMap<String, Vector2f> systemsInPlayerDefinedRange = new HashMap<String, Vector2f>();
    private static DecimalFormat df = new DecimalFormat("#.##");

    public void findObject(LocationAPI loc) {
        if (!loc.isHyperspace() && loc != null) {
            String constellation = "";
            List<SectorEntityToken> objectEntity = loc.getEntitiesWithTag(objectToSearch);
            if (!objectEntity.isEmpty()) {
                try{
                    constellation = loc.getConstellation().getName();
                } catch (Exception e) {
                    constellation = "Core World";
                }

                objectCoordinates.put(constellation + " - " + loc.getNameWithLowercaseType(), loc.getLocation());
                Console.showMessage("Found "+objectToSearch+" in " + constellation + " - "+ loc.getNameWithLowercaseType() + " at " + loc.getLocation());
            } else {
                try{
                    try{
                        constellation = loc.getConstellation().getName();
                    } catch (Exception e) {
                        constellation = "Core World";
                    }
                    systems.put(constellation + " - " + loc.getNameWithLowercaseType(), loc.getLocation());
                } catch (Exception e){
                    Console.showMessage("Location that doesn't have coordinates: " + loc);
                }
            }
        }
    }

    void findCloseSystems() {
        for (Map.Entry<String, Vector2f> objEntity : objectCoordinates.entrySet()) {
            Console.showMessage("\n"+objEntity.getKey() + ":");
            for (Map.Entry<String, Vector2f> candidateSystem : systems.entrySet()) {
                double result = checkDistance((Vector2f) objEntity.getValue(), (Vector2f) candidateSystem.getValue());
                if (result <= playerLightYearDistance) {
                    // TODO print to console in order (nearest-furthest)
                    // systemsInPlayerDefinedRange.put(candidateSystem.getKey(), candidateSystem.getValue());
                    Console.showMessage("\t- "+candidateSystem.getKey() + " ("+df.format(result)+"LY)");

                }
            }
        }
    }

    double checkDistance(Vector2f coronalCoord, Vector2f systemCoord) {
        double ac = Math.abs(coronalCoord.y - systemCoord.y);
        double cb = Math.abs(coronalCoord.x - systemCoord.x);
        return Math.hypot(ac, cb) / 2000;
    }

    @Override
    public CommandResult runCommand(String args, CommandContext context) {

        if (!context.isInCampaign()) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (args.isEmpty()) {
            Console.showMessage("Arguments required for testing purposes.");
            return CommandResult.BAD_SYNTAX;
        } else {
            final String[] tmp = args.toLowerCase().split(" ");

            if (tmp.length != 2) {
                Console.showMessage("You must pass exactly TWO arguments: name of the object and the distance in LY.");
                return CommandResult.BAD_SYNTAX;
            }

            if (Arrays.asList(searchableObjects).contains(tmp[0])){
                objectToSearch = tmp[0];
            } else {
                Console.showMessage("Can't search for entity: " + tmp[0] + ". Try one of the following: " + Arrays.toString(searchableObjects));
                return CommandResult.BAD_SYNTAX;
            }

            try {
                playerLightYearDistance = Integer.parseInt(tmp[1]);
            } catch (NumberFormatException e) {
                Console.showMessage("You must type in a integer. You typed " + tmp[1]);
                return CommandResult.BAD_SYNTAX;
            }
            Console.showMessage("\n---------------------------------------------------------------------------------------");
            for (LocationAPI loc : Global.getSector().getAllLocations()) {
                // finds entities and also takes no on all the coordinates of each star system
                findObject(loc);
            }
            findCloseSystems();

            return CommandResult.SUCCESS;
        }
    }
}
