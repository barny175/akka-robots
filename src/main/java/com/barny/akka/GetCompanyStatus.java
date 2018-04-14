package com.barny.akka;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Cancellable;
import akka.actor.Props;
import akka.japi.pf.ReceiveBuilder;
import scala.concurrent.duration.FiniteDuration;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class GetCompanyStatus extends AbstractActor {
    public static final int TIMEOUT = 10;
    private final Cancellable cancellationTimer;

    public static class Timeout {}

    private final Collection<ActorRef> company;
    private final Map<ActorRef, RobotStatus> statuses = new HashMap<>();
    private final ActorRef requester;

    public static class RobotStatus { }

    public static class RobotDamage extends RobotStatus {
        private int damage;

        public RobotDamage(int damage) {
            this.damage = damage;
        }

        public int getDamage() {
            return damage;
        }
    }

    public static class RobotTimeout extends RobotStatus { }

    public static class RobotNotAvailable extends RobotStatus { }

    public static class CompanyStatus {
        List<RobotStatus> robotStatuses;

        public List<RobotStatus> getRobotStatuses() {
            return robotStatuses;
        }
    }

    public GetCompanyStatus(ActorRef requester, Collection<ActorRef> company) {
        this.company = company;
        this.requester = requester;

        cancellationTimer = getContext().getSystem().scheduler()
                .scheduleOnce(
                        FiniteDuration.apply(TIMEOUT, TimeUnit.SECONDS),
                        getSelf(),
                        new Timeout(),
                        getContext().dispatcher(),
                        getSelf());

        this.getStatus();
    }

    public static Props props(ActorRef requester, Collection<ActorRef> company) {
        return Props.create(GetCompanyStatus.class, () -> new GetCompanyStatus(requester, company));
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
                .match(Timeout.class, this::onTimeout)
                .match(Robot.Status.class, this::onRobotStatus)
                .build();
    }

    private void onRobotStatus(Robot.Status status) {
        System.out.println("Got response from " + status.getId());
        this.statuses.put(getSender(), new RobotDamage(status.getDamage()));
        if (statuses.size() == this.company.size()) {
            requester.tell(createResponse(), getSelf());
        }
    }

    @Override
    public void postStop() throws Exception {
        this.cancellationTimer.cancel();
    }

    private void getStatus() {
        for (ActorRef robot: this.company) {
            getContext().watch(robot);
            robot.tell(new Robot.GetStatus(), getSelf());
        }
    }

    private void onTimeout(Timeout timeout) {
        requester.tell(createResponse(), getSelf());
    }

    private Object createResponse() {
        CompanyStatus status = new CompanyStatus();
        status.robotStatuses = new ArrayList<>(this.statuses.values());
        return status;
    }
}
