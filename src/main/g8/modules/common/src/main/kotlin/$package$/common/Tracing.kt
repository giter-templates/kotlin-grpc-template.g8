package $package$.common

import io.jaegertracing.Configuration
import io.jaegertracing.internal.JaegerTracer
import io.jaegertracing.internal.propagation.B3TextMapCodec
import io.jaegertracing.internal.reporters.RemoteReporter
import io.jaegertracing.internal.samplers.ConstSampler
import io.jaegertracing.thrift.internal.senders.UdpSender
import io.opentracing.propagation.Format

object Tracing {
    fun tracer(serviceName: String): JaegerTracer {
        val reporter =
            RemoteReporter.Builder().withSender(UdpSender("jaeger", 6831, 0)).withFlushInterval(100).build()

        val b3Codec = B3TextMapCodec.Builder().build()

        return Configuration(serviceName).tracerBuilder
            .withSampler(ConstSampler(true))
            .withReporter(reporter)
            .registerInjector(Format.Builtin.HTTP_HEADERS, b3Codec)
            .registerExtractor(Format.Builtin.HTTP_HEADERS, b3Codec)
            .build()
    }
}
