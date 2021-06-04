package Exploration.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import Exploration.campaign.listeners.LocationChangeListener;
import Exploration.campaign.listeners.EntityDiscoveryListener;
import Exploration.campaign.listeners.PlanetaryConditionsListener;
import Exploration.campaign.listeners.PlanetDiscoveryListener;
import org.json.JSONObject;

// TODO Campaign chatter mod! kako krstaris svemirom tako dabijas hintove od jednog kapetana, moze da navede na pogresna trag
//  "As you approach a system with a Blue Giant, your captain seems to notice some peculiarities about it.
//  The instruments suggest a large amount of celestial bodies orbiting the blue behemoth."

@SuppressWarnings("unused")
public class ExplorationModPlugin extends BaseModPlugin {

    private static boolean settingsAlreadyRead = false;
    public static final String ID = "exploration";
    public static final String SETTINGS_PATH = "data/config/settings.json";

    public static boolean enableChatterCompanion; // unused
    public static int MAX_TABLE_WIDTH;
    // Explore
    public static boolean excludeClaimedSystems;
    public static boolean playerSurveyAll;
    public static int playerPlanetCount;
    public static boolean hasGate;
    public static int playerStableLocCount;
    public static int playerJumpPointsCount;
    public static boolean filterByHazard;
    public static int maxHazard;
    // SystemInRange
    public static double playerLightYearDistance;
    public static int playerPlanetCountForSystemInRange;

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            if (!settingsAlreadyRead) {
//                JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);
                JSONObject cfg = Global.getSettings().loadJSON(SETTINGS_PATH, ID);

                enableChatterCompanion = cfg.getBoolean("enableChatterCompanion");

                excludeClaimedSystems = cfg.getBoolean("excludeClaimedSystems");
                playerSurveyAll = cfg.getBoolean("playerSurveyAll");
                playerPlanetCount = cfg.getInt("playerPlanetCount");
                hasGate = cfg.getBoolean("hasGate");
                playerStableLocCount = cfg.getInt("playerStableLocCount");
                playerJumpPointsCount = cfg.getInt("playerJumpPointsCount");
                filterByHazard = cfg.getBoolean("filterByHazard");
                maxHazard = cfg.getInt("maxHazard");

                playerLightYearDistance = (double) cfg.getInt("playerLightYearDistance");
                playerPlanetCountForSystemInRange = cfg.getInt("playerPlanetCountForSystemInRange");

                MAX_TABLE_WIDTH = cfg.getInt("MAX_TABLE_WIDTH");

                settingsAlreadyRead = true;
            }
        } catch (Exception e) {
            String stackTrace = "";
            for(int i = 0; i < e.getStackTrace().length; i++) {
                StackTraceElement ste = e.getStackTrace()[i];
                stackTrace += "    " + ste.toString() + System.lineSeparator();
            }
            Global.getLogger(ExplorationModPlugin.class).error(e.getMessage() + System.lineSeparator() + stackTrace);
        }

        Global.getSector().addTransientScript(new ExplorationEveryFrame());

//        Global.getSector().getListenerManager().addListener(new PlanetaryConditionsListener(), true);
//        Global.getSector().getListenerManager().addListener(new EntityDiscoveryListener(), true);
//        Global.getSector().getListenerManager().addListener(new LocationChangeListener(), true);
//        Global.getSector().getListenerManager().addListener(new PlanetDiscoveryListener(), true);
    }
}
