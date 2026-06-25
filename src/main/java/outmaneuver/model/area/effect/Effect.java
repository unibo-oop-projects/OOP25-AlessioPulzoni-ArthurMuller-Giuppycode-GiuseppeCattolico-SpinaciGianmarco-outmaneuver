package outmaneuver.model.area.effect;

public interface Effect {

    void update(long deltaMs);

    boolean isActive();

    double getMultiplier();

    EffectType getType();
}
