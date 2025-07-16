package io.github.techtastic.newtons_nonsense;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import physx.PxTopLevelFunctions;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        // Write common init code here.

        LOGGER.info("PhysX Version: {}", PxTopLevelFunctions.getPHYSICS_VERSION());
    }
}
