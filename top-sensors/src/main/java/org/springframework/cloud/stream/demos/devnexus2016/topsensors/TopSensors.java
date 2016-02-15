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

package org.springframework.cloud.stream.demos.devnexus2016.topsensors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.cloud.stream.tuple.TupleBuilder.tuple;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.rxjava.EnableRxJavaProcessor;
import org.springframework.cloud.stream.annotation.rxjava.RxJavaProcessor;
import org.springframework.cloud.stream.tuple.Tuple;
import org.springframework.context.annotation.Bean;

/**
 * @author Marius Bogoevici
 */
@SpringBootApplication
@EnableRxJavaProcessor
@EnableConfigurationProperties(TopSensorsProperties.class)
public class TopSensors {

	@Autowired
	private TopSensorsProperties topSensorsProperties;

	public static void main(String[] args) {
		SpringApplication.run(TopSensors.class,args);
	}

	@Bean
	public RxJavaProcessor<Tuple,Tuple> processor() {
		return inputStream -> inputStream
				.window(topSensorsProperties.getTimeWindowLength(), MILLISECONDS)
				.flatMap(w ->
						w.groupBy(t -> t.getInt("sensorId"))
								.flatMap(g -> g.last())
								.toSortedList(TopSensors::compareTemperatures)
								.map(l -> l.subList(0, Math.min(topSensorsProperties.getMaxTopSensors(), l.size())))
								.map(l -> tuple().of("hottest", asMap(l))));
	}

	private static Integer compareTemperatures(Tuple t1,Tuple t2) {
		return Double.compare(t2.getDouble("averageTemperature"), t1.getDouble("averageTemperature"));
	}

	private static Map<String, Double> asMap(List<Tuple> tuples) {
		Map<String, Double> returnValue = new LinkedHashMap<>();
		for (Tuple tuple : tuples) {
			returnValue.put(Integer.toString(tuple.getInt("sensorId")), tuple.getDouble("averageTemperature"));
		}
		return returnValue;
	}
 }
