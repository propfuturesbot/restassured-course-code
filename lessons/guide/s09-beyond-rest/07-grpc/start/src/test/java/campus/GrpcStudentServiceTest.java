// gRPC tests with grpc-java against Campus Lab's StudentService: unary, server streaming, client streaming,
// status codes and deadlines.  Run:  mvn test -Dtest=GrpcStudentServiceTest   (CAMPUS_GRPC_TARGET, CAMPUS_API_KEY)
package campus;

import campuslab.v1.Campus.EnrollSummary;
import campuslab.v1.Campus.GetStudentRequest;
import campuslab.v1.Campus.ListStudentsRequest;
import campuslab.v1.Campus.NewStudent;
import campuslab.v1.Campus.Programme;
import campuslab.v1.Campus.Student;
import campuslab.v1.StudentServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

// --- keep
// One channel (the connection) for the class; stubs are cheap views on it.
public class GrpcStudentServiceTest {

    ManagedChannel channel;
    StudentServiceGrpc.StudentServiceBlockingStub blocking;
    StudentServiceGrpc.StudentServiceStub async;

    // ---
    // A plaintext channel to Campus Lab's gRPC port. The stubs are GENERATED at build time from campus.proto
    // by the protobuf Maven plugin: a blocking stub (waits for answers) and an async stub (callbacks).
    // getState(true) starts connecting now, so the first test does not pay for the connection.


    // ---
    // Unary: one request message, one response message, typed fields instead of JSON paths.


    // ---
    // Errors are status codes, not HTTP codes: the stub throws StatusRuntimeException.
    // A missing id is NOT_FOUND, a zero id is INVALID_ARGUMENT, each with a description.


    // ---
    // Server streaming: one request, the students arrive one message at a time.
    // The blocking stub gives an Iterator; every next() waits for the next message.


    // ---
    // Deadlines: the server sends one student every 0.4 s, ten of them take 4 s.
    // With a 1.5 second deadline the call is cut: we get a few messages, then DEADLINE_EXCEEDED.


    // ---
    // Client streaming: WE stream several students, the server answers once with a summary.
    // The key goes in gRPC metadata (the headers of gRPC), added by an interceptor.
    // One student has year 9, so the summary must reject exactly that one.


    // --- keep
    // Without the metadata the call fails with UNAUTHENTICATED.
    @Test
    public void enrollWithoutAKeyIsUnauthenticated() {
        CompletableFuture<EnrollSummary> summary = new CompletableFuture<>();
        StreamObserver<NewStudent> upload = async.enrollStudents(observer(summary));
        upload.onNext(student("Ada", "Lovelace", 2));
        upload.onCompleted();
        Exception e = expectThrows(Exception.class, () -> summary.get(5, TimeUnit.SECONDS));
        assertEquals(Status.fromThrowable(e.getCause()).getCode(), Status.Code.UNAUTHENTICATED);
    }

    static NewStudent student(String first, String last, int year) {
        return NewStudent.newBuilder().setFirstName(first).setLastName(last).setProgramme(Programme.MATHEMATICS).setYear(year).build();
    }

    // The async stub reports the answer to a StreamObserver; we hand it on to a CompletableFuture.
    static StreamObserver<EnrollSummary> observer(CompletableFuture<EnrollSummary> future) {
        return new StreamObserver<>() {
            public void onNext(EnrollSummary value) { future.complete(value); }
            public void onError(Throwable t) { future.completeExceptionally(t); }
            public void onCompleted() { }
        };
    }
}
