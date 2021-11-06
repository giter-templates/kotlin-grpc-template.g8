package $package$.server

import io.grpc.*
import io.prometheus.client.Counter

class MeteredServerInterceptor : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val meteredServerCall = MeteredServerCall(call)
        return MeteredServerCallListener(next.startCall(meteredServerCall, headers))
    }

    companion object {
        val sent: Counter =
            Counter.build("sent_requests", "sent requests").labelNames("source").register()
        val received: Counter = Counter.build("received_responses", "received responses").register()

        private class MeteredServerCallListener<Req>(
            val delegate: ServerCall.Listener<Req>
        ) : ForwardingServerCallListener<Req>() {
            override fun delegate(): ServerCall.Listener<Req> = delegate

            override fun onMessage(message: Req) {
                received.inc()
                super.onMessage(message)
            }
        }

        private class MeteredServerCall<Req, Res>(
            delegate: ServerCall<Req, Res>
        ) : ForwardingServerCall.SimpleForwardingServerCall<Req, Res>(delegate) {
            override fun sendMessage(message: Res) {
                sent.labels("server").inc()
                super.sendMessage(message)
            }
        }
    }
}
