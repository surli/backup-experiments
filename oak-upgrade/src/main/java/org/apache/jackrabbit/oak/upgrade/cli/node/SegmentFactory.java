/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.upgrade.cli.node;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import org.apache.jackrabbit.oak.plugins.blob.ReferenceCollector;
import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore;
import org.apache.jackrabbit.oak.plugins.segment.file.FileStore.Builder;
import org.apache.jackrabbit.oak.spi.blob.BlobStore;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.state.NodeStore;

import com.google.common.io.Closer;

import javax.annotation.Nullable;

public class SegmentFactory implements NodeStoreFactory {

    private final File dir;

    private final boolean disableMmap;

    private final boolean readOnly;

    public SegmentFactory(String directory, boolean disableMmap, boolean readOnly) {
        this.dir = new File(directory);
        this.disableMmap = disableMmap;
        this.readOnly = readOnly;
        createDirectoryIfMissing(dir);
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + dir.getPath());
        }
    }

    private void createDirectoryIfMissing(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public NodeStore create(BlobStore blobStore, Closer closer) throws IOException {
        File directory = new File(dir, "segmentstore");

        final FileStore fs;
        if (readOnly) {
            if (disableMmap) {
                fs = new FileStore.ReadOnlyStore(directory, 0, false, blobStore);
            } else {
                fs = new FileStore.ReadOnlyStore(directory, blobStore);
            }
        } else {
            Builder builder = FileStore.newFileStore(directory);
            if (blobStore != null) {
                builder.withBlobStore(blobStore);
            }
            builder.withMaxFileSize(256);
            if (disableMmap) {
                builder.withMemoryMapping(false);
            } else {
                builder.withDefaultMemoryMapping();
            }
            fs = builder.create();
        }
        closer.register(asCloseable(fs));

        return new TarNodeStore(SegmentNodeStore.newSegmentNodeStore(fs).create(), new TarNodeStore.SuperRootProvider() {
            @Override
            public NodeState getSuperRoot() {
                return fs.getHead();
            }
        });
    }

    @Override
    public boolean hasExternalBlobReferences() throws IOException {
        FileStore fs = new FileStore.ReadOnlyStore(new File(dir, "segmentstore"), 0, false);
        try {
            fs.getTracker().collectBlobReferences(new ReferenceCollector() {
                @Override
                public void addReference(String reference, @Nullable String nodeId) {
                    // FIXME the collector should allow to stop processing
                    // see java.nio.file.FileVisitor
                    throw new ExternalBlobFound();
                }
            });
            return false;
        } catch (ExternalBlobFound e) {
            return true;
        } finally {
            fs.close();
        }
    }

    public File getRepositoryDir() {
        return dir;
    }

    private static Closeable asCloseable(final FileStore fs) {
        return new Closeable() {
            @Override
            public void close() throws IOException {
                fs.close();
            }
        };
    }

    @Override
    public String toString() {
        return String.format("SegmentNodeStore[%s]", dir);
    }

    private static class ExternalBlobFound extends RuntimeException {
    }
}
