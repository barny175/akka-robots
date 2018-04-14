package com.barny.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Robot extends AbstractActor {
    private final int id;
    private int damage = 0;

    public Robot(int id) {
        this.id = id;
    }

    public static class RobotAdded { }

    public static class GetStatus { }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MoveCommand.class, this::move)
                .match(Damaged.class, this::damage)
                .match(Commander.AddRobot.class, this::onAdded)
                .match(GetStatus.class, this::onGetStatus)
                .build();
    }

    private void onGetStatus(GetStatus msg) {
        getSender().tell(new Status(this.damage, this.id), getSelf());
    }

    private void onAdded(Commander.AddRobot ar) {
        ActorRef sender = getSender();
        sender.tell(new RobotAdded(), getSelf());
    }

    private void damage(Damaged damage) {
        System.out.println("Robot " + id + " damaged by " + damage.percent);
        this.damage += damage.percent;
        if (this.damage >= 100) {
            System.out.println("Robot " + id + " destroyed.");
            getContext().stop(getSelf());
        }
    }

    @Override
    public void preStart() {
        System.out.println("Starting robot");
    }

    @Override
    public void postStop() throws Exception {
        System.out.println("Stopping robot " + id);
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

    public static class Damaged {
        int percent;

        public Damaged(int percent) {
            if (percent < 0 || percent > 100)
                throw new IllegalArgumentException();

            this.percent = percent;
        }
    }

    public static Props props(int id) {
        return Props.create(Robot.class, () -> new Robot(id));
    }

    private void move(MoveCommand mc) {
        System.out.println("Moving " + id + " to " + mc.x + ", " + mc.y);
        getSender().tell(new Moved(), getSelf());
    }

    public static class Status {
        private int damage;
        private int id;

        public Status(int damage, int id) {
            this.damage = damage;
            this.id = id;
        }

        public int getDamage() {
            return damage;
        }

        public int getId() {
            return id;
        }
    }
}
