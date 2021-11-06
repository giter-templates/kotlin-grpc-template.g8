package $package$.common

import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.HTTPServer
import java.net.InetSocketAddress

object Metrics {
    val registry: CollectorRegistry = CollectorRegistry.defaultRegistry

    fun httpExporter(port: Int): HTTPServer = HTTPServer(InetSocketAddress(port), registry)
}
