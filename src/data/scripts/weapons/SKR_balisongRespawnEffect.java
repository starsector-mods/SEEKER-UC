package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatFleetManagerAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import data.scripts.util.MagicAnim;
import data.scripts.util.MagicRender;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class SKR_balisongRespawnEffect implements EveryFrameWeaponEffectPlugin {

    private boolean runOnce = false;
    private ShipAPI ship;
    private WeaponAPI weaponLeft, weaponRight;
    private WeaponSlotAPI slotLeft, slotRight;
    private ShipAPI lastLeft, lastRight;
    private SKR_balisongEffect systemEffect;

    private boolean hasInitializedLeft = false;
    private boolean hasInitializedRight = false;

    private float respawnTimerLeft = -1f;
    private float respawnTimerRight = -1f;

    private static final float RESPAWN_DELAY = 60f;

    // =========================================================================
    // HARDCODED DOCKED MODULE TRANSFORMS (relative to Balisong ship center)
    // =========================================================================
    // When the station modules are fully docked/folded against the hull:
    // Left Wing:  local offset (-28.29f,  51.02f), facing relative to ship: -15.0f
    // Right Wing: local offset (-28.29f, -51.02f), facing relative to ship:  15.0f
    public static final Vector2f DOCKED_LOCAL_LEFT = new Vector2f(-28.29f, 51.02f);
    public static final Vector2f DOCKED_LOCAL_RIGHT = new Vector2f(-28.29f, -51.02f);
    public static final float DOCKED_ANGLE_LEFT = -15.0f;
    public static final float DOCKED_ANGLE_RIGHT = 15.0f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine == null || engine.isPaused()) return;

        if (!runOnce) {
            if (weapon == null || weapon.getShip() == null) return;
            runOnce = true;
            ship = weapon.getShip();

            if (ship.getAllWeapons() != null) {
                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w == null || w.getSlot() == null || w.getSlot().getId() == null) continue;
                    String slotId = w.getSlot().getId();
                    if ("LEFT".equals(slotId)) {
                        weaponLeft = w;
                    } else if ("RIGHT".equals(slotId)) {
                        weaponRight = w;
                    } else if ("SYSTEM".equals(slotId)) {
                        if (w.getEffectPlugin() instanceof SKR_balisongEffect) {
                            systemEffect = (SKR_balisongEffect) w.getEffectPlugin();
                        }
                    }
                }
            }

            if (slotLeft == null && ship.getHullSpec() != null) {
                slotLeft = ship.getHullSpec().getWeaponSlotAPI("MODULE_LEFT");
            }
            if (slotRight == null && ship.getHullSpec() != null) {
                slotRight = ship.getHullSpec().getWeaponSlotAPI("MODULE_RIGHT");
            }
        }

        // Resolve systemEffect fallback
        if (systemEffect == null && ship != null) {
            if (ship.getCustomData().containsKey("SKR_balisong_systemEffect")) {
                systemEffect = (SKR_balisongEffect) ship.getCustomData().get("SKR_balisong_systemEffect");
            } else if (ship.getAllWeapons() != null) {
                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w != null && "SYSTEM".equals(w.getSlot().getId()) && w.getEffectPlugin() instanceof SKR_balisongEffect) {
                        systemEffect = (SKR_balisongEffect) w.getEffectPlugin();
                        break;
                    }
                }
            }
        }

        // Dynamic module discovery: ensure living modules are recognized and never misidentified as dead hulks
        if (lastLeft == null && respawnTimerLeft <= 0f && ship != null) {
            if (ship.getChildModulesCopy() != null) {
                for (ShipAPI s : ship.getChildModulesCopy()) {
                    if (s != null && s.getStationSlot() != null && "MODULE_LEFT".equals(s.getStationSlot().getId())) {
                        if (!s.isHulk() && s.getHitpoints() > 0) {
                            lastLeft = s;
                            slotLeft = s.getStationSlot();
                            hasInitializedLeft = true;
                            respawnTimerLeft = -1f;
                            ship.getCustomData().put("SKR_balisong_moduleLeft", s);
                            if (systemEffect != null) systemEffect.setModuleLeft(s);
                            if (weaponLeft != null) {
                                weaponLeft.setForceDisabled(false);
                                weaponLeft.setRemainingCooldownTo(0f);
                            }
                            break;
                        }
                    }
                }
            }
            if (lastLeft == null && ship.getCustomData().containsKey("SKR_balisong_moduleLeft")) {
                ShipAPI m = (ShipAPI) ship.getCustomData().get("SKR_balisong_moduleLeft");
                if (m != null && !m.isHulk() && m.getHitpoints() > 0) {
                    lastLeft = m;
                    if (m.getStationSlot() != null) slotLeft = m.getStationSlot();
                    hasInitializedLeft = true;
                    respawnTimerLeft = -1f;
                    if (systemEffect != null) systemEffect.setModuleLeft(m);
                    if (weaponLeft != null) {
                        weaponLeft.setForceDisabled(false);
                        weaponLeft.setRemainingCooldownTo(0f);
                    }
                }
            }
        }

        if (lastRight == null && respawnTimerRight <= 0f && ship != null) {
            if (ship.getChildModulesCopy() != null) {
                for (ShipAPI s : ship.getChildModulesCopy()) {
                    if (s != null && s.getStationSlot() != null && "MODULE_RIGHT".equals(s.getStationSlot().getId())) {
                        if (!s.isHulk() && s.getHitpoints() > 0) {
                            lastRight = s;
                            slotRight = s.getStationSlot();
                            hasInitializedRight = true;
                            respawnTimerRight = -1f;
                            ship.getCustomData().put("SKR_balisong_moduleRight", s);
                            if (systemEffect != null) systemEffect.setModuleRight(s);
                            if (weaponRight != null) {
                                weaponRight.setForceDisabled(false);
                                weaponRight.setRemainingCooldownTo(0f);
                            }
                            break;
                        }
                    }
                }
            }
            if (lastRight == null && ship.getCustomData().containsKey("SKR_balisong_moduleRight")) {
                ShipAPI m = (ShipAPI) ship.getCustomData().get("SKR_balisong_moduleRight");
                if (m != null && !m.isHulk() && m.getHitpoints() > 0) {
                    lastRight = m;
                    if (m.getStationSlot() != null) slotRight = m.getStationSlot();
                    hasInitializedRight = true;
                    respawnTimerRight = -1f;
                    if (systemEffect != null) systemEffect.setModuleRight(m);
                    if (weaponRight != null) {
                        weaponRight.setForceDisabled(false);
                        weaponRight.setRemainingCooldownTo(0f);
                    }
                }
            }
        }

        // ========================
        // LEFT MODULE LIFECYCLE
        // ========================
        boolean leftAlive = (lastLeft != null && !lastLeft.isHulk() && lastLeft.getHitpoints() > 0 && lastLeft.getStationSlot() != null);

        if (leftAlive) {
            hasInitializedLeft = true;
            respawnTimerLeft = -1f;
            float currentFold = (systemEffect != null) ? systemEffect.getFold() : 0f;
            SKR_balisongEffect.syncModulePosition(ship, lastLeft, slotLeft, 1, currentFold);
        } else if (hasInitializedLeft) {
            // Module was confirmed alive and has now been destroyed
            if (lastLeft != null) {
                removeChildModuleFromShip(ship, lastLeft);
                lastLeft = null;
                ship.getCustomData().remove("SKR_balisong_moduleLeft");
                if (systemEffect != null) systemEffect.setModuleLeft(null);
                if (weaponLeft != null) {
                    weaponLeft.setAmmo(0);
                    weaponLeft.setForceDisabled(true);
                    weaponLeft.disable(true);
                }
            }

            if (respawnTimerLeft == -1f) {
                respawnTimerLeft = RESPAWN_DELAY;
            } else if (respawnTimerLeft > 0f) {
                respawnTimerLeft -= amount;
                renderGhost(engine, ship, true, respawnTimerLeft);
                if (respawnTimerLeft <= 3f) {
                    playCountdownConvergence(engine, ship, true);
                }
                if (respawnTimerLeft <= 0f) {
                    warpInModule(engine, ship, "MODULE_LEFT", true);
                }
            }
        }

        // ========================
        // RIGHT MODULE LIFECYCLE
        // ========================
        boolean rightAlive = (lastRight != null && !lastRight.isHulk() && lastRight.getHitpoints() > 0 && lastRight.getStationSlot() != null);

        if (rightAlive) {
            hasInitializedRight = true;
            respawnTimerRight = -1f;
            float currentFold = (systemEffect != null) ? systemEffect.getFold() : 0f;
            SKR_balisongEffect.syncModulePosition(ship, lastRight, slotRight, -1, currentFold);
        } else if (hasInitializedRight) {
            // Module was confirmed alive and has now been destroyed
            if (lastRight != null) {
                removeChildModuleFromShip(ship, lastRight);
                lastRight = null;
                ship.getCustomData().remove("SKR_balisong_moduleRight");
                if (systemEffect != null) systemEffect.setModuleRight(null);
                if (weaponRight != null) {
                    weaponRight.setAmmo(0);
                    weaponRight.setForceDisabled(true);
                    weaponRight.disable(true);
                }
            }

            if (respawnTimerRight == -1f) {
                respawnTimerRight = RESPAWN_DELAY;
            } else if (respawnTimerRight > 0f) {
                respawnTimerRight -= amount;
                renderGhost(engine, ship, false, respawnTimerRight);
                if (respawnTimerRight <= 3f) {
                    playCountdownConvergence(engine, ship, false);
                }
                if (respawnTimerRight <= 0f) {
                    warpInModule(engine, ship, "MODULE_RIGHT", false);
                }
            }
        }
    }

    private void warpInModule(CombatEngineAPI engine, ShipAPI ship, String slotId, boolean isLeft) {
        if (engine == null || ship == null) return;

        String variantId = isLeft ? "SKR_balisongLeft_overdrive" : "SKR_balisongRight_overdrive";

        CombatFleetManagerAPI manager = engine.getFleetManager(ship.getOwner());
        if (manager == null) manager = engine.getFleetManager(ship.getOriginalOwner());
        if (manager == null) return;

        WeaponSlotAPI targetSlot = isLeft ? slotLeft : slotRight;
        if (targetSlot == null && ship.getHullSpec() != null) {
            targetSlot = ship.getHullSpec().getWeaponSlotAPI(slotId);
        }
        if (targetSlot == null) return;

        int side = isLeft ? 1 : -1;
        // Spawn at docked position (fold = 1.0f)
        float dockAngle = isLeft ? DOCKED_ANGLE_LEFT : DOCKED_ANGLE_RIGHT;
        targetSlot.setAngle(dockAngle);

        Vector2f localPos = new Vector2f(isLeft ? DOCKED_LOCAL_LEFT : DOCKED_LOCAL_RIGHT);
        VectorUtils.rotate(localPos, ship.getFacing(), localPos);
        Vector2f spawnLoc = Vector2f.add(ship.getLocation(), localPos, null);

        Vector2f offset = new Vector2f(25f, 16f * side);

        FleetMemberAPI member = Global.getFactory().createFleetMember(
                com.fs.starfarer.api.fleet.FleetMemberType.SHIP,
                variantId
        );
        member.setOwner(ship.getOwner());
        if (member.getVariant() != null) {
            member.getVariant().addTag("no_sell");
            member.getVariant().addTag("no_dealer");
            member.getVariant().addTag("no_autofit");
            member.getVariant().addTag("restricted");
            member.getVariant().addTag("no_battle_salvage");
            member.getVariant().addTag("no_sim");
            member.getVariant().addTag("module_hull_bar_only");
        }
        if (member.getRepairTracker() != null) {
            member.getRepairTracker().setCR(ship.getCurrentCR());
        }

        float facing = ship.getFacing() + dockAngle;

        ShipAPI newMod = null;
        boolean prevSuppress = manager.isSuppressDeploymentMessages();
        try {
            manager.setSuppressDeploymentMessages(true);
            newMod = manager.spawnFleetMember(member, spawnLoc, facing, 0f);
        } catch (Throwable t) {
            newMod = null;
        } finally {
            manager.setSuppressDeploymentMessages(prevSuppress);
        }

        if (newMod != null) {
            try {
                ship.ensureClonedStationSlotSpec();
                ship.setShipWithModules(true);
                newMod.setParentStation(ship);
                newMod.setStationSlot(targetSlot);
                newMod.ensureClonedStationSlotSpec();
                newMod.setOwner(ship.getOwner());
                newMod.setOriginalOwner(ship.getOriginalOwner());
                newMod.setAlly(ship.isAlly());
                if (newMod.getModuleOffset() != null) {
                    newMod.getModuleOffset().set(offset);
                }

                if (newMod.getLocation() != null) {
                    newMod.getLocation().set(spawnLoc);
                }
                newMod.setFacing(facing);
                if (newMod.getVelocity() != null && ship.getVelocity() != null) {
                    float angVelRad = (float) Math.toRadians(ship.getAngularVelocity());
                    Vector2f r = Vector2f.sub(spawnLoc, ship.getLocation(), null);
                    newMod.getVelocity().set(
                            ship.getVelocity().x - angVelRad * r.y,
                            ship.getVelocity().y + angVelRad * r.x
                    );
                }
                newMod.setAngularVelocity(ship.getAngularVelocity());

                newMod.syncWithArmorGridState();
                newMod.syncWeaponDecalsWithArmorDamage();
            } catch (Throwable ignored) {}

            // Flush the new module directly into the parent ship's childModules list!
            flushChildModule(ship, newMod, slotId);

            if (isLeft) {
                lastLeft = newMod;
                hasInitializedLeft = true;
                respawnTimerLeft = -1f;
                ship.getCustomData().put("SKR_balisong_moduleLeft", newMod);
                if (systemEffect != null) {
                    systemEffect.setModuleLeft(newMod);
                }
                if (weaponLeft != null) {
                    weaponLeft.setForceDisabled(false);
                    weaponLeft.repair();
                    weaponLeft.setAmmo(weaponLeft.getMaxAmmo());
                    weaponLeft.setRemainingCooldownTo(0f);
                }
            } else {
                lastRight = newMod;
                hasInitializedRight = true;
                respawnTimerRight = -1f;
                ship.getCustomData().put("SKR_balisong_moduleRight", newMod);
                if (systemEffect != null) {
                    systemEffect.setModuleRight(newMod);
                }
                if (weaponRight != null) {
                    weaponRight.setForceDisabled(false);
                    weaponRight.repair();
                    weaponRight.setAmmo(weaponRight.getMaxAmmo());
                    weaponRight.setRemainingCooldownTo(0f);
                }
            }

            SKR_balisongEffect.syncModulePosition(ship, newMod, targetSlot, side, 1.0f);

            try {
                engine.updateStationModuleLocations(ship);
            } catch (Throwable ignored) {}

            playWarpInEffect(engine, ship, newMod, spawnLoc, isLeft);
        }
    }

    /**
     * Integrates the newly spawned child module directly into the parent station's internal childModules list.
     * This ensures the combat engine treats it as a native station module and updates its transform every frame.
     */
    @SuppressWarnings("unchecked")
    public static void flushChildModule(ShipAPI parentStation, ShipAPI childModule, String slotId) {
        if (parentStation == null || childModule == null) return;
        try {
            List<Object> list = null;
            try {
                Method m = parentStation.getClass().getMethod("getChildModules");
                m.setAccessible(true);
                list = (List<Object>) m.invoke(parentStation);
            } catch (Throwable ignored) {}

            if (list == null) {
                Field f = null;
                Class<?> clazz = parentStation.getClass();
                while (clazz != null && f == null) {
                    try {
                        f = clazz.getDeclaredField("childModules");
                    } catch (Throwable ignored) {
                        clazz = clazz.getSuperclass();
                    }
                }
                if (f != null) {
                    f.setAccessible(true);
                    list = (List<Object>) f.get(parentStation);
                }
            }

            if (list != null) {
                Iterator<Object> it = list.iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    if (obj instanceof ShipAPI) {
                        ShipAPI existing = (ShipAPI) obj;
                        if (existing == childModule) {
                            it.remove();
                        } else if (existing.getStationSlot() != null && slotId != null && slotId.equals(existing.getStationSlot().getId())) {
                            it.remove();
                            try {
                                Global.getCombatEngine().removeEntity(existing);
                            } catch (Throwable ignored) {}
                        }
                    }
                }
                list.add(childModule);
                parentStation.setShipWithModules(true);
            }
        } catch (Throwable ignored) {}
    }

    /**
     * Removes a destroyed child module from the parent station's childModules list.
     */
    @SuppressWarnings("unchecked")
    public static void removeChildModuleFromShip(ShipAPI parentStation, ShipAPI moduleToRemove) {
        if (parentStation == null || moduleToRemove == null) return;
        try {
            List<Object> list = null;
            try {
                Method m = parentStation.getClass().getMethod("getChildModules");
                m.setAccessible(true);
                list = (List<Object>) m.invoke(parentStation);
            } catch (Throwable ignored) {}

            if (list == null) {
                Field f = null;
                Class<?> clazz = parentStation.getClass();
                while (clazz != null && f == null) {
                    try {
                        f = clazz.getDeclaredField("childModules");
                    } catch (Throwable ignored) {
                        clazz = clazz.getSuperclass();
                    }
                }
                if (f != null) {
                    f.setAccessible(true);
                    list = (List<Object>) f.get(parentStation);
                }
            }

            if (list != null) {
                Iterator<Object> it = list.iterator();
                while (it.hasNext()) {
                    Object obj = it.next();
                    if (obj == moduleToRemove) {
                        it.remove();
                        break;
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    private void playWarpInEffect(CombatEngineAPI engine, ShipAPI ship, ShipAPI newMod, Vector2f spawnLoc, boolean isLeft) {
        if (engine == null || ship == null || newMod == null || spawnLoc == null) return;
        try {
            if (ship.getVelocity() != null) {
                Global.getSoundPlayer().playSound("SKR_balisongSystem_activation", 1.2f, 1f, spawnLoc, ship.getVelocity());
                Global.getSoundPlayer().playSound("SKR_balisong_open", 1f, 1f, spawnLoc, ship.getVelocity());
            }

            newMod.setJitter(newMod, new Color(255, 100, 150, 200), 0.8f, 3, 4f);
            newMod.setJitterUnder(newMod, new Color(255, 50, 100, 160), 0.8f, 3, 5f);

            Vector2f vel = (ship.getVelocity() != null) ? ship.getVelocity() : new Vector2f();
            engine.addHitParticle(spawnLoc, vel, 120f, 1f, 0.2f, new Color(255, 120, 180, 200));
            engine.addHitParticle(spawnLoc, vel, 80f, 1f, 0.1f, Color.WHITE);
            engine.addNebulaSmoothParticle(spawnLoc, vel, 100f, 1.5f, 0.1f, 0.5f, 0.8f, new Color(255, 80, 160, 150));
            engine.addNegativeParticle(spawnLoc, vel, 80f, 0.1f, 0.5f, new Color(255, 100, 180));

            if (newMod.getLocation() != null) {
                engine.spawnEmpArcVisual(spawnLoc, ship, newMod.getLocation(), newMod, 8f, new Color(255, 100, 180, 200), Color.WHITE);
            }

            engine.addFloatingTextAlways(
                    spawnLoc,
                    (isLeft ? "LEFT" : "RIGHT") + " MODULE RESTORED",
                    16f,
                    new Color(255, 120, 180, 255),
                    ship,
                    0f,
                    0f,
                    1.2f,
                    0.4f,
                    0.4f,
                    0.15f
            );

            boolean SHADER = Global.getSettings().getModManager().isModEnabled("shaderLib");
            if (SHADER) {
                data.scripts.util.SKR_graphicLibEffects.balisongRing(newMod, 5f);
            }
        } catch (Throwable ignored) {}
    }

    private void renderGhost(CombatEngineAPI engine, ShipAPI ship, boolean isLeft, float respawnTimer) {
        if (engine == null || ship == null || ship.getLocation() == null || respawnTimer <= 0f) return;
        try {
            // Silhouette strictly locks to the hardcoded docking position
            Vector2f localPos = new Vector2f(isLeft ? DOCKED_LOCAL_LEFT : DOCKED_LOCAL_RIGHT);
            VectorUtils.rotate(localPos, ship.getFacing(), localPos);
            Vector2f renderLoc = Vector2f.add(ship.getLocation(), localPos, null);

            float dockAngle = isLeft ? DOCKED_ANGLE_LEFT : DOCKED_ANGLE_RIGHT;
            float spriteAngle = ship.getFacing() + dockAngle - 90f;

            if (engine.getPlayerShip() == ship) {
                String sideStr = isLeft ? "LEFT" : "RIGHT";
                engine.maintainStatusForPlayerShip(
                        "balisong_recon_" + sideStr,
                        "graphics/icons/hullsys/repair.png",
                        "REBUILDING: " + (int) Math.ceil(respawnTimer) + "s",
                        sideStr + " MODULE",
                        false
                );
            }

            if (!MagicRender.screenCheck(0.25f, renderLoc)) return;

            String spritePath = isLeft ? "graphics/SEEKER/ships/hidden/SKR_balisongLEFT.png" : "graphics/SEEKER/ships/hidden/SKR_balisongRIGHT.png";
            SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
            if (sprite != null) {
                float blink = (float) Math.abs(Math.sin(respawnTimer * Math.PI * 2f));
                Color c = new Color(255, 100, 180, (int) (140 + 70 * blink));
                MagicRender.singleframe(sprite, renderLoc, new Vector2f(76, 205), spriteAngle, c, true);
            }
        } catch (Throwable ignored) {}
    }

    private void playCountdownConvergence(CombatEngineAPI engine, ShipAPI ship, boolean isLeft) {
        if (engine == null || ship == null || ship.getLocation() == null || ship.getVelocity() == null) return;
        try {
            Vector2f localPos = new Vector2f(isLeft ? DOCKED_LOCAL_LEFT : DOCKED_LOCAL_RIGHT);
            VectorUtils.rotate(localPos, ship.getFacing(), localPos);
            Vector2f loc = Vector2f.add(ship.getLocation(), localPos, null);

            if (Math.random() < 0.35f) {
                Vector2f p = MathUtils.getRandomPointInCircle(loc, 35f);
                Vector2f vel = Vector2f.sub(loc, p, null);
                vel.scale(2f);
                Vector2f.add(vel, ship.getVelocity(), vel);
                engine.addHitParticle(p, vel, 2f + (float) Math.random() * 3f, 1f, 0.2f, new Color(255, 120, 180, 200));
            }
        } catch (Throwable ignored) {}
    }
}
