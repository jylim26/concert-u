package com.concertu.ticketing.global.config;

import java.time.*;
import org.springframework.context.annotation.*;

@Configuration
public class ClockConfig {

	@Bean
	public Clock clock() {
		return Clock.systemUTC();
	}
}
