package org.springframework.cloud.stream.devnexus2016.datagen;/*
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

import javax.annotation.PostConstruct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

/**
 * @author Marius Bogoevici
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationProperties
@Controller
public class DataGenerator {

	private String url = "http://localhost:9000";

	private int sensorCount = 5;

	private double initialValue = 10.0;

	private SensorData[] sensorData;

	public void setUrl(String url) {
		this.url = url;
	}

	public void setSensorCount(int sensorCount) {
		this.sensorCount = sensorCount;
	}

	public void setInitialValue(double initialValue) {
		this.initialValue = initialValue;
	}

	private RestTemplate restTemplate = new RestTemplate();

	@PostConstruct
	public void afterConstruction() {
		sensorData = new SensorData[sensorCount];
		for (int i = 0; i < sensorCount; i++) {
			sensorData[i] = new SensorData(i,initialValue);
		}
	}

	@RequestMapping(path = "/sensors", method = RequestMethod.POST)
	public HttpStatus updateValue(@RequestBody  SensorData sensorData) {
		if (sensorData.getSensorId() < 0) {
			return HttpStatus.BAD_REQUEST;
		}
		if (sensorData.getSensorId() >= sensorCount) {
			return HttpStatus.NOT_FOUND;
		}
		this.sensorData[sensorData.getSensorId()] = sensorData;
		return HttpStatus.ACCEPTED;
	}



	@Scheduled(fixedRate = 100)
	public void sendSensorData() {
		for (int i = 0; i < sensorCount; i++) {
			ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity(url, new HttpEntity<>(sensorData[i]), String.class);
			if (!HttpStatus.ACCEPTED.equals(stringResponseEntity.getStatusCode())) {
				System.out.println("Not ok");
			}
		}

	}

	public static void main(String[] args) {
		SpringApplication.run(DataGenerator.class, args);
	}

}
