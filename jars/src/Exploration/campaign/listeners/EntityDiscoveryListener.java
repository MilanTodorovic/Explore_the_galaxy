package Exploration.campaign.listeners;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import org.apache.log4j.Logger;

import Exploration.scripts.Utils;

public class EntityDiscoveryListener implements DiscoverEntityListener {
    private static final Logger log = Global.getLogger(EntityDiscoveryListener.class);

    @Override
    public void reportEntityDiscovered(SectorEntityToken entity) {
        // TODO for cryosleeper and coronal_tap
        //  maybe auto display best (discovered?) system in range of those
        if (entity.hasTag(Tags.CRYOSLEEPER) || entity.hasTag(Tags.CORONAL_TAP)){
            Utils.showNotification(entity.getFullName());
        } else {
            Utils.showNotification("Discovered: " + entity.getFullName());
        }
    }

}
