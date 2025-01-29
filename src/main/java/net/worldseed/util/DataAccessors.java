package net.worldseed.util;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Interaction;

// Utility class for easy access to data accessors
@SuppressWarnings("rawtypes")
public class DataAccessors {
    public static EntityDataAccessor display_translationData = DataMappings.getAccessor(Display.class, "DATA_TRANSLATION_ID");
    public static EntityDataAccessor display_scaleData = DataMappings.getAccessor(Display.class, "DATA_SCALE_ID");
    public static EntityDataAccessor display_leftRotationData = DataMappings.getAccessor(Display.class, "DATA_LEFT_ROTATION_ID");
    public static EntityDataAccessor display_rightRotationData = DataMappings.getAccessor(Display.class, "DATA_RIGHT_ROTATION_ID");
    public static EntityDataAccessor display_posRotInterpolationData = DataMappings.getAccessor(Display.class, "DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID"); // Int
    public static EntityDataAccessor display_transformationInterpolationData = DataMappings.getAccessor(Display.class, "DATA_TRANSFORMATION_INTERPOLATION_DURATION_ID"); // Int
    public static EntityDataAccessor display_viewRangeData = DataMappings.getAccessor(Display.class, "DATA_VIEW_RANGE_ID"); // Float
    public static EntityDataAccessor itemDisplay_viewContextData = DataMappings.getAccessor(Display.ItemDisplay.class, "DATA_ITEM_DISPLAY_ID"); // Byte
    public static EntityDataAccessor itemDisplay_itemStackData = DataMappings.getAccessor(Display.ItemDisplay.class, "DATA_ITEM_STACK_ID");
    public static EntityDataAccessor interaction_widthData = DataMappings.getAccessor(Interaction.class, "DATA_WIDTH_ID");
    public static EntityDataAccessor interaction_heightData = DataMappings.getAccessor(Interaction.class, "DATA_HEIGHT_ID");
}
