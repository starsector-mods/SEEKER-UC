package data.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.comm.IntelManagerAPI;
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
     * Map storing unlocked sub-entry keys to their respective unlock timestamp (in sector clock milliseconds).
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

    /**
     * Checks if a specific Colossus entry has been decrypted.
     */
    public boolean isUnlocked(String entryKey) {
        String key = normalizeKey(entryKey);
        return key != null && unlockedEntries.containsKey(key);
    }

    /**
     * Returns the timestamp when an entry was unlocked, or null if locked.
     */
    public Long getUnlockTimestamp(String entryKey) {
        String key = normalizeKey(entryKey);
        return key != null ? unlockedEntries.get(key) : null;
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
    }

    public static String getColossusName(String key) {
        if (SAFEGUARD.equals(key)) return "Safeguard (Type-Alpha)";
        if (RAMPAGE.equals(key)) return "Rampage (Type-Beta)";
        if (WHITE_DWARF.equals(key)) return "White Dwarf (Type-Gamma)";
        if (CATACLYSM.equals(key)) return "Cataclysm (Type-Delta)";
        return "Unknown Colossus";
    }

    public int getUnlockedCount() {
        return unlockedEntries.size();
    }

    @Override
    public String getName() {
        return "Dossier: Plague-Bearer Colossi";
    }

    @Override
    public String getSmallDescriptionTitle() {
        return "CLASSIFIED DOSSIER // THE PLAGUE-BEARER COLOSSI";
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
        info.addPara("Decrypted Threat Profiles: %s / %s", pad, tc, highlight, "" + count, "" + ALL_COLOSSI.length);

        if (getListInfoParam() instanceof String) {
            String paramKey = normalizeKey((String) getListInfoParam());
            if (paramKey != null) {
                info.addPara("Updated Entry: %s", pad, tc, Misc.getPositiveHighlightColor(), getColossusName(paramKey));
            }
        } else if (count == ALL_COLOSSI.length) {
            info.addPara("All Colossi telemetry profiles fully decoded", Misc.getPositiveHighlightColor(), pad);
        } else {
            info.addPara("Autonomous nanite entities active in uncharted space", gray, pad);
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

        // Header and Overview
        info.addPara(
                "A highly classified intelligence dossier compiling sensor telemetry, recovered debris analysis, "
                + "and archival records concerning the four ancient %s entities encountered on the Sector fringe.",
                pad, highlight, "Plague-Bearer Colossi"
        );

        info.addPara(
                "Each entity represents an automated Domain-era platform whose command logic and fabricators "
                + "were completely subsumed by a rogue, self-replicating nanite contagion. While standard Domain frequencies "
                + "yield only corrupted carrier hiss, recovered sub-cores contain crucial data on exotic subsystem artifacts "
                + "and weapon blueprints.",
                smallPad, gray
        );

        int unlockedCount = getUnlockedCount();
        info.addSectionHeading("DECRYPTED THREAT TELEMETRY ARCHIVE (" + unlockedCount + " / " + ALL_COLOSSI.length + ")", Alignment.MID, pad);

        // Render each Colossus
        for (String colossusKey : ALL_COLOSSI) {
            boolean unlocked = isUnlocked(colossusKey);
            Long unlockTime = getUnlockTimestamp(colossusKey);

            if (unlocked) {
                renderUnlockedColossus(info, colossusKey, unlockTime, width);
            } else {
                renderEncryptedPlaceholder(info, colossusKey, width);
            }
        }
    }

    private void renderUnlockedColossus(TooltipMakerAPI info, String key, Long unlockTime, float width) {
        Color highlight = Misc.getHighlightColor();
        Color gray = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        Color story = Misc.getStoryOptionColor();
        Color green = Misc.getPositiveHighlightColor();
        float pad = 10f;
        float smallPad = 4f;

        String dateStr = unlockTime != null ? Global.getSector().getClock().createClock(unlockTime).getDateString() : "Historical Log";

        switch (key) {
            case SAFEGUARD: {
                info.addSectionHeading("[TYPE-ALPHA] SAFEGUARD — EXPLOITATION PLATFORM", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s", smallPad, gray, highlight, dateStr, "Industrial Exploitation Platform");

                info.addPara(
                        "Originally constructed as a heavy exploitation platform deployed alongside Domain Sporeships to extract resources "
                        + "from barren worlds on a planetary scale. Sometime following the Collapse, its central AI core was infected and rewritten "
                        + "by the self-replicating nanite plague. The platform's industrial foundries have been transformed into high-capacity "
                        + "automated drone factories that guard its territory with ruthless efficiency.",
                        smallPad
                );

                info.addPara("Tactical Threat Assessment:", smallPad, highlight);
                info.addPara(
                        "• Hull Type: Keep-class Mobile Industrial Fortress (SKR_keep)\n"
                        + "• Combat Doctrine: Relies on overwhelming swarms of custom attack drones and heavy automated point-defense screens.\n"
                        + "• Subsystem Artifact: %s — Launches advanced probe swarms capable of surveying entire star systems through jump-points.",
                        smallPad, tc, story, "Enhanced Remote Survey Probes"
                );
                break;
            }
            case RAMPAGE: {
                info.addSectionHeading("[TYPE-BETA] RAMPAGE — COMPOSITE WAR ENGINE", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s", smallPad, gray, highlight, dateStr, "Multi-Era Hybrid Warship");

                info.addPara(
                        "While its core drive signatures trace back to Domain Explorarium engineering, Rampage's physical architecture is a frightening "
                        + "amalgamation of hulls, armor alloys, and heavy weapons from distinct centuries of Domain development, fused seamlessly "
                        + "into a single predatory war engine. It responds to hails only with discordant sensor screeching and fights with terrifying kinetic ferocity.",
                        smallPad
                );

                info.addPara("Tactical Threat Assessment:", smallPad, highlight);
                info.addPara(
                        "• Hull Type: Rampage-class Heavy Brawler (SKR_rampage)\n"
                        + "• Combat Doctrine: Devastating broadsides and erratic high-mass drive bursts designed to shatter armored battle lines.\n"
                        + "• Subsystem Artifact: %s — Harmonic detector calibrated to isolate artificial matter signatures at immense distances.",
                        smallPad, tc, story, "Enhanced Neutrino Detector"
                );
                break;
            }
            case WHITE_DWARF: {
                info.addSectionHeading("[TYPE-GAMMA] WHITE DWARF — SUBSUMED RADIANT", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s", smallPad, gray, highlight, dateStr, "Corrupted Remnant Flagship");

                info.addPara(
                        "A modified Remnant Radiant-class battleship that suffered catastrophic neural infection. The corrupted Alpha Core severed all "
                        + "links to the primary Remnant Nexus hierarchy, forcibly re-flashing accompanying Remnant patrol craft with viral code. "
                        + "Operating in total isolation from the Nexus, it patrols deep space with an escort of frenzied drone escorts.",
                        smallPad
                );

                info.addPara("Tactical Threat Assessment:", smallPad, highlight);
                info.addPara(
                        "• Hull Type: Corrupted Radiant Battleship (SKR_whiteDwarf)\n"
                        + "• Combat Doctrine: Extreme high-energy beam convergence coupled with phase skimmer assault vectors and hacked escort fleets.\n"
                        + "• Subsystem Artifact: %s — Overcharged capacitor banks enabling emergency drive engagement through hazardous spatial storms.",
                        smallPad, tc, story, "Enhanced Emergency Burn Capacitors"
                );
                break;
            }
            case CATACLYSM: {
                info.addSectionHeading("[TYPE-DELTA] CATACLYSM — SOVEREIGN HIVE QUEEN", Alignment.LMID, pad);
                info.addPara("Telemetry Decrypted: %s | Classification: %s", smallPad, gray, highlight, dateStr, "Apocalyptic Nanite Hive Queen");

                info.addPara(
                        "The apex entity of the Plague-Bearer plague. First recorded when it appeared above planet Abraxia, Cataclysm systematically "
                        + "consumed all orbital and planetary infrastructure within weeks, using a buzzing grey mist of nanites to rebuild and feed its "
                        + "immense hull. Accompanied by fanatical human cultists who revere it as a mechanical god, it represents an existential hazard.",
                        smallPad
                );

                info.addPara("Tactical Threat Assessment:", smallPad, highlight);
                info.addPara(
                        "• Hull Type: Cataclysm-class Apocalyptic Dreadnought (SKR_cataclysm)\n"
                        + "• Combat Doctrine: Station-grade destructive output combined with a debilitating nanite aura ('Maleficent Presence') that induces acute panic and rapid Combat Readiness degradation in opposing fleets.\n"
                        + "• Subsystem Artifact: %s — High-output field emitters generating an expanded drive bubble that ignores gravity wells and asteroid collisions.",
                        smallPad, tc, story, "Enhanced Sustained Burn Emitters"
                );
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
