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
 * Captain's Log & Decrypted Codex tracking the Sector's legendary capital prototypes:
 * Titanic VII, Onyx, Siegfried, Voulge, Dawn, Demeter, Gawon, and Quicksilver.
 * 
 * @author Tartiflette, modified for Seeker UC
 */
public class SKR_derelictCodexIntel extends BaseIntelPlugin {

    public static final String TITANIC = "TITANIC";
    public static final String ONYX = "ONYX";
    public static final String SIEGFRIED = "SIEGFRIED";
    public static final String VOULGE = "VOULGE";
    public static final String DAWN = "DAWN";
    public static final String DEMETER = "DEMETER";
    public static final String GAWON = "GAWON";
    public static final String QUICKSILVER = "QUICKSILVER";

    public static final String[] ALL_DERELICTS = {
        TITANIC,
        ONYX,
        SIEGFRIED,
        VOULGE,
        DAWN,
        DEMETER,
        GAWON,
        QUICKSILVER
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
        if (clean.contains("DEMETER") || clean.contains("CIV_DEMETER")) {
            return DEMETER;
        }
        if (clean.contains("GAWON") || clean.contains("SKR_GAWON")) {
            return GAWON;
        }
        if (clean.contains("QUICKSILVER") || clean.contains("SKR_QUICKSILVER")) {
            return QUICKSILVER;
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
        if (DEMETER.equals(key)) return "Demeter Agri-Fortress";
        if (GAWON.equals(key)) return "Gawon AI Battlecruiser";
        if (QUICKSILVER.equals(key)) return "Quicksilver Skunkworks Capital";
        return "Unknown Colossus";
    }

    public static String getBaseHullId(String key) {
        if (TITANIC.equals(key)) return "CIV_titanic";
        if (ONYX.equals(key)) return "SKR_onyx";
        if (SIEGFRIED.equals(key)) return "SKR_siegfried";
        if (VOULGE.equals(key)) return "SKR_voulge";
        if (DAWN.equals(key)) return "SKR_dawn";
        if (DEMETER.equals(key)) return "CIV_demeter";
        if (GAWON.equals(key)) return "SKR_gawon";
        if (QUICKSILVER.equals(key)) return "SKR_quicksilver";
        return "";
    }

    public static String getShipStatus(String baseHullId) {
        if (baseHullId == null || baseHullId.isEmpty()) {
            return "[STATUS UNKNOWN]";
        }

        // 1. Check Player Fleet
        if (Global.getSector().getPlayerFleet() != null) {
            FleetMemberAPI flagship = Global.getSector().getPlayerFleet().getFlagship();
            if (flagship != null && matchesHull(flagship, baseHullId)) {
                return "[IN ACTIVE SERVICE — FLEET FLAGSHIP]";
            }
            for (FleetMemberAPI member : Global.getSector().getPlayerFleet().getFleetData().getMembersListCopy()) {
                if (matchesHull(member, baseHullId)) {
                    return "[IN ACTIVE SERVICE — COMBAT LINE]";
                }
            }
        }

        // 2. Check Storage / Colonies
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity.getMarket() != null && entity.getMarket().isPlayerOwned()) {
                    // Check if player has it in storage
                    if (entity.getMarket().getSubmarket("storage") != null) {
                        for (FleetMemberAPI member : entity.getMarket().getSubmarket("storage").getCargo().getMothballedShips().getMembersListCopy()) {
                            if (matchesHull(member, baseHullId)) {
                                return "[IN RESERVE STORAGE — " + entity.getMarket().getName() + "]";
                            }
                        }
                    }
                }
            }
        }

        return "[DEEP SPACE / RECOVERED]";
    }

    private static boolean matchesHull(FleetMemberAPI member, String baseHullId) {
        if (member == null) return false;
        String hid = member.getHullId();
        String bhid = member.getHullSpec().getBaseHullId();
        return baseHullId.equals(hid) || baseHullId.equals(bhid);
    }

    @Override
    public String getName() {
        int count = getUnlockedCount();
        if (count > 0) {
            return "Captain's Log: Legendary Colossi & Prototypes [" + count + "/" + ALL_DERELICTS.length + " Discovered]";
        }
        return "Captain's Log: Legendary Colossi & Prototypes";
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
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color titleColor = getTitleColor(mode);

        info.addPara(getName(), titleColor, 0f);

        int unlocked = getUnlockedCount();
        float initPad = 3f;

        if (unlocked == ALL_DERELICTS.length) {
            info.addPara("Status: %s (All vessels cataloged)", initPad, gray, Misc.getPositiveHighlightColor(), "COMPLETE");
        } else if (unlocked > 0) {
            info.addPara("Status: %s (%s of %s logged)", initPad, gray, highlight, "CHRONICLE EXPANDING", "" + unlocked, "" + ALL_DERELICTS.length);
        } else {
            info.addPara("Status: %s", initPad, gray, highlight, "AWAITING DISCOVERY");
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        float pad = 10f;
        float smallPad = 4f;

        int unlockedCount = getUnlockedCount();

        info.addSectionHeading("SECTOR RECOVERY CHRONICLE: ANCIENT & EXPERIMENTAL WARSHIPS", Alignment.MID, pad);
        info.addPara(
                "A personal captain's log chronicling the discovery, black box flight telemetry, and recovery of unique capital "
                + "prototypes and lost behemoths across the Persean Sector. Entries are automatically decrypted upon investigating "
                + "derelict wrecks or commissioning these vessels into fleet service.",
                smallPad
        );

        info.addSectionHeading(String.format("DISCOVERED PROTOTYPES: [%d / %d CATALOGED]", unlockedCount, ALL_DERELICTS.length), Alignment.LMID, pad);

        for (String derelictKey : ALL_DERELICTS) {
            boolean unlocked = isUnlocked(derelictKey);
            Long unlockTime = getUnlockTimestamp(derelictKey);

            if (unlocked) {
                renderUnlockedDerelict(info, derelictKey, unlockTime, width);
            } else {
                renderEncryptedPlaceholder(info, derelictKey, width);
            }
        }
    }

    private void renderUnlockedDerelict(TooltipMakerAPI info, String key, Long unlockTime, float width) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color green = Misc.getPositiveHighlightColor();
        Color story = Misc.getStoryOptionColor();
        float pad = 10f;
        float smallPad = 4f;

        String dateStr = unlockTime != null ? Global.getSector().getClock().createClock(unlockTime).getDateString() : "Historical Log";
        String baseHull = getBaseHullId(key);
        String status = getShipStatus(baseHull);

        switch (key) {
            case TITANIC: {
                info.addSectionHeading("[LUXURY SUPER-LINER] BHS TITANIC VII", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "CIV_titanic", status);
                info.addPara(
                        "An Onslaught-class battleship purchased and heavily converted by the Black Hole Line company. Its TPC armament bays "
                        + "were reconstructed into grand ballrooms, swimming pools, and observation lounges for 2,500 wealthy tourists. "
                        + "The vessel was lost during its maiden voyage when tidal shear overwhelmed its secondary gravity stabilizers near an accretion disk.",
                        smallPad
                );
                info.addPara("• Tactical Role: High-capacity civilian transport & mobile luxury command center with extreme structural durability.", smallPad, tc);
                break;
            }
            case ONYX: {
                info.addSectionHeading("[OUTSIDER PROTOTYPE] ONYX-CLASS BATTLECRUISER", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "SKR_onyx", status);
                info.addPara(
                        "A striking vessel originating from an isolated human polity that separated from the Domain long before the Collapse. "
                        + "Recovered from the heart of a Remnant fortress where it was revered as a sacred cradle by the false-idol entity NOVA. "
                        + "Features twin built-in Blackout siege cannons and an integrated temporal acceleration core.",
                        smallPad
                );
                info.addPara("• Tactical Role: Heavy energy brawler mounting the massive Blackout siege cannon and Temporal Shell system.", smallPad, tc);
                break;
            }
            case SIEGFRIED: {
                info.addSectionHeading("[DOMAIN GATE-KEEPER] SIEGFRIED-CLASS SENTRY DREADNOUGHT", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "SKR_siegfried", status);
                info.addPara(
                        "Where the Onslaught was the Domain's mobile fist, the colossal Siegfried was its immovable gatekeeper. "
                        + "Stationed directly beside strategic hyperspace gates with all main battery firepower focused into a lethal forward cone. "
                        + "Entered emergency stasis lockdown during the Collapse, awaiting admiralty orders that never came.",
                        smallPad
                );
                info.addPara("• Tactical Role: Super-heavy forward line anchor with unmatched frontal armor density and battery convergence.", smallPad, tc);
                break;
            }
            case VOULGE: {
                info.addSectionHeading("[SILVER LINE RETROFIT] III CAUSALITY IS A MYTH (VOULGE)", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "SKR_voulge", status);
                info.addPara(
                        "A deceptively nimble battlecruiser forged by the ancient Silver Line Shipyards before their closure following the Collapse. "
                        + "Features a massive spinal energy projector and heavy kinetic mounts arrayed along its forward prow, designed for sudden high-speed interception.",
                        smallPad
                );
                info.addPara("• Tactical Role: High-speed battlecruiser optimized for aggressive breakthrough spearheads.", smallPad, tc);
                break;
            }
            case DAWN: {
                info.addSectionHeading("[CERAMIC DEMONSTRATOR] DAWN-CLASS BATTLECRUISER", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "SKR_dawn", status);
                info.addPara(
                        "The counterpart to the Onyx ('Day and Night'). Built as a technological demonstrator for advanced low-density ceramic composite armor. "
                        + "Boasts extreme agility for its tonnage and mounts twin built-in Sunburst energy cannons paired with a Phase Displacer.",
                        smallPad
                );
                info.addPara("• Tactical Role: Agile phase-skimming skirmisher capital with rapid thermal dissipation.", smallPad, tc);
                break;
            }
            case DEMETER: {
                info.addSectionHeading("[MOBILE HYDROPONICS] DEMETER AGRI-FORTRESS", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "CIV_demeter", status);
                info.addPara(
                        "Built inside the hollowed-out hull of an unfinished Paragon-class battleship by a rogue Tri-Tachyon director. "
                        + "Its massive interior holds complete automated hydroponics biomes and atmospheric scrubbers capable of supplying food to entire sectors.",
                        smallPad
                );
                info.addPara("• Tactical Role: Heavy logistical powerhouse with fortified shields and massive food generation capacity.", smallPad, tc);
                break;
            }
            case GAWON: {
                info.addSectionHeading("[AI-ENGINEERED BATTLESHIP] GAWON-CLASS CAPITAL", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "SKR_gawon", status);
                info.addPara(
                        "A custom-engineered heavy capital allegedly built by a rogue Alpha AI attempting to escape a dying shipyard colony. "
                        + "Features automated cooling matrices and an experimental energy distribution grid.",
                        smallPad
                );
                info.addPara("• Tactical Role: Sustained energy bombardment platform with automated flux-sink architecture.", smallPad, tc);
                break;
            }
            case QUICKSILVER: {
                info.addSectionHeading("[SKUNKWORKS DEMONSTRATOR] QUICKSILVER-CLASS BATTLECRUISER", Alignment.LMID, pad);
                info.addPara("Discovered: %s | Hull ID: %s | Status: %s", smallPad, gray, highlight, dateStr, "SKR_quicksilver", status);
                info.addPara(
                        "A razor-thin technological demonstrator constructed by Apollo Skunkworks. "
                        + "Fitted with experimental micro-missile arrays and high-coherence beam directors at the cost of crew comfort and maintenance overhead.",
                        smallPad
                );
                info.addPara("• Tactical Role: High-tech sniper capital utilizing integrated beam projection systems.", smallPad, tc);
                break;
            }
        }
    }

    private void renderEncryptedPlaceholder(TooltipMakerAPI info, String key, float width) {
        Color gray = Misc.getGrayColor();
        float pad = 10f;
        float smallPad = 4f;

        String name = getDerelictName(key).toUpperCase();
        info.addSectionHeading("[UNRESOLVED VESSEL] // " + name + " // [UNDISCOVERED]", Alignment.LMID, pad);
        info.addPara(
                "// CHRONICLE ARCHIVE: RECOVERY LOG ENCRYPTED\n"
                + "No telemetry recorded in active fleet archives. Explore deep space ruins, follow bar rumors, "
                + "or commission this vessel into service to decrypt its complete historical profile.",
                smallPad, gray
        );
    }
}
