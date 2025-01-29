package net.worldseed.multipart;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.util.RGBLike;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.worldseed.multipart.animations.AnimationHandlerImpl;
import net.worldseed.multipart.events.AnimationCompleteEvent;
import net.worldseed.multipart.events.ModelEvent;
import net.worldseed.multipart.model_bones.*;
import net.worldseed.multipart.model_bones.bone_types.HeadBone;
import net.worldseed.multipart.model_bones.bone_types.RideableBone;
import net.worldseed.multipart.model_bones.display_entity.RootBoneEntity;
import net.worldseed.multipart.model_bones.display_entity.ModelBoneHeadDisplay;
import net.worldseed.multipart.model_bones.display_entity.ModelBonePartDisplay;
import net.worldseed.multipart.model_bones.misc.ModelBoneHitbox;
import net.worldseed.multipart.model_bones.misc.ModelBoneNametag;
import net.worldseed.multipart.model_bones.misc.ModelBoneSeat;
import net.worldseed.multipart.model_bones.misc.ModelBoneVFX;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import net.worldseed.util.math.Shape;
import net.worldseed.util.math.Vec;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class GenericModelImpl implements GenericModel {
    protected final LinkedHashMap<String, ModelBone> parts = new LinkedHashMap<>();
    protected final Set<ModelBoneImpl> viewableBones = new LinkedHashSet<>();
    protected Level level;

    private final Collection<ModelBone> additionalBones = new ArrayList<>();
    private final Set<ServerPlayer> viewers = ConcurrentHashMap.newKeySet();
    //private final EventNode<ModelEvent> eventNode;
    private final Map<ServerPlayer, RGBLike> playerGlowColors = Collections.synchronizedMap(new WeakHashMap<>());
    private Pos position;
    private double globalRotation;
    private double pitch;

    protected record ModelBoneInfo(String name, Point pivot, Point rotation, JsonArray cubes, GenericModel model,
                                   float scale) {
    }

    protected final Map<Predicate<String>, Function<ModelBoneInfo, @Nullable ModelBone>> boneSuppliers = new LinkedHashMap<>();
    Function<ModelBoneInfo, ModelBone> defaultBoneSupplier = (info) -> new ModelBonePartDisplay(info.pivot, info.name, info.rotation, info.model, info.scale);

    public GenericModelImpl() {
        /*final ServerProcess process = MinecraftServer.process();
        if (process != null) {
            this.eventNode = process.eventHandler().map(this, EventFilter.from(ModelEvent.class, GenericModel.class, ModelEvent::model));
        } else {
            // Local nodes require a server process
            this.eventNode = null;
        }*/

        registerBoneSuppliers();
    }

    @Override
    public double getGlobalRotation() {
        return globalRotation;
    }

    @Override
    public double getPitch(){
        return pitch;
    }

    public void setGlobalRotation(double yaw) {
        setGlobalRotation(yaw, 0.0f);
    }

    public void setGlobalRotation(double yaw, double pitch) {
        this.globalRotation = yaw;
        this.pitch = pitch;

        this.viewableBones.forEach(part -> {
            part.setGlobalRotation(yaw, pitch);
        });
    }

    @Override
    public Pos getPosition() {
        return position;
    }

    public void setPosition(Pos pos) {
        this.position = pos;
        this.parts.values().forEach(part -> part.teleport(pos));
    }

    @Override
    public BoneEntity generateRoot() {
        return new RootBoneEntity(this);
    }

    public void triggerAnimationEnd(String animation, AnimationHandlerImpl.AnimationDirection direction) {
        Bukkit.getPluginManager().callEvent(new AnimationCompleteEvent(this, animation, direction));
    }

    public void init(@Nullable Level level, @NotNull Pos position) {
        init(level, position, 1);
    }

    public void init(@Nullable Level level, @NotNull Pos position, float scale) {
        this.level = level;
        this.position = position;

        JsonObject loadedModel = ModelLoader.loadModel(getId());
        this.setGlobalRotation(position.yaw());

        loadBones(loadedModel, scale);

        for (ModelBone modelBonePart : this.parts.values()) {
            if (modelBonePart instanceof ModelBoneViewable)
                viewableBones.add((ModelBoneImpl) modelBonePart);

            modelBonePart.spawn(level, modelBonePart.calculatePosition()).join();
        }

        draw();

        this.setState("normal");
    }

    @Override
    public void setGlobalScale(float scale) {
        for (ModelBone modelBonePart : this.parts.values()) {
            modelBonePart.setGlobalScale(scale);
        }
    }

    protected void registerBoneSuppliers() {
        boneSuppliers.put(name -> name.equals("nametag") || name.equals("tag_name"), (info) -> new ModelBoneNametag(info.pivot, info.name, info.rotation, info.model, info.scale));
        boneSuppliers.put(name -> name.contains("hitbox"), (info) -> {
            if (info.cubes.isEmpty()) return null;

            var cube = info.cubes.get(0);
            JsonArray sizeArray = cube.getAsJsonObject().get("size").getAsJsonArray();
            JsonArray p = cube.getAsJsonObject().get("pivot").getAsJsonArray();

            Point sizePoint = new Vec(sizeArray.get(0).getAsFloat(), sizeArray.get(1).getAsFloat(), sizeArray.get(2).getAsFloat());
            Point pivotPoint = new Vec(p.get(0).getAsFloat(), p.get(1).getAsFloat(), p.get(2).getAsFloat());

            var newOffset = pivotPoint.mul(-1, 1, 1);
            return new ModelBoneHitbox(info.pivot, info.name, info.rotation, info.model, newOffset, sizePoint.x(), sizePoint.y(), info.cubes, true, info.scale);
        });
        boneSuppliers.put(name -> name.contains("vfx"), (info) -> new ModelBoneVFX(info.pivot, info.name, info.rotation, info.model, info.scale));
        boneSuppliers.put(name -> name.contains("seat"), (info) -> new ModelBoneSeat(info.pivot, info.name, info.rotation, info.model, info.scale));
        boneSuppliers.put(name -> name.equals("head"), (info) -> new ModelBoneHeadDisplay(info.pivot, info.name, info.rotation, info.model, info.scale));
    }

    protected void loadBones(JsonObject loadedModel, float scale) {
        // Build bones
        for (JsonElement bone : loadedModel.get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject().get("bones").getAsJsonArray()) {
            JsonElement pivot = bone.getAsJsonObject().get("pivot");
            String name = bone.getAsJsonObject().get("name").getAsString();

            Point boneRotation = ModelEngine.getPos(bone.getAsJsonObject().get("rotation")).orElse(Pos.ZERO).mul(-1, -1, 1);
            Point pivotPos = ModelEngine.getPos(pivot).orElse(Pos.ZERO).mul(-1, 1, 1);

            boolean found = false;
            for (Map.Entry<Predicate<String>, Function<ModelBoneInfo, @Nullable ModelBone>> entry : this.boneSuppliers.entrySet()) {
                var predicate = entry.getKey();
                var supplier = entry.getValue();

                if (predicate.test(name)) {
                    var modelBonePart = supplier.apply(new ModelBoneInfo(name, pivotPos, boneRotation, bone.getAsJsonObject().getAsJsonArray("cubes"), this, scale));

                    additionalBones.addAll(modelBonePart.getChildren());
                    parts.put(name, modelBonePart);
                    found = true;

                    break;
                }
            }

            if (!found) {
                var modelBonePart = defaultBoneSupplier.apply(new ModelBoneInfo(name, pivotPos, boneRotation, bone.getAsJsonObject().getAsJsonArray("cubes"), this, scale));

                additionalBones.addAll(modelBonePart.getChildren());
                parts.put(name, modelBonePart);
            }
        }

        // Link parents
        for (JsonElement bone : loadedModel.get("minecraft:geometry").getAsJsonArray().get(0).getAsJsonObject().get("bones").getAsJsonArray()) {
            String name = bone.getAsJsonObject().get("name").getAsString();
            JsonElement parent = bone.getAsJsonObject().get("parent");
            String parentString = parent == null ? null : parent.getAsString();

            if (parentString != null) {
                ModelBone child = this.parts.get(name);

                if (child == null) continue;
                ModelBone parentBone = this.parts.get(parentString);

                child.setParent(parentBone);
                parentBone.addChild(child);
            }
        }
    }

    public void setState(String state) {
        for (ModelBoneImpl part : viewableBones) {
            part.setState(state);
        }
    }

    public ModelBone getPart(String boneName) {
        return this.parts.get(boneName);
    }

    public void draw() {
        for (ModelBone modelBonePart : this.parts.values()) {
            if (modelBonePart.getParent() == null)
                modelBonePart.draw();
        }
    }

    public void destroy() {
        for (ModelBone modelBonePart : this.parts.values()) {
            modelBonePart.destroy();
        }

        this.viewableBones.clear();
        this.parts.clear();
    }

    @Override
    public Point getVFX(String name) {
        ModelBone found = this.parts.get(name);
        if (found == null) return null;
        return found.getPosition();
    }

    @Override
    public void setHeadRotation(String name, double rotation) {
        ModelBone found = this.parts.get(name);
        if (found instanceof HeadBone head) head.setRotation(rotation);
    }

    public @NotNull List<ModelBone> getParts() {
        ArrayList<ModelBone> res = this.parts.values().stream()
                .filter(Objects::nonNull)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        res.addAll(this.additionalBones);
        return res;
    }

    public Pos getPivot() {
        return Pos.ZERO;
    }

    public Pos getGlobalOffset() {
        return Pos.ZERO;
    }

    public Point getDiff(String boneName) {
        return ModelEngine.diffMappings.get(getId() + "/" + boneName);
    }

    public Point getOffset(String boneName) {
        return ModelEngine.offsetMappings.get(getId() + "/" + boneName);
    }

    public boolean isViewer(@NotNull Player player) {
        return this.viewers.contains(player);
    }

    public @NotNull Audience getViewersAsAudience() {
        Set<Player> bukkitViewers = new HashSet<>();
        for (ServerPlayer viewer : this.viewers) {
            bukkitViewers.add(viewer.getBukkitEntity());
        }
        return Audience.audience(bukkitViewers);
    }

    public @NotNull Iterable<? extends Audience> getViewersAsAudiences() {
        return List.of(getViewersAsAudience());
    }

    @Override
    public void addViewer(@NotNull ServerPlayer player) {
        getParts().forEach(part -> part.addViewer(player));

        var foundPlayerGlowing = this.playerGlowColors.get(player);
        if (foundPlayerGlowing != null)
            this.viewableBones.forEach(part -> part.setGlowing(player, foundPlayerGlowing));

        viewers.add(player);
    }

    @Override
    public void removeViewer(@NotNull ServerPlayer player) {
        getParts().forEach(part -> part.removeViewer(player));
        viewers.remove(player);
    }

    @Override
    public @NotNull Set<@NotNull ServerPlayer> getViewers() {
        return Set.copyOf(this.viewers);
    }

    @Override
    public boolean isFaceFull(@NotNull BlockFace face) {
        return true;
    }

    @Override
    public boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace blockFace) {
        return false;
    }

    @Override
    public @NotNull Point relativeStart() {
        Pos currentPosition = getPosition();
        Point p = currentPosition;

        for (ModelBone bone : this.parts.values()) {
            for (var part : bone.getChildren()) {
                var entity = part.getEntity();
                var absoluteStart = entity.relativeStart().add(entity.getPosition());

                if (p.x() > absoluteStart.x()) p = p.withX(absoluteStart.x());
                if (p.y() > absoluteStart.y()) p = p.withY(absoluteStart.y());
                if (p.z() > absoluteStart.z()) p = p.withZ(absoluteStart.z());
            }
        }

        return p.sub(currentPosition);
    }

    @Override
    public @NotNull Point relativeEnd() {
        Pos currentPosition = getPosition();
        Point p = currentPosition;

        for (var bone : this.parts.values()) {
            for (var part : bone.getChildren()) {
                var entity = part.getEntity();
                var absoluteStart = entity.relativeEnd().add(entity.getPosition());

                if (p.x() < absoluteStart.x()) p = p.withX(absoluteStart.x());
                if (p.y() < absoluteStart.y()) p = p.withY(absoluteStart.y());
                if (p.z() < absoluteStart.z()) p = p.withZ(absoluteStart.z());
            }
        }

        return p.sub(currentPosition);
    }

    @Override
    public boolean intersectBox(@NotNull Point point, @NotNull AABB boundingBox) {
        var pos = getPosition();

        for (var bone : this.parts.values()) {
            for (var part : bone.getChildren()) {
                if (boundingBox.intersects(part.getEntity().getBoundingBox())) return true;
            }
        }
        return false;
    }

    @Override
    public void setGlowing(RGBLike color) {
        this.viewableBones.forEach(part -> part.setGlowing(color));
    }

    @Override
    public void removeGlowing() {
        this.viewableBones.forEach(ModelBoneImpl::removeGlowing);
    }

    @Override
    public void setGlowing(ServerPlayer player, RGBLike color) {
        this.playerGlowColors.put(player, color);
        this.viewableBones.forEach(part -> part.setGlowing(player, color));
    }

    @Override
    public void removeGlowing(ServerPlayer player) {
        this.playerGlowColors.remove(player);
        this.viewableBones.forEach(part -> part.removeGlowing(player));
    }

    @Override
    public void attachModel(GenericModel model, String boneName) {
        ModelBone bone = this.parts.get(boneName);
        if (bone != null) bone.attachModel(model);
    }

    @Override
    public void detachModel(GenericModel model, String boneName) {
        ModelBone bone = this.parts.get(boneName);
        if (bone != null) bone.detachModel(model);
    }

    @Override
    public Map<String, List<GenericModel>> getAttachedModels() {
        Map<String, List<GenericModel>> attached = new HashMap<>();
        for (ModelBone part : this.parts.values()) {
            attached.put(part.getName(), part.getAttachedModels());
        }
        return attached;
    }

    @Override
    public void mountEntity(String name, Entity entity) {
        if (this.parts.get(name) instanceof RideableBone rideable) rideable.addPassenger(entity);
    }

    @Override
    public void dismountEntity(String name, Entity entity) {
        if (this.parts.get(name) instanceof RideableBone rideable) rideable.removePassenger(entity);
    }

    @Override
    public Set<Entity> getPassengers(String name) {
        if (this.parts.get(name) instanceof RideableBone rideable) return rideable.getPassengers();
        return Collections.emptySet();
    }

    /*@Override
    public void bindNametag(String name, Entity nametag) {
        if (this.parts.get(name) instanceof ModelBoneNametag nametagBone) nametagBone.bind(nametag);
    }

    @Override
    public void unbindNametag(String name) {
        if (this.parts.get(name) instanceof ModelBoneNametag nametagBone) nametagBone.unbind();
    }

    @Override
    public Entity getNametag(String name) {
        if (this.parts.get(name) instanceof ModelBoneNametag nametagBone) return nametagBone.getNametag();
        return null;
    }*/
}
