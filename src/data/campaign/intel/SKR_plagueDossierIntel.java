package data.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.plog.PLTextEntry;
import com.fs.starfarer.api.impl.campaign.plog.PlaythroughLog;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.campaign.ids.SKR_ids;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Tactical & scientific threat dossier tracking the 4 ancient Plague-Bearer Colossi.
 * Provides unlockable detailed analysis, battle records, and corrupted telemetry archives.
 * 
 * @author Tartiflette, modified for Seeker UC
 */
public class SKR_plagueDossierIntel extends BaseIntelPlugin {

    public static final String SAFEGUARD = "SAFEGUARD";
    public static final String RAMPAGE = "RAMPAGE";
    public static final String WHITE_DWARF = "WHITE_DWARF";
    public static final String CATACLYSM = "CATACLYSM";

    public static final String[] ALL_COLOSSI = {
        SAFEGUARD,
        RAMPAGE,
        WHITE_DWARF,
        CATACLYSM
    };

    /**
     * Map storing unlocked sub-entry keys to their respective unlock timestamp.
     */
    protected Map<String, Long> unlockedEntries = new LinkedHashMap<>();

    public SKR_plagueDossierIntel() {
    }

    /**
     * Retrieves or creates the persistent singleton instance in the Sector Intel Manager.
     */
    public static SKR_plagueDossierIntel get() {
        IntelManagerAPI intelManager = Global.getSector().getIntelManager();
        if (intelManager.hasIntelOfClass(SKR_plagueDossierIntel.class)) {
            return (SKR_plagueDossierIntel) intelManager.getFirstIntel(SKR_plagueDossierIntel.class);
        }
        SKR_plagueDossierIntel intel = new SKR_plagueDossierIntel();
        intelManager.addIntel(intel, false);
        return intel;
    }

    /**
     * Normalizes arbitrary entry strings to standard Colossus keys.
     */
    public static String normalizeKey(String entryKey) {
        if (entryKey == null) return null;
        String clean = entryKey.trim().toUpperCase();
        if (clean.contains("SAFEGUARD") || clean.contains("PLAGUE_A") || clean.contains("KEEP")) {
            return SAFEGUARD;
        }
        if (clean.contains("RAMPAGE") || clean.contains("PLAGUE_B")) {
            return RAMPAGE;
        }
        if (clean.contains("WHITE_DWARF") || clean.contains("WHITEDWARF") || clean.contains("PLAGUE_C")) {
            return WHITE_DWARF;
        }
        if (clean.contains("CATACLYSM") || clean.contains("PLAGUE_D")) {
            return CATACLYSM;
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
     * Unlocks a sub-entry, logs the discovery in the playthrough captain's log,
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

        String colossusName = getColossusName(key);

        // Record in Captain's Playthrough Log
        try {
            PlaythroughLog.getInstance().addEntry(
                    new PLTextEntry("Plague Threat Dossier: Decrypted telemetry profile for [" + colossusName + "].", true)
            );
        } catch (Throwable t) {
            // Graceful fallback
        }

        // Send intel update
        sendUpdateIfPlayerHasIntel(key, false);
        SKR_campaignLogListener.checkFleetChronicle();
    }

    public static String getColossusName(String key) {
        if (SAFEGUARD.equals(key)) return "Safeguard (Type-Alpha)";
        if (RAMPAGE.equals(key)) return "Rampage (Type-Beta)";
        if (WHITE_DWARF.equals(key)) return "White Dwarf (Type-Gamma)";
        if (CATACLYSM.equals(key)) return "Cataclysm (Type-Delta)";
        return "Unknown Entity";
    }

    public static String getHullId(String key) {
        if (SAFEGUARD.equals(key)) return "SKR_keep";
        if (RAMPAGE.equals(key)) return "SKR_rampage";
        if (WHITE_DWARF.equals(key)) return "SKR_whiteDwarf";
        if (CATACLYSM.equals(key)) return "SKR_cataclysm";
        return "";
    }

    @Override
    public String getName() {
        int count = getUnlockedCount();
        if (count >= 4) {
            return "Threat Dossier: Plague-Bearer Colossi [PURGED - 4/4]";
        } else if (count > 0) {
            return "Threat Dossier: Plague-Bearer Colossi [" + count + "/4 Decrypted]";
        }
        return "Threat Dossier: Plague-Bearer Colossi";
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

        if (unlocked >= 4) {
            info.addPara("Status: %s — All entities neutralized", initPad, gray, Misc.getPositiveHighlightColor(), "DECONTAMINATED");
        } else if (unlocked > 0) {
            info.addPara("Status: %s (%s of 4 active strains analyzed)", initPad, gray, highlight, "INVESTIGATION ONGOING", "" + unlocked);
        } else {
            info.addPara("Status: %s", initPad, gray, Misc.getNegativeHighlightColor(), "CRITICAL THREAT PENDING");
        }
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color positive = Misc.getPositiveHighlightColor();
        float pad = 10f;
        float smallPad = 4f;

        int unlockedCount = getUnlockedCount();

        // Header Summary
        info.addSectionHeading("SECTOR THREAT ASSESSMENT: NANOTECHNOLOGICAL ASSIMILATION", Alignment.MID, pad);
        info.addPara(
                "A classified compilation of telemetry, sensor logs, and scientific assessments regarding the "
                + "self-replicating nanite plague infesting ancient Domain Explorarium and Remnant war hulls. "
                + "Defeating each Colossus yields a unique sub-core upgrade and decrypts its permanent operational file.",
                smallPad
        );

        // Progress Bar & Ability Installation Overview
        info.addSectionHeading(String.format("INVESTIGATION STATUS: [%d / 4 THREAT SOURCES CLEANSED]", unlockedCount), Alignment.LMID, pad);

        Set<String> playerAbilities = Global.getSector().getCharacterData().getAbilities();

        // Render each Colossus
        for (String colossusKey : ALL_COLOSSI) {
            boolean unlocked = isUnlocked(colossusKey);
            Long unlockTime = getUnlockTimestamp(colossusKey);

            if (unlocked) {
                renderUnlockedColossus(info, colossusKey, unlockTime, playerAbilities, width);
            } else {
                renderEncryptedPlaceholder(info, colossusKey, width);
            }
        }
    }

    private void renderUnlockedColossus(TooltipMakerAPI info, String key, Long unlockTime, Set<String> playerAbilities, float width) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color story = Misc.getStoryOptionColor();
        Color green = Misc.getPositiveHighlightColor();
        Color yellow = Misc.getHighlightColor();
        float pad = 10f;
        float smallPad = 4f;

        String dateStr = unlockTime != null ? Global.getSector().getClock().createClock(unlockTime).getDateString() : "Historical Log";
        String hullId = getHullId(key);

        switch (key) {
            case SAFEGUARD: {
                info.addSectionHeading("[TYPE-ALPHA] SAFEGUARD — INDUSTRIAL EXPLOITATION PLATFORM", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s | Status: %s", smallPad, gray, highlight, dateStr, "Industrial Drone Hive", "PURGED");

                info.addPara(
                        "Originally constructed as a heavy exploitation platform deployed alongside Domain Sporeships to extract resources "
                        + "from barren worlds on a planetary scale. Sometime following the Collapse, its central AI core was infected and rewritten "
                        + "by the self-replicating nanite plague. The platform's industrial foundries have been transformed into high-capacity "
                        + "automated drone factories that guard its territory with ruthless efficiency.",
                        smallPad
                );

                boolean hasAbility = playerAbilities.contains(SKR_ids.ABILITY_REMOTE_SURVEY);
                String abilityStatus = hasAbility ? "[INTEGRATED INTO FLEET DOCTRINE]" : "[PENDING INSTALLATION FROM SPECIAL ITEMS]";
                Color abilityColor = hasAbility ? green : yellow;

                info.addPara("Sub-Core Artifact: %s — %s", smallPad, tc, story, "Enhanced Remote Survey Probes", abilityStatus);
                info.addPara("• Grants hyperspace remote planetary survey capability and scans deep space ruins across jump points.", smallPad, gray);
                break;
            }
            case RAMPAGE: {
                info.addSectionHeading("[TYPE-BETA] RAMPAGE — COMPOSITE WAR ENGINE", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s | Status: %s", smallPad, gray, highlight, dateStr, "Multi-Era Hybrid Brawler", "PURGED");

                info.addPara(
                        "While its core drive signatures trace back to Domain Explorarium engineering, Rampage's physical architecture is a frightening "
                        + "amalgamation of hulls, armor alloys, and heavy weapons from distinct centuries of Domain development, fused seamlessly "
                        + "into a single predatory war engine. It responds to hails only with discordant sensor screeching and fights with terrifying kinetic ferocity.",
                        smallPad
                );

                boolean hasAbility = playerAbilities.contains(SKR_ids.ABILITY_NEUTRINO_DETECTOR);
                String abilityStatus = hasAbility ? "[INTEGRATED INTO FLEET DOCTRINE]" : "[PENDING INSTALLATION FROM SPECIAL ITEMS]";
                Color abilityColor = hasAbility ? green : yellow;

                info.addPara("Sub-Core Artifact: %s — %s", smallPad, tc, story, "Nano-laminated Neutrino Sensors", abilityStatus);
                info.addPara("• Enhances Neutrino Detector with extended sensor range and automated false-signal suppression.", smallPad, gray);
                break;
            }
            case WHITE_DWARF: {
                info.addSectionHeading("[TYPE-GAMMA] WHITE DWARF — SUBSUMED RADIANT", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s | Status: %s", smallPad, gray, highlight, dateStr, "Corrupted Remnant Flagship", "PURGED");

                info.addPara(
                        "A modified Remnant Radiant-class battleship that suffered catastrophic neural infection. The corrupted Alpha Core severed all "
                        + "links to the primary Remnant Nexus hierarchy, forcibly re-flashing accompanying Remnant patrol craft with viral code. "
                        + "Operating in total isolation from the Nexus, it patrols deep space with an escort of frenzied drone escorts.",
                        smallPad
                );

                boolean hasAbility = playerAbilities.contains(SKR_ids.ABILITY_EMERGENCY_BURN);
                String abilityStatus = hasAbility ? "[INTEGRATED INTO FLEET DOCTRINE]" : "[PENDING INSTALLATION FROM SPECIAL ITEMS]";
                Color abilityColor = hasAbility ? green : yellow;

                info.addPara("Sub-Core Artifact: %s — %s", smallPad, tc, story, "Omostatic Drive Field Capacitors", abilityStatus);
                info.addPara("• Supercharges Emergency Burn and grants complete fleet immunity to hyperspace storm lightning damage.", smallPad, gray);
                break;
            }
            case CATACLYSM: {
                info.addSectionHeading("[TYPE-DELTA] CATACLYSM — SOVEREIGN HIVE QUEEN", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s | Status: %s", smallPad, gray, highlight, dateStr, "Apocalyptic Nanite Hive Queen", "PURGED");

                info.addPara(
                        "The apex entity of the Plague-Bearer plague. First recorded when it appeared above planet Abraxia, Cataclysm systematically "
                        + "consumed all orbital and planetary infrastructure within weeks, using a buzzing grey mist of nanites to rebuild and feed its "
                        + "immense hull. Accompanied by fanatical human cultists who revere it as a mechanical god, it represents an existential hazard.",
                        smallPad
                );

                boolean hasAbility = playerAbilities.contains(SKR_ids.ABILITY_SUSTAINED_BURN);
                String abilityStatus = hasAbility ? "[INTEGRATED INTO FLEET DOCTRINE]" : "[PENDING INSTALLATION FROM SPECIAL ITEMS]";
                Color abilityColor = hasAbility ? green : yellow;

                info.addPara("Sub-Core Artifact: %s — %s", smallPad, tc, story, "Recursive Drive Field Emitters", abilityStatus);
                info.addPara("• Expands Sustained Burn drive envelope to ignore asteroid collisions and halve terrain movement drag.", smallPad, gray);
                break;
            }
        }
    }

    private void renderEncryptedPlaceholder(TooltipMakerAPI info, String key, float width) {
        Color gray = Misc.getGrayColor();
        Color red = Misc.getNegativeHighlightColor();
        float pad = 10f;
        float smallPad = 4f;

        String designation = getColossusName(key).toUpperCase();
        info.addSectionHeading("[ENCRYPTED TELEMETRY] // " + designation + " // [LOCKED]", Alignment.LMID, pad);

        info.addPara(
                "// SENSOR STATUS: TELEMETRY ENCRYPTED // HASH: [0x7F 0x9B 0x33 0xAA 0x14]\n"
                + "// ANOMALY SIGNATURE: Autonomous Nanite Contagion Detected\n"
                + "No direct tactical scan data archived in fleet memory. Sensors indicate sporadic high-energy neutrino emissions "
                + "originating from unmapped fringe systems. Defeat or salvage this entity to decrypt full threat telemetry and unlock its subsystem schematic.",
                smallPad, gray
        );
    }
}
