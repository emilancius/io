package org.cosybox.commons.io.compression

class CompressorFactory {
    companion object {
        fun create(type: CompressionType, parameters: CompressionParameters): Compressor =
            when (type) {
                CompressionType.ZIP -> ZipCompressor(parameters)
            }
    }
}