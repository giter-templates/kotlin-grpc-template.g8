package $package$.client

import $package$.common.MetadataAdapter
import io.grpc.*
import io.jaegertracing.internal.JaegerTracer
import io.opentracing.Span
import io.opentracing.propagation.Format

class TracedClientInterceptor(private val tracer: JaegerTracer) : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val span = tracer.activeSpan() ?: tracer.buildSpan("ping_request").start()
        return TracedClientCall(next.newCall(method, callOptions), tracer, span)
    }

    companion object {
        private class TracedClientCall<ReqT, RespT>(
            val delegate: ClientCall<ReqT, RespT>,
            val tracer: JaegerTracer,
            val span: Span
        ) : ForwardingClientCall<ReqT, RespT>() {
            override fun delegate(): ClientCall<ReqT, RespT> = delegate

            override fun start(responseListener: Listener<RespT>, headers: Metadata) {
                span.log("start")
                val adapter = MetadataAdapter(headers)
                val format = Format.Builtin.HTTP_HEADERS
                tracer.inject(span.context(), format, adapter)
                super.start(TracedClientCallListener(responseListener, span), headers)
            }

            override fun request(numMessages: Int) {
                span.log("request: \$numMessages")
                super.request(numMessages)
            }

            override fun cancel(message: String?, cause: Throwable?) {
                span.log("cancel: \${cause?.localizedMessage}")
                super.cancel(message, cause)
            }

            override fun halfClose() {
                span.log("halfClose")
                super.halfClose()
            }

            override fun sendMessage(message: ReqT) {
                span.log("sendMessage")
                super.sendMessage(message)
            }

            override fun isReady(): Boolean {
                span.log("isReady")
                return super.isReady()
            }

            override fun setMessageCompression(enabled: Boolean) {
                span.log("setMessageCompression")
                super.setMessageCompression(enabled)
            }
        }

        private class TracedClientCallListener<RespT>(val delegate: ClientCall.Listener<RespT>, val span: Span) :
            ForwardingClientCallListener<RespT>() {
            override fun delegate(): ClientCall.Listener<RespT> = delegate

            override fun onHeaders(headers: Metadata?) {
                span.log("onHeaders")
                super.onHeaders(headers)
            }

            override fun onMessage(message: RespT) {
                span.log("onMessage")
                super.onMessage(message)
            }

            override fun onClose(status: Status, trailers: Metadata) {
                span.log("onClose")
                span.setTag("status", status.code.name)
                span.finish()
                super.onClose(status, trailers)
            }

            override fun onReady() {
                span.log("onReady")
                super.onReady()
            }
        }
    }
}
