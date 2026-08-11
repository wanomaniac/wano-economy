package com.wanomaniac.economy;

import net.minecraft.TracingExecutor;
import net.minecraft.Util;

import java.util.concurrent.ExecutorService;

public class GeneralUtils {
    public static TracingExecutor getBackgroundExecutor(){
        return Util.backgroundExecutor();
    }
}
