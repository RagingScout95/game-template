package com.pixelgame.engine.model.enums;

public enum NPCMovementType {
    STATIC,           // NPC doesn't move
    FOLLOW_PLAYER,    // NPC follows the player
    PATROL,           // NPC follows a predefined path
    EVENT_DRIVEN      // NPC moves based on events
}

