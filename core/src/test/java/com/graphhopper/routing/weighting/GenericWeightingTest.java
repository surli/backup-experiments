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
package com.graphhopper.routing.weighting;

import com.graphhopper.json.GHJson;
import com.graphhopper.json.GHJsonFactory;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.AbstractRoutingAlgorithmTester;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.util.DataFlagEncoder;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.HintsMap;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * @author Peter Karich
 */
public class GenericWeightingTest {
    private GHJson json = new GHJsonFactory().create();
    private final PMap properties;
    private final DataFlagEncoder encoder;
    private final EncodingManager em;
    private Graph graph;

    private final double edgeWeight = 566111;

    public GenericWeightingTest() {
        properties = new PMap();
        properties.put("store_height", true);
        properties.put("store_weight", true);
        properties.put("store_width", true);
        encoder = new DataFlagEncoder(properties);
        em = new EncodingManager.Builder().addGlobalEncodedValues().addAll(Arrays.asList(encoder), 8).build();
    }

    @Before
    public void setUp() {
        ReaderWay way = new ReaderWay(27l);
        way.setTag("highway", "primary");
        way.setTag("maxspeed", "10");
        way.setTag("maxheight", "4.4");

        graph = new GraphBuilder(em, json).create();
        // 0-1
        GHUtility.createEdge(graph, em.getDecimalEncodedValue("generic." + "average_speed"), 60, em.getBooleanEncodedValue("generic." + "access"),
                0, 1, true, 1);
        AbstractRoutingAlgorithmTester.updateDistancesFor(graph, 0, 0.00, 0.00);
        AbstractRoutingAlgorithmTester.updateDistancesFor(graph, 1, 0.01, 0.01);
        graph.getEdgeIteratorState(0, 1).setData(
                encoder.handleWayTags(em.createIntsRef(), way, EncodingManager.Access.WAY, 0));
    }

    @Test
    public void testCalcTime() {
        GenericWeighting weighting = new GenericWeighting(encoder, new HintsMap());
        EdgeIteratorState edge = graph.getEdgeIteratorState(0, 1);
        assertEquals(edgeWeight, weighting.calcMillis(edge, false, EdgeIterator.NO_EDGE), .1);
    }

    @Test
    public void testRoadAttributeRestriction() {
        EdgeIteratorState edge = graph.getEdgeIteratorState(0, 1);
        Weighting instance = new GenericWeighting(encoder, new HintsMap().put(GenericWeighting.HEIGHT_LIMIT, 4.0));
        assertEquals(edgeWeight, instance.calcWeight(edge, false, EdgeIterator.NO_EDGE), 1e-8);

        instance = new GenericWeighting(encoder, new HintsMap().put(GenericWeighting.HEIGHT_LIMIT, 5.0));
        assertEquals(Double.POSITIVE_INFINITY, instance.calcWeight(edge, false, EdgeIterator.NO_EDGE), 1e-8);
    }

    @Test
    public void testDisabledRoadAttributes() {
        DataFlagEncoder simpleEncoder = new DataFlagEncoder();
        EncodingManager simpleEncodingManager = new EncodingManager.Builder().addGlobalEncodedValues().addAll(simpleEncoder).build();
        BooleanEncodedValue accessEnc = simpleEncodingManager.getBooleanEncodedValue("generic." + "access");
        DecimalEncodedValue avSpeedEnc = simpleEncodingManager.getDecimalEncodedValue("generic." + "average_speed");
        Graph simpleGraph = new GraphBuilder(simpleEncodingManager, json).create();

        ReaderWay way = new ReaderWay(27l);
        way.setTag("highway", "primary");
        way.setTag("maxspeed", "10");
        way.setTag("maxheight", "4.4");

        // 0-1
        GHUtility.createEdge(simpleGraph, avSpeedEnc, 60, accessEnc, 0, 1, true, 1);
        AbstractRoutingAlgorithmTester.updateDistancesFor(simpleGraph, 0, 0.00, 0.00);
        AbstractRoutingAlgorithmTester.updateDistancesFor(simpleGraph, 1, 0.01, 0.01);
        simpleGraph.getEdgeIteratorState(0, 1).setData(
                simpleEncodingManager.handleWayTags(simpleEncodingManager.createIntsRef(), way, new EncodingManager.AcceptWay(), 0));

        Weighting instance = new GenericWeighting(simpleEncoder, new HintsMap().put(GenericWeighting.HEIGHT_LIMIT, 5.0));
        EdgeIteratorState edge = simpleGraph.getEdgeIteratorState(0, 1);
        assertEquals(edgeWeight, instance.calcWeight(edge, false, EdgeIterator.NO_EDGE), 1e-8);
    }
}
