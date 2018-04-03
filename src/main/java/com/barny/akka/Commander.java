package com.barny.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.HashMap;
import java.util.Map;

public class Commander extends AbstractActor {
    private Map<Integer, ActorRef> robots = new HashMap<>();

    public static class AddRobot {
        int id;

        public AddRobot(int id) {
            this.id = id;
        }
    }

    public static class RobotAdded {

    }

    public static Props props() {
        return Props.create(Commander.class, () -> new Commander());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AddRobot.class, this::addRobot)
                .build();
    }

    private void addRobot(AddRobot ar) {
        ActorRef ref = getContext().getSystem().actorOf(Robot.props(ar.id));
        robots.put(ar.id, ref);
        ActorRef sender = getSender();
        sender.tell(new RobotAdded(), getSelf());
    }
}
