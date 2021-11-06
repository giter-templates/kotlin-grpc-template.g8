package $package$.server

import $package$.common.MetadataAdapter
import io.grpc.*
import io.jaegertracing.internal.JaegerSpan
import io.jaegertracing.internal.JaegerTracer
import io.opentracing.propagation.Format

class TracedServerInterceptor(val tracer: JaegerTracer) : ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>
    ): ServerCall.Listener<ReqT> {
        val adapter = MetadataAdapter(headers)
        val parentSpan = tracer.extract(Format.Builtin.HTTP_HEADERS, adapter)

        val span: JaegerSpan = if (parentSpan != null) {
            tracer.buildSpan("ping_response").asChildOf(parentSpan).start()
        } else {
            tracer.buildSpan("ping_response").start()
        }

        val meteredServerCall = TracedServerCall(call, span)
        return TracedServerCallListener(next.startCall(meteredServerCall, headers), span)
    }

    companion object {
        private class TracedServerCallListener<Req>(
            val delegate: ServerCall.Listener<Req>,
            val span: JaegerSpan
        ) : ForwardingServerCallListener<Req>() {
            override fun delegate(): ServerCall.Listener<Req> = delegate

            override fun onMessage(message: Req) {
                span.log("onMessage")
                super.onMessage(message)
            }

            override fun onHalfClose() {
                span.log("onHalfClose")
                super.onHalfClose()
            }

            override fun onCancel() {
                span.log("onCancel")
                super.onCancel()
            }

            override fun onComplete() {
                span.log("onComplete")
                super.onComplete()
            }

            override fun onReady() {
                span.log("onReady")
                super.onReady()
            }
        }

        private class TracedServerCall<Req, Res>(
            delegate: ServerCall<Req, Res>,
            val span: JaegerSpan
        ) : ForwardingServerCall.SimpleForwardingServerCall<Req, Res>(delegate) {
            override fun sendMessage(message: Res) {
                span.log("sendMessage")
                super.sendMessage(message)
            }

            override fun close(status: Status, trailers: Metadata) {
                span.log("close")
                span.setTag("status", status.code.name)
                span.finish()
                super.close(status, trailers)
            }
        }
    }
}
