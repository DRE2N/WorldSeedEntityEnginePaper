package net.worldseed.multipart.animations;


import net.worldseed.util.math.Point;
import net.worldseed.util.math.Vec;

public interface FrameProvider {
    Point RotationMul = new Vec(-1, -1, 1);
    Point TranslationMul = new Vec(-1, 1, 1);

    Point getFrame(int tick);
}
