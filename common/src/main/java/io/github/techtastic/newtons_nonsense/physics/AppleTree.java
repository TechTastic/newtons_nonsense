package io.github.techtastic.newtons_nonsense.physics;

import io.github.techtastic.newtons_nonsense.util.Constants;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import physx.common.PxCollection;
import physx.extensions.PxCollectionExt;
import physx.extensions.PxExtensionTopLevelFunctions;
import physx.extensions.PxRigidActorExt;
import physx.extensions.PxRigidBodyExt;
import physx.physics.PxRigidStatic;
import physx.physics.PxScene;
import physx.physics.PxSceneSQSystem;
import physx.physics.PxShapeExt;

import java.util.HashMap;
import java.util.UUID;

public class AppleTree extends SavedData {
    private static final String TREE_TAG = "newtons_nonsense$apple_tree";

    // Make Scene
    // Manage Apples
    // Simulate

    private final PxScene scene = null;
    private final HashMap<UUID, Apple> apples = new HashMap<>();

    public AppleTree(Level level) {
        // If client, ignore

        // Load previous tree if it exists for this Level
        // Get Apples from UUID and serialized Actor
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        CompoundTag tree = new CompoundTag();

        compoundTag.put(TREE_TAG, tree);
        return compoundTag;
    }
}
