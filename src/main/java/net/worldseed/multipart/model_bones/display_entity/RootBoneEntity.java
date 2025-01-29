package net.worldseed.multipart.model_bones.display_entity;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.worldseed.multipart.GenericModel;
import net.worldseed.multipart.model_bones.BoneEntity;
import net.worldseed.multipart.model_bones.ModelBone;
import net.worldseed.util.PacketEntity;
import net.worldseed.util.DataMappings;

import java.lang.reflect.InvocationTargetException;

import static net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS;

public class RootBoneEntity extends BoneEntity {
    public RootBoneEntity(GenericModel model) {
        super(EntityType.ARMOR_STAND, model, "root");
        synchedEntityData.set(DATA_CLIENT_FLAGS, this.setBit(synchedEntityData.get(DATA_CLIENT_FLAGS), 16, true)); // Marker
        setSharedFlag(5, true); // Invisible
    }

    @Override
    public void addNewViewer(ServerPlayer serverPlayer) {
        super.addNewViewer(serverPlayer);
        int[] passengerIds = this.getModel().getParts().stream()
                .map(ModelBone::getEntity)
                .filter(e -> e != null && e.getEntityType() == EntityType.ITEM_DISPLAY)
                .map(PacketEntity::getEntityId)
                .mapToInt(Integer::intValue)
                .toArray();
        // The public constructor expects an entity, so we have to use the private constructor
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        buf.writeVarInt(entityId);
        buf.writeVarIntArray(passengerIds);
        try {
            ClientboundSetPassengersPacket setPassengersPacket = (ClientboundSetPassengersPacket) DataMappings.SET_PASSENGERS_PACKET.newInstance(buf);
            serverPlayer.connection.send(setPassengersPacket);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
