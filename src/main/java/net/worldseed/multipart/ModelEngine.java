package net.worldseed.multipart;

import com.google.gson.*;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.worldseed.multipart.events.ModelControlEvent;
import net.worldseed.multipart.events.ModelDamageEvent;
import net.worldseed.multipart.events.ModelInteractEvent;
import net.worldseed.multipart.model_bones.BoneEntity;
import net.worldseed.multipart.mql.MQLPoint;
import net.worldseed.util.DataMappings;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import net.worldseed.util.math.Vec;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInputEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import javax.json.JsonNumber;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Optional;

public class ModelEngine implements Listener {

    private static JavaPlugin plugin = null;

    public final static HashMap<String, Point> offsetMappings = new HashMap<>();
    public final static HashMap<String, Point> diffMappings = new HashMap<>();
    static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private final static HashMap<String, HashMap<String, ItemStack>> blockMappings = new HashMap<>();

    private static Path modelPath;
    private static Material modelMaterial = Material.MAGMA_CREAM;

    public ModelEngine(JavaPlugin provider) {
        plugin = provider;
        Bukkit.getPluginManager().registerEvents(this, provider);
        CraftWorld craftWorld = (CraftWorld) Bukkit.getWorlds().get(0);
        DataMappings.generateMappings(craftWorld.getHandle());
    }

    @EventHandler
    private void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof BoneEntity bone) {
            event.setCancelled(true);
            ModelDamageEvent modelDamageEvent = new ModelDamageEvent(bone.getModel(), event, bone);
            Bukkit.getPluginManager().callEvent(modelDamageEvent);
        }
    }

    @EventHandler
    private void onEntityInteract(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof BoneEntity bone) {
            ModelInteractEvent modelInteractEvent = new ModelInteractEvent(bone.getModel(), event, bone);
            Bukkit.getPluginManager().callEvent(modelInteractEvent);
        }

    }

    @EventHandler
    private void onPlayerInput(PlayerInputEvent event) {
        Entity riding = event.getPlayer().getVehicle();
        if (riding instanceof BoneEntity bone) {
            ModelControlEvent modelControlEvent = new ModelControlEvent(bone.getModel(), event);
            Bukkit.getPluginManager().callEvent(modelControlEvent);
        }
    }

    public static JavaPlugin getProvider() {
        return plugin;
    }

    /**
     * Loads the model from the given path
     *
     * @param mappingsData mappings file created by model parser
     * @param modelPath    path of the models
     */
    public static void loadMappings(Reader mappingsData, Path modelPath) {
        JsonObject map = GSON.fromJson(mappingsData, JsonObject.class);
        ModelEngine.modelPath = modelPath;

        blockMappings.clear();
        offsetMappings.clear();
        diffMappings.clear();
        ModelLoader.clearCache();

        map.entrySet().forEach(entry -> {
            HashMap<String, ItemStack> keys = new HashMap<>();

            entry.getValue().getAsJsonObject()
                    .get("id")
                    .getAsJsonObject()
                    .entrySet()
                    .forEach(id -> keys.put(id.getKey(), generateBoneItem(new NamespacedKey("erethon", id.getValue().getAsString()))));

            blockMappings.put(entry.getKey(), keys);
            offsetMappings.put(entry.getKey(), getPos(entry.getValue().getAsJsonObject().get("offset").getAsJsonArray()).orElse(Pos.ZERO));
            diffMappings.put(entry.getKey(), getPos(entry.getValue().getAsJsonObject().get("diff").getAsJsonArray()).orElse(Pos.ZERO));
        });
    }

    private static ItemStack generateBoneItem(NamespacedKey modelKey) {
        ItemStack item = new ItemStack(modelMaterial);
        item.setData(DataComponentTypes.ITEM_MODEL, modelKey);
        return item;
    }

    public static HashMap<String, ItemStack> getItems(String model, String name) {
        return blockMappings.get(model + "/" + name);
    }

    public static Path getGeoPath(String id) {
        return modelPath.resolve(id).resolve("model.geo.json");
    }

    public static Path getAnimationPath(String id) {
        return modelPath.resolve(id).resolve("model.animation.json");
    }

    public static Optional<Point> getPos(JsonElement pivot) {
        if (pivot == null) return Optional.empty();
        else {
            JsonArray arr = pivot.getAsJsonArray();
            return Optional.of(new Vec(arr.get(0).getAsDouble(), arr.get(1).getAsDouble(), arr.get(2).getAsDouble()));
        }
    }

    public static Optional<MQLPoint> getMQLPos(JsonElement pivot) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (pivot == null) return Optional.empty();
        else if (pivot instanceof JsonObject obj) {
            return Optional.of(new MQLPoint(obj));
        } else if (pivot instanceof JsonNumber num) {
            return Optional.of(new MQLPoint(num.doubleValue(), num.doubleValue(), num.doubleValue()));
        } else {
            return Optional.empty();
        }
    }

    public static Material getModelMaterial() {
        return modelMaterial;
    }

    public static void setModelMaterial(Material modelMaterial) {
        ModelEngine.modelMaterial = modelMaterial;
    }

    public static Optional<MQLPoint> getMQLPos(JsonArray arr) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        if (arr == null) return Optional.empty();
        else {
            return Optional.of(new MQLPoint(arr));
        }
    }
}
