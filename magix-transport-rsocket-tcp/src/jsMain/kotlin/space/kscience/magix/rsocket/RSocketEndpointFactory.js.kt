package space.kscience.magix.rsocket

import space.kscience.magix.api.MagixEndpointFactory

/**
 * Actual implementation for JS platform. TODO make better, not just error
 */
public actual val RSocketWithTcpEndpointFactory: MagixEndpointFactory = MagixEndpointFactory { _, _ ->
    throw UnsupportedOperationException("RSocket with raw TCP transport is not supported in a browser environment.")
}