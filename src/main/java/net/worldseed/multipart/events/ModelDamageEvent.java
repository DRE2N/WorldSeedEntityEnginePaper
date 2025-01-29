package net.worldseed.multipart.events;

import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.model_bones.BoneEntity;
import org.bukkit.Sound;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public class ModelDamageEvent extends ModelEvent {

    private final BoneEntity hitBone;
    private final double damage;
    private Sound sound;

    private boolean animation = true;

    private boolean cancelled;

    public ModelDamageEvent(GenericModel model, EntityDamageEvent event) {
        this(model, event, null);
    }
    
    public ModelDamageEvent(GenericModel model, EntityDamageEvent event, @Nullable BoneEntity hitBone) {
        this.model = model;
        this.hitBone = hitBone;
        this.damage = event.getDamage();

        //this.sound = event.getSound();
        //this.animation = event.shouldAnimate();
    }

    /**
     * Gets the damage.
     *
     * @return the damage
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Gets the damage sound.
     *
     * @return the damage sound
     */
    @Nullable
    public Sound getSound() {
        return sound;
    }

    /**
     * Changes the damage sound.
     *
     * @param sound the new damage sound
     */
    public void setSound(@Nullable Sound sound) {
        this.sound = sound;
    }

    /**
     * Gets whether the damage animation should be played.
     *
     * @return true if the animation should be played
     */
    public boolean shouldAnimate() {
        return animation;
    }

    /**
     * Sets whether the damage animation should be played.
     *
     * @param animation whether the animation should be played or not
     */
    public void setAnimation(boolean animation) {
        this.animation = animation;
    }

    /**
     * Gets the hitbox bone that has been hit.
     *
     * @return the hitbox bone that has been hit, or null if it was an EmotePlayer
     */
    public @Nullable BoneEntity getBone() {
        return hitBone;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

}
