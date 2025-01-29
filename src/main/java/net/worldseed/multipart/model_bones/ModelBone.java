package net.worldseed.multipart.model_bones;

import net.kyori.adventure.util.RGBLike;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.Quaternion;
import net.worldseed.multipart.animations.BoneAnimation;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ApiStatus.Internal
public interface ModelBone {
    default CompletableFuture<Void> spawn(Level level, Pos position) {
        return null;
    }

    Point applyTransform(Point p);

    void draw();

    void destroy();

    void setState(String state);

    String getName();

    BoneEntity getEntity();

    Point getOffset();

    Point getPosition();

    ModelBone getParent();

    void setParent(ModelBone parent);

    Point getPropogatedRotation();

    Point getPropogatedScale();

    Point calculateScale();

    Pos calculatePosition();

    Point calculateRotation(Point p, Point rotation, Point pivot);

    Point calculateScale(Point p, Point scale, Point pivot);

    Point calculateRotation();

    Point calculateFinalScale(Point p);

    Quaternion calculateFinalAngle(Quaternion q);

    void addChild(ModelBone child);

    void addAnimation(BoneAnimation animation);

    void addViewer(ServerPlayer player);

    void removeViewer(ServerPlayer player);

    void setGlobalScale(float scale);

    void removeGlowing();

    void setGlowing(RGBLike color);

    void removeGlowing(ServerPlayer player);

    void setGlowing(ServerPlayer player, RGBLike color);

    void attachModel(GenericModel model);

    List<GenericModel> getAttachedModels();

    void detachModel(GenericModel model);

    void setGlobalRotation(double yaw, double pitch);

    default void teleport(Point position) {}

    default @NotNull Collection<ModelBone> getChildren() {
        return List.of();
    };
}
