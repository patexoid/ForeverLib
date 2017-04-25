package com.patex.service;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedWebApplicationContext;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Alexey on 22.04.2017.
 */
@Component
public class ConfigComponent {

    @Autowired
    private EmbeddedWebApplicationContext appContext;

    private String baseUrl;

    public String getBaseUrl() { //TODO temporary solution
        if (baseUrl == null) {
            EmbeddedServletContainer esc = appContext.getEmbeddedServletContainer();
            try {
                if (esc instanceof TomcatEmbeddedServletContainer) {
                    Connector connector = ((TomcatEmbeddedServletContainer) esc).getTomcat().getConnector();
                    String scheme = connector.getScheme();
                    String ip = InetAddress.getLocalHost().getHostAddress();
                    int port = connector.getPort();
                    String contextPath = appContext.getServletContext().getContextPath();
                    baseUrl = scheme + "://" + ip + ":" + port + contextPath;
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return baseUrl;
    }
}
