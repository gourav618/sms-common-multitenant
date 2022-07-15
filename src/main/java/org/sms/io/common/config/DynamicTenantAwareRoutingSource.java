package org.sms.io.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.sms.io.common.utils.DatabaseConfig;
import org.sms.io.common.utils.TenantContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class DynamicTenantAwareRoutingSource extends AbstractRoutingDataSource {

    @Value("${master-database.username}")
    private String masterUsername;

    @Value("${master-database.password}")
    private String masterPassword;

    @Value("${master-database.url}")
    private String masterUrl;

    @Value("${master-database.driverClassName}")
    private String masterDriverClassName;

    private final String filename;
    private final ObjectMapper objectMapper;
    private final Map<Object, Object> tenants;

    public DynamicTenantAwareRoutingSource(String filename) {
        this(filename, new ObjectMapper());
    }


    @Override
    protected DataSource determineTargetDataSource() {
        String lookupKey = (String) determineCurrentLookupKey();

        return (DataSource) tenants.get(lookupKey);
    }

    public DynamicTenantAwareRoutingSource(String filename, ObjectMapper objectMapper) {
        this.filename = filename;
        this.objectMapper = objectMapper;
        this.tenants = getDataSources();
    }

    private Map<Object, Object> getDataSources() {
        DatabaseConfig[] db = getDataBaseConfiguration();
        return Arrays.stream(db)
                .collect(Collectors.toMap(x -> x.getName(), x -> buildDataSource(x)));
    }

    private DatabaseConfig getMasterDataBaseConfig() {
        return new DatabaseConfig("MASTER", masterDriverClassName, masterUrl, masterUsername, masterPassword);
    }

    private HikariDataSource buildDataSource(DatabaseConfig configuration) {
        HikariDataSource dataSource = new HikariDataSource();

        log.info(configuration.toString());

        dataSource.setInitializationFailTimeout(0);
        dataSource.setMaximumPoolSize(5);
        dataSource.setJdbcUrl(configuration.getUrl());
        dataSource.setUsername(configuration.getUsername());
        dataSource.setPassword(configuration.getPassword());
        dataSource.addDataSourceProperty("url", configuration.getUrl());
        dataSource.addDataSourceProperty("user", configuration.getUsername());
        dataSource.addDataSourceProperty("password", configuration.getPassword());

        try {
            dataSource.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return dataSource;
    }

    private DatabaseConfig[] getDataBaseConfiguration() {

        Resource resource = new ClassPathResource(filename);
        try {
            return objectMapper.readValue(resource.getFile(), DatabaseConfig[].class);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return TenantContext.getCurrentTenant();
    }

    @Scheduled(cron = "0 0 12 * * ?")
    public void insertOrUpdateDataSources() {

        DatabaseConfig[] configurations = getDataBaseConfiguration();

        for (DatabaseConfig configuration : configurations) {
            if (tenants.containsKey(configuration.getName())) {
                HikariDataSource dataSource = (HikariDataSource) tenants.get(configuration.getName());
                // We only shutdown and reload, if the configuration has actually changed...
                if (!isCurrentConfiguration(dataSource, configuration)) {
                    // Make sure we close this DataSource first...
                    dataSource.close();
                    // ... and then insert a new DataSource:
                    tenants.put(configuration.getName(), buildDataSource(configuration));
                }
            } else {
                tenants.put(configuration.getName(), buildDataSource(configuration));
            }
        }
    }

    private boolean isCurrentConfiguration(HikariDataSource dataSource, DatabaseConfig configuration) {
        return Objects.equals(dataSource.getDataSourceProperties().getProperty("user"), configuration.getUsername())
                && Objects.equals(dataSource.getDataSourceProperties().getProperty("url"), configuration.getUrl())
                && Objects.equals(dataSource.getDataSourceProperties().getProperty("password"), configuration.getPassword())
                && Objects.equals(dataSource.getDataSourceClassName(), configuration.getDriverClassName());
    }

    public String getFilename() {
        return filename;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Map<Object, Object> getTenants() {
        return tenants;
    }

    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
    }
}
