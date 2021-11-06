package $package$.client

import io.grpc.ClientInterceptor
import io.grpc.kotlin.AbstractCoroutineStub
import java.util.concurrent.TimeUnit

class GrpcWrapper<A : AbstractCoroutineStub<A>>(val stub: A) {
    fun withDeadline(): GrpcWrapper<A> = GrpcWrapper(stub.withDeadlineAfter(5, TimeUnit.SECONDS))

    fun withInterceptor(interceptor: ClientInterceptor): GrpcWrapper<A> =
        GrpcWrapper(stub.withInterceptors(interceptor))

    suspend fun <B> call(effect: suspend (A) -> B): B = effect(stub)
}
