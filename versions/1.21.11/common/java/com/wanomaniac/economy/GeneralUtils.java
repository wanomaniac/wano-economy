package com.wanomaniac.economy;

import net.minecraft.TracingExecutor;
import net.minecraft.util.Util;

public class GeneralUtils {
    public static TracingExecutor getBackgroundExecutor(){
        return Util.backgroundExecutor();
    }
}
