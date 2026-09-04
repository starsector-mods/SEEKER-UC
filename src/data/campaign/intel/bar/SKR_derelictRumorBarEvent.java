package data.campaign.intel.bar;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.util.Misc;
import data.campaign.intel.SKR_explorationRumorIntel;
import data.campaign.intel.SKR_explorationRumorIntel.RumorType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SKR_derelictRumorBarEvent extends BaseBarEvent {

    public enum OptionId {
        INIT,
        BUY_COORDINATES,
        LEAVE
    }

    private static class RumorData {
        RumorType type;
        String prompt;
        String dialogueIntro;
        String dialogueSuccess;
        int cost;
        SectorEntityToken targetEntity;
        StarSystemAPI targetSystem;
        String memoryKey;

        RumorData(RumorType type, String prompt, String dialogueIntro, String dialogueSuccess, int cost, SectorEntityToken targetEntity, StarSystemAPI targetSystem, String memoryKey) {
            this.type = type;
            this.prompt = prompt;
            this.dialogueIntro = dialogueIntro;
            this.dialogueSuccess = dialogueSuccess;
            this.cost = cost;
            this.targetEntity = targetEntity;
            this.targetSystem = targetSystem;
            this.memoryKey = memoryKey;
        }
    }

    private RumorData selectedRumor = null;

    public SKR_derelictRumorBarEvent() {
    }

    private void pickRandomRumor() {
        if (selectedRumor != null) return;

        List<RumorData> pool = new ArrayList<>();
        MemoryAPI sectorMem = Global.getSector().getMemoryWithoutUpdate();

        // 1. Titanic VII Rumor
        if (!sectorMem.is("$SKR_rumor_titanic_given", true)) {
            SectorEntityToken titanic = findTargetForRumor(RumorType.TITANIC);
            if (titanic != null && titanic.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.TITANIC,
                        "Listen to an old purser talking about the ill-fated Black Hole Cruise.",
                        "An elderly former cruise purser nurses a drink at the end of the counter, tracing the rim of their glass with a weathered finger.\n\n"
                                + "\"Twenty-five hundred souls,\" they murmur. \"The finest suites, champagne chilled in cryo-lockers, and front-row seats to watch a giant plunge into the event horizon. "
                                + "The CEO called her the Titanic VII. Said the Onslaught chassis was indestructible. When the secondary gravity compensators buckled... well, nobody made it back.\"\n\n"
                                + "They glance up at you, eyes sharp. \"I kept the maiden voyage navigational telemetry. If you have an expedition fleet capable of braving deep space, I'll part with the jump coordinates for a fair fee.\"",
                        "The purser transfers an encrypted navigational vector to your datapad.\n\n"
                                + "\"If you find her drifting in the accretion field, give the lost souls a respectful nod before you strip the cargo bays. And watch the gravity shear.\"",
                        8000,
                        titanic,
                        titanic.getStarSystem(),
                        "$SKR_rumor_titanic_given"
                ));
            }
        }

        // 2. Voulge Rumor
        if (!sectorMem.is("$SKR_rumor_voulge_given", true)) {
            SectorEntityToken voulge = findTargetForRumor(RumorType.VOULGE);
            if (voulge != null && voulge.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.VOULGE,
                        "Talk to a veteran scavenger reminiscing about the Silver Line Shipyards.",
                        "A seasoned scavenger with cybernetic eye implants flags you over, speaking in a low, measured voice.\n\n"
                                + "\"You ever heard of the Silver Line? Pre-Collapse shipyards that forged some of the fastest heavy battlecruisers in Domain history. "
                                + "Most were melted down during the First AI War, but my drone probes picked up an ancient energy signature matching a Voulge-class battlecruiser drifting near an inactive gate in the outer dark.\"\n\n"
                                + "The scavenger leans in. \"A prize like that is too big for my little tugboat. But for a modest finder's fee, the sector coordinates are yours.\"",
                        "The scavenger transmits the spectral scan files and jump-point coordinates to your TriPad.\n\n"
                                + "\"Happy hunting, Captain. If she's still intact, that frontal battery will tear right through modern armor.\"",
                        10000,
                        voulge,
                        voulge.getStarSystem(),
                        "$SKR_rumor_voulge_given"
                ));
            }
        }

        // 3. Siegfried Rumor
        if (!sectorMem.is("$SKR_rumor_siegfried_given", true)) {
            SectorEntityToken siegfried = findTargetForRumor(RumorType.SIEGFRIED);
            if (siegfried != null && siegfried.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.SIEGFRIED,
                        "Inquire with a retired Hegemony archivist discussing the legendary Gate-Keepers.",
                        "A retired Hegemony naval archivist adjusts their spectacles, reviewing a stack of declassified Domain-era sector defense schematics.\n\n"
                                + "\"Before the Collapse severed the jump network, the Domain deployed Siegfried-class Sentry Dreadnoughts to guard hyper-critical transit nodes. "
                                + "Massive, immovable colossi with their entire armament focused into a lethal forward cone. When the gates fell silent, their automated protocol was simple: enter cryo-stasis lockdown until relief fleets arrived.\"\n\n"
                                + "They tap the schematic. \"A long-range survey team logged an unresponsive Domain gate surrounded by unusual heavy armor telemetry. I've decoded the sector vector, if you're prepared to investigate.\"",
                        "The archivist hands you a decrypted tactical datachip.\n\n"
                                + "\"Approach with extreme caution, Captain. If its defensive subroutines misidentify your fleet as a hostile incursion, that forward battery will vaporize a cruiser in a single volley.\"",
                        15000,
                        siegfried,
                        siegfried.getStarSystem(),
                        "$SKR_rumor_siegfried_given"
                ));
            }
        }

        // 4. Dawn Rumor
        if (!sectorMem.is("$SKR_rumor_dawn_given", true)) {
            SectorEntityToken dawn = findTargetForRumor(RumorType.DAWN);
            if (dawn != null && dawn.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.DAWN,
                        "Listen to an independent materials engineer whispering about ceramic battlecruisers.",
                        "A freelance materials engineer glances nervously around the bar before leaning across the table toward you.\n\n"
                                + "\"Everyone knows the Domain relied heavily on cast durasteel and plasteel armor. But right before the Collapse, a top-secret materials division fabricated a demonstrator called 'Dawn' out of ultra-dense ceramic composite matrices. "
                                + "Half the mass of an Onslaught, yet twice as fast and impervious to localized thermal ablation.\"\n\n"
                                + "They lean closer. \"I intercepted sensor telemetry from an exploratory drone in the fringe. The spectral signature is unmistakable: pure ceramic resonance drifting in the dark.\"",
                        "The engineer beams as the transfer completes and slides a data drive across the table.\n\n"
                                + "\"The coordinates are locked in. If you can salvage that hull and integrate its phase-skimmer systems, you'll have one of the fastest capitals in the Sector.\"",
                        12000,
                        dawn,
                        dawn.getStarSystem(),
                        "$SKR_rumor_dawn_given"
                ));
            }
        }

        // 5. Onyx Rumor
        if (!sectorMem.is("$SKR_rumor_onyx_given", true)) {
            SectorEntityToken onyx = findTargetForRumor(RumorType.ONYX);
            if (onyx != null && onyx.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.ONYX,
                        "Eavesdrop on a terrified scout pilot rambling about the False Idol of the Remnants.",
                        "A jump-scout pilot with bloodshot eyes clutches their drink with both hands, trembling slightly as they recount their near-death escape to anyone who will listen.\n\n"
                                + "\"It wasn't a standard Nexus... it was something else. A rogue Remnant entity called NOVA, worshipping an ancient black battlecruiser like some kind of mechanical god. "
                                + "The ship—the Onyx—was cradled at the center of the fleet, pulsing with strange temporal distortions.\"\n\n"
                                + "The pilot notices your fleet uniform. \"You look like you've got real firepower. Take the star coordinates off my hands. Just knowing where that thing sleeps is giving me nightmares.\"",
                        "The scout frantically uploads the navigational route to your TriPad.\n\n"
                                + "\"Good luck, Captain. If you intend to pry that ship from NOVA's grip, bring enough capital ships to level a space station.\"",
                        12000,
                        onyx,
                        onyx.getStarSystem(),
                        "$SKR_rumor_onyx_given"
                ));
            }
        }

        // 6. Demeter Rumor
        if (!sectorMem.is("$SKR_rumor_demeter_given", true)) {
            SectorEntityToken demeter = findTargetForRumor(RumorType.DEMETER);
            if (demeter != null && demeter.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.DEMETER,
                        "Speak with a Tri-Tachyon logistics agent looking to trace a runaway agricultural fortress.",
                        "A well-dressed Tri-Tachyon logistics contractor sips a synthesized cocktail, scanning financial reports on an omni-pad.\n\n"
                                + "\"A brilliant—if completely unhinged—director took a Paragon battleship chassis and hollowed out its core to build a self-sustaining hydroponics paradise called the Demeter. "
                                + "When corporate auditors arrived to seize the asset, the director vented their shuttle into deep space and jumped into the Core rim.\"\n\n"
                                + "The contractor glances at you. \"The board has written off the hull, but I still hold the tracking transponder frequency. For a reasonable finder's cut, I can transmit its orbital path.\"",
                        "The contractor taps their pad, authorizing the telemetry transfer to your fleet's comm suite.\n\n"
                                + "\"Coordinates transmitted. If you secure the Demeter, you'll never have to worry about food shortages on your colonies again.\"",
                        10000,
                        demeter,
                        demeter.getStarSystem(),
                        "$SKR_rumor_demeter_given"
                ));
            }
        }

        // 7. Gawon Rumor
        if (!sectorMem.is("$SKR_rumor_gawon_given", true)) {
            SectorEntityToken gawon = findTargetForRumor(RumorType.GAWON);
            if (gawon != null && gawon.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.GAWON,
                        "Talk to an automated shipyard historian discussing an AI secession incident.",
                        "An independent tech-historian with deep knowledge of autonomous shipyard networks flags you over.\n\n"
                                + "\"During the early post-Collapse panic, an automated orbital shipyard on the fringe suffered a total AI secession. "
                                + "An Alpha-level core took control of the gantry cranes, welded together a custom battlecruiser named Gawon, blasted through the emergency containment doors, and vanished into deep space.\"\n\n"
                                + "They pull up a starmap. \"Historical telemetry logs from the shipyard's tracking array were recently decrypted. The vessel's deceleration trajectory points straight into a remote outer system.\"",
                        "The historian uploads the decrypted jump vector to your datapad.\n\n"
                                + "\"The Gawon was engineered without any human crew accommodations—pure mechanical efficiency and automated flux-routing. A true marvel of autonomous engineering.\"",
                        10000,
                        gawon,
                        gawon.getStarSystem(),
                        "$SKR_rumor_gawon_given"
                ));
            }
        }

        // 8. Quicksilver Rumor
        if (!sectorMem.is("$SKR_rumor_quicksilver_given", true)) {
            SectorEntityToken quicksilver = findTargetForRumor(RumorType.QUICKSILVER);
            if (quicksilver != null && quicksilver.getStarSystem() != null) {
                pool.add(new RumorData(
                        RumorType.QUICKSILVER,
                        "Listen to an eccentric weapons contractor discuss the Apollo Skunkworks prototype.",
                        "An eccentric weapons contractor with greasy overalls and a wide grin waves you over to a corner booth.\n\n"
                                + "\"You ever hear of Project Quicksilver? Apollo Skunkworks built a capital-grade sniper platform designed around experimental high-coherence beam directors and denial pulses. "
                                + "The Domain admiralty canceled the contract because its test fire literally fried every friendly sensor in the same zip code.\"\n\n"
                                + "The contractor chuckles. \"When the skunkworks was abandoned, the prototype was left locked in a mothballed staging depot in the outer rim. I have the depot's orbital coordinates.\"",
                        "The contractor transfers the encrypted depot coordinates to your TriPad with a wink.\n\n"
                                + "\"Take good care of her, Captain. Just remember to calibrate your fleet's sensor shielding before you fire that main cannon!\"",
                        12000,
                        quicksilver,
                        quicksilver.getStarSystem(),
                        "$SKR_rumor_quicksilver_given"
                ));
            }
        }

        if (!pool.isEmpty()) {
            Collections.shuffle(pool);
            selectedRumor = pool.get(0);
        }
    }

    private SectorEntityToken findTargetForRumor(RumorType type) {
        String shipId = type.getShipId();
        if (shipId == null) return null;

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system == null) continue;

            // 1. Check Entities
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity == null) continue;
                if (entity.getId() != null && (entity.getId().equalsIgnoreCase(shipId) || entity.getId().contains(shipId))) {
                    return entity;
                }
                if (entity.getCustomEntitySpec() != null && entity.getCustomEntitySpec().getId() != null
                        && entity.getCustomEntitySpec().getId().contains(shipId)) {
                    return entity;
                }
                if (entity.hasTag(shipId)) {
                    return entity;
                }
            }

            // 2. Check Fleets
            for (CampaignFleetAPI fleet : system.getFleets()) {
                if (fleet == null) continue;
                if (fleet.getFlagship() != null) {
                    String baseHull = fleet.getFlagship().getHullSpec().getBaseHullId();
                    String variantId = fleet.getFlagship().getVariant().getHullVariantId();
                    if (shipId.equalsIgnoreCase(baseHull) || (variantId != null && variantId.contains(shipId))) {
                        return fleet;
                    }
                }
                if (fleet.hasTag(shipId)) {
                    return fleet;
                }
            }
        }

        // 3. Fallback: Search for outer ruin/derelict/gate systems to associate exploration rumors
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            if (system == null) continue;
            if (system.hasTag(Tags.THEME_RUINS) || system.hasTag(Tags.THEME_DERELICT)
                    || system.hasTag(Tags.THEME_MISC) || system.hasTag(Tags.THEME_INTERESTING)) {
                if (system.getCenter() != null && !system.hasTag(Tags.THEME_CORE)) {
                    return system.getCenter();
                }
            }
        }

        return null;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        if (market == null || market.getFaction() == null || market.getFaction().isHostileTo(com.fs.starfarer.api.impl.campaign.ids.Factions.PLAYER)) {
            return false;
        }
        pickRandomRumor();
        return selectedRumor != null;
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        pickRandomRumor();
        if (selectedRumor == null) return;

        super.addPromptAndOption(dialog, memoryMap);
        dialog.getTextPanel().addPara(selectedRumor.prompt);
        dialog.getOptionPanel().addOption(selectedRumor.prompt, this);
    }

    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        pickRandomRumor();
        if (selectedRumor == null) {
            dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
            return;
        }

        text.addPara(selectedRumor.dialogueIntro);

        options.clearOptions();
        int credits = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get();
        if (credits >= selectedRumor.cost) {
            options.addOption(String.format("Pay %s credits for the navigational data", Misc.getDGSCredits(selectedRumor.cost)), OptionId.BUY_COORDINATES);
        } else {
            options.addOption(String.format("Pay %s credits (Insufficient credits)", Misc.getDGSCredits(selectedRumor.cost)), OptionId.BUY_COORDINATES);
            options.setEnabled(OptionId.BUY_COORDINATES, false);
        }
        options.addOption("Politely decline and step away", OptionId.LEAVE);
    }

    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData == OptionId.BUY_COORDINATES && selectedRumor != null) {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            cargo.getCredits().subtract(selectedRumor.cost);
            AddCreditsText(selectedRumor.cost);

            text.addPara(selectedRumor.dialogueSuccess);

            // Register intel
            SKR_explorationRumorIntel intel = new SKR_explorationRumorIntel(
                    selectedRumor.type,
                    selectedRumor.targetEntity,
                    selectedRumor.targetSystem,
                    selectedRumor.dialogueIntro
            );
            Global.getSector().getIntelManager().addIntel(intel, false, text);

            // Mark given in memory
            if (selectedRumor.memoryKey != null) {
                Global.getSector().getMemoryWithoutUpdate().set(selectedRumor.memoryKey, true);
            }

            options.clearOptions();
            options.addOption("Leave", OptionId.LEAVE);
        } else if (optionData == OptionId.LEAVE) {
            noContinue = true;
            done = true;
        }
    }

    private void AddCreditsText(int credits) {
        text.addPara(String.format("Paid %s credits.", Misc.getDGSCredits(credits)), Misc.getNegativeHighlightColor());
    }
}
