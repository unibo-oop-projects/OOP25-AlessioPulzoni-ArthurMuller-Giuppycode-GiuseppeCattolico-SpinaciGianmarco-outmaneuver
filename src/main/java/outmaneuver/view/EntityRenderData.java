package outmaneuver.view;

/**
 * Immutable, view-facing snapshot of an entity's position, orientation and sprite,
 * used to decouple the model from the rendering code.
 */
public final class EntityRenderData {

    private final double x;
    private final double y;
    private final double directionRad;
    private final String spriteId;
    private final double radius;

    /**
     * Creates a render data snapshot for an entity.
     *
     * @param x world X coordinate
     * @param y world Y coordinate
     * @param directionRad facing direction, in radians
     * @param spriteId identifier of the sprite used to render the entity
     * @param radius hitbox radius used to size the sprite
     */
    public EntityRenderData(final double x, final double y,
                            final double directionRad, final String spriteId,
                            final double radius) {
        this.x = x;
        this.y = y;
        this.directionRad = directionRad;
        this.spriteId = spriteId;
        this.radius = radius;
    }

    /**
     * Returns the entity's world X coordinate.
     *
     * @return the X coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the entity's world Y coordinate.
     *
     * @return the Y coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the entity's facing direction, in radians.
     *
     * @return the direction in radians
     */
    public double getDirectionRad() {
        return directionRad;
    }

    /**
     * Returns the identifier of the sprite used to render this entity.
     *
     * @return the sprite identifier
     */
    public String getSpriteId() {
        return spriteId;
    }

    /**
     * Raggio di collisione (hitbox) dell'entita': la view ci dimensiona lo sprite.
     *
     * @return the hitbox radius
     */
    public double getRadius() {
        return radius;
    }
}
