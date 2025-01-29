package net.worldseed.multipart.model_bones.bone_types;

import net.worldseed.multipart.model_bones.ModelBone;
import org.bukkit.entity.Entity;

import java.util.Set;

public interface RideableBone extends ModelBone {
    void addPassenger(Entity entity);
    void removePassenger(Entity entity);
    Set<Entity> getPassengers();
}
