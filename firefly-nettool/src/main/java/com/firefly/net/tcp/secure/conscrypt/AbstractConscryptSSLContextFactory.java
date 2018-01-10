package com.firefly.net.tcp.secure.conscrypt;

import com.firefly.net.ApplicationProtocolSelector;
import com.firefly.net.SSLContextFactory;
import com.firefly.utils.lang.Pair;
import com.firefly.utils.time.Millisecond100Clock;
import org.conscrypt.Conscrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

/**
 * @author Pengtao Qiu
 */
abstract public class AbstractConscryptSSLContextFactory implements SSLContextFactory {
    protected static final Logger log = LoggerFactory.getLogger("firefly-system");

    private static String provideName;
    private List<String> supportedProtocols;

    static {
        Provider provider = Conscrypt.newProvider();
        provideName = provider.getName();
        Security.addProvider(provider);
        log.info("add Conscrypt security provider");
    }

    public static String getProvideName() {
        return provideName;
    }

    public SSLContext getSSLContextWithManager(KeyManager[] km, TrustManager[] tm, SecureRandom random)
            throws NoSuchAlgorithmException, KeyManagementException, NoSuchProviderException {
        long start = Millisecond100Clock.currentTimeMillis();
        final SSLContext sslContext = SSLContext.getInstance("TLSv1.2", provideName);
        sslContext.init(km, tm, random);
        long end = Millisecond100Clock.currentTimeMillis();
        log.info("creating Conscrypt SSL context spends {} ms", (end - start));
        return sslContext;
    }

    public SSLContext getSSLContext(InputStream in, String keystorePassword, String keyPassword)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {
        return getSSLContext(in, keystorePassword, keyPassword, null, null, null);
    }

    public SSLContext getSSLContext(InputStream in, String keystorePassword, String keyPassword,
                                    String keyManagerFactoryType, String trustManagerFactoryType, String sslProtocol)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException,
            UnrecoverableKeyException, KeyManagementException, NoSuchProviderException {
        long start = Millisecond100Clock.currentTimeMillis();
        final SSLContext sslContext;

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(in, keystorePassword != null ? keystorePassword.toCharArray() : null);

        // PKIX,SunX509
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(keyManagerFactoryType == null ? "SunX509" : keyManagerFactoryType);
        kmf.init(ks, keyPassword != null ? keyPassword.toCharArray() : null);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(trustManagerFactoryType == null ? "SunX509" : trustManagerFactoryType);
        tmf.init(ks);

        // TLSv1 TLSv1.2
        sslContext = SSLContext.getInstance(sslProtocol == null ? "TLSv1.2" : sslProtocol, provideName);
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        long end = Millisecond100Clock.currentTimeMillis();
        log.info("creating Conscrypt SSL context spends {} ms", (end - start));
        return sslContext;
    }

    abstract public SSLContext getSSLContext();

    @Override
    public Pair<SSLEngine, ApplicationProtocolSelector> createSSLEngine(boolean clientMode) {
        SSLEngine sslEngine = getSSLContext().createSSLEngine();
        sslEngine.setUseClientMode(clientMode);
        return new Pair<>(sslEngine, new ConscryptALPNSelector(sslEngine, supportedProtocols));
    }

    @Override
    public Pair<SSLEngine, ApplicationProtocolSelector> createSSLEngine(boolean clientMode, String peerHost, int peerPort) {
        SSLEngine sslEngine = getSSLContext().createSSLEngine(peerHost, peerPort);
        sslEngine.setUseClientMode(clientMode);
        return new Pair<>(sslEngine, new ConscryptALPNSelector(sslEngine, supportedProtocols));
    }

    @Override
    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @Override
    public void setSupportedProtocols(List<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }
}
