package net.worldseed.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.worldseed.multipart.GenericModel;
import net.worldseed.util.math.Point;
import net.worldseed.util.math.Pos;
import net.worldseed.util.math.Shape;
import net.worldseed.util.math.Vec;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PacketEntity implements Shape {

    protected EntityType entityType;
    protected int entityId;
    protected UUID uuid;
    protected Set<ServerPlayer> viewers = new HashSet<>();
    protected SynchedEntityData synchedEntityData;
    protected AABB boundingBox;
    protected boolean isRemoved = false;
    protected EntityDataAccessor<Byte> sharedFlags;

    protected GenericModel model;

    protected Pos position;
    protected Vec positionDelta;

    public PacketEntity(EntityType entityType, int entityId, GenericModel model) {
        synchedEntityData = DataMappings.getSynchedEntityData(entityType);
        this.entityType = entityType;
        this.entityId = entityId;
        this.model = model;
        this.position = model.getPosition();
        this.positionDelta = Vec.ZERO;
        this.uuid = UUID.randomUUID();
        this.sharedFlags = DataMappings.getAccessor(Entity.class, "DATA_SHARED_FLAGS_ID");
    }

    public void addNewViewer(ServerPlayer serverPlayer) {
        viewers.add(serverPlayer);
        Pos pos = model.getPosition().withView(position.yaw(), 0);
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(entityId, uuid, pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch(), entityType, 0, Vec3.ZERO, 0);
        serverPlayer.connection.send(addEntityPacket);
        if (synchedEntityData != null) {
            ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(entityId, synchedEntityData.packAll());
            serverPlayer.connection.send(entityDataPacket);
        }
        System.out.println("Added viewer: " + serverPlayer.getName().getString() + " for entity: " + entityId + " (" + entityType + ") at position: " + pos);
    }

    public void removeViewer(ServerPlayer serverPlayer) {
        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(entityId);
        serverPlayer.connection.send(removeEntityPacket);
        viewers.remove(serverPlayer);
        System.out.println("Removed viewer: " + serverPlayer.getName().getString() + " for entity: " + entityId + " (" + entityType + ")");
    }

    public void remove() {
        isRemoved = true;
        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(entityId);
        sendPacketToAllViewers(removeEntityPacket);
        System.out.println("Entity removed: " + entityId + " (" + entityType + ")");
    }

    public void resendEntityData(ServerPlayer serverPlayer) {
        if (synchedEntityData != null) {
            ClientboundSetEntityDataPacket entityDataPacket = new ClientboundSetEntityDataPacket(entityId, synchedEntityData.packAll());
            serverPlayer.connection.send(entityDataPacket);
        }
    }

    public void resendEntityDataForAll() {
        for (ServerPlayer viewer : viewers) {
            resendEntityData(viewer);
        }
    }

    protected void setSharedFlag(int flag, boolean set) {
        byte b = synchedEntityData.get(sharedFlags);
        if (set) {
            synchedEntityData.set(sharedFlags, (byte)(b | 1 << flag));
        } else {
            synchedEntityData.set(sharedFlags, (byte)(b & ~(1 << flag)));
        }
    }

    public void teleport(@NotNull Pos pos) {
        position = pos;
        Vec3 position = new Vec3(pos.x(), pos.y(), pos.z());
        Vec3 delta = new Vec3(positionDelta.x(), positionDelta.y(), positionDelta.z());
        PositionMoveRotation positionMoveRotation = new PositionMoveRotation(position, delta, pos.yaw(), pos.pitch());
        ClientboundTeleportEntityPacket teleportEntityPacket = new ClientboundTeleportEntityPacket(entityId, positionMoveRotation, new HashSet<>(), false);
        sendPacketToAllViewers(teleportEntityPacket);
    }

    public void setView(double yaw, double pitch) {
        position = position.withView((float) yaw, (float) pitch);
        ClientboundMoveEntityPacket moveEntityPacket = new ClientboundMoveEntityPacket.Rot(entityId, (byte) (yaw * 256 / 360), (byte) (pitch * 256 / 360), true);
        sendPacketToAllViewers(moveEntityPacket);
    }

    private void sendPacketToAllViewers(Packet<?> packet) {
        for (ServerPlayer viewer : viewers) {
            viewer.connection.send(packet);
        }
    }

    public int getEntityId() {
        return entityId;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    public SynchedEntityData getSynchedEntityData() {
        return synchedEntityData;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setGlowing(boolean glowing) {
        setSharedFlag(6, glowing);
        resendEntityDataForAll();
    }

    public Pos getPosition() {
        return position;
    }

    public AABB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AABB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public double getDistanceSquared(Pos newPos) {
        return position.distanceSquared(newPos);
    }

    public void addPassenger(org.bukkit.entity.Entity entity) {
    }

    public void removePassenger(org.bukkit.entity.Entity entity) {

    }

    public Set<org.bukkit.entity.Entity> getPassengers() {
        return null;
    }

    @Override
    public boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face) {
        return false;
    }

    @Override
    public boolean intersectBox(@NotNull Point positionRelative, @NotNull AABB boundingBox) {
        return false;
    }

    @Override
    public @NotNull Point relativeStart() {
        return new Vec(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
    }

    @Override
    public @NotNull Point relativeEnd() {
        return new Vec(boundingBox.minX, boundingBox.minY, boundingBox.minZ);
    }

    public void setInvisible(boolean b) {
        setSharedFlag(5, b);
        resendEntityDataForAll();
    }

    public void setSilent(boolean b) {
        setSharedFlag(4, b);
        resendEntityDataForAll();
    }

    // Util method for client flags
    public byte setBit(byte oldBit, int offset, boolean value) {
        if (value) {
            oldBit = (byte)(oldBit | offset);
        } else {
            oldBit = (byte)(oldBit & ~offset);
        }

        return oldBit;
    }
}
