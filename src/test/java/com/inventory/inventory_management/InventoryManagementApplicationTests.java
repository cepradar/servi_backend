package com.inventory.inventory_management;

import com.inventory.InventoryManagementApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
		classes = InventoryManagementApplication.class,
		properties = "jwt.secret=0123456789012345678901234567890123456789012345678901234567890123")
class InventoryManagementApplicationTests {

	@Test
	void contextLoads() {
	}

}
