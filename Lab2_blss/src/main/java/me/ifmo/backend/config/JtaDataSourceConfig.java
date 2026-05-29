package me.ifmo.backend.config;

import com.atomikos.jdbc.AtomikosDataSourceBean;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JtaDataSourceConfig {

    @Primary
    @Bean(initMethod = "init", destroyMethod = "close")
    public AtomikosDataSourceBean dataSource(
            @Value("${spring.datasource.xa.properties.user}") String username,
            @Value("${spring.datasource.xa.properties.password}") String password,
            @Value("${spring.datasource.xa.properties.serverName}") String serverName,
            @Value("${spring.datasource.xa.properties.portNumber}") int portNumber,
            @Value("${spring.datasource.xa.properties.databaseName}") String databaseName,
            @Value("${spring.datasource.xa.unique-resource-name}") String uniqueResourceName,
            @Value("${spring.datasource.xa.min-pool-size}") int minPoolSize,
            @Value("${spring.datasource.xa.max-pool-size}") int maxPoolSize,
            @Value("${spring.datasource.xa.borrow-connection-timeout}") int borrowConnectionTimeout,
            @Value("${spring.datasource.xa.max-idle-time}") int maxIdleTime,
            @Value("${spring.datasource.xa.maintenance-interval}") int maintenanceInterval,
            @Value("${spring.datasource.xa.test-query}") String testQuery
    ) {
        PGXADataSource xaDataSource = new PGXADataSource();
        xaDataSource.setUser(username);
        xaDataSource.setPassword(password);
        xaDataSource.setServerNames(new String[]{serverName});
        xaDataSource.setPortNumbers(new int[]{portNumber});
        xaDataSource.setDatabaseName(databaseName);

        AtomikosDataSourceBean dataSource = new AtomikosDataSourceBean();
        dataSource.setXaDataSource(xaDataSource);
        dataSource.setUniqueResourceName(uniqueResourceName);
        dataSource.setMinPoolSize(minPoolSize);
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setBorrowConnectionTimeout(borrowConnectionTimeout);
        dataSource.setMaxIdleTime(maxIdleTime);
        dataSource.setMaintenanceInterval(maintenanceInterval);
        dataSource.setTestQuery(testQuery);

        return dataSource;
    }
}