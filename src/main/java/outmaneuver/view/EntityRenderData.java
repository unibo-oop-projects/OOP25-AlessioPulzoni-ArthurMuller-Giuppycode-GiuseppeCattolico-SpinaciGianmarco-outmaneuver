package outmaneuver.view;

public final class EntityRenderData {

    private final double x;
    private final double y;
    private final double directionRad;
    private final String spriteId;
    private final double radius;

    public EntityRenderData(final double x, final double y,
                            final double directionRad, final String spriteId,
                            final double radius) {
        this.x = x;
        this.y = y;
        this.directionRad = directionRad;
        this.spriteId = spriteId;
        this.radius = radius;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getDirectionRad() {
        return directionRad;
    }

    public String getSpriteId() {
        return spriteId;
    }

    /** Raggio di collisione (hitbox) dell'entita': la view ci dimensiona lo sprite. */
    public double getRadius() {
        return radius;
    }
}
