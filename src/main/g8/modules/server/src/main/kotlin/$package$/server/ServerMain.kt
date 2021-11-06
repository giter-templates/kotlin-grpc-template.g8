package $package$.server

import $package$.common.Metrics
import $package$.common.Tracing
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors

fun main(args: Array<String>) {
    val tracer = Tracing.tracer("grpc-server")
    val httpExporter = Metrics.httpExporter(8081)


    val server = ServerBuilder.forPort(8080)
        .addService(
            ServerInterceptors.intercept(
                PingServiceImpl(),
                MeteredServerInterceptor(),
                TracedServerInterceptor(tracer)
            )
        )
        .build()
        .start()

    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            System.err.println("*** shutting down gRPC server since JVM is shutting down")
            try {
                httpExporter.close()
                server.shutdown().awaitTermination()
            } catch (e: InterruptedException) {
                e.printStackTrace(System.err)
            }
            System.err.println("*** server shut down")
        }
    })
    
    server.awaitTermination()
}
