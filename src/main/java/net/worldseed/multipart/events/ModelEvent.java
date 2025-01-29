package net.worldseed.multipart.events;

import net.worldseed.multipart.GenericModel;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ModelEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    protected boolean cancelled;

    protected GenericModel model;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    public GenericModel model() {
        return model;
    }
}
