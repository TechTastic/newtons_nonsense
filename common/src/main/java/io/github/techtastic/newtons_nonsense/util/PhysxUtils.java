package io.github.techtastic.newtons_nonsense.util;

import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import physx.common.PxQuat;
import physx.common.PxVec3;
import physx.geometry.PxBoxGeometry;
import physx.geometry.PxCustomGeometry;
import physx.geometry.PxGeometryHolder;
import physx.geometry.SimpleCustomGeometryCallbacks;
import physx.physics.PxShape;

public class PhysxUtils {
    public static Vec3 toVec3(PxVec3 vec) {
        return new Vec3(vec.getX(), vec.getY(), vec.getZ());
    }

    public static PxVec3 toPxVec3(double x, double y, double z) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            return PxVec3.createAt(mem, MemoryStack::nmalloc, (float) x, (float) y, (float) z);
        }
    }

    public static PxVec3 toPxVec3(Vec3 vec) {
        return toPxVec3((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public static PxVec3 toPxVec3(Vec3i vec) {
        return toPxVec3((float) vec.getX(), (float) vec.getY(), (float) vec.getZ());
    }

    public static PxVec3 toPxVec3(Vector3dc vec) {
        return toPxVec3((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public static PxVec3 toPxVec3(Vector3fc vec) {
        return toPxVec3(vec.x(), vec.y(), vec.z());
    }

    public static PxVec3 toPxVec3(Vector3ic vec) {
        return toPxVec3((float) vec.x(), (float) vec.y(), (float) vec.z());
    }



    public static Quaterniondc toQuat(PxQuat quat) {
        return new Quaterniond(quat.getX(), quat.getY(), quat.getZ(), quat.getW());
    }

    public static PxQuat toPxQuat(double x, double y, double z, double w) {
        try (MemoryStack mem = MemoryStack.stackPush()) {
            return PxQuat.createAt(mem, MemoryStack::nmalloc, (float) x, (float) y, (float) z, (float) w);
        }
    }

    public static PxQuat toPxQuat(Quaterniondc quat) {
        return toPxQuat((float) quat.x(), (float) quat.y(), (float) quat.z(), (float) quat.w());
    }

    public static PxQuat toPxQuat(Quaternionfc quat) {
        return toPxQuat(quat.x(), quat.y(), quat.z(), quat.w());
    }


    public static PxShape toPxShape(VoxelShape voxel) {
        PxShape shape = null;

        //SimpleCustomGeometryCallbacks

        //new PxCustomGeometry();

        return null;
    }
}