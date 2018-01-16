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

import com.graphhopper.routing.profiles.BooleanEncodedValue;
import com.graphhopper.routing.profiles.DecimalEncodedValue;
import com.graphhopper.routing.profiles.TagParserFactory;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.routing.util.FlagEncoder;
import com.graphhopper.storage.IntsRef;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;
import com.graphhopper.util.GHUtility;
import com.graphhopper.util.PMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Peter Karich
 */
public class ShortFastestWeightingTest {
    private final EncodingManager encodingManager = new EncodingManager.Builder().addGlobalEncodedValues().addAllFlagEncoders("car").build();
    private final FlagEncoder encoder = encodingManager.getEncoder("car");

    @Test
    public void testShort() {
        BooleanEncodedValue accessEnc = encodingManager.getBooleanEncodedValue(TagParserFactory.CAR_ACCESS);
        DecimalEncodedValue averageSpeedEnc = encodingManager.getDecimalEncodedValue(TagParserFactory.CAR_AVERAGE_SPEED);

        IntsRef ints = encodingManager.createIntsRef();
        accessEnc.setBool(false, ints, true);
        averageSpeedEnc.setDecimal(false, ints, 50d);
        EdgeIteratorState edge = createEdge(10, ints);
        Weighting instance = new ShortFastestWeighting(encoder, 0.03);
        assertEquals(1.02, instance.calcWeight(edge, false, EdgeIterator.NO_EDGE), 1e-8);

        // more influence from distance
        instance = new ShortFastestWeighting(encoder, 0.1);
        assertEquals(1.72, instance.calcWeight(edge, false, EdgeIterator.NO_EDGE), 1e-8);
    }

    @Test
    public void testTooSmall() {
        try {
            new ShortFastestWeighting(encoder, new PMap("short_fastest.distance_factor=0|short_fastest.time_factor=0"));
            assertTrue(false);
        } catch (Exception ex) {
        }
    }

    EdgeIterator createEdge(final double distance, final IntsRef flags) {
        return new GHUtility.DisabledEdgeIterator() {
            @Override
            public double getDistance() {
                return distance;
            }

            @Override
            public double get(DecimalEncodedValue property) {
                return property.getDecimal(false, flags);
            }

            @Override
            public boolean get(BooleanEncodedValue property) {
                return property.getBool(false, flags);
            }
        };
    }
}
