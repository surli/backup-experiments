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
package com.graphhopper.reader.dem;

import com.graphhopper.coll.GHBitSetImpl;
import com.graphhopper.coll.GHIntHashSet;
import com.graphhopper.json.GHJsonFactory;
import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.profiles.StringEncodedValue;
import com.graphhopper.routing.profiles.TagParserFactory;
import com.graphhopper.routing.util.*;
import com.graphhopper.storage.GraphExtension;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.Helper;
import org.junit.After;
import org.junit.Before;

import java.util.Arrays;

/**
 * @author Alexey Valikov
 */
public abstract class AbstractEdgeElevationInterpolatorTest {

    protected static final double PRECISION = ElevationInterpolator.EPSILON2;
    protected ReaderWay interpolatableWay;
    protected ReaderWay normalWay;

    protected GraphHopperStorage graph;
    protected AbstractEdgeElevationInterpolator edgeElevationInterpolator;
    protected EncodingManager encodingManager;
    protected CarFlagEncoder carFlagEncoder;
    protected BooleanEncodedValue accessEnc;
    protected DecimalEncodedValue avSpeedEnc;
    protected StringEncodedValue roadEnvironmentEnc;
    EncodingManager.AcceptWay acceptWay;

    @SuppressWarnings("resource")
    @Before
    public void setUp() {
        encodingManager = new EncodingManager.Builder().addGlobalEncodedValues().addAll(Arrays.asList(new CarFlagEncoder()), 8).build();
        graph = new GraphHopperStorage(new RAMDirectory(), encodingManager, new GHJsonFactory().create(), true,
                new GraphExtension.NoOpExtension()).create(100);

        carFlagEncoder = (CarFlagEncoder) encodingManager.getEncoder("car");
        accessEnc = encodingManager.getBooleanEncodedValue("car.access");
        avSpeedEnc = encodingManager.getDecimalEncodedValue("car.average_speed");
        roadEnvironmentEnc = encodingManager.getStringEncodedValue(TagParserFactory.ROAD_ENVIRONMENT);

        edgeElevationInterpolator = createEdgeElevationInterpolator();

        interpolatableWay = createInterpolatableWay();
        normalWay = new ReaderWay(0);
        normalWay.setTag("highway", "primary");

        acceptWay = new EncodingManager.AcceptWay();
        encodingManager.acceptWay(normalWay, acceptWay);
    }

    @After
    public void tearDown() {
        Helper.close(graph);
    }

    protected abstract ReaderWay createInterpolatableWay();

    protected AbstractEdgeElevationInterpolator createEdgeElevationInterpolator() {
        return new BridgeElevationInterpolator(graph, encodingManager.getStringEncodedValue(TagParserFactory.ROAD_ENVIRONMENT));
    }

    protected void gatherOuterAndInnerNodeIdsOfStructure(EdgeIteratorState edge,
                                                         final GHIntHashSet outerNodeIds, final GHIntHashSet innerNodeIds) {
        edgeElevationInterpolator.gatherOuterAndInnerNodeIds(
                edgeElevationInterpolator.getStorage().createEdgeExplorer(), edge,
                new GHBitSetImpl(), outerNodeIds, innerNodeIds);
    }
}
