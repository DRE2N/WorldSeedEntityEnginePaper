package net.worldseed.multipart.events;

import net.worldseed.multipart.GenericModel;
import org.bukkit.event.player.PlayerInputEvent;
import org.jetbrains.annotations.NotNull;

public class ModelControlEvent extends ModelEvent {

    private final PlayerInputEvent event;

    public ModelControlEvent(@NotNull GenericModel model, PlayerInputEvent event) {
        this.event = event;
    }

    @Override
    public @NotNull GenericModel model() {
        return model;
    }

    public @NotNull PlayerInputEvent event() {
        return event;
    }
}

