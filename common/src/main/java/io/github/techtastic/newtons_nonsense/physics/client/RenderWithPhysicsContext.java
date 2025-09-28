package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import io.github.techtastic.newtons_nonsense.physics.AbstractPhysicsObject;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

public interface RenderWithPhysicsContext {
    void render(ClientLevel level, AbstractPhysicsObject object, @Nullable AbstractPhysicsObject previousObject, VisualizationContext visualizationContext, DynamicVisual.Context dynamicContext);
}
