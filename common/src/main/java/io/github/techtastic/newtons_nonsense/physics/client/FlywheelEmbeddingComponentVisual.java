package io.github.techtastic.newtons_nonsense.physics.client;

import dev.engine_room.flywheel.api.task.Plan;
import dev.engine_room.flywheel.api.visual.DynamicVisual;
import dev.engine_room.flywheel.api.visualization.VisualEmbedding;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.task.RunnablePlan;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;

public class FlywheelEmbeddingComponentVisual implements DynamicVisual {
    private final VisualEmbedding embedding;
    private final Vec3i prevRenderOrigin;
    private final ClientApple apple;

    protected FlywheelEmbeddingComponentVisual(ClientApple apple, VisualizationContext context) {
        this.apple = apple;
        this.prevRenderOrigin = context.renderOrigin();
        this.embedding = context.createEmbedding(this.prevRenderOrigin);
    }

    @Override
    public @NotNull Plan<Context> planFrame() {
        return RunnablePlan.of(context -> this.apple.render(context, this.prevRenderOrigin));
    }

    @Override
    public void update(float v) {

    }

    @Override
    public void delete() {
        embedding.delete();
    }
}
