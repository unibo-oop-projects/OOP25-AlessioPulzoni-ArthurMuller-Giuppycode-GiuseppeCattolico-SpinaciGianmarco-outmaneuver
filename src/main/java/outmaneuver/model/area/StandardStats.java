package outmaneuver.model.area;

public final class StandardStats implements PlaneStats {

    private static final String ID = "standard";
    private static final double BASE_SPEED = 200.0;
    private static final double TURN_RATE = 3.0;
    private static final double HITBOX_RADIUS = 20.0;
    private static final String SPRITE_ID = "aircraft_standard";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public double getBaseSpeed() {
        return BASE_SPEED;
    }

    @Override
    public double getTurnRate() {
        return TURN_RATE;
    }

    @Override
    public double getHitboxRadius() {
        return HITBOX_RADIUS;
    }

    @Override
    public String getSpriteId() {
        return SPRITE_ID;
    }
}
