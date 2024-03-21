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

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;


public class DataLoader {

    public static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final Map<String, String> NULL_PLUGIN_PROPERTIES = null;
    private final DataLoaderProperties properties;
    private RequestOptions requestOptions;
    private KillBillHttpClient killBillHttpClient;
    private AccountApi accountApi;
    private CatalogApi catalogApi;
    private SubscriptionApi subscriptionApi;
    private TenantApi tenantApi;


    public DataLoader(final String configPath) throws Exception {
        this.properties = new DataLoaderProperties(configPath);

        if (properties.getLogbackXMLPath() != null && !properties.getLogbackXMLPath().isEmpty() && new File(properties.getLogbackXMLPath()).exists()) {
            configureLogback(properties.getLogbackXMLPath());
        }

        requestOptions = RequestOptions.builder()
                .withCreatedBy("Integration test")
                .withReason("reason")
                .withComment("comment")
                .build();


        setupClient(properties.getAdminUserName(), properties.getAdminPassword(), properties.getTenantApiKey(), properties.getTenantApiSecret());
        createTenant(properties.getTenantApiKey(), properties.getTenantApiSecret(), true);
        uploadTenantCatalog("Catalog.xml", true);
        setDate(properties.getToday().toString());

    }

    public static void main(String[] args) {
        logger.info("Usage: java -jar killbill-data-load-tool.jar <config file path>");
        final String configPath;

        if (args.length == 0 || !new File(args[0]).exists()) {
            logger.info("Config file not specified or file does not exist at the specified location, using default using default src/main/resources/config.properties");
            configPath = null;
        } else {
            configPath = args[0];
            logger.info("Using user specified config file:{}", configPath);
        }
        try {
            DataLoader dataLoader = new DataLoader(configPath);
            dataLoader.createAccountsAndSubscriptions();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void createAccountsAndSubscriptions() throws KillBillClientException, InterruptedException {
        LocalDate today = properties.getToday();
        int nbDays = properties.getNbDays();
        if (nbDays == -1) {
            logger.info("nbdays=-1, Running forever...");
        } else {
            logger.info("Creating accounts and subscriptions for {} days", nbDays);
        }
        do {
            createAccountsAndSubscriptionsOnDate(today);
            today = today.plusDays(1);
            setDate(today.toString());
            nbDays--;
        } while (nbDays != 0);
    }

    private void createAccountsAndSubscriptionsOnDate(LocalDate today) throws KillBillClientException, InterruptedException {
        logger.info("[{}]:Creating {} accounts and {} subscriptions", today, properties.getNbAccountsPerDay(), properties.getNbSubscriptionsPerAccount());
        for (int j = 1; j <= properties.getNbAccountsPerDay(); j++) {
            Account account = createAccount();
            logger.debug("Sleeping for {} secs between account and subscription creation", properties.getSleepTime());
            Thread.sleep(properties.getSleepTime()*1000);
            logger.debug("Waking up and resuming subscription creation");
            for (int k = 1; k <= properties.getNbSubscriptionsPerAccount(); k++) {
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

        killBillHttpClient = new KillBillHttpClient(String.format("http://%s:%d", properties.getServerHost(), properties.getServerPort()),
                username,
                password,
                apiKey,
                apiSecret,
                null,
                null, properties.getServerConnectionTimeout() * 1000,
                properties.getServerReadTimeout() * 1000);

        accountApi = new AccountApi(killBillHttpClient);
        catalogApi = new CatalogApi(killBillHttpClient);
        subscriptionApi = new SubscriptionApi(killBillHttpClient);
        tenantApi = new TenantApi(killBillHttpClient);
    }

    private String uploadTenantCatalog(final String catalog, final boolean fetch) throws IOException, URISyntaxException, KillBillClientException {

        catalogApi.deleteCatalog(requestOptions);// delete existing catalog if any
        catalogApi.uploadCatalogXml(FileUtil.toString(catalog, true), requestOptions);
        return fetch ? catalogApi.getCatalogXml(null, null, requestOptions) : null;
    }

    private void setDate(String dateTime) throws KillBillClientException {
        killBillHttpClient.doPost("/1.0/kb/test/clock?requestedDate=" + dateTime, null, requestOptions);
    }

    private static void configureLogback(String customConfigFilePath) {
        if (customConfigFilePath != null && !customConfigFilePath.isEmpty()) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            context.reset();

            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                configurator.doConfigure(customConfigFilePath);
            } catch (Exception e) {
                // Configuration error, fallback to default configuration
                logger.error("Error configuring Logback with custom file: {}", e.getMessage());
            }
        }
    }

}
