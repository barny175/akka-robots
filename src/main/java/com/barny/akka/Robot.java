package com.barny.akka;

import akka.actor.AbstractActor;
import akka.actor.Props;

public class Robot extends AbstractActor {
    private final int id;

    public Robot(int id) {
        this.id = id;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MoveCommand.class, this::move)
                .build();
    }

    public static class MoveCommand {
        int x;
        int y;

        public MoveCommand(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class Moved {}

    public static Props props(int id) {
        return Props.create(Robot.class, () -> new Robot(id));
    }

    private void move(MoveCommand mc) {
        System.out.println("Moving " + id + " to " + mc.x + ", " + mc.y);
        getSender().tell(new Moved(), getSelf());
    }
}
