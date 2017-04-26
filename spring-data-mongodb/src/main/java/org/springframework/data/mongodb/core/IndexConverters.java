/*
 * Copyright 2016 the original author or authors.
 *
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
 */

package org.springframework.data.mongodb.core;

import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.util.ObjectUtils;

import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationAlternate;
import com.mongodb.client.model.CollationCaseFirst;
import com.mongodb.client.model.CollationMaxVariable;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.IndexOptions;

/**
 * {@link Converter Converters} for index-related MongoDB documents/types.
 *
 * @author Mark Paluch
 * @author Christoph Strobl
 * @since 2.0
 */
abstract class IndexConverters {

	private static final Converter<IndexDefinition, IndexOptions> DEFINITION_TO_MONGO_INDEX_OPTIONS;
	private static final Converter<Document, IndexInfo> DOCUMENT_INDEX_INFO;

	static {

		DEFINITION_TO_MONGO_INDEX_OPTIONS = getIndexDefinitionIndexOptionsConverter();
		DOCUMENT_INDEX_INFO = getDocumentIndexInfoConverter();
	}

	private IndexConverters() {

	}

	static Converter<IndexDefinition, IndexOptions> indexDefinitionToIndexOptionsConverter() {
		return DEFINITION_TO_MONGO_INDEX_OPTIONS;
	}

	static Converter<Document, IndexInfo> documentToIndexInfoConverter() {
		return DOCUMENT_INDEX_INFO;
	}

	private static Converter<IndexDefinition, IndexOptions> getIndexDefinitionIndexOptionsConverter() {

		return indexDefinition -> {

			Document indexOptions = indexDefinition.getIndexOptions();
			IndexOptions ops = new IndexOptions();

			if (indexOptions.containsKey("name")) {
				ops = ops.name(indexOptions.get("name").toString());
			}
			if (indexOptions.containsKey("unique")) {
				ops = ops.unique((Boolean) indexOptions.get("unique"));
			}
			if (indexOptions.containsKey("sparse")) {
				ops = ops.sparse((Boolean) indexOptions.get("sparse"));
			}
			if (indexOptions.containsKey("background")) {
				ops = ops.background((Boolean) indexOptions.get("background"));
			}
			if (indexOptions.containsKey("expireAfterSeconds")) {
				ops = ops.expireAfter((Long) indexOptions.get("expireAfterSeconds"), TimeUnit.SECONDS);
			}
			if (indexOptions.containsKey("min")) {
				ops = ops.min(((Number) indexOptions.get("min")).doubleValue());
			}
			if (indexOptions.containsKey("max")) {
				ops = ops.max(((Number) indexOptions.get("max")).doubleValue());
			}
			if (indexOptions.containsKey("bits")) {
				ops = ops.bits((Integer) indexOptions.get("bits"));
			}
			if (indexOptions.containsKey("bucketSize")) {
				ops = ops.bucketSize(((Number) indexOptions.get("bucketSize")).doubleValue());
			}
			if (indexOptions.containsKey("default_language")) {
				ops = ops.defaultLanguage(indexOptions.get("default_language").toString());
			}
			if (indexOptions.containsKey("language_override")) {
				ops = ops.languageOverride(indexOptions.get("language_override").toString());
			}
			if (indexOptions.containsKey("weights")) {
				ops = ops.weights((org.bson.Document) indexOptions.get("weights"));
			}

			for (String key : indexOptions.keySet()) {
				if (ObjectUtils.nullSafeEquals("2dsphere", indexOptions.get(key))) {
					ops = ops.sphereVersion(2);
				}
			}

			if (indexOptions.containsKey("partialFilterExpression")) {
				ops = ops.partialFilterExpression((org.bson.Document) indexOptions.get("partialFilterExpression"));
			}

			if (indexOptions.containsKey("collation")) {

				com.mongodb.client.model.Collation.Builder collationBuilder = Collation.builder();
				Document collation = indexOptions.get("collation", Document.class);

				collationBuilder.locale(collation.getString("locale"));
				if (collation.containsKey("caseLevel")) {
					collationBuilder.caseLevel(collation.getBoolean("caseLevel"));
				}
				if (collation.containsKey("caseFirst")) {
					collationBuilder.collationCaseFirst(CollationCaseFirst.fromString(collation.getString("caseFirst")));
				}
				if (collation.containsKey("strength")) {
					collationBuilder.collationStrength(CollationStrength.fromInt(collation.getInteger("strength")));
				}
				if (collation.containsKey("numericOrdering")) {
					collationBuilder.numericOrdering(collation.getBoolean("numericOrdering"));
				}
				if (collation.containsKey("alternate")) {
					collationBuilder.collationAlternate(CollationAlternate.fromString(collation.getString("alternate")));
				}
				if (collation.containsKey("maxVariable")) {
					collationBuilder.collationMaxVariable(CollationMaxVariable.fromString(collation.getString("maxVariable")));
				}
				if (collation.containsKey("backwards")) {
					collationBuilder.backwards(collation.getBoolean("backwards"));
				}

				ops = ops.collation(collationBuilder.build());
			}

			return ops;
		};
	}

	private static Converter<Document, IndexInfo> getDocumentIndexInfoConverter() {
		return ix -> IndexInfo.indexInfoOf(ix);
	}

}
