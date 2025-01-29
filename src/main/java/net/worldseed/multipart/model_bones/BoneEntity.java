package net.worldseed.multipart.model_bones;

import net.minecraft.world.entity.EntityType;
import net.worldseed.multipart.GenericModel;
import net.worldseed.util.PacketEntity;
import org.bukkit.Bukkit;

public class BoneEntity extends PacketEntity {
    private final GenericModel model;
    private final String name;

    public BoneEntity(EntityType<?> entityType, GenericModel model, String name) {
        super(entityType, Bukkit.getUnsafe().nextEntityId(), model);
        this.model = model;
        this.name = name;
    }

    public GenericModel getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public void tick(long time) {
    }

}
