package data.scripts.weapons;

import com.fs.starfarer.api.AnimationAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import data.scripts.util.MagicUI;
import data.scripts.util.SKR_graphicLibEffects;
import static data.scripts.util.SKR_txt.txt;
import java.awt.Color;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_balisongEffect implements EveryFrameWeaponEffectPlugin {
    
    private float overcharge=0, totalCharge=0;
    private final String ID="SKR_overcharge", leftSlot = "LEFT", rightSlot = "RIGHT", leftModule = "MODULE_LEFT", rightModule = "MODULE_RIGHT";
    private final String CHRG = txt("charge");
    
    private final float FOLDED_ANGLE=-15;
    private final Vector2f FOLDED_POS=new Vector2f(25,16);
        
    //modules overlap
    private Map<WeaponAPI, Vector2f> MODULE_POS=new WeakHashMap<>();
    private Map<WeaponAPI, Vector2f> MODULE_OFST=new WeakHashMap<>();
    
    private boolean runOnce=false, activated=false, fullCharge=false, inbound=false, SHADER=false;
    private ShipAPI ship, moduleLeft, moduleRight;
    private ShipSystemAPI system;
    private WeaponAPI weaponLeft, weaponRight, decoCharge;
    private WeaponSlotAPI slotLeft, slotRight;
    private AnimationAPI charge;
    private float delay=0, fold=0, timer=0, systemExpertise=1;

    private final IntervalUtil animation = new IntervalUtil(0.05f,0.05f);
    private final String zapSprite="zap_0";
    private final int zapFrames=8;    
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        
        if (engine == null || engine.isPaused()) return;
        
        //SETUP        
        if(!runOnce){
            if (weapon == null || weapon.getShip() == null) return;
            //distortion + light effects
            SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
            
            runOnce=true;            
            ship=weapon.getShip();
            system=ship.getSystem();
            ship.getCustomData().put("SKR_balisong_systemEffect", this);
            
            if (ship.getMutableStats() != null && ship.getMutableStats().getSystemRegenBonus() != null) {
                systemExpertise=ship.getMutableStats().getSystemRegenBonus().getBonusMult();
            }
            
            //module shenanigans
            ship.ensureClonedStationSlotSpec();
            
            decoCharge=weapon;
            charge=weapon.getAnimation();
            
            if (ship.getAllWeapons() != null) {
                for(WeaponAPI w : ship.getAllWeapons()){
                    if (w == null || w.getSlot() == null || w.getSlot().getId() == null) continue;
                    switch (w.getSlot().getId()){
                        case leftSlot:
                            weaponLeft=w;
                            MODULE_POS.put(w,w.getLocation());
                            MODULE_OFST.put(w,new Vector2f());
                            break;
                        case rightSlot:
                            weaponRight=w;
                            MODULE_POS.put(w,w.getLocation());
                            MODULE_OFST.put(w,new Vector2f());
                            break;
                        default:
                            break;
                    }
                }
            }
            if (ship.getChildModulesCopy() != null) {
                for(ShipAPI s : ship.getChildModulesCopy()){
                    if (s == null || s.getStationSlot() == null || s.getStationSlot().getId() == null) continue;
                    switch(s.getStationSlot().getId()){
                        case leftModule:
                            moduleLeft=s;
                            slotLeft=s.getStationSlot();
                            slotLeft.setAngle(0);
                            break;
                        case rightModule:
                            moduleRight=s;
                            slotRight=s.getStationSlot();
                            slotRight.setAngle(0);
                            break;
                        default:
                            break;
                    }
                }
            }
            
            activated=false;
            overcharge=0;
            
            if(moduleLeft!=null && slotLeft!=null){
                moduleAnimation(ship, moduleLeft, slotLeft, weaponLeft, 1, fold);
                syncModulePosition(ship, moduleLeft, slotLeft, 1, fold);
            } else if (weaponLeft != null) {
                weaponLeft.disable(true);
            }
            
            if(moduleRight!=null && slotRight!=null){
                moduleAnimation(ship, moduleRight, slotRight, weaponRight, -1, fold);
                syncModulePosition(ship, moduleRight, slotRight, -1, fold);
            } else if (weaponRight != null) {
                weaponRight.disable(true);
            }
            
            try {
                engine.updateStationModuleLocations(ship);
            } catch (Throwable ignored) {}
            
            fold=0;
        }

        if (moduleLeft != null && (moduleLeft.isHulk() || moduleLeft.getHitpoints() <= 0)) {
            moduleLeft = null;
        }
        if (moduleRight != null && (moduleRight.isHulk() || moduleRight.getHitpoints() <= 0)) {
            moduleRight = null;
        }

        if (moduleLeft == null && ship.getChildModulesCopy() != null) {
            for (ShipAPI s : ship.getChildModulesCopy()) {
                if (s != null && s.getStationSlot() != null && leftModule.equals(s.getStationSlot().getId())) {
                    if (!s.isHulk() && s.getHitpoints() > 0) {
                        setModuleLeft(s);
                        break;
                    }
                }
            }
        }
        if (moduleLeft == null && ship.getCustomData().containsKey("SKR_balisong_moduleLeft")) {
            ShipAPI m = (ShipAPI) ship.getCustomData().get("SKR_balisong_moduleLeft");
            if (m != null && !m.isHulk() && m.getHitpoints() > 0) {
                setModuleLeft(m);
            }
        }
        if (moduleRight == null && ship.getChildModulesCopy() != null) {
            for (ShipAPI s : ship.getChildModulesCopy()) {
                if (s != null && s.getStationSlot() != null && rightModule.equals(s.getStationSlot().getId())) {
                    if (!s.isHulk() && s.getHitpoints() > 0) {
                        setModuleRight(s);
                        break;
                    }
                }
            }
        }
        if (moduleRight == null && ship.getCustomData().containsKey("SKR_balisong_moduleRight")) {
            ShipAPI m = (ShipAPI) ship.getCustomData().get("SKR_balisong_moduleRight");
            if (m != null && !m.isHulk() && m.getHitpoints() > 0) {
                setModuleRight(m);
            }
        }
        
        ///////////////////////////////////
        //                               //
        //      TRAVEL DRIVE CHECK       //
        //                               //
        /////////////////////////////////// 
        
        if(ship.getTravelDrive() != null && ship.getTravelDrive().isActive()){
            
            inbound=true;
            
            if(moduleLeft!=null && slotLeft!=null){
                moduleAnimation(ship, moduleLeft, slotLeft, weaponLeft, 1, fold);
                syncModulePosition(ship, moduleLeft, slotLeft, 1, fold);
            }        
            if(moduleRight!=null && slotRight!=null){
                moduleAnimation(ship, moduleRight, slotRight, weaponRight, -1, fold);
                syncModulePosition(ship, moduleRight, slotRight, -1, fold);
            }
            return;
        } else if(inbound){
            inbound=false;
            if (ship.getLocation() != null && ship.getVelocity() != null) {
                Global.getSoundPlayer().playSound("SKR_balisong_close", 1, 1, ship.getLocation(), ship.getVelocity());
            }
        }
                
        ///////////////////////////////////
        //                               //
        //          OVERCHARGE           //
        //                               //
        ///////////////////////////////////
        
        if(system != null && system.isActive()){
            totalCharge += amount*systemExpertise;
            overcharge= Math.min(totalCharge, 10);
            
            //sound effects
            if(overcharge==10){
                if(!fullCharge){
                    fullCharge=true;                    
                    Global.getSoundPlayer().playSound("SKR_balisongSystem_maxCharge", 1, 0.7f, ship.getLocation(), ship.getVelocity());
                }
                Global.getSoundPlayer().playLoop("SKR_balisongSystem_charged", ship, 1, 1, ship.getLocation(), ship.getVelocity());
            } else{
                Global.getSoundPlayer().playLoop("SKR_balisongSystem_charging", ship, 0.75f+overcharge/40, Math.min(1,overcharge), ship.getLocation(), ship.getVelocity());
            }
            
            Color ui = new Color(255,128,0);
            if(overcharge==10){
                ui=new Color(255,0,0);
            }
            int maxA = weaponLeft != null ? weaponLeft.getMaxAmmo() : 0;
            int specMaxA = (weaponLeft != null && weaponLeft.getSpec() != null) ? weaponLeft.getSpec().getMaxAmmo() : 0;
            MagicUI.drawInterfaceStatusBar(ship, overcharge/10, ui, null, 0, CHRG, (int) Math.min(maxA, totalCharge/10 * specMaxA));
            
            //charge visual effect            
            timer+=amount;
            animation.advance(amount);

            if(timer>(15-overcharge)/30){
                timer=0;
                if(Math.random()<0.75f){
                    zap(engine, MathUtils.getRandomPointInCircle(new Vector2f(), 50+5*overcharge), false);
                }
            }
            
            //animation
            if(animation.intervalElapsed()){
                int frame = charge.getFrame();
                frame++;
                if(frame>=charge.getNumFrames()-1){
                    frame=1;
                }
                charge.setFrame(frame);
                decoCharge.getSprite().setColor(new Color(1,1,1,MagicAnim.smoothNormalizeRange(overcharge/10,0,0.5f)));
                
                if(overcharge==10){
                    if(Math.random()<0.75f){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(ship.getLocation(), 50),
                                ship.getVelocity(),
                                10,
                                1,
                                0.2f,
                                Color.PINK
                        );
                    }
                    if(Math.random()<0.5f){
                        engine.addHitParticle(
                                MathUtils.getRandomPointInCircle(ship.getLocation(), 25),
                                ship.getVelocity(),
                                5,
                                1,
                                0.1f,
                                Color.WHITE
                        );
                    }
                    if(Math.random()<0.25f){
                        zap(engine, MathUtils.getRandomPointInCircle(new Vector2f(), 50+10*overcharge), true);
                    }
                }
            }
            
            //weapons forced folding
            fold=Math.min(1, fold+amount);
                    
            //Charging stats
            applyCharge(ship,Math.max(0, Math.min(1,overcharge-9)),ID);
            
        } else if (overcharge>0){
            overcharge= Math.max(0, overcharge-amount);
            if(ship.getFluxTracker().isOverloadedOrVenting()){
                overcharge= Math.max(0, overcharge-2*amount);
            }
            MagicUI.drawInterfaceStatusBar(ship, overcharge/10, Color.CYAN, null, 0, CHRG, 0000);
            
            //loop sound
            Global.getSoundPlayer().playLoop("SKR_balisongSystem_active", ship, 1f+overcharge/40, Math.min(1,overcharge/2), ship.getLocation(), ship.getVelocity());
            
            //release module weapons + free ammo
            if(!activated){
                activated=true;
                
                if(weaponLeft!=null){
                    weaponLeft.setForceDisabled(false);
                    weaponLeft.setRemainingCooldownTo(0);
                    weaponLeft.setAmmo(weaponLeft.getMaxAmmo());
                }
                if(weaponRight!=null){
                    weaponRight.setForceDisabled(false);
                    weaponRight.setRemainingCooldownTo(0);
                    weaponRight.setAmmo(weaponRight.getMaxAmmo());
                }
            
                //activation sound
                Global.getSoundPlayer().playSound("SKR_balisongSystem_activation", 1, 1, ship.getLocation(), ship.getVelocity());
                
                //burst animation
                charge.setFrame(11);
                
                //warp effect
                if(SHADER){
                    SKR_graphicLibEffects.balisongRing(ship, overcharge);
                }
                
                //burst effect
                engine.addHitParticle(
                        ship.getLocation(),
                        ship.getVelocity(),
                        500,
                        1,
                        0.5f,
                        Color.PINK
                );
                engine.addHitParticle(
                        ship.getLocation(),
                        ship.getVelocity(),
                        250,
                        1,
                        0.25f,
                        Color.WHITE
                );
                
                for(int i=0; i<(int)(overcharge*2); i++){
                    zap(engine, MathUtils.getRandomPointInCircle(new Vector2f(), 50+15*overcharge), true);
                }
                for(int i=0; i<3; i++){
                    engine.addSmoothParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            200+50*i,
                            0.5f,
                            0.1f,
                            new Color(255,100,200)
                    );
                }
                
                for(int i=0; i<5; i++){
                    engine.addSmoothParticle(
                            ship.getLocation(),
                            ship.getVelocity(),
                            100+25*i,
                            1f,
                            0.05f,
                            Color.WHITE
                    );
                }
                
                if(moduleLeft!=null){
                    moduleLeft.setJitter(ship, new Color(255,100,150,180), 0.5f, 3, 5f); //subtle wing surge
                }
                if(moduleRight!=null){
                    moduleRight.setJitter(ship, new Color(255,100,150,180), 0.5f, 3, 5f); //subtle wing surge
                }
            }
            
            //residual lighting
            timer+=amount;
            if(timer>(overcharge)/5){
                timer=0;
                if(Math.random()<0.5f){
                    zap(engine, MathUtils.getRandomPointInCircle(new Vector2f(), 50+5*overcharge), false);
                }
            }
            
            if(moduleLeft!=null){
                moduleLeft.setJitterUnder(ship, new Color(200,50,100,100), 0.1f+overcharge/20f, 2, 3f); //subtle underglow
            }
            if(moduleRight!=null){
                moduleRight.setJitterUnder(ship, new Color(200,50,100,100), 0.1f+overcharge/20f, 2, 3f); //subtle underglow
            }
            
            if(fold==1){
                Global.getSoundPlayer().playSound("SKR_balisong_open", 1, 1, ship.getLocation(), ship.getVelocity());
            }
            
            //weapons unfolding and overlap
            fold=Math.max(0, fold-amount);
            
            //Overcharged stats
            applyOvercharge(ship,Math.max(0, Math.min(1,overcharge)),ID);
            
        } else {
            MagicUI.drawInterfaceStatusBar(ship, 0, Color.CYAN, null, 0, CHRG, 0);
            if(activated){
                totalCharge=0;
                fullCharge=false;
                activated = false;
                //Cancel mutable stats
                unapplyCharge(ship,ID);
                system.setAmmo(1);
                charge.setFrame(0);
            }
            
            //weapons folding if empty
            int ammoL = weaponLeft != null ? weaponLeft.getAmmo() : 0;
            int ammoR = weaponRight != null ? weaponRight.getAmmo() : 0;
            if(ammoL==0 && ammoR==0){
                delay+=amount;
                if(delay>1){
                    if(fold==0){
                        Global.getSoundPlayer().playSound("SKR_balisong_close", 1, 1, ship.getLocation(), ship.getVelocity());
                    }
                    fold=Math.min(1, fold+amount);
                } else {                    
                    fold=Math.max(0, fold-amount);
                }
            } else {
                delay=0;
                fold=Math.max(0, fold-amount);
            }            
        }
        
        ///////////////////////////////////
        //                               //
        //        MODULES FACING         //
        //                               //
        ///////////////////////////////////
        
        if(moduleLeft!=null && slotLeft!=null){
            moduleAnimation(ship, moduleLeft, slotLeft, weaponLeft, 1, fold);
            syncModulePosition(ship, moduleLeft, slotLeft, 1, fold);
            if(fold>0 && weaponLeft!=null){
                weaponLeft.setRemainingCooldownTo(1f);
            }
        }
        
        if(moduleRight!=null && slotRight!=null){
            moduleAnimation(ship, moduleRight, slotRight, weaponRight, -1, fold);
            syncModulePosition(ship, moduleRight, slotRight, -1, fold);
            if(fold>0 && weaponRight!=null){
                weaponRight.setRemainingCooldownTo(1f);
            }
        }

        if (moduleLeft != null || moduleRight != null) {
            try {
                engine.updateStationModuleLocations(ship);
            } catch (Throwable ignored) {}
        }
    }
    
    ///////////////////////////////////
    //                               //
    //       MODULES ANIMATION       //
    //                               //
    ///////////////////////////////////
    
    private void moduleAnimation(ShipAPI ship, ShipAPI module, WeaponSlotAPI slot, WeaponAPI weapon, Integer side, float folding){
        if (ship == null || module == null || slot == null) return;
        float wAngle = weapon != null ? weapon.getCurrAngle() : ship.getFacing();
        slot.setAngle(side*FOLDED_ANGLE+(1-MagicAnim.smoothNormalizeRange(folding,0,1))*(MathUtils.getShortestRotation(ship.getFacing(), wAngle) - side*FOLDED_ANGLE));

        if (module.getModuleOffset() != null) {
            module.getModuleOffset().set(
                    new Vector2f(
                            FOLDED_POS.x*MagicAnim.smoothNormalizeRange(folding,0,0.75f),
                            FOLDED_POS.y*(side*MagicAnim.smoothNormalizeRange(folding,0.25f,1))
                    )
            );
        }
    }
    
    ///////////////////////////////////
    //                               //
    //             ZAPS              //
    //                               //
    ///////////////////////////////////
    
    private void zap (CombatEngineAPI engine, Vector2f offset, boolean violent){
        if (ship == null || ship.getLocation() == null) return;
        int chooser = new Random().nextInt(zapFrames);
        float rand = 0.5f * (float) Math.random() + 0.5f;
        Vector2f vel = new Vector2f(offset);
        float boost = 0;
        if (violent) {
            vel.scale(0.66f);
            boost += 5 + (float) Math.random() * 5;
        } else {
            vel.scale(0.25f);
        }

        MagicRender.objectspace(
                Global.getSettings().getSprite("fx", zapSprite + chooser),
                ship,
                offset,
                new Vector2f(vel),
                new Vector2f(48 * rand + boost, 48 * rand + boost),
                new Vector2f((float) Math.random() * 20, (float) Math.random() * 20),
                (float) Math.random() * 360,
                (float) (Math.random() - 0.5f) * 50,
                false,
                new Color(255, 100, 155),
                true,
                0,
                0.25f + (float) Math.random() * 0.1f,
                0.25f,
                true
        );

        Vector2f loc = new Vector2f(offset);
        VectorUtils.rotate(loc, ship.getFacing(), loc);
        Vector2f.add(loc, ship.getLocation(), loc);
        Vector2f velWorld = (ship.getVelocity() != null) ? new Vector2f(ship.getVelocity()) : new Vector2f();
        Vector2f.add(vel, velWorld, velWorld);

        engine.addHitParticle(
                loc,
                velWorld,
                50 * rand + boost * 2,
                1,
                (float) Math.random() * 0.1f,
                new Color(255, 100, 155)
        );
    }
    
    ///////////////////////////////////
    //                               //
    //             BUFFS             //
    //                               //
    ///////////////////////////////////
    
    private void applyCharge (ShipAPI ship, float intensity, String id){
        if (ship == null || ship.getMutableStats() == null) return;
        ship.getMutableStats().getTimeMult().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getDeceleration().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getAcceleration().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getMaxTurnRate().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id, 20f * intensity);        
        ship.getMutableStats().getMaxSpeed().modifyFlat(id, 20f * intensity);
    }
    
    private void applyOvercharge (ShipAPI ship, float intensity, String id){
        if (ship == null || ship.getMutableStats() == null) return;
        ship.getMutableStats().getTimeMult().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getDeceleration().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getAcceleration().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getMaxTurnRate().modifyPercent(id, 20f * intensity);
        ship.getMutableStats().getTurnAcceleration().modifyPercent(id, 20f * intensity);        
        ship.getMutableStats().getMaxSpeed().modifyFlat(id, 20f * intensity);
        
        ship.getMutableStats().getEnergyRoFMult().modifyPercent(id, 10f * intensity);
        ship.getMutableStats().getBallisticRoFMult().modifyPercent(id, 10f * intensity);
        ship.getMutableStats().getMissileRoFMult().modifyPercent(id, 10f * intensity);
        
        ship.getMutableStats().getEnergyWeaponFluxCostMod().modifyPercent(id, -5f * intensity);
        ship.getMutableStats().getBallisticWeaponFluxCostMod().modifyPercent(id, -5f * intensity);
        ship.getMutableStats().getMissileWeaponFluxCostMod().modifyPercent(id, -5f * intensity);
    }
    
    private void unapplyCharge (ShipAPI ship, String id){
        if (ship == null || ship.getMutableStats() == null) return;
        ship.getMutableStats().getTimeMult().unmodify(id);
        ship.getMutableStats().getDeceleration().unmodify(id);
        ship.getMutableStats().getAcceleration().unmodify(id);
        ship.getMutableStats().getMaxTurnRate().unmodify(id);
        ship.getMutableStats().getTurnAcceleration().unmodify(id);        
        ship.getMutableStats().getMaxSpeed().unmodify(id);
        
        ship.getMutableStats().getEnergyRoFMult().unmodify(id);
        ship.getMutableStats().getBallisticRoFMult().unmodify(id);
        ship.getMutableStats().getMissileRoFMult().unmodify(id);
        
        ship.getMutableStats().getEnergyWeaponFluxCostMod().unmodify(id);
        ship.getMutableStats().getBallisticWeaponFluxCostMod().unmodify(id);
        ship.getMutableStats().getMissileWeaponFluxCostMod().unmodify(id);
    }
    
    public static void syncModulePosition(ShipAPI ship, ShipAPI module, WeaponSlotAPI slot, int side, float fold) {
        if (ship == null || module == null || ship.getLocation() == null) return;
        try {
            float slotAngle = (slot != null) ? slot.getAngle() : side * -15f * fold;

            Vector2f offset = module.getModuleOffset();
            if (offset == null) {
                offset = new Vector2f(
                        25f * MagicAnim.smoothNormalizeRange(fold, 0, 0.75f),
                        16f * (side * MagicAnim.smoothNormalizeRange(fold, 0.25f, 1))
                );
            }

            Vector2f slotLoc = (slot != null && slot.getLocation() != null)
                    ? slot.getLocation()
                    : new Vector2f(0f, 60f * side);

            // Compute local transform matching Starsector's internal negate math:
            // worldPos = ship.location + rotate(slotLocation - rotate(offset, slotAngle), ship.facing)
            Vector2f offRot = new Vector2f(offset);
            VectorUtils.rotate(offRot, slotAngle, offRot);
            Vector2f localPos = new Vector2f(slotLoc.x - offRot.x, slotLoc.y - offRot.y);

            VectorUtils.rotate(localPos, ship.getFacing(), localPos);
            Vector2f worldPos = Vector2f.add(ship.getLocation(), localPos, null);

            if (module.getLocation() != null) {
                module.getLocation().set(worldPos);
            }
            module.setFacing(ship.getFacing() + slotAngle);

            if (module.getVelocity() != null && ship.getVelocity() != null) {
                float angVelRad = (float) Math.toRadians(ship.getAngularVelocity());
                Vector2f r = Vector2f.sub(worldPos, ship.getLocation(), null);
                module.getVelocity().set(
                        ship.getVelocity().x - angVelRad * r.y,
                        ship.getVelocity().y + angVelRad * r.x
                );
            }
            module.setAngularVelocity(ship.getAngularVelocity());
        } catch (Throwable ignored) {}
    }
    public float getOvercharge() {
        return overcharge;
    }

    public float getTotalCharge() {
        return totalCharge;
    }

    public ShipAPI getModuleLeft() {
        return moduleLeft;
    }

    public void setModuleLeft(ShipAPI moduleLeft) {
        this.moduleLeft = moduleLeft;
        if (moduleLeft != null) {
            if (moduleLeft.getStationSlot() != null) {
                this.slotLeft = moduleLeft.getStationSlot();
            } else if (ship != null && ship.getHullSpec() != null) {
                this.slotLeft = ship.getHullSpec().getWeaponSlotAPI(leftModule);
            }
        }
    }

    public WeaponSlotAPI getSlotLeft() {
        return slotLeft;
    }

    public ShipAPI getModuleRight() {
        return moduleRight;
    }

    public void setModuleRight(ShipAPI moduleRight) {
        this.moduleRight = moduleRight;
        if (moduleRight != null) {
            if (moduleRight.getStationSlot() != null) {
                this.slotRight = moduleRight.getStationSlot();
            } else if (ship != null && ship.getHullSpec() != null) {
                this.slotRight = ship.getHullSpec().getWeaponSlotAPI(rightModule);
            }
        }
    }

    public WeaponSlotAPI getSlotRight() {
        return slotRight;
    }

    public float getFold() {
        return fold;
    }
}
