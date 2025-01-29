package net.worldseed.util.math;

import net.minecraft.world.phys.AABB;
import net.worldseed.util.PacketEntity;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

public interface Shape {
    boolean isOccluded(@NotNull Shape shape, @NotNull BlockFace face);

    /**
     * Returns true if the given block face is completely covered by this shape, false otherwise.
     * @param face The face to test
     */
    default boolean isFaceFull(@NotNull BlockFace face) {
        return false;
    }

    /**
     * Checks if two bounding boxes intersect.
     *
     * @param positionRelative Relative position of bounding box to check with
     * @param boundingBox      Bounding box to check for intersections with
     * @return is an intersection found
     */
    boolean intersectBox(@NotNull Point positionRelative, @NotNull AABB boundingBox);


    /**
     *
     * @param entity the entity to check the bounding box
     * @return true if this bounding box intersects with the entity, false otherwise
     */
    default boolean intersectEntity(@NotNull Point src, @NotNull PacketEntity entity) {
        return intersectBox(src.sub(entity.getPosition()), entity.getBoundingBox());
    }

    /**
     * Relative Start
     *
     * @return Start of shape
     */
    @NotNull Point relativeStart();

    /**
     * Relative End
     *
     * @return End of shape
     */
    @NotNull Point relativeEnd();
}