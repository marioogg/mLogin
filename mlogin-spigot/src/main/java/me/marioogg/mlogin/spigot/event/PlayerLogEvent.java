package me.marioogg.mlogin.spigot.event;

import lombok.Getter;
import me.marioogg.mlogin.core.model.AuthMessage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

@Getter
public class PlayerLogEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final UUID uuid;
    private final AuthMessage msg;

    public PlayerLogEvent(AuthMessage msg) {
        this.msg = msg;
        this.uuid = msg.getUuid();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}