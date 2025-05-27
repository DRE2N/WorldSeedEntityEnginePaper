package net.worldseed.plugin;

import net.minecraft.world.level.Level;
import net.worldseed.model.DModel;
import net.worldseed.multipart.ModelEngine;
import net.worldseed.multipart.animations.AnimationHandler;
import net.worldseed.multipart.animations.AnimationHandlerImpl;
import net.worldseed.resourcepack.PackBuilder;
import net.worldseed.util.math.Pos;
import org.apache.commons.io.FileUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.zeroturnaround.zip.ZipUtil;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

public class DaedalusPlugin extends JavaPlugin implements CommandExecutor {

    private ModelEngine modelEngine;

    @Override
    public void onEnable() {
        getCommand("daedalus").setExecutor(this);
        Path BASE_PATH = getDataFolder().toPath();
        if (!BASE_PATH.toFile().exists()) {
            BASE_PATH.toFile().mkdirs();
        }

        Path ZIP_PATH = BASE_PATH.resolve("resourcepack.zip");
        Path MODEL_PATH = BASE_PATH.resolve("models");
        modelEngine = new ModelEngine(this);
        try {
            try {
                FileUtils.deleteDirectory(BASE_PATH.resolve("resourcepack").toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IllegalArgumentException ignored) {
        }
        ModelEngine.setModelMaterial(Material.MAGMA_CREAM);

        try {
            FileUtils.copyDirectory(BASE_PATH.resolve("resourcepack_template").toFile(), BASE_PATH.resolve("resourcepack").toFile());
            var config = PackBuilder.Generate(BASE_PATH.resolve("bbmodel"), BASE_PATH.resolve("resourcepack"), MODEL_PATH);
            FileUtils.writeStringToFile(BASE_PATH.resolve("model_mappings.json").toFile(), config.modelMappings(), Charset.defaultCharset());

            Reader mappingsData = new InputStreamReader(new FileInputStream(BASE_PATH.resolve("model_mappings.json").toFile()));
            ModelEngine.loadMappings(mappingsData, MODEL_PATH);

            ZipUtil.pack(BASE_PATH.resolve("resourcepack").toFile(), ZIP_PATH.toFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Usage: /daedalus <modelId> [scale]");
            return false;
        }
        String modelId = args[0];
        float scale = 1.0f;
        if (args.length > 1) {
            try {
                scale = Float.parseFloat(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid scale value. Using default scale of 1.0.");
            }
        }
        Location location = sender instanceof org.bukkit.entity.Player player ? player.getLocation() : new Location(getServer().getWorlds().get(0), 0, 64, 0);
        CraftPlayer p = (CraftPlayer) sender;
        if (p == null || !(p.getWorld() instanceof CraftWorld)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }
        DModel model = spawnModel(modelId, location, scale);
        model.addViewer(p.getHandle());
        sender.sendMessage("Spawned model '" + modelId + "' at " + location + " with scale " + scale);
        return true;
    }

    @Override
    public void onDisable() {
    }

    public DModel spawnModel(String modelId, Location location) {
        return spawnModel(modelId, location, 1.0f);
    }

    public DModel spawnModel(String modelId, Location location, float scale) {
        CraftWorld cw = (CraftWorld) location.getWorld();
        Level level = cw.getHandle();
        DModel model = new DModel(modelId, this);
        model.init(level, new Pos(location.getX(), location.getY(), location.getZ()), scale);
        return model;
    }
}
