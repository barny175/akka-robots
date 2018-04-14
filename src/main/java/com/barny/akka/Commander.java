package com.barny.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.Terminated;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Commander extends AbstractActor {
    private Map<Integer, ActorRef> robots = new HashMap<>();
    private Map<ActorRef, Integer> robotToIdMappings = new HashMap<>();

    public static class AddRobot {
        int id;

        public AddRobot(int id) {
            this.id = id;
        }
    }

    public static class GetRobotCount { }

    public static class GetCompanyStatusReq { }

    public static class RobotCount {
        public int count;
        RobotCount(int count) {
            this.count = count;
        }
    }

    public static Props props() {
        return Props.create(Commander.class, () -> new Commander());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AddRobot.class, this::addRobot)
                .match(GetRobotCount.class, this::getRobotoCount)
                .match(GetCompanyStatusReq.class, this::onGetCompanyStatus)
                .match(Terminated.class, this::onTerminated)
                .build();
    }

    private void getRobotoCount(GetRobotCount msg) {
        getSender().tell(new RobotCount(robots.size()), getSelf());
    }

    private void onTerminated(Terminated terminated) {
        System.out.println("Terminated " + terminated.getActor());
        Integer id = robotToIdMappings.remove(terminated.actor());
        if  (id != null) {
            robots.remove(id);
        }
    }

    private void addRobot(AddRobot ar) {
        ActorRef ref = getContext().actorOf(Robot.props(ar.id), "robot" + ar.id);
        getContext().watch(ref);
        robots.put(ar.id, ref);
        robotToIdMappings.put(ref, ar.id);
        ref.forward(ar, getContext());
    }

    private void onGetCompanyStatus(GetCompanyStatusReq msg) {
        Collection<ActorRef> company = Collections.unmodifiableCollection(this.robotToIdMappings.keySet());
        ActorRef ref = getContext().actorOf(GetCompanyStatus.props(getSender(), company));
        ref.tell(msg, getSender());
    }
}
