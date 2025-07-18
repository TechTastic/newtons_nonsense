package io.github.techtastic.newtons_nonsense;

import com.mojang.logging.LogUtils;
import io.github.techtastic.newtons_nonsense.physics.Orchard;
import org.slf4j.Logger;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // Write common init code here.

        LOGGER.info("PhysX Version: {}.{}.{}", Orchard.PHYSX_VERSION >> 24, (Orchard.PHYSX_VERSION  >> 16) & 0xff, (Orchard.PHYSX_VERSION >> 8) & 0xff);
    }
}