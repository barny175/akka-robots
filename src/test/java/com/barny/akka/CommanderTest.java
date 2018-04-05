package com.barny.akka;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommanderTest {
    private static ActorSystem system;

    @Before
    public void setUp() throws Exception {
        system = ActorSystem.create();
    }

    @After
    public void tearDown() throws Exception {
        TestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAddRobot() {
        TestKit testKit = new TestKit(system);

        ActorRef commander = system.actorOf(Commander.props());
        commander.tell(new Commander.AddRobot(1), testKit.getRef());
        testKit.expectMsgClass(Robot.RobotAdded.class);
    }

    @Test
    public void testKillRobot() throws InterruptedException {
        TestKit testKit = new TestKit(system);

        ActorRef commander = system.actorOf(Commander.props());
        commander.tell(new Commander.AddRobot(1), testKit.getRef());
        testKit.expectMsgClass(Robot.RobotAdded.class);
        ActorRef robot = testKit.getLastSender();

        commander.tell(new Commander.AddRobot(2), testKit.getRef());
        testKit.expectMsgClass(Robot.RobotAdded.class);

        commander.tell(new Commander.GetRobotCount(), testKit.getRef());
        Commander.RobotCount robotCount = testKit.expectMsgClass(Commander.RobotCount.class);
        assertEquals(2, robotCount.count);

        robot.tell(new Robot.Damaged(50), testKit.getRef());
        commander.tell(new Commander.GetRobotCount(), testKit.getRef());
        robotCount = testKit.expectMsgClass(Commander.RobotCount.class);
        assertEquals(2, robotCount.count);

        testKit.watch(robot);
        robot.tell(new Robot.Damaged(50), testKit.getRef());
        testKit.expectTerminated(robot);
        commander.tell(new Commander.GetRobotCount(), testKit.getRef());
        robotCount = testKit.expectMsgClass(Commander.RobotCount.class);
        assertEquals(1, robotCount.count);
    }
}