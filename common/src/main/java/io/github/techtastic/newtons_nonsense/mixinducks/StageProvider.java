package io.github.techtastic.newtons_nonsense.mixinducks;

import io.github.techtastic.newtons_nonsense.physics.Stage;
import org.jetbrains.annotations.NotNull;

public interface StageProvider {
    @NotNull
    Stage newtons_nonsense$getOrCreateStage();
}