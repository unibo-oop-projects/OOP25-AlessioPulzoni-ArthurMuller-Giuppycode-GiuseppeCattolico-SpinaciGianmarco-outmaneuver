package outmaneuver.controller.event;

/** Signals that a gameplay {@code Effect} (e.g. shield, speed boost) has started or expired. */
public enum EffectEvent implements Event {
    EFFECT_APPLIED,
    EFFECT_EXPIRED
}
