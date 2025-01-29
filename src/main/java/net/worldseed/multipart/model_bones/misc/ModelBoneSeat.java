package net.worldseed.multipart.model_bones.misc;

import net.kyori.adventure.util.RGBLike;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.Quaternion;
import net.worldseed.multipart.model_bones.BoneEntity;
import net.worldseed.multipart.model_bones.ModelBone;
import net.worldseed.multipart.model_bones.ModelBoneImpl;
import net.worldseed.multipart.model_bones.bone_types.RideableBone;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import net.worldseed.util.math.Vec;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS;

public class ModelBoneSeat extends ModelBoneImpl implements RideableBone {

    public ModelBoneSeat(Point pivot, String name, Point rotation, GenericModel model, float scale) {
        super(pivot, name, rotation, model, scale);

        if (this.offset != null) {
            entity = new BoneEntity(EntityType.ARMOR_STAND, model, name);
            SynchedEntityData synchedEntityData = entity.getSynchedEntityData();
            synchedEntityData.set(DATA_CLIENT_FLAGS, entity.setBit(synchedEntityData.get(DATA_CLIENT_FLAGS), 16, true)); // Marker
            entity.setInvisible(true);
        }
    }

    @Override
    public void addViewer(ServerPlayer player) {
        if (entity != null) entity.addNewViewer(player);
    }

    @Override
    public void removeViewer(ServerPlayer player) {
        if (entity != null) entity.removeViewer(player);
    }

    @Override
    public void removeGlowing() {

    }

    @Override
    public void setGlowing(RGBLike color) {

    }

    @Override
    public void removeGlowing(ServerPlayer player) {

    }

    @Override
    public void setGlowing(ServerPlayer player, RGBLike color) {

    }

    @Override
    public void attachModel(GenericModel model) {
        throw new UnsupportedOperationException("Cannot attach a model to a seat");
    }

    @Override
    public List<GenericModel> getAttachedModels() {
        return List.of();
    }

    @Override
    public void detachModel(GenericModel model) {
        throw new UnsupportedOperationException("Cannot detach a model from a seat");
    }

    @Override
    public void setGlobalRotation(double yaw, double pitch) {

    }

    @Override
    public void setState(String state) {
    }

    @Override
    public Point getPosition() {
        return calculatePosition();
    }

    public CompletableFuture<Void> spawn(Level level, Point position) {
        if (this.offset != null) {
            entity.setInvisible(true);
            entity.setSilent(true);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public Pos calculatePosition() {
        if (this.offset == null) return Pos.ZERO;

        var rotation = calculateRotation();

        var p = applyTransform(this.offset);
        p = calculateGlobalRotation(p);
        Pos endPos = Pos.fromPoint(p);

        return endPos
                .div(4, 4, 4).mul(scale)
                .add(model.getPosition())
                .add(model.getGlobalOffset())
                .withView((float) -rotation.y(), (float) rotation.x());
    }

    @Override
    public Point calculateRotation() {
        Quaternion q = new Quaternion(new Vec(0, 180 - this.model.getGlobalRotation(), 0));
        return q.toEulerYZX();
    }

    @Override
    public Point calculateScale() {
        return Vec.ZERO;
    }

    public void draw() {
        this.children.forEach(ModelBone::draw);
        if (this.offset == null) return;

        Pos found = calculatePosition();

        // TODO: needed by minestom?
        entity.setView(found.yaw(), found.pitch());
        entity.teleport(found);
    }

    @Override
    public void addPassenger(Entity entity) {
        this.entity.addPassenger(entity);
    }

    @Override
    public void removePassenger(Entity entity) {
        this.entity.removePassenger(entity);
    }

    @Override
    public Set<Entity> getPassengers() {
        return entity.getPassengers();
    }
}
