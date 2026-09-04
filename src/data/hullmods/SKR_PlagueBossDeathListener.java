package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import org.lwjgl.util.vector.Vector2f;

public class SKR_PlagueBossDeathListener implements AdvanceableListener, DamageListener, HullDamageAboutToBeTakenListener {

    private final ShipAPI ship;
    private boolean dead = false;

    public SKR_PlagueBossDeathListener(ShipAPI ship) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount) {
        if (dead) return;
        if (isDead(ship)) {
            onDeath();
        }
    }

    @Override
    public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
        if (dead) return;
        if (isDead(ship)) {
            onDeath();
        }
    }

    @Override
    public boolean notifyAboutToTakeHullDamage(Object paramObject, ShipAPI ship, Vector2f point, float amount) {
        if (dead) return false;
        if (ship != null && (ship.getHitpoints() <= amount || isDead(ship))) {
            onDeath();
        }
        return false;
    }

    private boolean isDead(ShipAPI s) {
        if (s == null) return true;
        return !s.isAlive() || s.isHulk() || s.getHitpoints() <= 0f;
    }

    private void onDeath() {
        if (dead) return;
        dead = true;

        CombatEngineAPI engine = Global.getCombatEngine();

        // If this is the parent/core ship (either marked with modules, or root parent station)
        if (ship.isShipWithModules() || ship.getParentStation() == null) {
            // Cascade destruction to all child modules
            if (ship.getChildModulesCopy() != null) {
                for (ShipAPI module : ship.getChildModulesCopy()) {
                    if (module != null) {
                        destroyEntity(module, engine, ship);
                    }
                }
            }
            // Destroy the parent ship
            destroyEntity(ship, engine, ship);
        } else {
            // Child module dying on its own
            destroyEntity(ship, engine, ship);
        }
    }

    private void destroyEntity(ShipAPI targetShip, CombatEngineAPI engine, ShipAPI damageSource) {
        if (targetShip == null) return;

        try {
            // Detach station links
            targetShip.setStationSlot(null);
            targetShip.setParentStation(null);
            targetShip.setShipWithModules(false);

            // Turn off systems, cloak, and AI
            targetShip.setShipAI(null);
            if (targetShip.getSystem() != null && targetShip.getSystem().isActive()) {
                targetShip.getSystem().deactivate();
            }
            if (targetShip.getPhaseCloak() != null && targetShip.getPhaseCloak().isActive()) {
                targetShip.getPhaseCloak().deactivate();
            }
            if (targetShip.getTravelDrive() != null && targetShip.getTravelDrive().isActive()) {
                targetShip.getTravelDrive().deactivate();
            }
            if (targetShip.getShield() != null) {
                targetShip.getShield().toggleOff();
            }

            // Disable all weapons
            for (WeaponAPI w : targetShip.getAllWeapons()) {
                if (w != null) {
                    w.disable(true);
                }
            }

            // Mark ship as disabled hulk
            targetShip.setHitpoints(0f);
            targetShip.makeLookDisabled();
            targetShip.setHulk(true);

            // Trigger ship explosion effects and notify combat engine
            if (engine != null) {
                try {
                    DamagingExplosionSpec spec = DamagingExplosionSpec.explosionSpecForShip(targetShip);
                    if (spec != null) {
                        engine.spawnDamagingExplosion(spec, targetShip, targetShip.getLocation());
                    }
                    engine.playShipExplosionSound(targetShip);
                } catch (Throwable t) {
                    // Ignore explosion visual failure if any
                }

                try {
                    engine.applyDamage(targetShip, targetShip.getLocation(), 1000000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, damageSource, false);
                } catch (Throwable t) {
                    // Ignore damage failure
                }
            }
        } catch (Throwable t) {
            // Safeguard against any unexpected API errors
        }
    }
}
