package com.wanomaniac.economy.trading.server;

import java.util.UUID;

public record HistoryMenuDetails(int lastPage, boolean asAdmin, UUID filterPlayerAdmin) {
    
}
