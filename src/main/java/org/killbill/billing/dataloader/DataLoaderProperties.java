package org.killbill.billing.dataloader;

import org.joda.time.LocalDate;

import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

public class DataLoaderProperties {
    private final String serverHost;
    private final int serverPort;
    private final int serverConnectionTimeout;
    private final int serverReadTimeout;
    private final String adminUserName;
    private final String adminPassword;
    private final String tenantApiKey;
    private final String tenantApiSecret;
    private final int nbAccountsPerDay;
    private final int nbSubscriptionsPerAccount;
    private final int nbDays;
    private final LocalDate today;

    public DataLoaderProperties() throws Exception {

        Properties properties = loadProperties();
        this.serverHost = isPresent(properties, "org.killbill.dataloader.server.host") ? properties.getProperty("org.killbill.dataloader.server.host") : "1277.0.0.1";
        this.serverPort = isPresent(properties, "org.killbill.dataloader.server.port") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.port")) : 8080;
        this.serverConnectionTimeout = isPresent(properties, "org.killbill.dataloader.server.connection.timeout") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.connection.timeout")) : 10;
        this.serverReadTimeout = isPresent(properties, "org.killbill.dataloader.server.read.timeout") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.read.timeout")) : 60;
        this.adminUserName = isPresent(properties, "org.killbill.dataloader.admin.username") ? properties.getProperty("org.killbill.dataloader.admin.username") : "admin";
        this.adminPassword = isPresent(properties, "org.killbill.dataloader.admin.password") ? properties.getProperty("org.killbill.dataloader.admin.password") : "password";
        this.tenantApiKey = isPresent(properties, "org.killbill.dataloader.tenant.apiKey") ? properties.getProperty("org.killbill.dataloader.tenant.apiKey") : UUID.randomUUID().toString();
        this.tenantApiSecret = isPresent(properties, "org.killbill.dataloader.tenant.apiSecret") ? properties.getProperty("org.killbill.dataloader.tenant.apiSecret") : UUID.randomUUID().toString();
        this.nbAccountsPerDay = isPresent(properties, "org.killbill.dataloader.nbAccountsPerDay") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbAccountsPerDay")) : 3;
        this.nbDays = isPresent(properties, "org.killbill.dataloader.nbDays") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbDays")) : 30;
        this.nbSubscriptionsPerAccount = isPresent(properties, "org.killbill.dataloader.nbSubscriptionsPerAccount") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbSubscriptionsPerAccount")) : 2;
        this.today = isPresent(properties, "org.killbill.dataloader.startDate") ? new LocalDate(properties.getProperty("org.killbill.dataloader.startDate")) : new LocalDate("2024-01-01");

    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getServerConnectionTimeout() {
        return serverConnectionTimeout;
    }

    public int getServerReadTimeout() {
        return serverReadTimeout;
    }

    public String getAdminUserName() {
        return adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public String getTenantApiKey() {
        return tenantApiKey;
    }

    public String getTenantApiSecret() {
        return tenantApiSecret;
    }

    public int getNbAccountsPerDay() {
        return nbAccountsPerDay;
    }

    public int getNbSubscriptionsPerAccount() {
        return nbSubscriptionsPerAccount;
    }

    public int getNbDays() {
        return nbDays;
    }

    public LocalDate getToday() {
        return today;
    }

    private Properties loadProperties() throws Exception {
        final String propertiesAsString = FileUtil.toString("config.properties");
        final Properties properties = new Properties();
        properties.load(new StringReader(propertiesAsString));
        return properties;

    }

    private boolean isPresent(final Properties properties, final String propertyName) {
        return properties.getProperty(propertyName) != null && !properties.getProperty(propertyName).isEmpty();
    }


}
