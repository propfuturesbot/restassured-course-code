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
    @BeforeClass
    public void connect() {
        channel = ManagedChannelBuilder.forTarget(System.getenv().getOrDefault("CAMPUS_GRPC_TARGET", "localhost:50051"))
                .usePlaintext().build();
        channel.getState(true);
        blocking = StudentServiceGrpc.newBlockingStub(channel);
        async = StudentServiceGrpc.newStub(channel);
    }

    @AfterClass(alwaysRun = true)
    public void close() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    // ---
    // Unary: one request message, one response message, typed fields instead of JSON paths.
    @Test
    public void getOneStudent() {
        Student s = blocking.getStudent(GetStudentRequest.newBuilder().setId(1).build());
        System.out.println("unary: " + s.getFirstName() + " " + s.getLastName() + ", " + s.getProgramme());
        assertEquals(s.getEmail(), "asha.verma@students.campuslab.edu");
        assertEquals(s.getProgramme(), Programme.PHYSICS);
    }

    // ---
    // Errors are status codes, not HTTP codes: the stub throws StatusRuntimeException.
    // A missing id is NOT_FOUND, a zero id is INVALID_ARGUMENT, each with a description.
    @Test
    public void errorsAreGrpcStatusCodes() {
        StatusRuntimeException missing = expectThrows(StatusRuntimeException.class,
                () -> blocking.getStudent(GetStudentRequest.newBuilder().setId(99).build()));
        assertEquals(missing.getStatus().getCode(), Status.Code.NOT_FOUND);
        System.out.println("status: " + missing.getStatus());
        StatusRuntimeException zero = expectThrows(StatusRuntimeException.class,
                () -> blocking.getStudent(GetStudentRequest.newBuilder().setId(0).build()));
        assertEquals(zero.getStatus().getCode(), Status.Code.INVALID_ARGUMENT);
    }

    // ---
    // Server streaming: one request, the students arrive one message at a time.
    // The blocking stub gives an Iterator; every next() waits for the next message.
    @Test
    public void listStudentsStreamsOneMessageEach() {
        Iterator<Student> stream = blocking.listStudents(ListStudentsRequest.newBuilder()
                .setProgramme(Programme.COMPUTER_SCIENCE).setLimit(3).build());
        List<Integer> ids = new ArrayList<>();
        stream.forEachRemaining(s -> ids.add(s.getId()));
        System.out.println("streamed ids: " + ids);
        assertEquals(ids, List.of(5, 10, 15));
    }

    // ---
    // Deadlines: the server sends one student every 0.4 s, ten of them take 4 s.
    // With a 1.5 second deadline the call is cut: we get a few messages, then DEADLINE_EXCEEDED.
    @Test
    public void aDeadlineCutsALongStream() {
        Iterator<Student> stream = blocking.withDeadlineAfter(1500, TimeUnit.MILLISECONDS)
                .listStudents(ListStudentsRequest.newBuilder().setLimit(10).build());
        List<Integer> got = new ArrayList<>();
        StatusRuntimeException e = expectThrows(StatusRuntimeException.class, () -> stream.forEachRemaining(s -> got.add(s.getId())));
        System.out.println("before the deadline: " + got + " then " + e.getStatus().getCode());
        assertEquals(e.getStatus().getCode(), Status.Code.DEADLINE_EXCEEDED);
        assertTrue(got.size() < 10);
    }

    // ---
    // Client streaming: WE stream several students, the server answers once with a summary.
    // The key goes in gRPC metadata (the headers of gRPC), added by an interceptor.
    // One student has year 9, so the summary must reject exactly that one.
    @Test
    public void enrollStudentsStreamsUpAndGetsOneSummary() throws Exception {
        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("x-api-key", Metadata.ASCII_STRING_MARSHALLER), System.getenv("CAMPUS_API_KEY"));
        CompletableFuture<EnrollSummary> summary = new CompletableFuture<>();
        StreamObserver<NewStudent> upload = async.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers))
                .enrollStudents(observer(summary));
        upload.onNext(student("Ada", "Lovelace", 2));
        upload.onNext(student("Alan", "Turing", 9));
        upload.onNext(student("Grace", "Hopper", 3));
        upload.onCompleted();

        EnrollSummary result = summary.get(5, TimeUnit.SECONDS);
        System.out.println("summary: " + result.getReceived() + " received, " + result.getEnrolled() + " enrolled, rejected " + result.getRejectedList());
        assertEquals(result.getEnrolled(), 2);
        assertEquals(result.getRejectedList(), List.of("message 2: year must be 1 to 6, got 9"));
    }

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
