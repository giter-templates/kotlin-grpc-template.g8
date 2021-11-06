package $package$.client

import $package$.common.Metrics
import $package$.common.Tracing
import $package$..proto.PingServiceGrpcKt
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.*

fun main(args: Array<String>) {
    val tracer = Tracing.tracer("grpc-client")
    val httpExporter = Metrics.httpExporter(8082)

    val channel = ManagedChannelBuilder.forAddress("server", 8080).usePlaintext().build()
    val stub: GrpcWrapper<PingServiceGrpcKt.PingServiceCoroutineStub> =
        GrpcWrapper(PingServiceGrpcKt.PingServiceCoroutineStub(channel))
    val client = PingClient(stub, tracer)

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            System.err.println("*** shutting down exposed http metrics server since JVM is shutting down")
            try {
                httpExporter.close()
            } catch (e: InterruptedException) {
                e.printStackTrace(System.err)
            }
            System.err.println("*** server shut down")
        }
    })

    while (true) {
        runBlocking {
            client.ping()
            delay(1500)
        }
    }
}
