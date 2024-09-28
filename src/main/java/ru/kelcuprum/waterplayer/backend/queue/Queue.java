package ru.kelcuprum.waterplayer.backend.queue;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;

public class Queue extends AbstractQueue {
    public Queue() {
        super(new ArrayList<>());
    }

    @Override
    public Boolean addTrackAvailable() {
        return true;
    }

    @Override
    public String getName() {
        return Component.translatable(getQueue().isEmpty() ?  "waterplayer.command.queue.blank" : "waterplayer.command.queue").getString();
    }
}

