package $package$.common

import io.grpc.Metadata
import io.opentracing.propagation.TextMap

class MetadataAdapter(val headers: Metadata): TextMap {
    override fun put(key: String?, value: String?) {
        key?.let {
            k ->
            value?.let { v ->
                headers.put(
                    Metadata.Key.of(k.lowercase(), Metadata.ASCII_STRING_MARSHALLER),
                    v.lowercase()
                )
            }
        }
    }

    override fun iterator(): MutableIterator<MutableMap.MutableEntry<String, String>> {
        val map: MutableMap<String, String> = mutableMapOf()

        val keys = headers.keys()

        keys.forEach { key ->
            if (headers.containsKey(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))) {
                val value: String? = headers.get(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER))
                value?.let {
                    map.put(key, it)
                }
            }
        }

        return map.iterator()
    }
}
