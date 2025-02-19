package com.monbat.planning.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
//    private static final String ALGORITHM = "AES";

    @Bean
    public DataSource getDataSource() {

//        Properties properties = new Properties();
//        properties.load(new FileInputStream("src/main/resources/config.properties"));
//
//        Properties secretKeyProperties = new Properties();
//        secretKeyProperties.load(new FileInputStream("src/main/resources/env.properties"));
//
//        String encryptedUsername = properties.getProperty("db.username");
//        String encryptedPassword = properties.getProperty("db.password");
//
//        String secretKey = secretKeyProperties.getProperty("secretKey");
//
//        String decryptedUsername = decrypt(encryptedUsername, secretKey);
//        String decryptedPassword = decrypt(encryptedPassword, secretKey);

        return DataSourceBuilder.create()
                .driverClassName("org.apache.derby.jdbc.EmbeddedDriver")
                .url("jdbc:derby:planning_db;create=true")
                .username("")
                .password("")
//                .driverClassName("org.postgresql.Driver")
//                .url(properties.getProperty("db.url"))
//                .username(decryptedUsername)
//                .password(decryptedPassword)
                .build();
    }

//    private String decrypt(String encryptedData, String secretKey) throws Exception {
//        SecretKey key = new SecretKeySpec(secretKey.getBytes(), ALGORITHM);
//        Cipher cipher = Cipher.getInstance(ALGORITHM);
//        cipher.init(Cipher.DECRYPT_MODE, key);
//        byte[] decodedData = Base64.getDecoder().decode(encryptedData);
//        byte[] decryptedData = cipher.doFinal(decodedData);
//        return new String(decryptedData);
//    }
}
