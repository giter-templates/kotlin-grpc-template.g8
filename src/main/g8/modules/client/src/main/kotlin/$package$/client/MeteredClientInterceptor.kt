package $package$.client

import io.grpc.*
import io.prometheus.client.Counter

class MeteredClientInterceptor : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        return MeteredClientCall(next.newCall(method, callOptions))
    }

    companion object {
        val sent: Counter =
            Counter.build("sent_requests", "sent requests").labelNames("source").register()

        private class MeteredClientCall<ReqT, RespT>(val delegate: ClientCall<ReqT, RespT>) :
            ForwardingClientCall<ReqT, RespT>() {
            override fun delegate(): ClientCall<ReqT, RespT> = delegate

            override fun sendMessage(message: ReqT) {
                sent.labels("client").inc()
                super.sendMessage(message)
            }
        }
    }
}
