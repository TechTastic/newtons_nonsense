package io.github.techtastic.newtons_nonsense.util;

import io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryStack;
import physx.PxTopLevelFunctions;
import physx.common.PxBoundedData;
import physx.common.PxQuat;
import physx.common.PxVec3;
import physx.cooking.PxConvexFlagEnum;
import physx.cooking.PxConvexFlags;
import physx.cooking.PxConvexMeshDesc;
import physx.cooking.PxCookingParams;
import physx.extensions.ConvexMeshSupport;
import physx.geometry.*;
import physx.physics.PxMaterial;
import physx.physics.PxPhysics;
import physx.physics.PxShape;
import physx.physics.PxShapeExt;
import physx.support.PxArray_PxVec3;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.List;

import static io.github.techtastic.newtons_nonsense.physics.pipeline.Backstage.PX_PHYSICS;

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

    @Nullable
    public static PxGeometry toPxGeometry(VoxelShape voxel, PxCookingParams params) {
        if (voxel.isEmpty())
            return null;

        List<AABB> boxes = voxel.toAabbs();

        try (MemoryStack mem = MemoryStack.stackPush()){
            if (boxes.size() == 1) {
                AABB box = boxes.getFirst();
                return Backstage.createBoxGeometry((float) (box.getXsize() / 2), (float) (box.getYsize() / 2), (float) (box.getZsize() / 2), mem);
            }

            int totalVertices = boxes.size() * 8;
            PxArray_PxVec3 points = PxArray_PxVec3.createAt(mem, MemoryStack::nmalloc);

            for (AABB box : boxes) {
                // Bottom corners
                PxVec3 tmp = PxVec3.createAt(mem, MemoryStack::nmalloc, (float) box.minX, (float) box.minY, (float) box.minZ);
                points.pushBack(tmp);
                tmp.setX((float) box.maxX); tmp.setY((float) box.minY); tmp.setZ((float) box.minZ);
                points.pushBack(tmp);
                tmp.setX((float) box.maxX); tmp.setY((float) box.minY); tmp.setZ((float) box.maxZ);
                points.pushBack(tmp);
                tmp.setX((float) box.minX); tmp.setY((float) box.minY); tmp.setZ((float) box.maxZ);
                points.pushBack(tmp);

                // Top corners
                tmp.setX((float) box.minX); tmp.setY((float) box.maxY); tmp.setZ((float) box.minZ);
                points.pushBack(tmp);
                tmp.setX((float) box.maxX); tmp.setY((float) box.maxY); tmp.setZ((float) box.minZ);
                points.pushBack(tmp);
                tmp.setX((float) box.maxX); tmp.setY((float) box.maxY); tmp.setZ((float) box.maxZ);
                points.pushBack(tmp);
                tmp.setX((float) box.minX); tmp.setY((float) box.maxY); tmp.setZ((float) box.maxZ);
                points.pushBack(tmp);
            }

            PxBoundedData pointsData = PxBoundedData.createAt(mem, MemoryStack::nmalloc);
            pointsData.setCount(totalVertices);
            pointsData.setStride(PxVec3.SIZEOF);
            pointsData.setData(points.begin());

            PxConvexMeshDesc meshDesc = PxConvexMeshDesc.createAt(mem, MemoryStack::nmalloc);
            meshDesc.setPoints(pointsData);
            meshDesc.getFlags().raise(PxConvexFlagEnum.eCOMPUTE_CONVEX);

            PxConvexMesh mesh = PxTopLevelFunctions.CreateConvexMesh(params, meshDesc);

            PxConvexMeshGeometry geom = new PxConvexMeshGeometry(mesh);
            mesh.release();

            return geom;
        }
    }

    @Nullable
    public static PxShape toPxShape(VoxelShape voxel, PxMaterial material, PxCookingParams params) {
        PxGeometry geom = toPxGeometry(voxel, params);
        if (geom == null)
            return null;
        return PX_PHYSICS.createShape(geom, material);
    }
}