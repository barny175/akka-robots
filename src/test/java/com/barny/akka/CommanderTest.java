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
        testKit.expectMsgClass(Commander.RobotAdded.class);
        assertEquals(commander, testKit.getLastSender());
    }
}