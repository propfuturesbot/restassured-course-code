// A tiny HTTPS server for tests, with a self-signed certificate made by keytool at start-up.
// Used by TlsSettingsTest. Nothing here is typed on camera.
package campus;

import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

public class LocalHttpsServer implements AutoCloseable {

    public static final String PASSWORD = "changeit";   // a throwaway test keystore, made fresh on every run
    public final Path dir;
    public final File keyStore, trustStore;
    private final HttpsServer server;

    public LocalHttpsServer(int port) throws Exception {
        dir = Files.createTempDirectory("tls");
        keyStore = dir.resolve("server.p12").toFile();
        trustStore = dir.resolve("truststore.p12").toFile();
        File cert = dir.resolve("server.crt").toFile();
        // 1. a key pair + self-signed certificate for localhost, 2. export the certificate,
        // 3. import it into a trust store that holds only that certificate.
        keytool("-genkeypair", "-alias", "server", "-keyalg", "RSA", "-keysize", "2048", "-validity", "2",
                "-dname", "CN=localhost", "-ext", "SAN=dns:localhost,ip:127.0.0.1",
                "-keystore", keyStore.getPath(), "-storetype", "PKCS12", "-storepass", PASSWORD);
        keytool("-exportcert", "-alias", "server", "-keystore", keyStore.getPath(), "-storepass", PASSWORD,
                "-rfc", "-file", cert.getPath());
        keytool("-importcert", "-noprompt", "-alias", "server", "-file", cert.getPath(),
                "-keystore", trustStore.getPath(), "-storetype", "PKCS12", "-storepass", PASSWORD);

        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream in = new FileInputStream(keyStore)) { ks.load(in, PASSWORD.toCharArray()); }
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, PASSWORD.toCharArray());
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), null, null);

        server = HttpsServer.create(new InetSocketAddress("localhost", port), 0);
        server.setHttpsConfigurator(new HttpsConfigurator(ssl));
        server.createContext("/hello", ex -> {
            byte[] body = "{\"message\":\"hello over TLS\"}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
    }

    private static void keytool(String... args) throws Exception {
        String[] cmd = new String[args.length + 1];
        cmd[0] = Path.of(System.getProperty("java.home"), "bin", "keytool").toString();
        System.arraycopy(args, 0, cmd, 1, args.length);
        Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (p.waitFor() != 0) throw new IllegalStateException("keytool failed: " + out);
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
