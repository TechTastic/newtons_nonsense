package io.github.techtastic.newtons_nonsense.physics.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class LevelPhysicsSavedData extends SavedData {
    // Chunks
    // Apples
    // Tick
    // Start/Stop
    // Block Changed

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        return tag;
    }

    public static LevelPhysicsSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        LevelPhysicsSavedData data = new LevelPhysicsSavedData();

        return data;
    }

    public static LevelPhysicsSavedData getOrCreateData(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new Factory<>(LevelPhysicsSavedData::new, LevelPhysicsSavedData::load, null), "physics");
    }
}
