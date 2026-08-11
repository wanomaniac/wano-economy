package com.wanomaniac.economy.auctioning.server;

import java.util.UUID;

// Record for storing notifications for inactive players
public record NotificationRecord(UUID id, int notificationType){

}
