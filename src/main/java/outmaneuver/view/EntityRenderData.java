package outmaneuver.view;

public final class EntityRenderData {

    private final double x;
    private final double y;
    private final double directionRad;
    private final String spriteId;

    public EntityRenderData(final double x, final double y,
                            final double directionRad, final String spriteId) {
        this.x = x;
        this.y = y;
        this.directionRad = directionRad;
        this.spriteId = spriteId;
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
}
