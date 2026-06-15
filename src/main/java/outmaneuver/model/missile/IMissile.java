package outmaneuver.model.missile;

import java.util.List;

import outmaneuver.model.area.Plane;
import outmaneuver.view.MissileRenderData;

/*
 * Contratto pubblico per tutti i tipi di missile.
 * Gli altri moduli (collision detection, controller) usano solo questa interfaccia.
 */
public interface IMissile {

    // --- UPDATE E MOVIMENTO ---
    void update(Plane plane, double dt);
    boolean redirectIfOutOfBounds(Plane plane, int screenW, int screenH);
    void setInitialDirection(double targetX, double targetY);

    // --- STATO ---
    void destroy();
    boolean isAlive();

    // --- COLLISIONE ---
    boolean collidesWith(Plane plane);

    // --- EFFETTI ---
    void freeze(double duration);
    void slowDown(double factor, double duration);

    // --- SPAWN ON DEATH ---
    List<IMissile> getSpawnOnDeath();

    // --- RENDER ---
    MissileRenderData getRenderData();
    boolean isGhostVisible();

    // --- GETTERS ---
    double getWorldX();
    double getWorldY();
    double getHitboxRadius();
    String getMissileType();
}