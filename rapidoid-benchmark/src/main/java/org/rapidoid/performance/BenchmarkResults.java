package org.rapidoid.performance;

/*
 * #%L
 * rapidoid-benchmark
 * %%
 * Copyright (C) 2014 - 2017 Nikolche Mihajlovski and contributors
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

import org.rapidoid.RapidoidThing;
import org.rapidoid.annotation.Authors;
import org.rapidoid.annotation.Since;
import org.rapidoid.u.U;

import java.util.DoubleSummaryStatistics;
import java.util.List;

@Authors("Nikolche Mihajlovski")
@Since("5.3.0")
public class BenchmarkResults extends RapidoidThing {

	int rounds = 0;

	int errors = 0;

	List<Double> throughputs = U.list();

	public DoubleSummaryStatistics stats() {
		return throughputs.stream()
			.mapToDouble(x -> x)
			.summaryStatistics();
	}

	public int bestThroughput() {
		return (int) Math.round(stats().getMax());
	}

	@Override
	public String toString() {
		return "BenchmarkResults{" +
			"rounds=" + rounds +
			", errors=" + errors +
			", throughputs=" + throughputs +
			'}' + stats();
	}

}
