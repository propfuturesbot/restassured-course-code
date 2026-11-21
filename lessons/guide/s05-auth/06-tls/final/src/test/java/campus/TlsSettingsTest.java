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
    @Test
    public void aSelfSignedCertificateIsRefusedByDefault() {
        SSLHandshakeException e = expectThrows(SSLHandshakeException.class, () -> given().get(URL));
        System.out.println("default: " + e.getMessage());
    }

    // ---
    // The right fix for a test environment: a trust store that holds the server's certificate.
    // The connection is still fully checked (the chain and the host name); we just trust one more certificate.
    @Test
    public void aTrustStoreWithTheServerCertificateWorks() {
        given().trustStore(server.trustStore, LocalHttpsServer.PASSWORD)
                .when().get(URL)
                .then().statusCode(200).body("message", equalTo("hello over TLS"));
    }

    // ---
    // The quick fix: relaxedHTTPSValidation() trusts ANY certificate and any host name.
    // Fine for a throwaway test server, never for a test that is meant to prove the TLS setup is right.
    @Test
    public void relaxedValidationTrustsAnything() {
        given().relaxedHTTPSValidation()
                .when().get(URL)
                .then().statusCode(200);
    }

    // ---
    // A common question: "Unrecognized SSL message, plaintext connection?" (Java 21 words it
    // "Unsupported or unrecognized SSL message"). It means we spoke HTTPS to a port that speaks plain HTTP.
    // Here: https:// to the Students app, which serves http on 8080.
    @Test
    public void httpsToAPlainHttpPortGivesUnrecognizedSslMessage() {
        String plainHttp = System.getenv().getOrDefault("STUDENTS_BASE_URI", "http://localhost:8080")
                .replace("http://", "https://");
        SSLException e = expectThrows(SSLException.class,
                () -> given().relaxedHTTPSValidation().get(plainHttp + "/student/list"));
        System.out.println("https to an http port: " + e.getMessage());
        org.hamcrest.MatcherAssert.assertThat(e.getMessage(), containsString("Unsupported or unrecognized SSL message"));
    }
}
