package com.wanomaniac.economy;

import net.minecraft.Util;
import java.util.concurrent.ExecutorService;

public class GeneralUtils {
    public static ExecutorService getBackgroundExecutor(){
        return Util.backgroundExecutor();
    }
}
