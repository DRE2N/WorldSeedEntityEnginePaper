package net.worldseed.model;

import net.minecraft.world.level.Level;
import net.worldseed.multipart.GenericModelImpl;
import net.worldseed.plugin.DaedalusPlugin;
import net.worldseed.util.math.Pos;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class DModel extends GenericModelImpl {

    private final String modelId;
    private final DaedalusPlugin plugin;

    public DModel(String modelId, DaedalusPlugin plugin) {
        this.modelId = modelId;
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return modelId;
    }

    @Override
    public void init(@Nullable Level level, @NotNull Pos position, float scale) {
        super.init(level, position, scale);
        plugin.getLogger().info("Model initialized at " + position + " with scale " + scale + " in level " + (level != null ? level.dimension().location() : "null"));
    }

    @Override
    public Level getInstance() {
        return null;
    }

    @Override
    public void bindNametag(String s, Entity entity) {

    }

    @Override
    public void unbindNametag(String s) {

    }

    @Override
    public @Nullable Entity getNametag(String s) {
        return null;
    }
}
