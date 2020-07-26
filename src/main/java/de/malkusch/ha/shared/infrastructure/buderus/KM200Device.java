package de.malkusch.ha.shared.infrastructure.buderus;

/*
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class was taken from the OpenHAB 1.x Buderus / KM200 binding, and
 * modified to run without the OpenHAB infrastructure.
 *
 * The KM200Device representing the device with its all capabilities
 *
 * @author Markus Eckhardt
 * @since 1.9.0
 */

final class KM200Device {

    private static final Logger logger = LoggerFactory.getLogger(KM200Device.class);

    /* valid IPv4 address of the KMxxx. */
    protected String ip4Address = null;

    /* The gateway password which is provided on the type sign of the KMxxx. */
    protected String gatewayPassword = null;

    /* The private password which has been defined by the user via EasyControl. */
    protected String privatePassword = null;

    /* The returned device charset for communication */
    protected String charSet = null;

    /* Needed keys for the communication */
    protected byte[] cryptKeyInit = null;
    protected byte[] cryptKeyPriv = null;

    /* Buderus_MD5Salt */
    protected byte[] MD5Salt = null;

    /* Device services */
    HashMap<String, KM200CommObject> serviceMap;
    /* Device services blacklist */
    List<String> blacklistMap;

    /* Is the first INIT done */
    protected Boolean inited = false;

    public KM200Device() {
        serviceMap = new HashMap<>();
        blacklistMap = new ArrayList<>();
        blacklistMap.add("/gateway/firmware");
    }

    /**
     * This function creates the private key from the MD5Salt, the device and the
     * private password
     *
     * @author Markus Eckhardt
     * @since 1.9.0
     */
    @SuppressWarnings("null")
    private void RecreateKeys() {
        if (StringUtils.isNotBlank(gatewayPassword) && StringUtils.isNotBlank(privatePassword) && MD5Salt != null) {
            byte[] MD5_K1;
            byte[] MD5_K2_Init;
            byte[] MD5_K2_Private;
            byte[] bytesOfGatewayPassword;
            byte[] bytesOfPrivatePassword;
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.error("No such algorithm: 'MD5'. Please check Java installation.");
                return;
            }

            /* First half of the key: MD5 of (GatewayPassword . Salt) */
            bytesOfGatewayPassword = gatewayPassword.getBytes(StandardCharsets.UTF_8);
            byte[] CombParts1 = new byte[bytesOfGatewayPassword.length + MD5Salt.length];
            System.arraycopy(bytesOfGatewayPassword, 0, CombParts1, 0, bytesOfGatewayPassword.length);
            System.arraycopy(MD5Salt, 0, CombParts1, bytesOfGatewayPassword.length, MD5Salt.length);
            MD5_K1 = md.digest(CombParts1);

            /* Second half of the key: - Initial: MD5 of ( Salt) */
            MD5_K2_Init = md.digest(MD5Salt);

            /* Second half of the key: - private: MD5 of ( Salt . PrivatePassword) */
            bytesOfPrivatePassword = privatePassword.getBytes(StandardCharsets.UTF_8);
            byte[] CombParts2 = new byte[bytesOfPrivatePassword.length + MD5Salt.length];
            System.arraycopy(MD5Salt, 0, CombParts2, 0, MD5Salt.length);
            System.arraycopy(bytesOfPrivatePassword, 0, CombParts2, MD5Salt.length, bytesOfPrivatePassword.length);
            MD5_K2_Private = md.digest(CombParts2);

            /* Create Keys */
            cryptKeyInit = new byte[MD5_K1.length + MD5_K2_Init.length];
            System.arraycopy(MD5_K1, 0, cryptKeyInit, 0, MD5_K1.length);
            System.arraycopy(MD5_K2_Init, 0, cryptKeyInit, MD5_K1.length, MD5_K2_Init.length);

            cryptKeyPriv = new byte[MD5_K1.length + MD5_K2_Private.length];
            System.arraycopy(MD5_K1, 0, cryptKeyPriv, 0, MD5_K1.length);
            System.arraycopy(MD5_K2_Private, 0, cryptKeyPriv, MD5_K1.length, MD5_K2_Private.length);

        }
    }

    // getter
    public String getIP4Address() {
        return ip4Address;
    }

    public byte[] getCryptKeyPriv() {
        return cryptKeyPriv;
    }

    public String getCharSet() {
        return charSet;
    }

    /**
     * This function outputs a ";" separated list of all on the device available
     * services with its capabilities
     */
    @SuppressWarnings("unchecked")
    public void listAllServices() {
        if (serviceMap != null) {
            logger.info("##################################################################");
            logger.info("List of avalible services");
            logger.info("readable;writeable;recordable;type;service;value;allowed;min;max");
            for (KM200CommObject object : serviceMap.values()) {
                if (object != null) {
                    String val;
                    String type;
                    StringBuilder valPara = new StringBuilder();
                    logger.debug("List type: {} service: {}", object.getServiceType(), object.getFullServiceName());
                    type = object.getServiceType();
                    if (type == null) {
                        type = "";
                    }
                    if (!type.equals("refEnum") && !type.equals("") && !type.equals("switchProgram")
                            && !type.equals("yRecording")) {
                        val = object.getValue().toString();
                        if (object.getValueParameter() != null) {
                            if (type.equals("stringValue")) {
                                // noinspection unchecked
                                List<String> valParas = (List<String>) object.getValueParameter();
                                for (int i = 0; i < valParas.size(); i++) {
                                    if (i > 0) {
                                        valPara.append("|");
                                    }
                                    valPara.append(valParas.get(i));
                                }
                                valPara.append(";;");
                            }
                            if (type.equals("floatValue")) {
                                // noinspection unchecked
                                List<Float> valParas = (List<Float>) object.getValueParameter();
                                valPara.append(";");
                                if (valParas.size() == 2) {
                                    valPara.append(valParas.get(0));
                                    valPara.append(";");
                                    valPara.append(valParas.get(1));
                                } else {
                                    logger.debug("Value parameter for float != 2, this shouldn't happen");
                                    valPara.append(";");
                                }
                            }
                        } else {
                            valPara.append(";;");
                        }
                    } else {
                        val = "";
                        valPara = new StringBuilder(";");
                    }
                    logger.info("{};{};{};{};{};{};{}", object.getReadable().toString(),
                            object.getWriteable().toString(), object.getRecordable().toString(), type,
                            object.getFullServiceName(), val, valPara.toString());
                }
            }
            logger.info("##################################################################");
        }
    }

    // setter
    public void setIP4Address(String ip) {
        ip4Address = ip;
    }

    public void setGatewayPassword(String password) {
        gatewayPassword = password;
        RecreateKeys();
    }

    public void setPrivatePassword(String password) {
        privatePassword = password;
        RecreateKeys();
    }

    public void setMD5Salt(String salt) {
        MD5Salt = DatatypeConverter.parseHexBinary(salt);
        RecreateKeys();
    }

    public void setCryptKeyPriv(String key) {
        cryptKeyPriv = DatatypeConverter.parseHexBinary(key);
    }

    public void setCharSet(String charset) {
        charSet = charset;
    }

    public void setInited(Boolean Init) {
        inited = Init;
    }
}