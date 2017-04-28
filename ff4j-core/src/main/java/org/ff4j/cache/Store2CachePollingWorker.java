package org.ff4j.cache;

/*
 * #%L
 * ff4j-core
 * %%
 * Copyright (C) 2013 - 2016 FF4J
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.ff4j.core.Feature;
import org.ff4j.core.FeatureStore;
import org.ff4j.property.Property;
import org.ff4j.property.store.PropertyStore;

import java.io.Serializable;

/**
 * Working thread to poll and fetch data from store and copy to local cache.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class Store2CachePollingWorker implements Runnable, Serializable {

    /** Serial. */
    private static final long serialVersionUID = 8252550757489651166L;

    /** feature store. */
    private FeatureStore sourceFeatureStore;
    
    /** Property store. */
    private PropertyStore sourcePropertyStore;
    
    /** Target feature store to be proxified to cache features. */
    private FF4JCacheManager cacheManager;
    
    /**
     * Parameterized constructor.
     *
     * @param sf
     *      source feature store
     * @param sp
     *      source property store
     * @param cp
     *      target cache
     */
    public Store2CachePollingWorker(FeatureStore sf, PropertyStore sp, FF4JCacheManager cp) {
        this.sourceFeatureStore  = sf;
        this.sourcePropertyStore = sp;
        this.cacheManager        = cp;
    }
    
    /** {@inheritDoc} */
    @Override
    public void run() {
        try {
            if (sourceFeatureStore != null) {
                cacheManager.clearFeatures();
                for (Feature f : sourceFeatureStore.readAll()
                                                   .values()) {
                    cacheManager.putFeature(f);
                }
            }
            if (sourcePropertyStore != null) {
                cacheManager.clearProperties();
                for (Property<?> p : sourcePropertyStore.readAllProperties()
                                                        .values()) {
                    cacheManager.putProperty(p);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
