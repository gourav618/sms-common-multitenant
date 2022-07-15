package org.sms.io.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseInitialisation {

    @Primary
	@Bean
	public DataSource dataSource() {

		DynamicTenantAwareRoutingSource dataSource = new DynamicTenantAwareRoutingSource("tenants.json");
		dataSource.setTargetDataSources(dataSource.getTenants());
		dataSource.afterPropertiesSet();
		return dataSource;

	}
}
