package net.worldseed.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.worldseed.multipart.ModelEngine;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DataMappings {

    public static Map<EntityType<?>, Entity> ENTITY_DATA_MAPPINGS = new HashMap<>();
    public static Map<Class<? extends Entity>, Map<String, EntityDataAccessor<?>>> DATA_ACCESSOR_MAPPINGS = new HashMap<>();
    public static Constructor<?> SET_PASSENGERS_PACKET;

    public static void generateMappings(Level level) {
        BuiltInRegistries.ENTITY_TYPE.forEach(type -> {
            Entity entity = type.create(level, EntitySpawnReason.LOAD);
            if (entity == null) {
                return;
            }
            ENTITY_DATA_MAPPINGS.put(type, entity);
            Map<String, EntityDataAccessor<?>> dataAccessors = new HashMap<>();
            for (Field declaredField : entity.getClass().getDeclaredFields()) {
                if (!declaredField.getName().startsWith("DATA_")) {
                    continue;
                }
                try {
                    declaredField.setAccessible(true);
                    if (declaredField.getType() != EntityDataAccessor.class) {
                        continue;
                    }
                    EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(entity);
                    dataAccessors.put(declaredField.getName(), accessor);
                    if (accessor != null) {
                        ModelEngine.getProvider().getLogger().info("[Models] Found data accessor " + declaredField.getName() + " for " + type.getDescriptionId());
                    } else {
                        ModelEngine.getProvider().getLogger().warning("[Models] Found null data accessor " + declaredField.getName() + " for " + type.getDescriptionId());
                    }
                }
                catch (IllegalAccessException e) {
                    ModelEngine.getProvider().getLogger().warning("[Models] Failed to access data accessor field " + declaredField.getName() + " for entity " + type.getDescriptionId());
                    e.printStackTrace();
                }
            }
            DATA_ACCESSOR_MAPPINGS.put(entity.getClass(), dataAccessors);

        });
        // Special case for players
        try {
            Player player = new ServerPlayer(MinecraftServer.getServer(), (ServerLevel) level, new GameProfile(UUID.randomUUID(), "MappingsGenerator"), ClientInformation.createDefault());
            ENTITY_DATA_MAPPINGS.put(EntityType.PLAYER, player);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // Add parent class data accessors
        Map<String, EntityDataAccessor<?>> entityDataAccessors = new HashMap<>();
        for (Field declaredField : Entity.class.getDeclaredFields()) {
            if (!declaredField.getName().startsWith("DATA_")) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(Entity.class);
                entityDataAccessors.put(declaredField.getName(), accessor);
                if (accessor != null) {
                    ModelEngine.getProvider().getLogger().info("[Models] Found data accessor " + declaredField.getName() + " for Entity base");
                } else {
                    ModelEngine.getProvider().getLogger().warning("[Models] Found null data accessor " + declaredField.getName() + " for Entity base");
                }
            }
            catch (IllegalAccessException e) {
                ModelEngine.getProvider().getLogger().warning("[Models] Failed to access data accessor field " + declaredField.getName() + " for Entity base");
                e.printStackTrace();
            }
        }
        DATA_ACCESSOR_MAPPINGS.put(Entity.class, entityDataAccessors);
        // Living too, who knows
        Map<String, EntityDataAccessor<?>> livingDataAccessors = new HashMap<>();
        for (Field declaredField : LivingEntity.class.getDeclaredFields()) {
            if (!declaredField.getName().startsWith("DATA_")) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(LivingEntity.class);
                livingDataAccessors.put(declaredField.getName(), accessor);
                if (accessor != null) {
                    ModelEngine.getProvider().getLogger().info("[Models] Found data accessor " + declaredField.getName() + " for LivingEntity base");
                } else {
                    ModelEngine.getProvider().getLogger().warning("[Models] Found null data accessor " + declaredField.getName() + " for LivingEntity base");
                }
            }
            catch (IllegalAccessException e) {
                ModelEngine.getProvider().getLogger().warning("[Models] Failed to access data accessor field " + declaredField.getName() + " for LivingEntity base");
                e.printStackTrace();
            }
        }
        DATA_ACCESSOR_MAPPINGS.put(LivingEntity.class, livingDataAccessors);
        // Special case for the display entity base class
        Map<String, EntityDataAccessor<?>> displayDataAccessors = new HashMap<>();
        for (Field declaredField : Display.class.getDeclaredFields()) {
            if (!declaredField.getName().startsWith("DATA_")) {
                continue;
            }
            try {
                declaredField.setAccessible(true);
                EntityDataAccessor<?> accessor = (EntityDataAccessor<?>) declaredField.get(Display.class);
                displayDataAccessors.put(declaredField.getName(), accessor);
                if (accessor != null) {
                    ModelEngine.getProvider().getLogger().info("[Models] Found data accessor " + declaredField.getName() + " for Display base");
                } else {
                    ModelEngine.getProvider().getLogger().warning("[Models] Found null data accessor " + declaredField.getName() + " for Display base");
                }
            }
            catch (IllegalAccessException e) {
                ModelEngine.getProvider().getLogger().warning("[Models] Failed to access data accessor field " + declaredField.getName() + " for Display base");
                e.printStackTrace();
            }
        }
        DATA_ACCESSOR_MAPPINGS.put(Display.class, displayDataAccessors);
        // Special case for the set passengers packet, as the only public constructor wants an entity
        try {
            Class<?> clazz = Class.forName("net.minecraft.network.protocol.game.ClientboundSetPassengersPacket");
            SET_PASSENGERS_PACKET = clazz.getDeclaredConstructor(FriendlyByteBuf.class);
            SET_PASSENGERS_PACKET.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static SynchedEntityData getSynchedEntityData(EntityType<?> type) {
        if (!ENTITY_DATA_MAPPINGS.containsKey(type)) {
            ModelEngine.getProvider().getLogger().warning("[Models] No entity found for type " + type.getDescriptionId());
            return null;
        }
        Entity entity = ENTITY_DATA_MAPPINGS.get(type);
        SynchedEntityData.Builder dataBuilder = new SynchedEntityData.Builder(entity);
        try {
            Method defineSynchedData = entity.getClass().getDeclaredMethod("defineSynchedData", SynchedEntityData.Builder.class);
            defineSynchedData.setAccessible(true);
            defineSynchedData.invoke(entity, dataBuilder);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            ModelEngine.getProvider().getLogger().warning("[Models] Failed to define synched data for " + type.getDescriptionId() + ": " + e.getMessage());
        }
        // Shared data
        dataBuilder.define(getAccessor(Entity.class, "DATA_SHARED_FLAGS_ID"), (byte) 0);
        dataBuilder.define(getAccessor(Entity.class, "DATA_AIR_SUPPLY_ID"), 300);
        dataBuilder.define(getAccessor(Entity.class, "DATA_CUSTOM_NAME"), Optional.empty());
        dataBuilder.define(getAccessor(Entity.class, "DATA_CUSTOM_NAME_VISIBLE"), false);
        dataBuilder.define(getAccessor(Entity.class, "DATA_SILENT"), false);
        dataBuilder.define(getAccessor(Entity.class, "DATA_NO_GRAVITY"), false);
        dataBuilder.define(getAccessor(Entity.class, "DATA_POSE"), Pose.STANDING);
        dataBuilder.define(getAccessor(Entity.class, "DATA_TICKS_FROZEN"), 0);
        // Build the data
        return dataBuilder.build();
    }

    public static EntityDataAccessor getAccessor(Class<? extends Entity> clazz, String dataAccessor) {
        ModelEngine.getProvider().getLogger().info("[Models] Looking for data accessor " + dataAccessor + " in class " + clazz.getName());
        if (!DATA_ACCESSOR_MAPPINGS.containsKey(clazz)) {
            ModelEngine.getProvider().getLogger().warning("[Models] No data accessors found for class " + clazz.getName());
            return null;
        }
        if (!DATA_ACCESSOR_MAPPINGS.get(clazz).containsKey(dataAccessor)) {
            ModelEngine.getProvider().getLogger().warning("[Models] No data accessor found for " + dataAccessor + " in class " + clazz.getName());
            // Print all we have for now
            DATA_ACCESSOR_MAPPINGS.get(clazz).forEach((key, value) -> ModelEngine.getProvider().getLogger().info("[Models] Data accessor " + key + " for " + clazz.getName()));
            return null;
        }
        return DATA_ACCESSOR_MAPPINGS.get(clazz).get(dataAccessor);
    }


}
