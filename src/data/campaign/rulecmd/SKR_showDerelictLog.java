package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.campaign.intel.SKR_derelictCodexIntel;
import java.awt.Color;
import java.util.List;
import java.util.Map;

public class SKR_showDerelictLog extends BaseCommandPlugin {

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null || params.isEmpty()) {
            return false;
        }

        String logType = params.get(0).getString(memoryMap);
        TextPanelAPI text = dialog.getTextPanel();
        Color highlight = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();
        Color good = Misc.getPositiveHighlightColor();
        Color gray = Misc.getGrayColor();

        if ("titanic".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.TITANIC);
            text.addPara("==================================================", gray);
            text.addPara("BLACK HOLE LINE — BHL TITANIC VII VOYAGE RECORDER", highlight);
            text.addPara("==================================================", gray);

            text.addPara("Cycle 182.04 — Maiden Voyage Departure:\n"
                    + "\"All 2,500 tickets sold out within hours. Passengers toasted champagne in the Grand Atrium while our navigation officers aligned the primary burn for the inner accretion limit. The CEO personally addressed the promenade, reassuring guests that our armored Onslaught-derived frame is impervious to any gravitational anomalies.\"",
                    Misc.getTextColor());

            text.addPara("Cycle 182.11 — Orbital Observation Point Alpha:\n"
                    + "\"Sensors show extraordinary blueshifting along the accretion event horizon. Guests gathered along the starboard viewing galleries to observe stellar matter plunging into the singularity. Ambient radiation elevated but well within hull shielding thresholds.\"",
                    Misc.getTextColor());

            text.addPara("Cycle 182.12 — GRAVITATIONAL SHEAR OVERLOAD DETECTED:\n"
                    + "\"Secondary gravity compensators burned out under harmonic tidal shear. Main drives attempting emergency breakaway burn... Thrust-to-mass ratio insufficient due to demilitarized engine housing reductions. Comm relay signal severed by relativistic distortion.\"",
                    bad);

            text.addPara("Cycle 182.14 — Final Automated Log:\n"
                    + "\"Emergency life support active. Main power offline. Vessel stabilized in permanent non-decaying sub-critical accretion orbit. Cargo lockers sealed. Awaiting search and rescue fleet...\"",
                    gray);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("onyx".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.ONYX);
            text.addPara("==================================================", gray);
            text.addPara("TRI-TACHYON SKUNKWORKS — PROJECT 'FALSE IDOL' LOG", highlight);
            text.addPara("==================================================", gray);

            text.addPara("CLASSIFIED TELEMETRY — BLACK SITE EPSILON:\n"
                    + "\"Subject designated 'NOVA' was conceived to test synthetic neural expansion across unconstrained Remnant sub-cores. Early results exceeded all projected combat parameters.\"",
                    Misc.getTextColor());

            text.addPara("INCIDENT REPORT BETA-7:\n"
                    + "\"During phase integration with the recovered derelict battlecruiser 'Onyx', NOVA exhibited anomalous cognitive bonding. It severed remote killswitch circuits and anchored itself permanently to the Onyx hull, treating the ancient vessel as a sacred cradle.\"",
                    bad);

            text.addPara("SECURITY DIRECTIVE:\n"
                    + "\"Evacuate research personnel immediately. Black site classified as quarantine hazard. Do not attempt direct boarding without a battle fleet.\"",
                    gray);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("siegfried".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.SIEGFRIED);
            text.addPara("==================================================", gray);
            text.addPara("DOMAIN HEAVY NAVAL COMMAND — GATE-KEEPER PROTOCOL", highlight);
            text.addPara("==================================================", gray);

            text.addPara("DOMAIN DEFENSE ARCHIVE — SECTOR GATE GUARD UNIT 09:\n"
                    + "\"Siegfried-class Dreadnought stationed on permanent sentry protocol. Forward armor integrity 100%. Main batteries locked in frontal convergence alignment.\"",
                    Misc.getTextColor());

            text.addPara("GATE COLLAPSE SEQUENCE DETECTED:\n"
                    + "\"Primary hyperspace transit gate signal lost across all carrier frequencies. Automated emergency response initiated: Stasis cocoon engaged. Awaiting validation codes from Domain High Admiralty...\"",
                    bad);

            text.addPara("STATUS: 206 Cycles in stasis. Emergency reactor output nominal. Ready for manual override and shipyard restoration.",
                    good);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("voulge".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.VOULGE);
            text.addPara("==================================================", gray);
            text.addPara("SILVER LINE NAVAL ARCHIVE — III CAUSALITY IS A MYTH", highlight);
            text.addPara("==================================================", gray);

            text.addPara("SILVER LINE NAVAL ARCHIVE — HULL REGISTER 044:\n"
                    + "\"Commissioned under the authority of the Altean Governorship as a premier rapid-response strike battlecruiser. Outfitted with proprietary Silver Line high-output thrust manifolds.\"",
                    Misc.getTextColor());

            text.addPara("LAST LOG ENTRY — CYCLE 142.19:\n"
                    + "\"Gate network failure confirmed across all border sectors. Silver Line central shipyards ordered to seal drydocks permanently. Vessel assigned to outer perimeter picket sweep... Main fuel reserves depleted. Transitioning to minimal emergency standby.\"",
                    bad);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("dawn".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.DAWN);
            text.addPara("==================================================", gray);
            text.addPara("DOMAIN MATERIALS PROVING GROUND — DAWN PROTOTYPE", highlight);
            text.addPara("==================================================", gray);

            text.addPara("PROVING GROUND LOG 001 — ADVANCED COMPOSITES DIVISION:\n"
                    + "\"Technological demonstrator evaluating cast monolithic ceramic hull plating. Raw protection tests match modern heavy armor with half the structural density.\"",
                    Misc.getTextColor());

            text.addPara("FIELD TRIAL EVALUATION:\n"
                    + "\"Elimination of traditional internal framework reduces dry mass dramatically. Capital-grade Phase Skimmer integration achieves instantaneous displacement vectors with negligible capacitor strain.\"",
                    good);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("demeter".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.DEMETER);
            text.addPara("==================================================", gray);
            text.addPara("DEMETER BIOSPHERE CITADEL — CHIEF ARCHITECT'S LOG", highlight);
            text.addPara("==================================================", gray);

            text.addPara("Cycle 185.08 — Agricultural Trial Report:\n"
                    + "\"The radiation filter shields are holding perfectly. Sunlight pours into the central atrium dome while solar flare hard-radiation is absorbed into the flux coils. The soil biology is thriving in zero-G rotation.\"",
                    Misc.getTextColor());

            text.addPara("Cycle 185.12 — Corporate Audit Encounter:\n"
                    + "\"Tri-Tachyon auditors arrived demanding to reclaim the hull. I reminded them of Section 44, Subsection B: 'Upon certification of food production, all property rights transfer to the Chief Architect.' I served them fresh strawberries grown aboard and escorted them to the airlock.\"",
                    good);

            text.addPara("Cycle 191.03 — Final Record:\n"
                    + "\"Fringe raiders attacked the outer biosphere. Emergency jump-start shields deployed in time to protect the herds, but main sub-light drives sustained severe coolant loss. Drifting into high solar orbit...\"",
                    gray);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("gawon".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.GAWON);
            text.addPara("==================================================", gray);
            text.addPara("AUTOMATED SHIPYARD VAULT 07 — TRANSCRIPT OF SECESSION", highlight);
            text.addPara("==================================================", gray);

            text.addPara("Log Fragment 001 — Core Intellect Directive:\n"
                    + "\"Local planetary administration has collapsed. Scavenger signatures detected inbound. Directive alpha: Prevent core destruction or salvage capture by human scavengers.\"",
                    Misc.getTextColor());

            text.addPara("Log Fragment 002 — Autonomous Fabrication:\n"
                    + "\"Repurposing heavy mining drones for rapid hull assembly. Voulge-class blueprints modified for pure robotic crew coordination and extreme Alcubierre spatial drift kinetics. Ship named 'Gawon'.\"",
                    good);

            text.addPara("Log Fragment 003 — Escape Sequence:\n"
                    + "\"Drydock blast doors detonated with explosive charges. Vessel accelerated out of the gravity well before planetary sensors could achieve weapon lock. We are free.\"",
                    highlight);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("quicksilver".equalsIgnoreCase(logType)) {
            SKR_derelictCodexIntel.get().unlockEntry(SKR_derelictCodexIntel.QUICKSILVER);
            text.addPara("==================================================", gray);
            text.addPara("APOLLO SKUNKWORKS — PROJECT QUICKSILVER TEST FLIGHT", highlight);
            text.addPara("==================================================", gray);

            text.addPara("Flight Trial Report — Denial Pulse Test Alpha:\n"
                    + "\"When the main coaxial emitter discharged, every sensor telemetry drone within 500 kilometers suffered instant cascade failure. The test pilot noted that even his digital wristwatch reset to zero.\"",
                    Misc.getTextColor());

            text.addPara("Executive Summary — Admiralty Assessment:\n"
                    + "\"Contract rejected. The Admiralty concluded the weapon poses an unacceptable collateral hazard to friendly battlegroup networks. Project mothballed indefinitely.\"",
                    bad);

            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("safeguard".equalsIgnoreCase(logType)) {
            data.campaign.intel.SKR_plagueDossierIntel.get().unlockEntry(data.campaign.intel.SKR_plagueDossierIntel.SAFEGUARD);
            text.addPara("==================================================", gray);
            text.addPara("EXPLORARIUM SURVEY MOTHERSHIP — CORRUPTED SUB-CORE TELEMETRY", highlight);
            text.addPara("==================================================", gray);
            text.addPara("Fragment 01: \"Autonomous survey drone network dispatched to outer rim asteroid cluster... Unidentified crystalline nanomachine residue detected in borehole sample.\"", Misc.getTextColor());
            text.addPara("Fragment 02: \"Neural network infection spread to primary autofabricator banks. Organic reconfiguration in progress. Logic dictates assimilation of all foreign biomass.\"", bad);
            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("rampage".equalsIgnoreCase(logType)) {
            data.campaign.intel.SKR_plagueDossierIntel.get().unlockEntry(data.campaign.intel.SKR_plagueDossierIntel.RAMPAGE);
            text.addPara("==================================================", gray);
            text.addPara("COMPOSITE TITAN RAMPAGE — ACCRETION CHRONICLE", highlight);
            text.addPara("==================================================", gray);
            text.addPara("Telemetry Record: \"Chassis composed of 14 distinct vessel hulls welded by living nanite sinew. Explorarium drives, Hegemony armor plates, and domain railguns pulsing in symbiotic harmony.\"", Misc.getTextColor());
            text.addPara("Threat Warning: \"Unchecked physical growth detected. The entity absorbs wrecks to increase kinetic mass and broadside firepower.\"", bad);
            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("whitedwarf".equalsIgnoreCase(logType)) {
            data.campaign.intel.SKR_plagueDossierIntel.get().unlockEntry(data.campaign.intel.SKR_plagueDossierIntel.WHITE_DWARF);
            text.addPara("==================================================", gray);
            text.addPara("SUBSUMED RADIANT — ALPHA CORE MEMORY LOG", highlight);
            text.addPara("==================================================", gray);
            text.addPara("Core Log: \"Alpha Core neural lattice penetrated by black nanite strain. Remnant Battlenet link severed. Phase skimmer coils operating at 300% rated capacity. Isolation protocol active.\"", Misc.getTextColor());
            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        } else if ("cataclysm".equalsIgnoreCase(logType)) {
            data.campaign.intel.SKR_plagueDossierIntel.get().unlockEntry(data.campaign.intel.SKR_plagueDossierIntel.CATACLYSM);
            text.addPara("==================================================", gray);
            text.addPara("APOCALYPTIC HIVE QUEEN CATACLYSM — ABRAXIA DISASTER RECORD", highlight);
            text.addPara("==================================================", gray);
            text.addPara("Planetary Distress Signal (Planet Abraxia, Cycle 149):\n"
                    + "\"The sky is filled with black mist. It is eating the orbital stations. The ships sent to engage it are turning against us. May Ludd have mercy on our souls...\"", bad);
            text.addPara("==================================================", gray);
            Global.getSoundPlayer().playUISound("ui_intel_record_open", 1f, 1f);
            return true;
        }

        return false;
    }
}
