package io.github.techtastic.newtons_nonsense.physics.util;

import com.google.common.util.concurrent.AtomicDouble;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import physx.common.PxBounds3;
import physx.common.PxTransform;
import physx.common.PxVec3;
import physx.extensions.*;
import physx.geometry.*;
import physx.physics.*;

import java.util.concurrent.atomic.AtomicBoolean;

public class VoxelShapeGeometry extends PxCustomGeometry {
    private final VoxelShape shape;
    private final VoxelShapeCallbacks callbacks;

    public VoxelShapeGeometry(VoxelShape shape) {
        super(new VoxelShapeCallbacks(shape));
        this.shape = shape;
        this.callbacks = new VoxelShapeCallbacks(this.shape);
    }

    private static class VoxelShapeCallbacks extends SimpleCustomGeometryCallbacks {
        private final VoxelShape shape;

        private VoxelShapeCallbacks(VoxelShape shape) {
            this.shape = shape;
        }

        @Override
        public PxBounds3 getLocalBoundsImpl(PxGeometry geometry) {
            if (this.shape.isEmpty())
                return new PxBounds3(new PxVec3(0, 0, 0), new PxVec3(0, 0, 0));

            AABB bounds = this.shape.bounds();
            return new PxBounds3(
                    new PxVec3((float) bounds.minX, (float) bounds.minY, (float) bounds.minZ),
                    new PxVec3((float) bounds.maxX, (float) bounds.maxY, (float) bounds.maxZ)
            );
        }

        @Override
        public boolean generateContactsImpl(PxGeometry selfGeom, PxGeometry otherGeom, PxTransform selfPose, PxTransform otherPose, float contactDistance, float meshContactMargin, float toleranceLength, PxContactBuffer contactBuffer) {
            AtomicBoolean hasContacts = new AtomicBoolean(false);

            this.shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                BoxSupport boxSupport = new BoxSupport(new PxVec3(
                        (float)(maxX - minX) / 2.0f,
                        (float)(maxY - minY) / 2.0f,
                        (float)(maxZ - minZ) / 2.0f
                ));

                PxVec3 boxCenter = new PxVec3(
                        (float)(minX + maxX) / 2.0f,
                        (float)(minY + maxY) / 2.0f,
                        (float)(minZ + maxZ) / 2.0f
                );

                PxTransform boxPose = new PxTransform(selfPose.transform(boxCenter), selfPose.getQ());

                Support otherSupport = null;
                if (otherGeom instanceof PxBoxGeometry box)
                    otherSupport = new BoxSupport(box.getHalfExtents());
                else if (otherGeom instanceof PxSphereGeometry sphere)
                    otherSupport = new SphereSupport(sphere.getRadius());
                else if (otherGeom instanceof PxCapsuleGeometry capsule)
                    otherSupport = new CapsuleSupport(capsule.getRadius(), capsule.getHalfHeight());
                else
                    return;

                if (PxGjkQueryExt.generateContacts(boxSupport, otherSupport, boxPose, otherPose, contactDistance, toleranceLength, contactBuffer))
                    hasContacts.set(true);
            });

            return hasContacts.get();
        }

        @Override
        public int raycastImpl(PxVec3 origin, PxVec3 unitDir, PxGeometry geom, PxTransform pose, float maxDist, PxHitFlags hitFlags, int maxHits, PxGeomRaycastHit rayHits, int stride) {
            AtomicDouble closestDistance = new AtomicDouble(Double.MAX_VALUE);
            AtomicBoolean hasHit = new AtomicBoolean(false);
            PxRaycastHit closestHit = null;

            PxTransform invPose = pose.getInverse();
            PxVec3 localOrigin = invPose.transform(origin);
            PxVec3 localDir = invPose.transform(unitDir);

            this.shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                BoxSupport boxSupport = new BoxSupport(new PxVec3(
                        (float)(maxX - minX) / 2.0f,
                        (float)(maxY - minY) / 2.0f,
                        (float)(maxZ - minZ) / 2.0f
                ));

                PxVec3 boxCenter = new PxVec3(
                        (float)(minX + maxX) / 2.0f,
                        (float)(minY + maxY) / 2.0f,
                        (float)(minZ + maxZ) / 2.0f
                );

                if (PxGjkQuery.raycast(boxSupport, pose, localOrigin, localDir, maxDist, new PxGjkQueryRaycastResult()))
                    hasHit.set(true);
            });

            return hasHit.get();

            //return super.raycastImpl(origin, unitDir, geom, pose, maxDist, hitFlags, maxHits, rayHits, stride);
        }

        @Override
        public boolean overlapImpl(PxGeometry geom0, PxTransform pose0, PxGeometry geom1, PxTransform pose1) {
            return super.overlapImpl(geom0, pose0, geom1, pose1);
        }

        @Override
        public boolean sweepImpl(PxVec3 unitDir, float maxDist, PxGeometry geom0, PxTransform pose0, PxGeometry geom1, PxTransform pose1, PxGeomSweepHit sweepHit, PxHitFlags hitFlags, float inflation) {
            return super.sweepImpl(unitDir, maxDist, geom0, pose0, geom1, pose1, sweepHit, hitFlags, inflation);
        }

        @Override
        public void computeMassPropertiesImpl(PxGeometry geometry, PxMassProperties massProperties) {
            super.computeMassPropertiesImpl(geometry, massProperties);
        }
    }
}
