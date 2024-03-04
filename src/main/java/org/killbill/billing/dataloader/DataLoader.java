package org.killbill.billing.dataloader;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import org.joda.time.LocalDate;
import org.killbill.billing.catalog.api.Currency;
import org.killbill.billing.client.KillBillClientException;
import org.killbill.billing.client.KillBillHttpClient;
import org.killbill.billing.client.RequestOptions;
import org.killbill.billing.client.api.gen.AccountApi;
import org.killbill.billing.client.api.gen.CatalogApi;
import org.killbill.billing.client.api.gen.SubscriptionApi;
import org.killbill.billing.client.api.gen.TenantApi;
import org.killbill.billing.client.model.gen.Account;
import org.killbill.billing.client.model.gen.Subscription;
import org.killbill.billing.client.model.gen.Tenant;
import org.killbill.commons.utils.io.ByteStreams;
import org.killbill.commons.utils.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class DataLoader {

    public static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final Map<String, String> NULL_PLUGIN_PROPERTIES = null;
    private String serverHost;
    private int serverPort;
    private int serverConnectionTimeout;
    private int serverReadTimeout;
    private String adminUserName;
    private String adminPassword;
    private String tenantApiKey;
    private String tenantApiSecret;

    private int nbAccountsPerDay;
    private int nbSubscriptionsPerAccount;

    private LocalDate today;
    private RequestOptions requestOptions;
    private KillBillHttpClient killBillHttpClient;
    private AccountApi accountApi;
    private CatalogApi catalogApi;
    private SubscriptionApi subscriptionApi;
    private TenantApi tenantApi;


    public DataLoader() throws Exception {
        initializeProperties();

        requestOptions = RequestOptions.builder()
                .withCreatedBy("Integration test")
                .withReason("reason")
                .withComment("comment")
                .build();


        setupClient(adminUserName, adminPassword, tenantApiKey, tenantApiSecret);
        createTenant(tenantApiKey, tenantApiSecret, true);
        uploadTenantCatalog("Catalog.xml", true);
        setDate(today.toString());

    }

    private void initializeProperties() throws Exception {
        Properties properties = loadProperties();

        this.serverHost = isPresent(properties,"org.killbill.dataloader.server.host")  ? properties.getProperty("org.killbill.dataloader.server.host") : "1277.0.0.1";
        this.serverPort = isPresent(properties,"org.killbill.dataloader.server.port")  ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.port")) : 8080;
        this.serverConnectionTimeout = isPresent(properties,"org.killbill.dataloader.server.connection.timeout")  ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.connection.timeout")) : 10;
        this.serverReadTimeout = isPresent(properties,"org.killbill.dataloader.server.read.timeout")  ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.server.read.timeout")) : 60;
        this.adminUserName = isPresent(properties,"org.killbill.dataloader.admin.username") ? properties.getProperty("org.killbill.dataloader.admin.username") : "admin";
        this.adminPassword = isPresent(properties,"org.killbill.dataloader.admin.password") ? properties.getProperty("org.killbill.dataloader.admin.password") : "password";
        this.tenantApiKey = isPresent(properties,"org.killbill.dataloader.tenant.apiKey") ? properties.getProperty("org.killbill.dataloader.tenant.apiKey") : UUID.randomUUID().toString();
        this.tenantApiSecret = isPresent(properties,"org.killbill.dataloader.tenant.apiSecret")  ? properties.getProperty("org.killbill.dataloader.tenant.apiSecret") : UUID.randomUUID().toString();
        this.nbAccountsPerDay = isPresent(properties, "org.killbill.dataloader.nbAccountsPerDay") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbAccountsPerDay")) : 3;
        this.nbSubscriptionsPerAccount = isPresent(properties, "org.killbill.dataloader.nbSubscriptionsPerAccount") ? Integer.parseInt(properties.getProperty("org.killbill.dataloader.nbSubscriptionsPerAccount")) : 2;
        this.today = isPresent(properties,"org.killbill.dataloader.startDate")  ? new LocalDate(properties.getProperty("org.killbill.dataloader.startDate")) : new LocalDate("2024-01-01");

    }

    private boolean isPresent(final Properties properties, final String propertyName) {
        return properties.getProperty(propertyName) != null && !properties.getProperty(propertyName).isEmpty();
    }

    public static void main(String[] args) {
        logger.info("Loading Data....");
        try {
            DataLoader dataLoader = new DataLoader();
            dataLoader.createAccountsAndSubscriptions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createAccountsAndSubscriptions() throws KillBillClientException {
        for (int i = 1; i <= nbAccountsPerDay; i++) {
            logger.info("Creating account {}", i);
            Account account = createAccount();
            for (int j = 1; j <= nbSubscriptionsPerAccount; j++) {
                logger.info("Creating subscription {}", j);
                Subscription subscription = createSubscription(account.getAccountId(), "pistol-monthly-notrial");
            }


        }
    }

    private Account createAccount() throws KillBillClientException {
        final Account input = getAccountData(UUID.randomUUID().toString());
        return accountApi.createAccount(input, requestOptions);
    }

    private Account getAccountData(final String externalKey) {
        final Account account = new Account();
        account.setCurrency(Currency.USD);
        account.setLocale("en-US");
        account.setExternalKey(externalKey);
        return account;
    }

    private Subscription createSubscription(final UUID accountId, final String planName) throws KillBillClientException {
        final Subscription subscription = getSubscriptionData(accountId, UUID.randomUUID().toString(), planName);
        return subscriptionApi.createSubscription(subscription, null, (LocalDate) null, Collections.emptyMap(), requestOptions);
    }

    private Subscription getSubscriptionData(final UUID accountId, final String externalKey, final String planName) {
        Subscription subscription = new Subscription();
        subscription.setExternalKey(externalKey);
        subscription.setPlanName(planName);
        subscription.setAccountId(accountId);
        return subscription;
    }

    private Tenant createTenant(final String apiKey, final String apiSecret, final boolean useGlobalDefault) throws KillBillClientException, JsonProcessingException {
        final Tenant tenantData = new Tenant();
        tenantData.setApiKey(apiKey);
        tenantData.setApiSecret(apiSecret);
        Tenant tenant = null;
        try {
            tenant = tenantApi.getTenantByApiKey(apiKey, requestOptions);
        } catch (KillBillClientException e) {
            //TODO add check here to confirm that this exception indeed indicates missing tenant
            logger.info("Tenant with apiKey {} does not exist, creating it", apiKey);
            tenant = tenantApi.createTenant(tenantData, useGlobalDefault, requestOptions);
            tenant.setApiSecret(apiSecret);
        }
        return tenant;
    }

    private void setupClient(final String username, final String password, final String apiKey, final String apiSecret) {
        requestOptions = requestOptions.extend()
                .withTenantApiKey(apiKey)
                .withTenantApiSecret(apiSecret)
                .build();

        killBillHttpClient = new KillBillHttpClient(String.format("http://%s:%d", serverHost, serverPort),
                username,
                password,
                apiKey,
                apiSecret,
                null,
                null, serverConnectionTimeout * 1000,
                serverReadTimeout * 1000);

        accountApi = new AccountApi(killBillHttpClient);
        catalogApi = new CatalogApi(killBillHttpClient);
        subscriptionApi = new SubscriptionApi(killBillHttpClient);
        tenantApi = new TenantApi(killBillHttpClient);
    }

    private String uploadTenantCatalog(final String catalog, final boolean fetch) throws IOException, URISyntaxException, KillBillClientException {

        catalogApi.deleteCatalog(requestOptions);// delete existing catalog if any
        catalogApi.uploadCatalogXml(toString(catalog), requestOptions);
        return fetch ? catalogApi.getCatalogXml(null, null, requestOptions) : null;
    }

    private void setDate(String dateTime) throws KillBillClientException {
        killBillHttpClient.doPost("/1.0/kb/test/clock?requestedDate=" + dateTime, null, requestOptions);
    }

    private Properties loadProperties() throws Exception {
        final String propertiesAsString = toString("config.properties");
        final Properties properties = new Properties();
        properties.load(new StringReader(propertiesAsString));
        return properties;

    }

    public static String toString(final String resourceName) throws IOException {
        final InputStream inputStream = Resources.getResource(resourceName).openStream();
        try {
            return new String(ByteStreams.toByteArray(inputStream), StandardCharsets.UTF_8);
        } finally {
            inputStream.close();
        }
    }
}
