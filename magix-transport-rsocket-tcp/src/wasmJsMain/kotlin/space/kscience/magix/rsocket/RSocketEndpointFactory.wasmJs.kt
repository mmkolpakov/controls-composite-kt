package space.kscience.magix.rsocket

import space.kscience.magix.api.MagixEndpointFactory

/**
 * Actual implementation for WasmJS platform. TCP is not supported in a browser environment.
 */
public actual val RSocketWithTcpEndpointFactory: MagixEndpointFactory = MagixEndpointFactory { _, _ ->
    throw UnsupportedOperationException("RSocket with raw TCP transport is not supported in a browser environment.")
}