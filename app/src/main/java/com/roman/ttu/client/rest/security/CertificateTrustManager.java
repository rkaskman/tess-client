package com.roman.ttu.client.rest.security;

import android.content.Context;
import android.content.res.AssetManager;

import com.roman.ttu.client.Configuration;

import org.bouncycastle.asn1.x509.X509Name;
import org.bouncycastle.jce.PrincipalUtil;
import org.bouncycastle.jce.X509Principal;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public class CertificateTrustManager implements X509TrustManager {
    private Map<String, X509Certificate> trustedCerts = new HashMap<>();

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    private void checkTrusted(X509Certificate[] chain) throws CertificateException {
        if (chain.length != 1) {
            throw new CertificateException("Invalid cert chain length");
        }
        X509Certificate trustedCert = trustedCerts.get(
                getCnFor(chain[0]));
        if (trustedCert == null) {
            throw new CertificateException("Untrusted certificate");
        }
        if (!Arrays.equals(chain[0].getPublicKey().getEncoded(),
                trustedCert.getPublicKey().getEncoded())) {
            throw new CertificateException("Invalid certificate");
        }
        trustedCert.checkValidity();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        checkTrusted(chain);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
            throws CertificateException {
        checkTrusted(chain);
    }

    public CertificateTrustManager(Configuration configuration, Context context) {
        try {
            KeyStore keyStore = readTrustStore(configuration, context);
            List<String> keyStoreAliases = Collections.list(keyStore.aliases());
            for (String alias : keyStoreAliases) {
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                trustedCerts.put(getCnFor(certificate), certificate);
            }
        } catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String getCnFor(X509Certificate certificate) throws CertificateEncodingException {
        X509Principal principal = PrincipalUtil.getSubjectX509Principal(certificate);
        Vector<?> cnValues = principal.getValues(X509Name.CN);
        return (String) cnValues.iterator().next();
    }

    private KeyStore readTrustStore(Configuration configuration, Context context) throws KeyStoreException,
            IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore ks = KeyStore.getInstance("pkcs12");
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open("keystore.p12");
        ks.load(is, configuration.getTrustStorePassword().toCharArray());
        return ks;
    }

}