package $package$.client

import $package$.proto.PingRequest
import $package$.proto.PingServiceGrpcKt
import io.jaegertracing.internal.JaegerTracer

class PingClient(val grpc: GrpcWrapper<PingServiceGrpcKt.PingServiceCoroutineStub>, val tracer: JaegerTracer) {
    suspend fun ping() {
        val request = PingRequest.newBuilder().build()
        return grpc
            .withDeadline()
            .withInterceptor(MeteredClientInterceptor())
            .withInterceptor(TracedClientInterceptor(tracer))
            .call {
                println("Ping")
                it.ping(request)
                println("Pong")
            }
    }
}
