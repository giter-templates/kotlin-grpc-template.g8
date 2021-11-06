package $package$.server

import com.google.protobuf.Empty
import $package$.proto.PingRequest
import $package$.proto.PingServiceGrpcKt

class PingServiceImpl : PingServiceGrpcKt.PingServiceCoroutineImplBase() {
    override suspend fun ping(request: PingRequest): Empty {
        println("Ping request")
        return Empty.getDefaultInstance()
    }
}
