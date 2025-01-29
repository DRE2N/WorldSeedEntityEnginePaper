package net.worldseed.multipart.model_bones.bone_types;

import net.worldseed.multipart.model_bones.ModelBone;
import net.worldseed.util.PacketEntity;

public interface NametagBone extends ModelBone {
    PacketEntity getNametag();
}