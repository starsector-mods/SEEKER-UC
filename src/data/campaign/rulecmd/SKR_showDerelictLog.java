package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
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
        }

        return false;
    }
}
