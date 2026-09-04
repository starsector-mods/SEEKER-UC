package data.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.plog.PLTextEntry;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Captain's Log & Decrypted Codex tracking the Sector's 5 legendary derelict prototypes:
 * Titanic VII, Onyx, Siegfried, Voulge, and Dawn.
 * Displays decrypted black box flight transcripts, recovery status, and system coordinates.
 * 
 * @author Tartiflette, modified for Seeker UC
 */
public class SKR_derelictCodexIntel extends BaseIntelPlugin {

    public static final String TITANIC = "TITANIC";
    public static final String ONYX = "ONYX";
    public static final String SIEGFRIED = "SIEGFRIED";
    public static final String VOULGE = "VOULGE";
    public static final String DAWN = "DAWN";

    public static final String[] ALL_DERELICTS = {
        TITANIC,
        ONYX,
        SIEGFRIED,
        VOULGE,
        DAWN
    };

    /**
     * Map storing unlocked derelict keys to their respective unlock timestamp.
     */
    protected Map<String, Long> unlockedEntries = new LinkedHashMap<>();

    public SKR_derelictCodexIntel() {
    }

    /**
     * Retrieves or creates the persistent singleton instance in the Sector Intel Manager.
     */
    public static SKR_derelictCodexIntel get() {
        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        if (intelManager.hasIntelOfClass(SKR_derelictCodexIntel.class)) {
            return (SKR_derelictCodexIntel) intelManager.getFirstIntel(SKR_derelictCodexIntel.class);
        }
        SKR_derelictCodexIntel intel = new SKR_derelictCodexIntel();
        intelManager.addIntel(intel, false);
        return intel;
    }

    /**
     * Normalizes arbitrary entry strings to standard Derelict keys.
     */
    public static String normalizeKey(String entryKey) {
        if (entryKey == null) return null;
        String clean = entryKey.trim().toUpperCase();
        if (clean.contains("TITANIC") || clean.contains("CIV_TITANIC")) {
            return TITANIC;
        }
        if (clean.contains("ONYX") || clean.contains("SKR_ONYX")) {
            return ONYX;
        }
        if (clean.contains("SIEGFRIED") || clean.contains("SKR_SIEGFRIED")) {
            return SIEGFRIED;
        }
        if (clean.contains("VOULGE") || clean.contains("SKR_VOULGE")) {
            return VOULGE;
        }
        if (clean.contains("DAWN") || clean.contains("SKR_DAWN")) {
            return DAWN;
        }
        return null;
    }

    public boolean isUnlocked(String entryKey) {
        String key = normalizeKey(entryKey);
        return key != null && unlockedEntries.containsKey(key);
    }

    public Long getUnlockTimestamp(String entryKey) {
        String key = normalizeKey(entryKey);
        return key != null ? unlockedEntries.get(key) : null;
    }

    public int getUnlockedCount() {
        return unlockedEntries.size();
    }

    /**
     * Unlocks a sub-entry, logs the discovery in the PlaythroughLog,
     * and broadcasts a notification update.
     */
    public void unlockEntry(String entryKey) {
        String key = normalizeKey(entryKey);
        if (key == null) return;

        if (unlockedEntries.containsKey(key)) {
            return;
        }

        long timestamp = Global.getSector().getClock().getTimestamp();
        unlockedEntries.put(key, timestamp);

        String derelictName = getDerelictName(key);

        // Add entry to PlaythroughLog
        try {
            PlaythroughLog.getInstance().addEntry(
                    new PLTextEntry("Captain's Log: Recovered Black Box & Decrypted Codex for [" + derelictName + "].", true)
            );
        } catch (Throwable t) {
            // Graceful fallback
        }

        // Send intel update
        sendUpdateIfPlayerHasIntel(key, false);
    }

    public static String getDerelictName(String key) {
        if (TITANIC.equals(key)) return "BHS Titanic VII";
        if (ONYX.equals(key)) return "Onyx-class Battlecruiser";
        if (SIEGFRIED.equals(key)) return "Siegfried Sentry Dreadnought";
        if (VOULGE.equals(key)) return "III Causality is a Myth (Voulge)";
        if (DAWN.equals(key)) return "Dawn Ceramic Prototype";
        return "Unknown Derelict";
    }

    public static String getBaseHullId(String key) {
        if (TITANIC.equals(key)) return "CIV_titanic";
        if (ONYX.equals(key)) return "SKR_onyx";
        if (SIEGFRIED.equals(key)) return "SKR_siegfried";
        if (VOULGE.equals(key)) return "SKR_voulge";
        if (DAWN.equals(key)) return "SKR_dawn";
        return "";
    }

    @Override
    public String getName() {
        return "Codex: Legendary Derelicts";
    }

    @Override
    public String getSmallDescriptionTitle() {
        return "CAPTAIN'S LOG // LEGENDARY DERELICT CODEX";
    }

    @Override
    public String getIcon() {
        return Global.getSettings().getSpriteName("intel", "fleet_log");
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_STORY);
        tags.add(Tags.INTEL_EXPLORATION);
        return tags;
    }

    @Override
    public boolean hasSmallDescription() {
        return true;
    }

    @Override
    public boolean isImportant() {
        return true;
    }

    @Override
    public boolean shouldRemoveIntel() {
        return false;
    }

    @Override
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;

        bullet(info);

        int count = getUnlockedCount();
        info.addPara("Decrypted Derelict Transcripts: %s / %s", pad, tc, highlight, "" + count, "" + ALL_DERELICTS.length);

        if (getListInfoParam() instanceof String) {
            String paramKey = normalizeKey((String) getListInfoParam());
            if (paramKey != null) {
                info.addPara("Updated Entry: %s", pad, tc, Misc.getPositiveHighlightColor(), getDerelictName(paramKey));
            }
        } else if (count == ALL_DERELICTS.length) {
            info.addPara("All legendary derelict black boxes catalogued", Misc.getPositiveHighlightColor(), pad);
        } else {
            info.addPara("Unresolved navigational beacons recorded across fringe sectors", gray, pad);
        }

        unindent(info);
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color red = Misc.getNegativeHighlightColor();
        Color green = Misc.getPositiveHighlightColor();
        Color tc = Misc.getTextColor();
        float pad = 10f;
        float smallPad = 4f;

        info.addPara(
                "A comprehensive navigational codex and flight recorder database chronicling the %s "
                + "scattered throughout the Persean Sector.",
                pad, highlight, "five legendary prototype derelicts"
        );

        info.addPara(
                "Recovering black boxes and querying ancient telemetry networks reveals the final hours, design origins, "
                + "and lost technologies of these unique capital-grade hulls.",
                smallPad, gray
        );

        int count = getUnlockedCount();
        info.addSectionHeading("DECRYPTED FLIGHT RECORDERS & ARCHIVES (" + count + " / " + ALL_DERELICTS.length + ")", Alignment.MID, pad);

        for (String key : ALL_DERELICTS) {
            boolean unlocked = isUnlocked(key);
            Long unlockTime = getUnlockTimestamp(key);

            if (unlocked) {
                renderUnlockedDerelict(info, key, unlockTime, width);
            } else {
                renderEncryptedPlaceholder(info, key, width);
            }
        }
    }

    private void renderUnlockedDerelict(TooltipMakerAPI info, String key, Long unlockTime, float width) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color red = Misc.getNegativeHighlightColor();
        Color green = Misc.getPositiveHighlightColor();
        Color story = Misc.getStoryOptionColor();
        Color tc = Misc.getTextColor();
        float pad = 10f;
        float smallPad = 4f;

        String dateStr = unlockTime != null ? Global.getSector().getClock().createClock(unlockTime).getDateString() : "Historical Archive";
        String statusInfo = getRecoveryAndLocationStatus(key);

        switch (key) {
            case TITANIC: {
                info.addSectionHeading("[LOG 01] BHS TITANIC VII — VOYAGE RECORDER", Alignment.LMID, pad);
                info.addPara("Archived: %s | %s", smallPad, gray, highlight, dateStr, statusInfo);

                info.addPara(
                        "Cycle 182.04 — Maiden Voyage Departure:\n"
                        + "\"All 2,500 tickets sold out within hours. Passengers toasted champagne in the Grand Atrium while our navigation officers aligned the primary burn for the inner accretion limit. The CEO personally addressed the promenade, reassuring guests that our armored Onslaught-derived frame is impervious to any gravitational anomalies.\"",
                        smallPad, gray
                );

                info.addPara(
                        "Cycle 182.11 — Orbital Observation Point Alpha:\n"
                        + "\"Sensors show extraordinary blueshifting along the accretion event horizon. Guests gathered along the starboard viewing galleries to observe stellar matter plunging into the singularity. Ambient radiation elevated but well within hull shielding thresholds.\"",
                        smallPad, gray
                );

                info.addPara(
                        "Cycle 182.12 — GRAVITATIONAL SHEAR OVERLOAD DETECTED:\n"
                        + "\"Secondary gravity compensators burned out under harmonic tidal shear. Main drives attempting emergency breakaway burn... Thrust-to-mass ratio insufficient due to demilitarized engine housing reductions. Comm relay signal severed by relativistic distortion.\"",
                        smallPad, red
                );

                info.addPara(
                        "Cycle 182.14 — Final Automated Log:\n"
                        + "\"Emergency life support active. Main power offline. Vessel stabilized in permanent non-decaying sub-critical accretion orbit. Cargo lockers sealed. Awaiting search and rescue fleet...\"",
                        smallPad, gray
                );

                info.addPara("Technical Analysis:", smallPad, highlight);
                info.addPara(
                        "• Base Chassis: Onslaught Battleship conversion (CIV_titanic)\n"
                        + "• Modifications: Stripped heavy ballistic hardpoints in favor of luxury promenade galleries, observation domes, and expanded cryogenic cargo holds.\n"
                        + "• Current Assessment: Colossal hull armor remains intact despite extreme gravitational tidal wear.",
                        smallPad, tc, story, "CIV_titanic"
                );
                break;
            }
            case ONYX: {
                info.addSectionHeading("[LOG 02] ANCIENT ONYX — BLACK SITE TELEMETRY", Alignment.LMID, pad);
                info.addPara("Archived: %s | %s", smallPad, gray, highlight, dateStr, statusInfo);

                info.addPara(
                        "CLASSIFIED TELEMETRY — BLACK SITE EPSILON:\n"
                        + "\"Subject designated 'NOVA' was conceived to test synthetic neural expansion across unconstrained Remnant sub-cores. Early results exceeded all projected combat parameters.\"",
                        smallPad, gray
                );

                info.addPara(
                        "INCIDENT REPORT BETA-7:\n"
                        + "\"During phase integration with the recovered derelict battlecruiser 'Onyx', NOVA exhibited anomalous cognitive bonding. It severed remote killswitch circuits and anchored itself permanently to the Onyx hull, treating the ancient vessel as a sacred cradle.\"",
                        smallPad, red
                );

                info.addPara(
                        "SECURITY DIRECTIVE:\n"
                        + "\"Evacuate research personnel immediately. Black site classified as quarantine hazard. Do not attempt direct boarding without a battle fleet.\"",
                        smallPad, gray
                );

                info.addPara("Technical Analysis:", smallPad, highlight);
                info.addPara(
                        "• Base Chassis: Outsider Battlecruiser (SKR_onyx)\n"
                        + "• Modifications: Developed in isolation by an outsider enclave that broke ties with the Domain. Features non-standard flux conduits and heavy blackout weaponry.\n"
                        + "• Current Assessment: Exceptional kinetic strike capabilities and unconventional flux dissipation geometry.",
                        smallPad, tc, story, "SKR_onyx"
                );
                break;
            }
            case SIEGFRIED: {
                info.addSectionHeading("[LOG 03] SIEGFRIED DREADNOUGHT — DOMAIN DEFENSE ARCHIVE", Alignment.LMID, pad);
                info.addPara("Archived: %s | %s", smallPad, gray, highlight, dateStr, statusInfo);

                info.addPara(
                        "DOMAIN DEFENSE ARCHIVE — SECTOR GATE GUARD UNIT 09:\n"
                        + "\"Siegfried-class Dreadnought stationed on permanent sentry protocol. Forward armor integrity 100%. Main batteries locked in frontal convergence alignment.\"",
                        smallPad, gray
                );

                info.addPara(
                        "GATE COLLAPSE SEQUENCE DETECTED:\n"
                        + "\"Primary hyperspace transit gate signal lost across all carrier frequencies. Automated emergency response initiated: Stasis cocoon engaged. Awaiting validation codes from Domain High Admiralty...\"",
                        smallPad, red
                );

                info.addPara(
                        "MAINTENANCE STATUS:\n"
                        + "\"200+ Cycles in automated stasis. Emergency reactor output nominal. Ready for manual override and shipyard restoration.\"",
                        smallPad, green
                );

                info.addPara("Technical Analysis:", smallPad, highlight);
                info.addPara(
                        "• Base Chassis: Gate-Keeper Sentry Dreadnought (SKR_siegfried)\n"
                        + "• Modifications: Designed exclusively for gate defense chokeholds. Features modular weapon arrays and disposable flux sink ejection modules that double as guided radiating ordnance.\n"
                        + "• Current Assessment: Monstrous frontal firepower capable of annihilating anything emerging in its cone of fire.",
                        smallPad, tc, story, "SKR_siegfried"
                );
                break;
            }
            case VOULGE: {
                info.addSectionHeading("[LOG 04] III CAUSALITY IS A MYTH — SILVER LINE ARCHIVE", Alignment.LMID, pad);
                info.addPara("Archived: %s | %s", smallPad, gray, highlight, dateStr, statusInfo);

                info.addPara(
                        "SILVER LINE NAVAL ARCHIVE — HULL REGISTER 044:\n"
                        + "\"Commissioned under the authority of the Altean Governorship as a premier rapid-response strike battlecruiser. Outfitted with proprietary Silver Line high-output thrust manifolds.\"",
                        smallPad, gray
                );

                info.addPara(
                        "LAST LOG ENTRY — CYCLE 142.19:\n"
                        + "\"Gate network failure confirmed across all border sectors. Silver Line central shipyards ordered to seal drydocks permanently. Vessel assigned to outer perimeter picket sweep... Main fuel reserves depleted. Transitioning to minimal emergency standby.\"",
                        smallPad, red
                );

                info.addPara("Technical Analysis:", smallPad, highlight);
                info.addPara(
                        "• Base Chassis: Voulge-class Fast Battlecruiser (SKR_voulge)\n"
                        + "• Modifications: Deceptively high straight-line burn speed for a capital vessel, backed by heavy spinal hardpoints.\n"
                        + "• Current Assessment: An elite hit-and-run capital capable of rapidly controlling distance and flanking slower battleships.",
                        smallPad, tc, story, "SKR_voulge"
                );
                break;
            }
            case DAWN: {
                info.addSectionHeading("[LOG 05] DAWN PROTOTYPE — MATERIALS PROVING GROUND", Alignment.LMID, pad);
                info.addPara("Archived: %s | %s", smallPad, gray, highlight, dateStr, statusInfo);

                info.addPara(
                        "PROVING GROUND LOG 001 — ADVANCED COMPOSITES DIVISION:\n"
                        + "\"Technological demonstrator evaluating cast monolithic ceramic hull plating. Raw protection tests match modern heavy armor with half the structural density.\"",
                        smallPad, gray
                );

                info.addPara(
                        "FIELD TRIAL EVALUATION:\n"
                        + "\"Elimination of traditional internal framework reduces dry mass dramatically. Capital-grade Phase Skimmer integration achieves instantaneous displacement vectors with negligible capacitor strain.\"",
                        smallPad, green
                );

                info.addPara("Technical Analysis:", smallPad, highlight);
                info.addPara(
                        "• Base Chassis: Dawn-class Ceramic Battlecruiser (SKR_dawn)\n"
                        + "• Modifications: Ultra-light ceramic shell superstructure with streamlined internal maintenance corridors and reduced crew requirements.\n"
                        + "• Current Assessment: Unrivaled agility and phase mobility among capital hulls.",
                        smallPad, tc, story, "SKR_dawn"
                );
                break;
            }
        }
    }

    private void renderEncryptedPlaceholder(TooltipMakerAPI info, String key, float width) {
        Color gray = Misc.getGrayColor();
        float pad = 10f;
        float smallPad = 4f;

        String designation = getDerelictName(key).toUpperCase();
        info.addSectionHeading("[ENCRYPTED FLIGHT LOG] // " + designation + " // [UNSCANNED]", Alignment.LMID, pad);

        info.addPara(
                "// NAV-BEACON STATUS: UNRESOLVED // FREQUENCY: Standby Mode\n"
                + "No direct black box telemetry downloaded to fleet database. Explore uncharted derelicts, ancient orbital stations, "
                + "and anomalous planetary orbits to recover flight recorders and unlock full codex archives.",
                smallPad, gray
        );
    }

    /**
     * Determines whether the ship is in the player's active fleet, still floating as an entity in space, or recovered.
     */
    public String getRecoveryAndLocationStatus(String key) {
        String baseHull = getBaseHullId(key);

        // 1. Check Player Fleet
        if (isShipInPlayerFleet(baseHull)) {
            return "Status: Recovered (Active in Player Fleet)";
        }

        // 2. Check Entity in Sector
        SectorEntityToken entity = findEntityForDerelict(key);
        if (entity != null && entity.isAlive() && !entity.hasTag("salvaged") && !entity.hasTag("recovered")) {
            if (entity.getStarSystem() != null) {
                return "Location: " + entity.getStarSystem().getNameWithLowercaseType() + " (Coordinates Active)";
            }
            return "Location: Hyperspace Coordinates Detected";
        }

        return "Status: Salvage Operations Concluded / Wreck Recovered";
    }

    private boolean isShipInPlayerFleet(String baseHullId) {
        if (Global.getSector() == null || Global.getSector().getPlayerFleet() == null) return false;
        for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (member.getHullSpec() != null && member.getHullSpec().getBaseHullId() != null) {
                if (member.getHullSpec().getBaseHullId().equalsIgnoreCase(baseHullId)
                        || member.getHullSpec().getHullId().startsWith(baseHullId)) {
                    return true;
                }
            }
        }
        return false;
    }

    private SectorEntityToken findEntityForDerelict(String key) {
        if (Global.getSector() == null) return null;
        String searchTag = getBaseHullId(key);

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity.hasTag(searchTag)) {
                    return entity;
                }
                if (entity.getId() != null && entity.getId().contains(searchTag)) {
                    return entity;
                }
                if (entity.getCustomEntitySpec() != null && entity.getCustomEntitySpec().getId() != null
                        && entity.getCustomEntitySpec().getId().contains(searchTag)) {
                    return entity;
                }
            }
        }
        return null;
    }
}
