/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.dataflow.core;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.cloud.dataflow.core.dsl.StreamNode;
import org.springframework.cloud.dataflow.core.dsl.StreamParser;
import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

/**
 * Representation of a defined stream. A stream consists of an ordered list of apps used
 * to process data. Each app may contain configuration options provided at the time of
 * stream creation.
 * <p>
 * This stream definition does not include any deployment or runtime configuration for a
 * stream.
 *
 * @author Patrick Peralta
 * @author Mark Fisher
 * @see StreamAppDefinition
 */
public class StreamDefinition {

	/**
	 * Name of stream.
	 */
	private final String name;

	/**
	 * DSL definition for stream.
	 */
	private final String dslText;

	/**
	 * Ordered list of {@link StreamAppDefinition}s comprising this stream. The source is
	 * the first entry and the sink is the last entry.
	 */
	private final LinkedList<StreamAppDefinition> applicationDefinitions;

	/**
	 * Construct a {@code StreamDefinition}.
	 *
	 * @param name name of stream
	 * @param dslText DSL definition for stream
	 */
	public StreamDefinition(String name, String dslText) {
		Assert.hasText(name, "name is required");
		Assert.hasText(dslText, "dslText is required");
		this.name = name;
		this.dslText = dslText;
		this.applicationDefinitions = new LinkedList<>();
		StreamNode streamNode = new StreamParser(name, dslText).parse();
		for (StreamAppDefinition appDefinition : new StreamApplicationDefinitionBuilder(name, streamNode).build()) {
			this.applicationDefinitions.addFirst(appDefinition);
		}
	}

	/**
	 * Return the name of this stream.
	 *
	 * @return stream name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Return the DSL definition for this stream.
	 *
	 * @return stream definition DSL
	 */
	public String getDslText() {
		return dslText;
	}

	/**
	 * Return the ordered list of application definitions for this stream as a
	 * {@link List}. This allows for retrieval of application definitions in the stream by
	 * index. Application definitions are maintained in stream flow order (source is
	 * first, sink is last).
	 *
	 * @return list of application definitions for this stream definition
	 */
	public List<StreamAppDefinition> getAppDefinitions() {
		return Collections.unmodifiableList(this.applicationDefinitions);
	}

	/**
	 * Return an iterator that indicates the order of application deployments for this
	 * stream. The application definitions are returned in reverse order; i.e. the sink is
	 * returned first followed by the processors in reverse order followed by the source.
	 *
	 * @return iterator that iterates over the application definitions in deployment order
	 */
	public Iterator<StreamAppDefinition> getDeploymentOrderIterator() {
		return new ReadOnlyIterator<>(this.applicationDefinitions.descendingIterator());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dslText == null) ? 0 : dslText.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StreamDefinition other = (StreamDefinition) obj;
		if (dslText == null) {
			if (other.dslText != null)
				return false;
		}
		else if (!dslText.equals(other.dslText))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return new ToStringCreator(this).append("name", this.name).append("definition", this.dslText).toString();
	}

	/**
	 * Iterator that prevents mutation of its backing data structure.
	 *
	 * @param <T> the type of elements returned by this iterator
	 */
	private static class ReadOnlyIterator<T> implements Iterator<T> {
		private final Iterator<T> wrapped;

		ReadOnlyIterator(Iterator<T> wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		@Override
		public T next() {
			return wrapped.next();
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
