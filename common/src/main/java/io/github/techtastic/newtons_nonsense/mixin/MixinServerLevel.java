package io.github.techtastic.newtons_nonsense.mixin;

import io.github.techtastic.newtons_nonsense.mixinducks.StageProvider;
import io.github.techtastic.newtons_nonsense.physics.Stage;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerLevel.class)
public abstract class MixinServerLevel implements StageProvider {
    @Unique
    private Stage newtons_nonsense$physicsLevel = null;

    @Override
    public @NotNull Stage newtons_nonsense$getOrCreateStage() {
        if (newtons_nonsense$physicsLevel == null) {
            newtons_nonsense$physicsLevel = new Stage();
        }
        return newtons_nonsense$physicsLevel;
    }
}