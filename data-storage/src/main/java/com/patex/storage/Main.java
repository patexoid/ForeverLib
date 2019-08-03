package com.patex.storage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.patex.storage.server.FileServer;
import com.netflix.config.DynamicPropertyFactory;

import java.time.Duration;

public class Main {

    private static ApplicationInfoManager applicationInfoManager;
    private static EurekaClient eurekaClient;

    private static synchronized ApplicationInfoManager initializeApplicationInfoManager(EurekaInstanceConfig instanceConfig) {
        if (applicationInfoManager == null) {
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }

        return applicationInfoManager;
    }

    private static synchronized EurekaClient initializeEurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig clientConfig) {
        if (eurekaClient == null) {
            eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        }

        return eurekaClient;
    }

    public static void main(String[] args) {
        System.setProperty("eureka.client.props","eureka-service");
        Thread thread = new Thread(() -> {
            DynamicPropertyFactory configInstance = DynamicPropertyFactory.getInstance();
            ApplicationInfoManager applicationInfoManager = initializeApplicationInfoManager(new MyDataCenterInstanceConfig());
            EurekaClient eurekaClient = initializeEurekaClient(applicationInfoManager, new DefaultEurekaClientConfig());

            ExampleServiceBase exampleServiceBase = new ExampleServiceBase(applicationInfoManager, eurekaClient, configInstance);
            try {
                exampleServiceBase.start();
            } finally {
                // the stop calls shutdown on eurekaClient
                exampleServiceBase.stop();
            }
        });
        thread.setDaemon(true);
        thread.start();
        Injector injector = Guice.createInjector(new GuicePropertyModule());
        FileServer fileServer = injector.getInstance(FileServer.class);
        fileServer.startServer()
                .bindUntilJavaShutdown(Duration.ofMillis(1), disposableServer -> {
                });;
    }
}
