package io.github.techtastic.newtons_nonsense.util;

import net.minecraft.world.phys.Vec3;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.lwjgl.system.MemoryStack;
import physx.common.PxQuat;
import physx.common.PxVec3;

public class MCConversions {
    public static Vec3 fromPxVec3(PxVec3 vec) {
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public static PxVec3 toPxVec3(Vec3 vec) {
        return new PxVec3((float) vec.x,(float) vec.y,(float) vec.z);
    }

    public static PxVec3 toPxVec3(Vec3 vec, MemoryStack stack) {
        return PxVec3.createAt(stack, MemoryStack::nmalloc, (float) vec.x,(float) vec.y,(float) vec.z);
    }

    ////////////////////////////////////////////

    public static Quaterniondc fromPxQuat(PxQuat quat) {
        return new Quaterniond(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public static PxQuat toPxQuat(Quaterniondc quat) {
        return new PxQuat((float) quat.x(),(float) quat.y(),(float) quat.z(),(float) quat.w());
    }

    public static PxQuat toPxQuat(Quaterniondc quat, MemoryStack stack) {
        return PxQuat.createAt(stack, MemoryStack::nmalloc, (float) quat.x(),(float) quat.y(),(float) quat.z(),(float) quat.w());
    }
}
