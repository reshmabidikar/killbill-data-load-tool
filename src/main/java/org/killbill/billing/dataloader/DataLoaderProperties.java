/*
 * Copyright 2014-2024 The Billing Project, LLC
 *
 * The Billing Project licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.killbill.billing.dataloader;

import org.joda.time.LocalDate;

import java.io.StringReader;
import java.util.Properties;
import java.util.UUID;

public class DataLoaderProperties {
    private final Properties properties;

    private final static String DEFAULT_CONFIG_FILE = "config.properties";

    public DataLoaderProperties(final String configPath) throws Exception {
        this.properties = loadProperties(configPath);
    }

    public String getServerHost() {
        return isPresent("org.killbill.dataloader.server.host") ? properties.getProperty("org.killbill.dataloader.server.host") : "1277.0.0.1";
    }

    public int getServerPort() {
        return isPresent("org.killbill.dataloader.server.port") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.port")) : 8080;
    }

    public int getServerConnectionTimeout() {
        return isPresent("org.killbill.dataloader.server.connection.timeout") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.connection.timeout")) : 10;
    }

    public int getServerReadTimeout() {
        return isPresent("org.killbill.dataloader.server.read.timeout") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.read.timeout")) : 60;
    }

    public String getAdminUserName() {
        return isPresent("org.killbill.dataloader.admin.username") ? properties.getProperty("org.killbill.dataloader.admin.username") : "admin";
    }

    public String getAdminPassword() {
        return isPresent("org.killbill.dataloader.admin.password") ? properties.getProperty("org.killbill.dataloader.admin.password") : "password";
    }

    public String getTenantApiKey() {
        return isPresent("org.killbill.dataloader.tenant.apiKey") ? properties.getProperty("org.killbill.dataloader.tenant.apiKey") : UUID.randomUUID().toString();
    }

    public String getTenantApiSecret() {
        return isPresent("org.killbill.dataloader.tenant.apiSecret") ? properties.getProperty("org.killbill.dataloader.tenant.apiSecret") : UUID.randomUUID().toString();
    }

    public int getNbAccountsPerDay() {
        return isPresent("org.killbill.dataloader.nbAccountsPerDay") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbAccountsPerDay")) : 3;
    }

    public int getNbSubscriptionsPerAccount() {
        return isPresent("org.killbill.dataloader.nbSubscriptionsPerAccount") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbSubscriptionsPerAccount")) : 2;
    }

    public int getNbDays() {
        return isPresent("org.killbill.dataloader.nbDays") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbDays")) : 30;
    }

    public Boolean isJitterEnabled() {
        return isPresent("org.killbill.dataloader.jitter.enable") ? Boolean.valueOf(properties.getProperty("org.killbill.dataloader.jitter.enable")) : false;
    }

    public Integer getJitterUpperBound() {
        return isPresent("org.killbill.dataloader.jitter.upperbound") ? Integer.valueOf(properties.getProperty("org.killbill.dataloader.jitter.upperbound")) : 10;
    }

    public LocalDate getToday() {
        return isPresent("org.killbill.dataloader.startDate") ? new LocalDate(properties.getProperty("org.killbill.dataloader.startDate")) : new LocalDate("2024-01-01");
    }

    public Long getSleepTime() {
        return isPresent("org.killbill.dataloader.sleep.time.secs") ? Long.valueOf(properties.getProperty("org.killbill.dataloader.sleep.time.secs")) : 10L;
    }

    public String getLogbackXMLPath() {
        return properties.getProperty("logback.configurationFile"); //We do not return a default here
    }

    private Properties loadProperties(final String configPath) throws Exception {
        final String propertiesAsString;
        if (configPath != null) {
            propertiesAsString = FileUtil.toString(configPath, false);
        } else {
            propertiesAsString = FileUtil.toString(DEFAULT_CONFIG_FILE, true);
        }
        final Properties properties = new Properties();
        properties.load(new StringReader(propertiesAsString));
        return properties;

    }

    private boolean isPresent(final String propertyName) {
        return properties.getProperty(propertyName) != null && !properties.getProperty(propertyName).isEmpty();
    }
}
