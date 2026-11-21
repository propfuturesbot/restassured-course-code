// HTTPS with a self-signed certificate: the default failure, a trust store, relaxed validation, and the
// "Unrecognized SSL message" error.  Run:  mvn test -Dtest=TlsSettingsTest   (starts its own server on :8121)
package campus;

import io.restassured.RestAssured;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.expectThrows;

// --- keep
// The test server: HTTPS on localhost:8121, certificate signed by itself (no certificate authority knows it).
public class TlsSettingsTest {

    static final String URL = "https://localhost:8121/hello";
    LocalHttpsServer server;

    @BeforeClass
    public void startServer() throws Exception {
        server = new LocalHttpsServer(8121);
    }

    @AfterClass(alwaysRun = true)
    public void stopServer() {
        server.close();
        RestAssured.reset();
    }

    // ---
    // By default Java trusts only the certificate authorities in its own trust store.
    // Our certificate is self-signed, so the TLS handshake fails before any HTTP is sent.


    // ---
    // The right fix for a test environment: a trust store that holds the server's certificate.
    // The connection is still fully checked (the chain and the host name); we just trust one more certificate.


    // ---
    // The quick fix: relaxedHTTPSValidation() trusts ANY certificate and any host name.
    // Fine for a throwaway test server, never for a test that is meant to prove the TLS setup is right.


    // ---
    // A common question: "Unrecognized SSL message, plaintext connection?" (Java 21 words it
    // "Unsupported or unrecognized SSL message"). It means we spoke HTTPS to a port that speaks plain HTTP.
    // Here: https:// to the Students app, which serves http on 8080.
