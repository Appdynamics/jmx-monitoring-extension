/*
 *   Copyright 2019 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.jmx.utils;

import com.appdynamics.extensions.crypto.Decryptor;
import com.appdynamics.extensions.util.PathResolver;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

public class SslUtils {
    private static final Logger logger = LoggerFactory.getLogger(SslUtils.class);

    /**
     *This method executes every time config file is changed.
     * sets SSL params only if [connection] is present in config.yml
     * if [connection] section is present in the config.yml --> means  SSL is required for atleast 1 server
     * if [sslTrustStorePath] is empty in the config.yml ---> then use MA certs at <MA-Home>/conf/cacerts.jks
     * if [sslTrustStorePath] is not empty -->then use custom truststore path specified in the config.yml
     * [sslTrustStorePath] cannot be null
     * in both cases, sslTrustStorePassword has to be specified in config.yml
     */
    public void setSslProperties(Map<String, ?> configMap) {

        if (configMap.containsKey(Constants.CONNECTION)) {
            Map<String, ?> connectionMap = (Map<String, ?>) configMap.get(Constants.CONNECTION);
            setTrustStore(configMap, connectionMap);
            setKeyStore(configMap,connectionMap);
        }

        else if(!configMap.containsKey(Constants.CONNECTION)){
            logger.debug("[connection] section is not present in the config.yml");
        }
    }

    private void setKeyStore(Map<String, ?> configMap, Map<String, ?> connectionMap) {
        if(configMap.containsKey(Constants.KEY_STORE_PATH)){
            Preconditions.checkNotNull(connectionMap.get(Constants.KEY_STORE_PATH),"[sslKeyStorePath] cannot be null");
            if(!(connectionMap.get(Constants.KEY_STORE_PATH).toString()).isEmpty()) {
                String sslKeyStorePath = connectionMap.get(Constants.KEY_STORE_PATH).toString();
                File customSslKeyStoreFile = new File(sslKeyStorePath);
                if (customSslKeyStoreFile == null || !customSslKeyStoreFile.exists()) {
                    logger.debug("The file [{}] doesn't exist", customSslKeyStoreFile.getAbsolutePath());
                } else {

                    logger.debug("Using custom SSL keystore [{}] ", sslKeyStorePath);
                    logger.debug("Setting SystemProperty [javax.net.ssl.keyStore] {} ", customSslKeyStoreFile.getAbsolutePath());
                    System.setProperty("javax.net.ssl.keyStore", customSslKeyStoreFile.getAbsolutePath());
                }
            }

            else if ((connectionMap.get(Constants.KEY_STORE_PATH).toString()).isEmpty()) {
                File installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
                File defaultKeyStoreFile = PathResolver.getFile("/conf/cacerts.jks", installDir);
                if (defaultKeyStoreFile == null || !defaultKeyStoreFile.exists()) {
                    logger.debug("The file [{}] doesn't exist", installDir + "/conf/cacerts.jks");
                } else {
                    logger.debug("Using Machine Agent keystore {}", installDir + "/conf/cacerts.jks");
                    logger.debug("Setting SystemProperty [javax.net.ssl.keyStore] {}",defaultKeyStoreFile.getAbsolutePath());
                    System.setProperty("javax.net.ssl.keyStore", defaultKeyStoreFile.getAbsolutePath());
                }
            }
            System.setProperty("javax.net.ssl.keyStore", getSslKeyStorePassword(connectionMap, configMap));
            System.setProperty("com.sun.management.jmxremote.ssl.need.client.auth", "true");
        }
    }

        private void setTrustStore(Map<String, ?> configMap, Map<String, ?> connectionMap) {
        if (connectionMap.containsKey(Constants.TRUST_STORE_PATH)){
            Preconditions.checkNotNull(connectionMap.get(Constants.TRUST_STORE_PATH), "[sslTrustStorePath] cannot be null");
            if(!(connectionMap.get(Constants.TRUST_STORE_PATH).toString()).isEmpty()) {
                String sslTrustStorePath = connectionMap.get(Constants.TRUST_STORE_PATH).toString();
                File customSslTrustStoreFile = new File(sslTrustStorePath);
                 if (customSslTrustStoreFile == null || !customSslTrustStoreFile.exists()) {
                    logger.debug("The file [{}] doesn't exist", customSslTrustStoreFile.getAbsolutePath());
                 } else {

                    logger.debug("Using custom SSL truststore [{}] ", sslTrustStorePath);
                    logger.debug("Setting SystemProperty [javax.net.ssl.trustStore] {} ", customSslTrustStoreFile.getAbsolutePath());
                    System.setProperty("javax.net.ssl.trustStore", customSslTrustStoreFile.getAbsolutePath());
                }
            }

            else if ((connectionMap.get(Constants.TRUST_STORE_PATH).toString()).isEmpty()) {
                File installDir = PathResolver.resolveDirectory(AManagedMonitor.class);
                File defaultTrustStoreFile = PathResolver.getFile("/conf/cacerts.jks", installDir);
                 if (defaultTrustStoreFile == null || !defaultTrustStoreFile.exists()) {
                    logger.debug("The file [{}] doesn't exist", installDir + "/conf/cacerts.jks");
                } else {
                    logger.debug("Using Machine Agent truststore {}", installDir + "/conf/cacerts.jks");
                    logger.debug("Setting SystemProperty [javax.net.ssl.trustStore] {}",defaultTrustStoreFile.getAbsolutePath());
                    System.setProperty("javax.net.ssl.trustStore", defaultTrustStoreFile.getAbsolutePath());
                }
            }
        System.setProperty("javax.net.ssl.trustStorePassword", getSslTrustStorePassword(connectionMap, configMap));

        }
    }

    private String getSslTrustStorePassword(Map<String, ?> connectionMap, Map<String, ?> config) {
        String password = (String) connectionMap.get(Constants.TRUST_STORE_PASSWORD);
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        } else {
            String encrypted = (String) connectionMap.get(Constants.TRUST_STORE_ENCRYPTED_PASSWORD);
            if (!Strings.isNullOrEmpty(encrypted)) {
                String encryptionKey = (String) config.get(Constants.ENCRYPTION_KEY);
                if (!Strings.isNullOrEmpty(encryptionKey)) {
                    return new Decryptor(encryptionKey).decrypt(encrypted);
                } else {
                    logger.error("Cannot decrypt the password. Encryption key not set");
                    throw new RuntimeException("Cannot decrypt [encryptedPassword], since [encryptionKey] is not set");
                }
            } else {
                logger.warn("No password set, using empty string");
                return "";
            }
        }
    }

    private String getSslKeyStorePassword(Map<String, ?> connectionMap, Map<String, ?> config) {
        String password = (String) connectionMap.get(Constants.KEY_STORE_PASSWORD);
        if (!Strings.isNullOrEmpty(password)) {
            return password;
        } else {
            String encrypted = (String) connectionMap.get(Constants.KEY_STORE_ENCRYPTED_PASSWORD);
            if (!Strings.isNullOrEmpty(encrypted)) {
                String encryptionKey = (String) config.get(Constants.ENCRYPTION_KEY);
                if (!Strings.isNullOrEmpty(encryptionKey)) {
                    return new Decryptor(encryptionKey).decrypt(encrypted);
                } else {
                    logger.error("Cannot decrypt the password. Encryption key not set");
                    throw new RuntimeException("Cannot decrypt [encryptedPassword], since [encryptionKey] is not set");
                }
            } else {
                logger.warn("No password set, using empty string");
                return "";
            }
        }
    }

}
