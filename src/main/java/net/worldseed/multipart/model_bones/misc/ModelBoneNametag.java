package net.worldseed.multipart.model_bones.misc;

import net.kyori.adventure.util.RGBLike;
import net.minecraft.server.level.ServerPlayer;
import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.model_bones.ModelBoneImpl;
import net.worldseed.multipart.model_bones.bone_types.NametagBone;
import net.worldseed.util.PacketEntity;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import net.worldseed.util.math.Vec;

import java.util.List;

public class ModelBoneNametag extends ModelBoneImpl implements NametagBone {
    private PacketEntity bound;

    public ModelBoneNametag(Point pivot, String name, Point rotation, GenericModel model, float scale) {
        super(pivot, name, rotation, model, scale);
    }

    @Override
    public void addViewer(ServerPlayer player) {
        if (this.bound != null) this.bound.addNewViewer(player);
    }

    @Override
    public void removeViewer(ServerPlayer player) {
        if (this.bound != null) this.bound.removeViewer(player);
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
        throw new UnsupportedOperationException("Cannot attach a model to a nametag");
    }

    @Override
    public List<GenericModel> getAttachedModels() {
        return List.of();
    }

    @Override
    public void detachModel(GenericModel model) {
        throw new UnsupportedOperationException("Cannot detach a model from a nametag");
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

    public void draw() {
        if (this.offset == null || bound == null) return;
        bound.teleport(calculatePosition());
    }

    @Override
    public Pos calculatePosition() {
        if (this.bound == null) return Pos.ZERO;
        if (this.offset == null) return Pos.ZERO;

        Point p = this.offset;
        p = applyTransform(p);
        p = calculateGlobalRotation(p);

        return Pos.fromPoint(p)
                .div(4, 4, 4).mul(scale)
                .add(model.getPosition())
                .add(model.getGlobalOffset());
    }

    @Override
    public Point calculateRotation() {
        return Vec.ZERO;
    }

    @Override
    public Point calculateScale() {
        return Vec.ZERO;
    }

    @Override
    public PacketEntity getNametag() {
        return this.bound;
    }
}
