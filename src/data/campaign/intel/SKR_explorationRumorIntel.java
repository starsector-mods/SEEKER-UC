package data.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.Set;

public class SKR_explorationRumorIntel extends BaseIntelPlugin {

    public enum RumorType {
        TITANIC("The Black Hole Cruise Tragedy", "CIV_titanic"),
        VOULGE("Ghost of the Silver Line", "SKR_voulge"),
        SIEGFRIED("The Gate-Keeper of the Outer Dark", "SKR_siegfried"),
        DAWN("The Ceramic Prototype", "SKR_dawn"),
        ONYX("The Anomaly Beneath NOVA", "SKR_onyx"),
        DEMETER("The Floating Garden of Demeter", "CIV_demeter"),
        GAWON("The Shipyard Secession Prototype", "SKR_gawon"),
        QUICKSILVER("The Apollo Skunkworks Phase Demonstrator", "SKR_quicksilver");

        private final String defaultTitle;
        private final String shipId;

        RumorType(String defaultTitle, String shipId) {
            this.defaultTitle = defaultTitle;
            this.shipId = shipId;
        }

        public String getDefaultTitle() {
            return defaultTitle;
        }

        public String getShipId() {
            return shipId;
        }
    }

    private final RumorType rumorType;
    private final SectorEntityToken targetEntity;
    private final StarSystemAPI targetSystem;
    private final String narrativeText;
    private boolean completed = false;

    public SKR_explorationRumorIntel(RumorType rumorType, SectorEntityToken targetEntity, StarSystemAPI targetSystem, String narrativeText) {
        this.rumorType = rumorType;
        this.targetEntity = targetEntity;
        this.targetSystem = targetSystem;
        this.narrativeText = narrativeText;
    }

    @Override
    protected void advanceImpl(float amount) {
        if (completed) {
            return;
        }

        // Complete when entity is salvaged, un-derelicted, or player enters location and discovers it
        if (targetEntity != null) {
            if (!targetEntity.isAlive() || targetEntity.hasTag("salvaged") || targetEntity.hasTag("recovered")) {
                completed = true;
                sendUpdateIfPlayerHasIntel(IntelInfoPlugin.ListInfoMode.INTEL, false);
                endAfterDelay();
            }
        }
    }

    @Override
    public String getName() {
        if (isEnding() || isEnded()) {
            return rumorType.getDefaultTitle() + " - Explored";
        }
        return rumorType.getDefaultTitle();
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (targetEntity != null && targetEntity.isInCurrentLocation()) {
            return targetEntity;
        }
        if (targetSystem != null) {
            return targetSystem.getCenter();
        }
        return null;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_EXPLORATION);
        tags.add(Tags.INTEL_STORY);
        return tags;
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        float pad = 10f;

        info.addPara(narrativeText, pad);

        if (targetSystem != null) {
            info.addPara("Reported Location: %s system (%s)",
                    pad,
                    highlight,
                    targetSystem.getNameWithLowercaseType(),
                    targetSystem.getStar() != null ? targetSystem.getStar().getName() : "Deep Space");
        }

        if (isEnding() || isEnded()) {
            info.addPara("You have investigated the coordinates and recovered what remained of the vessel.",
                    Misc.getPositiveHighlightColor(), pad);
        } else {
            info.addPara("The coordinates are recorded in your fleet log. Travel to the star system to investigate the anomaly.",
                    gray, pad);
        }
    }

    @Override
    public boolean hasSmallDescription() {
        return true;
    }

    @Override
    public boolean isImportant() {
        return true;
    }
}
