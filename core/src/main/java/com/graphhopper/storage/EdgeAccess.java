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

import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeIteratorState;

/**
 * @author Peter Karich
 */
abstract class EdgeAccess {
    private static final int NO_NODE = -1;
    // distance of around +-1000 000 meter are ok
    private static final double INT_DIST_FACTOR = 1000d;
    static double MAX_DIST = (Integer.MAX_VALUE - 1) / INT_DIST_FACTOR;
    final DataAccess edges;
    int edgeDataSizeInBytes;
    int E_NODEA, E_NODEB, E_LINKA, E_LINKB, E_DIST, E_EXT_BYTES_OFFSET;

    EdgeAccess(DataAccess edges) {
        this.edges = edges;
    }

    final void init(int E_NODEA, int E_NODEB, int E_LINKA, int E_LINKB, int E_EXT_BYTES_OFFSET, int E_DIST) {
        this.E_NODEA = E_NODEA;
        this.E_NODEB = E_NODEB;
        this.E_LINKA = E_LINKA;
        this.E_LINKB = E_LINKB;
        this.E_DIST = E_DIST;
        this.E_EXT_BYTES_OFFSET = E_EXT_BYTES_OFFSET;
        this.edgeDataSizeInBytes = E_DIST - E_EXT_BYTES_OFFSET;
    }

    abstract BaseGraph.EdgeIterable createSingleEdge(EdgeFilter edgeFilter);

    abstract long toPointer(int edgeOrShortcutId);

    abstract boolean isInBounds(int edgeOrShortcutId);

    abstract int getEdgeRef(int nodeId);

    abstract void setEdgeRef(int nodeId, int edgeId);

    abstract int getEntryBytes();

    final void invalidateEdge(long edgePointer) {
        edges.setInt(edgePointer + E_NODEB, NO_NODE);
    }

    static final boolean isInvalidNodeB(int node) {
        return node == EdgeAccess.NO_NODE;
    }

    final void setDist(long edgePointer, double distance) {
        edges.setInt(edgePointer + E_DIST, distToInt(distance));
    }

    /**
     * Translates double distance to integer in order to save it in a DataAccess object
     */
    private int distToInt(double distance) {
        int integ = (int) (distance * INT_DIST_FACTOR);
        if (integ < 0)
            throw new IllegalArgumentException("Distance cannot be negative: " + distance);
        if (integ >= Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        // throw new IllegalArgumentException("Distance too large leading to overflowed integer (#435): " + distance + " ");
        return integ;
    }

    /**
     * returns distance (already translated from integer to double)
     */
    final double getDist(long pointer) {
        int val = edges.getInt(pointer + E_DIST);
        // do never return infinity even if INT MAX, see #435
        return val / INT_DIST_FACTOR;
    }

    IntsRef getData(long edgePointer) {
        // TODO PERFORMANCE use the DataAccess array directly with a different offset
        // TODO Everything is int-based: the dataIndex, the offset and the EncodedValue hierarchy with the 'int'-value as base
        IntsRef ints = new IntsRef(edgeDataSizeInBytes / 4);
        for (int i = 0; i < ints.length; i++) {
            ints.ints[i] = edges.getInt(edgePointer + E_EXT_BYTES_OFFSET + i * 4);
        }
        return ints;
    }

    void setData(long edgePointer, IntsRef ref) {
        for (int i = 0; i < ref.ints.length; i++) {
            edges.setInt(edgePointer + E_EXT_BYTES_OFFSET + i * 4, ref.ints[i]);
        }
    }

    /**
     * Write new edge between nodes fromNodeId, and toNodeId both to nodes index and edges index
     */
    final int internalEdgeAdd(int newEdgeId, int nodeA, int nodeB) {
        writeEdge(newEdgeId, nodeA, nodeB, EdgeIterator.NO_EDGE, EdgeIterator.NO_EDGE);
        long edgePointer = toPointer(newEdgeId);

        int edge = getEdgeRef(nodeA);
        if (edge > EdgeIterator.NO_EDGE)
            edges.setInt(E_LINKA + edgePointer, edge);
        setEdgeRef(nodeA, newEdgeId);

        if (nodeA != nodeB) {
            edge = getEdgeRef(nodeB);
            if (edge > EdgeIterator.NO_EDGE)
                edges.setInt(E_LINKB + edgePointer, edge);
            setEdgeRef(nodeB, newEdgeId);
        }
        return newEdgeId;
    }

    final int getNodeA(long edgePointer) {
        return edges.getInt(edgePointer + E_NODEA);
    }

    final int getNodeB(long edgePointer) {
        return edges.getInt(edgePointer + E_NODEB);
    }

    final int getLinkA(long edgePointer) {
        return edges.getInt(edgePointer + E_LINKA);
    }

    final int getLinkB(long edgePointer) {
        return edges.getInt(edgePointer + E_LINKB);
    }

    final long writeEdge(int edgeId, int nodeA, int nodeB, int nextEdgeA, int nextEdgeB) {
        if (edgeId < 0 || edgeId == EdgeIterator.NO_EDGE)
            throw new IllegalStateException("Cannot write edge with illegal ID:" + edgeId + "; nodeA:" + nodeA + ", nodeB:" + nodeB);

        long edgePointer = toPointer(edgeId);
        edges.setInt(edgePointer + E_NODEA, nodeA);
        edges.setInt(edgePointer + E_NODEB, nodeB);
        edges.setInt(edgePointer + E_LINKA, nextEdgeA);
        edges.setInt(edgePointer + E_LINKB, nextEdgeB);
        return edgePointer;
    }

    /**
     * This method disconnects the specified edge from the list of edges of the specified node. It
     * does not release the freed space to be reused.
     *
     * @param edgeToUpdatePointer if it is negative then the nextEdgeId will be saved to refToEdges
     *                            of nodes
     */
    final long internalEdgeDisconnect(int edgeToRemove, long edgeToUpdatePointer, int baseNode) {
        long edgeToRemovePointer = toPointer(edgeToRemove);
        // an edge is shared across the two nodes even if the edge is not in both directions
        // so we need to know two edge-pointers pointing to the edge before edgeToRemovePointer
        int nextEdgeId = getNodeA(edgeToRemovePointer) == baseNode ? getLinkA(edgeToRemovePointer) : getLinkB(edgeToRemovePointer);
        if (edgeToUpdatePointer < 0) {
            setEdgeRef(baseNode, nextEdgeId);
        } else {
            // adjNode is different for the edge we want to update with the new link
            long link = getNodeA(edgeToUpdatePointer) == baseNode ? edgeToUpdatePointer + E_LINKA : edgeToUpdatePointer + E_LINKB;
            edges.setInt(link, nextEdgeId);
        }
        return edgeToRemovePointer;
    }

    final EdgeIteratorState getEdgeProps(int edgeId, int adjNode) {
        if (edgeId <= EdgeIterator.NO_EDGE)
            throw new IllegalStateException("edgeId invalid " + edgeId + ", " + this);

        BaseGraph.EdgeIterable edge = createSingleEdge(EdgeFilter.ALL_EDGES);
        if (edge.init(edgeId, adjNode))
            return edge;

        // if edgeId exists but adjacent nodes do not match
        return null;
    }
}
