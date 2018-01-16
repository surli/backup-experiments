/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  GraphHopper GmbH licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except in 
 *  compliance with the License. You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.storage;

import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.Helper;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Karl Hübner
 */
public class GraphHopperStorageWithTurnCostsTest extends GraphHopperStorageTest {
    private TurnCostExtension turnCostStorage;

    @Override
    protected GraphHopperStorage newGHStorage(Directory dir, boolean is3D) {
        turnCostStorage = new TurnCostExtension();
        return new GraphHopperStorage(dir, encodingManager, json, is3D, turnCostStorage);
    }

    @Override
    @Test
    public void testSave_and_fileFormat() throws IOException {
        graph = newGHStorage(new RAMDirectory(defaultGraphLoc, true), true).create(defaultSize);
        NodeAccess na = graph.getNodeAccess();
        assertTrue(na.is3D());
        na.setNode(0, 10, 10, 0);
        na.setNode(1, 11, 20, 1);
        na.setNode(2, 12, 12, 0.4);

        EdgeIteratorState iter2 = GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 0, 1, true, 100);
        iter2.setWayGeometry(Helper.createPointList3D(1.5, 1, 0, 2, 3, 0));
        EdgeIteratorState iter1 = GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 0, 2, true, 200);
        iter1.setWayGeometry(Helper.createPointList3D(3.5, 4.5, 0, 5, 6, 0));
        GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 9, 10, true, 200);
        GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 9, 11, true, 200);
        GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 1, 2, false, 120);

        turnCostStorage.addTurnInfo(iter1.getEdge(), 0, iter2.getEdge(), 1337);
        turnCostStorage.addTurnInfo(iter2.getEdge(), 0, iter1.getEdge(), 666);
        turnCostStorage.addTurnInfo(iter1.getEdge(), 1, iter2.getEdge(), 815);

        iter1.setName("named street1");
        iter2.setName("named street2");

        checkGraph(graph);
        graph.flush();
        graph.close();

        graph = newGHStorage(new MMapDirectory(defaultGraphLoc), true);
        assertTrue(graph.loadExisting());

        assertEquals(12, graph.getNodes());
        checkGraph(graph);

        assertEquals("named street1", graph.getEdgeIteratorState(iter1.getEdge(), iter1.getAdjNode()).getName());
        assertEquals("named street2", graph.getEdgeIteratorState(iter2.getEdge(), iter2.getAdjNode()).getName());

        assertEquals(1337, turnCostStorage.getTurnCostFlags(iter1.getEdge(), 0, iter2.getEdge()));
        assertEquals(666, turnCostStorage.getTurnCostFlags(iter2.getEdge(), 0, iter1.getEdge()));
        assertEquals(815, turnCostStorage.getTurnCostFlags(iter1.getEdge(), 1, iter2.getEdge()));
        assertEquals(0, turnCostStorage.getTurnCostFlags(iter1.getEdge(), 3, iter2.getEdge()));

        GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 3, 4, true, 123).setWayGeometry(Helper.createPointList3D(4.4, 5.5, 0, 6.6, 7.7, 0));
        checkGraph(graph);
    }

    @Test
    public void testEnsureCapacity() throws IOException {
        graph = newGHStorage(new MMapDirectory(defaultGraphLoc), false);
        graph.setSegmentSize(128);
        graph.create(100); // 100 is the minimum size

        // assert that turnCostStorage can hold 104 turn cost entries at the beginning
        assertEquals(128, turnCostStorage.getCapacity());

        Random r = new Random();

        NodeAccess na = graph.getNodeAccess();
        for (int i = 0; i < 100; i++) {
            double randomLat = 90 * r.nextDouble();
            double randomLon = 180 * r.nextDouble();

            na.setNode(i, randomLat, randomLon);
        }

        // Make node 50 the 'center' node
        for (int nodeId = 51; nodeId < 100; nodeId++) {
            GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, 50, nodeId, true, r.nextDouble());
        }
        for (int nodeId = 0; nodeId < 50; nodeId++) {
            GHUtility.createEdge(graph, carAverageSpeedEnc, 60, carAccessEnc, nodeId, 50, true, r.nextDouble());
        }

        // add 100 turn cost entries around node 50
        for (int edgeId = 0; edgeId < 50; edgeId++) {
            turnCostStorage.addTurnInfo(edgeId, 50, edgeId + 50, 1337);
            turnCostStorage.addTurnInfo(edgeId + 50, 50, edgeId, 1337);
        }

        turnCostStorage.addTurnInfo(0, 50, 1, 1337);
        assertEquals(104, turnCostStorage.getCapacity() / 16); // we are still good here

        turnCostStorage.addTurnInfo(0, 50, 2, 1337);
        // A new segment should be added, which will support 128 / 16 = 8 more entries.
        assertEquals(112, turnCostStorage.getCapacity() / 16);
    }
}
