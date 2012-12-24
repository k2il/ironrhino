package org.ironrhino.core.spring.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.jolbox.bonecp.BoneCPDataSource;

@Configuration
public class DataSourceConfiguration {

	@Value("${jdbc.driverClassName:com.mysql.jdbc.Driver}")
	private String driverClass;

	@Value("${jdbc.url:jdbc:mysql:///ironrhino?autoReconnectForPools=true&amp;useUnicode=true&amp;characterEncoding=UTF-8&amp;useServerPrepStmts=true}")
	private String jdbcUrl;

	@Value("${jdbc.username:root}")
	private String username;

	@Value("${jdbc.password:}")
	private String password;

	@Value("${dataSource.maxActive:50}")
	private int maxConnectionsPerPartition;

	@Value("${dataSource.initialSize:5}")
	private int minConnectionsPerPartition;

	@Value("${dataSource.statementsCacheSize:50}")
	private int statementsCacheSize;

	@Value("${dataSource.idleConnectionTestPeriodInMinutes:120}")
	private int idleConnectionTestPeriodInMinutes;

	@Value("${dataSource.idleMaxAgeInMinutes:60}")
	private int idleMaxAgeInMinutes;

	@Value("${dataSource.maxConnectionAgeInSeconds:14400}")
	private int maxConnectionAgeInSeconds;

	@Value("${jdbc.connectionTestStatement:/* ping *\\/ select 1}")
	private String connectionTestStatement;

	public @Bean(destroyMethod = "close")
	DataSource targetDataSource() {
		BoneCPDataSource ds = new BoneCPDataSource();
		ds.setDriverClass(driverClass);
		ds.setJdbcUrl(jdbcUrl);
		ds.setUsername(username);
		ds.setPassword(password);
		ds.setMaxConnectionsPerPartition(maxConnectionsPerPartition);
		ds.setMinConnectionsPerPartition(minConnectionsPerPartition);
		ds.setStatementsCacheSize(statementsCacheSize);
		ds.setIdleConnectionTestPeriodInMinutes(idleConnectionTestPeriodInMinutes);
		ds.setIdleMaxAgeInMinutes(idleMaxAgeInMinutes);
		ds.setMaxConnectionAgeInSeconds(maxConnectionAgeInSeconds);
		ds.setConnectionTestStatement(connectionTestStatement);
		return ds;
	}

	public @Bean
	DataSource dataSource() {
		return new LazyConnectionDataSourceProxy(targetDataSource());
	}
}
