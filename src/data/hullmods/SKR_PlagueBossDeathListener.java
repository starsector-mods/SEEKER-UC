package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;

/**
 * Ensures child modules are cleanly destroyed when the core Plaguebearer boss falls,
 * without prematurely killing the boss when individual modules take damage.
 */
public class SKR_PlagueBossDeathListener implements AdvanceableListener {

    private final ShipAPI ship;
    private boolean dead = false;

    public SKR_PlagueBossDeathListener(ShipAPI ship) {
        this.ship = ship;
    }

    @Override
    public void advance(float amount) {
        if (dead || ship == null) return;

        // Station modules must NEVER trigger cascade destruction on the parent core ship!
        if (ship.isStationModule()) {
            return;
        }

        // Only cascade when the parent core ship is actually dead or a hulk
        if (!ship.isAlive() || ship.isHulk() || ship.getHitpoints() <= 0f) {
            dead = true;
            CombatEngineAPI engine = Global.getCombatEngine();
            if (engine == null) return;

            // Cascade destruction to remaining child modules so they don't linger as unkillable zombies
            if (ship.isShipWithModules() && ship.getChildModulesCopy() != null) {
                for (ShipAPI module : ship.getChildModulesCopy()) {
                    if (module != null && module.isAlive() && !module.isHulk()) {
                        try {
                            engine.applyDamage(module, module.getLocation(), 1000000f, DamageType.HIGH_EXPLOSIVE, 0f, true, false, ship, false);
                        } catch (Throwable t) {
                            // Ignore cleanup errors
                        }
                    }
                }
            }
        }
    }
}
