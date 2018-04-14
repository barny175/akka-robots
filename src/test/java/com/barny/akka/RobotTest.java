package com.barny.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RobotTest {

    private static ActorSystem system;

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testMove() {
        TestKit probe = new TestKit(system);
        ActorRef robot = system.actorOf(Robot.props(1));
        robot.tell(new Robot.MoveCommand(1,1), probe.getRef());
        probe.expectMsgClass(Robot.Moved.class);
    }

    @Test
    public void testGetStatus() {
        TestKit probe = new TestKit(system);
        ActorRef ref = system.actorOf(Robot.props(1));
        ref.tell(new Robot.GetStatus(), probe.getRef());
        Robot.Status status = probe.expectMsgClass(Robot.Status.class);
        assertEquals(0, status.getDamage());

        ref.tell(new Robot.Damaged(10), probe.getRef());
        ref.tell(new Robot.GetStatus(), probe.getRef());
        status = probe.expectMsgClass(Robot.Status.class);
        assertEquals(10, status.getDamage());
    }
}