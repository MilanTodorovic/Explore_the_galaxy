package Exploration.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
// TODO mozda za obavestenja poput misija
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;
import java.util.HashMap;
import java.util.HashSet;

// TODO skill exploration shuttel? sruvey a system without entering it. pay 5 supplies and 5 fuel

public class ExplorationEveryFrame implements EveryFrameScript {

//    private static final Logger log = Global.getLogger(ExplorationEveryFrame.class);
    protected IntervalUtil interval = new IntervalUtil(1.0f, 3.0f);
    protected boolean notRunYet = true;
    protected boolean gameSpeedChanged = false;
    // TODO implement counters for periodic updates on certain things
    //  every 5 interval passes do X
    protected int counter = 0;
    protected SectorAPI sector;
    protected CampaignFleetAPI playerFleet;
    // TODO test
    protected Vector2f proximityInSystem = new Vector2f(200f,200f);
    protected Vector2f proximityInHyperspace = new Vector2f(200f,200f);
    // coordinates, system name
    protected HashMap<Vector2f,String> systemLocation = new HashMap<Vector2f,String>();
    // system name, goodies
    protected  HashMap<String,String> bestCharacteristicOfSystem = new HashMap<String,String>();

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {

        interval.advance(amount);

        // TODO premestiti ove vrednosti za fast forward u settings.json
//        if (Global.getSector().isFastForwardIteration() && !gameSpeedChanged){
//            Utils.showNotification("Time sped up.");
//            gameSpeedChanged = true;
//            interval = new IntervalUtil(4.5f, 9.0f);
//            interval.advance(amount);
////            interval.forceIntervalElapsed();
//        } else if (!Global.getSector().isFastForwardIteration() && gameSpeedChanged) {
//            Utils.showNotification("Time slowed down");
//            gameSpeedChanged = false;
//            interval = new IntervalUtil(1.5f, 3.0f);
//            interval.advance(amount);
////            interval.forceIntervalElapsed();
//        }


        if (!Global.getSector().isInNewGameAdvance() && !Global.getSector().getCampaignUI().isShowingMenu() &&
                !Global.getSector().getCampaignUI().isShowingDialog() && interval.intervalElapsed()) {

            // TODO ne radi kako treba
            Utils.showNotification("Is time fast? " + Global.getSector().isFastForwardIteration());

            if (notRunYet) {
                // set up everything
                sector = Global.getSector();
                // TODO does this reset after player death?
                playerFleet = sector.getPlayerFleet();
                // TODO gather all coordinates of jump points and star systems
                //  store in a persistent list
                //  check player coordinates every 2-3 sec
                //  print an appropriate message based on the coordinates
                //   show ONLY the best planet in the system, define the range at which the message triggers
                //  IF in hyperspace, trigger only on jump points, else trigger near planets
                //  unexplored ruins 'traces of civilizations',
                //  savages - decivilized
                //  nothing remarkable about this planet/system
                runAtStart();
                getAllLocationCoordinates();
                notRunYet = false;
            }

            CampaignFleetAPI playerFleet = sector.getPlayerFleet();

            if (playerFleet.isInHyperspace()){
                // TODO get only system centers, they are static
                interval.forceIntervalElapsed();
                checkPlayerProximityToSystem();
            } else {
                // TODO update regularly on planet locations
                // force update the coordinates data
                interval.forceIntervalElapsed();
                checkPlayerProximityToPlanet();
            }

        }
    }

    public void runAtStart(){
        Vector2f playerLoc = sector.getPlayerFleet().getLocation();
        sector.getCampaignUI().addMessage(
                "Current player location: %s,%s",
                Misc.getTextColor(),
                String.valueOf(playerLoc.getX()), // highlight 1
                String.valueOf(playerLoc.getY()), // highlight 2
                Misc.getHighlightColor(),
                Misc.getHighlightColor()
        );
    }

    public void checkPlayerProximityToSystem(){
        // TODO https://stackoverflow.com/questions/5174143/find-closest-point-of-every-point-nearest-neighbor
        Utils.showNotification("Player in hyperspace.");
    }

    public void checkPlayerProximityToPlanet(){
        Utils.showNotification("Player in star system.");
//        getAllPlanetDataInSystem();
        for (PlanetAPI planet : sector.getPlayerFleet().getStarSystem().getPlanets()){
            Utils.showNotification(planet.getFullName() + " - "+ planet.getTypeId());
        }
    }

    public void getAllLocationCoordinates() {
        //List<SectorEntityToken> jumpPoints = sector.getEntitiesWithTag(Tags.JUMP_POINT);
        for (StarSystemAPI starSystem : sector.getStarSystems()) {
            if (starSystem != null) {
                // ignores Core World and other non-procgen systems
                if (starSystem.getConstellation() != null) {
                    systemLocation.put(starSystem.getLocation(), starSystem.getNameWithLowercaseType());
                }
                getAllPlanetDataInSystem(starSystem);
            }
        }
    }

    public void getAllPlanetDataInSystem(StarSystemAPI starSystem){
        for (PlanetAPI planet : starSystem.getPlanets()) {
            if (!planet.isStar()) {
                HashSet<String> conditions = new HashSet<String>();
                for (MarketConditionAPI cond : planet.getMarket().getConditions()){
                    conditions.add(cond.getName());
                }
                // TODO sumirati rezultate za svaku planetu i tako napraviti procenu sistema?
                //  napraviti ocenu za najbolju planetu
//                PlanetValue value = Utils.weightPlanetValue(conditions, planet.getMarket().getHazardValue(), planet.getTypeId());
            }
        }
    }
}
