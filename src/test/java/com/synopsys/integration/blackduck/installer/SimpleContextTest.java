package com.synopsys.integration.blackduck.installer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
@Disabled
public class SimpleContextTest {
	@Test
	public void contextLoads() {
	}

}
