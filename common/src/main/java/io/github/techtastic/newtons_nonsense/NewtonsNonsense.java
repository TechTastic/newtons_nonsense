package io.github.techtastic.newtons_nonsense;

import physx.PxTopLevelFunctions;

import java.util.logging.Logger;

public final class NewtonsNonsense {
    public static final String MOD_ID = "newtons_nonsense";
    public static final Logger LOGGER = Logger.getLogger(MOD_ID);

    public static void init() {
        // Write common init code here.

        LOGGER.info("PhysX Version: " + PxTopLevelFunctions.getPHYSICS_VERSION());
    }
}
