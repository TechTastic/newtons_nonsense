package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.NewtonsNonsense;
import io.github.techtastic.newtons_nonsense.registries.Material;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import physx.geometry.PxBoxGeometry;
import physx.geometry.PxCapsuleGeometry;
import physx.geometry.PxSphereGeometry;
import physx.physics.PxMaterial;
import physx.physics.PxShape;

import java.util.ArrayList;

public class CollisionShapeBuilder {
    private final ArrayList<PxShape> shapes = new ArrayList<>();

    protected CollisionShapeBuilder() {}

    private PxMaterial getMaterial(RegistryAccess access, ResourceLocation material) {
        return access.lookup(Material.REGISTRY_KEY).orElseThrow().getOrThrow(ResourceKey.create(Material.REGISTRY_KEY, material)).value();
    }

    protected PxShape[] getShapes() {
        return this.shapes.toArray(new PxShape[0]);
    }

    // BOX

    public CollisionShapeBuilder box(RegistryAccess access, double halfLengthX, double halfLengthY, double halfLengthZ, ResourceLocation material) {
        PxBoxGeometry geom = new PxBoxGeometry((float)halfLengthX, (float)halfLengthY, (float)halfLengthZ);
        PxMaterial mat = getMaterial(access, material);
        this.shapes.add(Backend.PHYSICS.createShape(geom, mat));
        geom.destroy();
        return this;
    }

    public CollisionShapeBuilder box(RegistryAccess access, double halfLengthX, double halfLengthY, double halfLengthZ) {
        return box(access, halfLengthX, halfLengthY, halfLengthZ, ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "default"));
    }

    public CollisionShapeBuilder box(RegistryAccess access, Vec3 halfLengths, ResourceLocation material) {
        return box(access, halfLengths.x, halfLengths.y, halfLengths.z, material);
    }

    public CollisionShapeBuilder box(RegistryAccess access, Vec3 halfLengths) {
        return box(access, halfLengths.x, halfLengths.y, halfLengths.z, ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "default"));
    }

    // SPHERE

    public CollisionShapeBuilder sphere(RegistryAccess access, double radius, ResourceLocation material) {
        PxSphereGeometry geom = new PxSphereGeometry((float) radius);
        PxMaterial mat = getMaterial(access, material);
        this.shapes.add(Backend.PHYSICS.createShape(geom, mat));
        geom.destroy();
        return this;
    }

    public CollisionShapeBuilder sphere(RegistryAccess access, double radius) {
        return sphere(access, radius, ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "default"));
    }

    // CAPSULE

    public CollisionShapeBuilder capsule(RegistryAccess access, double radius, double halfHeight, ResourceLocation material) {
        PxCapsuleGeometry geom = new PxCapsuleGeometry((float)radius, (float)halfHeight);
        PxMaterial mat = getMaterial(access, material);
        this.shapes.add(Backend.PHYSICS.createShape(geom, mat));
        geom.destroy();
        return this;
    }

    public CollisionShapeBuilder capsule(RegistryAccess access, double radius, double halfHeight) {
        return capsule(access, radius, halfHeight, ResourceLocation.fromNamespaceAndPath(NewtonsNonsense.MOD_ID, "default"));
    }
}
