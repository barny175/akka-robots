package com.barny.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Robot extends AbstractActor {
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

    public static Props props() {
        return Props.create(Robot.class, () -> new Robot());
    }

    private void move(MoveCommand mc) {
        System.out.println("Moving to " + mc.x + ", " + mc.y);
    }
}
