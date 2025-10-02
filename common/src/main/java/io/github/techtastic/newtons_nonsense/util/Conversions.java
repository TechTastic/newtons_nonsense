package io.github.techtastic.newtons_nonsense.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import physx.common.PxQuat;
import physx.common.PxVec3;

public class Conversions {
    public static Vec3 fromPxVec(PxVec3 vec) {
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public static Quaterniond fromPxQuat(PxQuat quat) {
        return new Quaterniond(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }
}
