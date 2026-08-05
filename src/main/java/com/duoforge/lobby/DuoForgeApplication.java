package com.duoforge.lobby;

import com.duoforge.lobby.server.AppServer;
import com.duoforge.lobby.service.LobbyEngine;

public final class DuoForgeApplication {
    private DuoForgeApplication() {}

    public static void main(String[] args) throws Exception {
        int port = resolvePort();
        AppServer server = new AppServer(port, new LobbyEngine());
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        server.start();
        System.out.println("DuoForge Lobby is online at http://localhost:" + port);
    }

    private static int resolvePort() {
        String rawPort = System.getenv().getOrDefault("PORT", "8080");
        try {
            return Integer.parseInt(rawPort);
        } catch (NumberFormatException exception) {
            return 8080;
        }
    }
}
