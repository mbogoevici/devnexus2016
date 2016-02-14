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

package org.springframework.cloud.stream.demos.devnexus2016.average;

import static org.springframework.cloud.stream.tuple.TupleBuilder.tuple;
import static rx.observables.MathObservable.averageDouble;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.rxjava.EnableRxJavaProcessor;
import org.springframework.cloud.stream.annotation.rxjava.RxJavaProcessor;
import org.springframework.cloud.stream.tuple.Tuple;
import org.springframework.context.annotation.Bean;

/**
 * @author Marius Bogoevici
 */
@SpringBootApplication
@EnableRxJavaProcessor
public class AverageCalculator {

	@Autowired
	private AverageCalculatorProperties averageCalculatorProperties;


	public static void main(String[] args) {
		SpringApplication.run(AverageCalculator.class, args);
	}

	@Bean
	public RxJavaProcessor<Tuple,Tuple> processor() {
		return inputStream -> inputStream
				.window(averageCalculatorProperties.getTimeWindowLength(),
						averageCalculatorProperties.getTimeWindowShift(),
						TimeUnit.MILLISECONDS)
				.flatMap(w -> w.groupBy(data -> data.getInt("sensorId"))
								.flatMap(
										dataById -> averageDouble(dataById.map(t -> t.getDouble("temperature")))
												.map(d -> tuple().of("sensorId", dataById.getKey(), "averageTemperature", d))
								)
				);
	}

}
