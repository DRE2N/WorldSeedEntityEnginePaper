package net.worldseed.multipart.events;

import net.worldseed.multipart.GenericModel;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ModelDismountEvent extends ModelEvent {

    private final Entity rider;

    public ModelDismountEvent(@NotNull GenericModel model, Entity rider) {
        this.rider = rider;
        this.model = model;
    }

    public @NotNull Entity rider() {
        return rider;
    }
}

