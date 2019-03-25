package com.hotmail.intrinsic.menubuilder;

import org.bukkit.entity.Player;

public class InputListener extends ButtonListener {

    private Player waitingInput;

    /**
     * Wait for a player to message in game
     * @param player
     */
    protected void waitInput(Player player) {
        waitingInput = player;
    }

    /**
     * Get the player this button is waiting on input for
     * @return
     */
    protected Player waiting() {
        return waitingInput;
    }

    /**
     * Check if this button is waiting for a player to message
     * @return
     */
    protected boolean isWaiting() {
        return waitingInput != null;
    }

    /**
     * Stop waiting for a player to recieve input
     * @param player
     */
    protected void stopWaiting(Player player) {
        waitingInput = null;
    }

    /**
     * Called when a player sends a message to the server
     * after clicking this button
     * @param player
     */
    public void onInput(Player player, String message) {}

}
