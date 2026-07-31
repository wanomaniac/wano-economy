package com.wanomaniac.economy.trading.server;

public enum TradeCancelReason {
    PLAYER_LEFT_LOGGED_OUT("The trade was cancelled because a partner has disconnected."),
    MANUAL_CLOSE("The trade was cancelled!"),
    KILLED("The trade was cancelled because a partner was killed!"),
    SERVER_SHUTDOWN("The trade was cancelled due to the server shutting down!"),
    WORLD_ACTION("The trade was cancelled due to a world action!"),
    UNKNOWN("The trade was cancelled due to unknown causes.");

    private final String message;
    TradeCancelReason(String message) { this.message = message; }
    public String getMessage() { return this.message; }
}
