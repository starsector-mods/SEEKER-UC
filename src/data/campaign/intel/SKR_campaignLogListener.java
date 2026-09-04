package data.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.plog.PLTextEntry;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import data.campaign.ids.SKR_ids;
import java.util.HashSet;
import java.util.Set;

public class SKR_campaignLogListener extends BaseCampaignEventListener {

    public SKR_campaignLogListener() {
        super(false);
    }

    @Override
    public void reportPlayerOpenedMarket(com.fs.starfarer.api.campaign.econ.MarketAPI market) {
        checkFleetChronicle();
    }

    @Override
    public void reportPlayerClosedMarket(com.fs.starfarer.api.campaign.econ.MarketAPI market) {
        checkFleetChronicle();
    }

    @Override
    public void reportPlayerMarketTransaction(com.fs.starfarer.api.campaign.PlayerMarketTransaction transaction) {
        checkFleetChronicle();
    }

    @Override
    public void reportBattleFinished(CampaignFleetAPI primaryWinner, com.fs.starfarer.api.campaign.BattleAPI battle) {
        checkFleetChronicle();
    }

    @Override
    public void reportPlayerEngagement(com.fs.starfarer.api.combat.EngagementResultAPI result) {
        checkFleetChronicle();
    }

    @Override
    public void reportPlayerActivatedAbility(com.fs.starfarer.api.characters.AbilityPlugin ability, Object param) {
        checkFleetChronicle();
    }

    public static void checkFleetChronicle() {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet == null) return;

        MemoryAPI mem = Global.getSector().getMemoryWithoutUpdate();
        Set<String> shipsChecked = new HashSet<>();

        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.isFighterWing()) continue;
            String hullId = member.getHullId();
            String baseHullId = member.getHullSpec().getBaseHullId();
            if (baseHullId == null || baseHullId.isEmpty()) baseHullId = hullId;

            if (shipsChecked.contains(baseHullId)) continue;
            shipsChecked.add(baseHullId);

            String memKey = "$SKR_logged_recovery_" + baseHullId;
            if (!mem.is(memKey, true)) {
                String shipName = member.getShipName();
                String hullName = member.getHullSpec().getHullName();

                if ("SKR_onyx".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.ONYX);
                    logStory("Commissioned the ancient prototype battlecruiser [" + shipName + "] (" + hullName + "-class) into the fleet.");
                } else if ("CIV_titanic".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.TITANIC);
                    logStory("Recovered the legendary luxury cruise-liner [" + shipName + "] (" + hullName + ") from deep space.");
                } else if ("SKR_siegfried".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.SIEGFRIED);
                    logStory("Restored the ancient Domain Gate-Keeper dreadnought [" + shipName + "] to full operational status.");
                } else if ("SKR_voulge".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.VOULGE);
                    logStory("Restored the Silver Line battlecruiser [" + shipName + "] (" + hullName + "-class).");
                } else if ("SKR_dawn".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.DAWN);
                    logStory("Integrated the advanced ceramic technology demonstrator [" + shipName + "] (" + hullName + "-class) into the battle line.");
                } else if ("CIV_demeter".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.DEMETER);
                    logStory("Acquired the mobile agricultural fortress [" + shipName + "] (Demeter-class) to support fleet logistics.");
                } else if ("SKR_gawon".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.GAWON);
                    logStory("Recovered the AI-engineered battlecruiser [" + shipName + "] (Gawon-class).");
                } else if ("SKR_quicksilver".equals(baseHullId)) {
                    mem.set(memKey, true);
                    SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.QUICKSILVER);
                    logStory("Brought the Apollo Skunkworks capital demonstrator [" + shipName + "] (Quicksilver-class) into service.");
                }
            }
        }

        // Check if all 4 Plague abilities are unlocked
        if (!mem.is("$SKR_all_plague_abilities_logged", true)) {
            Set<String> abilities = Global.getSector().getCharacterData().getAbilities();
            if (abilities.contains(SKR_ids.ABILITY_REMOTE_SURVEY)
                    && abilities.contains(SKR_ids.ABILITY_NEUTRINO_DETECTOR)
                    && abilities.contains(SKR_ids.ABILITY_EMERGENCY_BURN)
                    && abilities.contains(SKR_ids.ABILITY_SUSTAINED_BURN)) {
                mem.set("$SKR_all_plague_abilities_logged", true);
                logStory("CHRONICLER RECORD: All four ancient Plague-Bearer sub-cores have been neutralized and assimilated into the fleet's permanent technological doctrine.");
            }
        }
    }

    private static void logStory(String text) {
        try {
            PlaythroughLog.getInstance().addEntry(new PLTextEntry(text, true));
        } catch (Throwable t) {
            // Graceful fallback
        }
    }
}
