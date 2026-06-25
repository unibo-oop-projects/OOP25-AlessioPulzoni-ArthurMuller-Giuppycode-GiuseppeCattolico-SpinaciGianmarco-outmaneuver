package outmaneuver.model.area.entity.missile.type;

import java.awt.Dimension;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/**
 * Va in linea retta e rimbalza sui bordi dello schermo (non insegue il giocatore,
 * non viene rediretto ne' distrutto fuori schermo).
 */
public final class BounceMissile extends MissileImpl {

    /**
     * Creates a bounce missile.
     *
     * @param spawnPos the initial position in world coordinates
     * @param data the missile's type definition
     */
    public BounceMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data);
    }

    @Override
    protected void steer(final Vector2 target) {
        // niente sterzata: mantiene la direzione iniziale, poi rimbalza (vedi checkBounce)
    }

    @Override
    public void checkBounce(final Vector2 planePos, final Dimension screenSize) {
        final Vector2 pos = getPosition();
        final Vector2 rel = pos.subtract(planePos);   // posizione rispetto al centro (l'aereo)
        final double halfW = screenSize.width / 2.0;
        final double halfH = screenSize.height / 2.0;
        final int margin = getOutOfBoundsMargin();

        Vector2 vel = getVelocity();
        double clampedX = pos.getX();
        double clampedY = pos.getY();

        if (rel.getX() < -halfW + margin) {
            if (vel.getX() < 0) {
                vel = vel.reflectX();   // tocca il bordo sinistro -> rimbalza a destra
            }
            clampedX = planePos.getX() - halfW + margin;
        } else if (rel.getX() > halfW - margin) {
            if (vel.getX() > 0) {
                vel = vel.reflectX();   // bordo destro -> rimbalza a sinistra
            }
            clampedX = planePos.getX() + halfW - margin;
        }

        if (rel.getY() < -halfH + margin) {
            if (vel.getY() < 0) {
                vel = vel.reflectY();   // bordo superiore -> rimbalza in giu'
            }
            clampedY = planePos.getY() - halfH + margin;
        } else if (rel.getY() > halfH - margin) {
            if (vel.getY() > 0) {
                vel = vel.reflectY();   // bordo inferiore -> rimbalza in su'
            }
            clampedY = planePos.getY() + halfH - margin;
        }

        setPosition(new Vector2(clampedX, clampedY));
        setVelocity(vel);
    }
}
