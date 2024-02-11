package com.patex.forever;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.jdbc.support.DatabaseStartupValidator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.stream.Stream;

@SpringBootApplication
@EnableJpaRepositories
@EnableSpringDataWebSupport
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableScheduling
@EnableTransactionManagement
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("*");
            }
        };
    }


    @Bean
    public PasswordEncoder getPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DatabaseStartupValidator databaseStartupValidator(DataSource dataSource) {
        DatabaseStartupValidator dsv = new DatabaseStartupValidator();
        dsv.setDataSource(dataSource);
        dsv.setValidationQuery(DatabaseDriver.POSTGRESQL.getValidationQuery());
        return dsv;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnPostProcessor() {
        return bf -> {
            String[] liqbase = bf.getBeanNamesForType(LiquibaseAutoConfiguration.class);
            String[] jpa = bf.getBeanNamesForType(EntityManagerFactory.class);
            Stream.concat(Stream.of(liqbase), Stream.of(jpa))
                    .map(bf::getBeanDefinition)
                    .forEach(bd -> {
                        String[] dependsOn = bd.getDependsOn();
                        if (dependsOn != null && dependsOn.length > 0) {
                            String[] nwDependsOn = new String[dependsOn.length + 1];
                            System.arraycopy(dependsOn,0, nwDependsOn,0, dependsOn.length);
                            nwDependsOn[dependsOn.length]= "databaseStartupValidator";
                            dependsOn=nwDependsOn;
                        } else {
                            dependsOn=new String[]{"databaseStartupValidator"};
                        }
                        bd.setDependsOn(dependsOn);
                    });
        };
    }
}