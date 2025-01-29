package net.worldseed.multipart.model_bones.display_entity;

import com.mojang.math.Transformation;
import io.papermc.paper.datacomponent.DataComponentTypes;
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
import net.worldseed.multipart.model_bones.ModelBoneViewable;
import net.worldseed.util.PacketEntity;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.worldseed.util.DataAccessors.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ModelBonePartDisplay extends ModelBoneImpl implements ModelBoneViewable {
    private final List<GenericModel> attached = new ArrayList<>();
    private PacketEntity baseEntity;

    public ModelBonePartDisplay(Point pivot, String name, Point rotation, GenericModel model, float scale) {
        super(pivot, name, rotation, model, scale);

        if (this.offset != null) {
            entity = new BoneEntity(EntityType.ITEM_DISPLAY, model, name);
            SynchedEntityData synchedEntityData = entity.getSynchedEntityData();
            synchedEntityData.set(display_posRotInterpolationData, 2);
            synchedEntityData.set(display_transformationInterpolationData, 2);
            synchedEntityData.set(display_viewRangeData, 1000f); // This needs to be a float
            synchedEntityData.set(itemDisplay_viewContextData, (byte) 1); // Third person left hand, as a byte
        }
    }

    @Override
    public void addViewer(ServerPlayer player) {
        if (entity != null) entity.addNewViewer(player);
        if (baseEntity != null) baseEntity.addNewViewer(player);
        this.attached.forEach(model -> model.addViewer(player));
    }

    @Override
    public void removeGlowing() {
        if (entity != null) {
            entity.setGlowing(false);
        }

        this.attached.forEach(GenericModel::removeGlowing);
    }


    public void setGlowing(RGBLike color) {
        /*if (entity != null) {
            int rgb = 0;
            rgb |= color.red() << 16;
            rgb |= color.green() << 8;
            rgb |= color.blue();

            var meta = (ItemDisplayMeta) this.stand.getEntityMeta();
            meta.setHasGlowingEffect(true);
            meta.setGlowColorOverride(rgb);
        }

        this.attached.forEach(model -> model.setGlowing(color));*/
    }

    public void removeGlowing(ServerPlayer player) {
        /*if (entity == null)
            return;

        EntityMetaDataPacket oldMetadataPacket = this.stand.getMetadataPacket();
        Map<Integer, Metadata.Entry<?>> oldEntries = oldMetadataPacket.entries();
        byte previousFlags = oldEntries.containsKey(0)
                ? (byte) oldEntries.get(0).value()
                : 0;

        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(oldMetadataPacket.entries());
        entries.put(0, Metadata.Byte((byte) (previousFlags & ~0x40)));
        entries.put(22, Metadata.VarInt(-1));

        player.sendPacket(new EntityMetaDataPacket(this.stand.getEntityId(), entries));
        this.attached.forEach(model -> model.removeGlowing(player));*/
    }

    @Override
    public void setGlowing(ServerPlayer player, RGBLike color) {
        /*if (entity == null)
            return;

        int rgb = 0;
        rgb |= color.red() << 16;
        rgb |= color.green() << 8;
        rgb |= color.blue();

        EntityMetaDataPacket oldMetadataPacket = this.stand.getMetadataPacket();
        Map<Integer, Metadata.Entry<?>> oldEntries = oldMetadataPacket.entries();
        byte previousFlags = oldEntries.containsKey(0)
                ? (byte) oldEntries.get(0).value()
                : 0;

        Map<Integer, Metadata.Entry<?>> entries = new HashMap<>(oldEntries);
        entries.put(0, Metadata.Byte((byte) (previousFlags | 0x40)));
        entries.put(22, Metadata.VarInt(rgb));

        player.sendPacket(new EntityMetaDataPacket(this.stand.getEntityId(), entries));
        this.attached.forEach(model -> model.setGlowing(player, color));*/
    }

    @Override
    public void attachModel(GenericModel model) {
        attached.add(model);
    }

    @Override
    public List<GenericModel> getAttachedModels() {
        return attached;
    }

    @Override
    public void detachModel(GenericModel model) {
        attached.remove(model);
    }

    @Override
    public void setGlobalRotation(double yaw, double pitch) {
        if (entity != null) {
            var correctYaw = (180 + yaw + 360) % 360;
            var correctPitch = (pitch + 360) % 360;
            entity.setView((float) correctYaw, (float) correctPitch);
        }
    }

    public void removeViewer(ServerPlayer player) {
        if (entity != null) entity.removeViewer(player);
        if (baseEntity != null) baseEntity.removeViewer(player);
        this.attached.forEach(model -> model.removeViewer(player));
    }

    @Override
    public void destroy() {
        super.destroy();
        if (baseEntity != null) {
            baseEntity.remove();
        }
    }

    @Override
    public Pos calculatePosition() {
        return Pos.fromPoint(model.getPosition()).withView(0, 0);
    }

    private Pos calculatePositionInternal() {
        if (this.offset == null) return Pos.ZERO;
        Point p = this.offset;
        p = applyTransform(p);
        return Pos.fromPoint(p).div(4).mul(scale).withView(0, 0);
    }

    @Override
    public Point calculateRotation() {
        Quaternion q = calculateFinalAngle(new Quaternion(getPropogatedRotation()));
        return q.toEuler();
    }

    @Override
    public Point calculateScale() {
        return calculateFinalScale(getPropogatedScale());
    }

    @Override
    public void teleport(Point position) {
        if (baseEntity != null) baseEntity.teleport(Pos.fromPoint(position));
    }

    @SuppressWarnings("unchecked")
    public void draw() {
        this.children.forEach(ModelBone::draw);
        if (this.offset == null) return;

        if (entity != null) {
            var position = calculatePositionInternal();
            var scale = calculateScale();

            if (entity.getEntityType() == EntityType.ITEM_DISPLAY) {

                Quaternion q = calculateFinalAngle(new Quaternion(getPropogatedRotation()));

                Transformation transformation = getTransformation(position, q, scale);

                SynchedEntityData synchedEntityData = entity.getSynchedEntityData();
                synchedEntityData.set(display_translationData, transformation.getTranslation());
                synchedEntityData.set(display_scaleData, transformation.getScale());
                synchedEntityData.set(display_leftRotationData, transformation.getLeftRotation());
                synchedEntityData.set(display_rightRotationData, transformation.getRightRotation());

                attached.forEach(model -> {
                    model.setPosition(this.model.getPosition().add(calculateGlobalRotation(position)));
                    model.setGlobalRotation(-q.toEuler().x() + this.model.getGlobalRotation());
                    model.draw();
                });
            }
        }
    }

    private static @NotNull Transformation getTransformation(Pos position, Quaternion q, Point scale) {
        Vector3f transformTranslation = new Vector3f((float) position.x(), (float) position.y(), (float) position.z());
        Quaternionf transformLeftRotation = new Quaternionf(0, 0, 0, 1);
        Quaternionf transformRightRotation = new Quaternionf(q.x(), q.y(), q.z(), q.w());
        Vector3f transformScale = new Vector3f((float) scale.x(), (float) scale.y(), (float) scale.z());
        Transformation transformation = new Transformation(transformTranslation, transformLeftRotation, transformScale, transformRightRotation);
        return transformation;
    }

    @Override
    public CompletableFuture<Void> spawn(Level level, Pos position) {
        var correctLocation = (180 + this.model.getGlobalRotation() + 360) % 360;
        return super.spawn(level, Pos.fromPoint(position).withYaw((float) correctLocation)).whenCompleteAsync((v, e) -> {
            if (e != null) {
                e.printStackTrace();
                return;
            }

            if (!(this.getParent() instanceof ModelBonePartDisplay)) {
                baseEntity = model.generateRoot();
            }
        });
    }

    @Override
    public void setState(String state) {
        if (entity != null && entity.getEntityType() == EntityType.ITEM_DISPLAY) {
            if (state.equals("invisible")) {
                setItemStack(state);
                return;
            }

            ItemStack item = this.items.get(state);
            if (item != null) {
                setItemStack(state);
            } else {
                setItemStack(state);
            }
        }
    }

    private void setItemStack(String state) {
        ItemStack item = new ItemStack(Material.MAGMA_CREAM);
        NamespacedKey modelKey = new NamespacedKey("erethon", "mobs/" + model.getId() + "/" + state + "/" + name);
        item.setData(DataComponentTypes.ITEM_MODEL, modelKey);
        if (entity != null) {
            SynchedEntityData synchedEntityData = entity.getSynchedEntityData();
            synchedEntityData.set(itemDisplay_itemStackData, CraftItemStack.asNMSCopy(item));
            entity.resendEntityDataForAll();
        }
    }

    @Override
    public Point getPosition() {
        return calculatePositionInternal().add(model.getPosition());
    }
}
