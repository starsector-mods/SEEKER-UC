package data.campaign.intel.bar;

import com.fs.starfarer.api.Global;
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
import java.awt.Color;
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

        RumorData(RumorType type, String prompt, String dialogueIntro, String dialogueSuccess, int cost, SectorEntityToken targetEntity, StarSystemAPI targetSystem) {
            this.type = type;
            this.prompt = prompt;
            this.dialogueIntro = dialogueIntro;
            this.dialogueSuccess = dialogueSuccess;
            this.cost = cost;
            this.targetEntity = targetEntity;
            this.targetSystem = targetSystem;
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
            SectorEntityToken titanic = findEntityWithTag("CIV_titanic");
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
                        titanic.getStarSystem()
                ));
            }
        }

        // 2. Voulge Rumor
        if (!sectorMem.is("$SKR_rumor_voulge_given", true)) {
            SectorEntityToken voulge = findEntityWithTag("SKR_voulge");
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
                        voulge.getStarSystem()
                ));
            }
        }

        if (!pool.isEmpty()) {
            Collections.shuffle(pool);
            selectedRumor = pool.get(0);
        }
    }

    private SectorEntityToken findEntityWithTag(String entityIdOrTag) {
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (SectorEntityToken entity : system.getAllEntities()) {
                if (entity.getId() != null && entity.getId().contains(entityIdOrTag)) {
                    return entity;
                }
                if (entity.getCustomEntitySpec() != null && entity.getCustomEntitySpec().getId() != null
                        && entity.getCustomEntitySpec().getId().contains(entityIdOrTag)) {
                    return entity;
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
            if (selectedRumor.type == RumorType.TITANIC) {
                Global.getSector().getMemoryWithoutUpdate().set("$SKR_rumor_titanic_given", true);
            } else if (selectedRumor.type == RumorType.VOULGE) {
                Global.getSector().getMemoryWithoutUpdate().set("$SKR_rumor_voulge_given", true);
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
