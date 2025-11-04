package space.kscience.magix.rsocket

import space.kscience.magix.api.MagixEndpointFactory

/**
 * A factory for creating an RSocket-based [MagixEndpoint] using raw TCP for transport.
 * This factory is platform-specific and will throw an exception on platforms that do not support Ktor's TCP client (like JS/WasmJs).
 */
public expect val RSocketWithTcpEndpointFactory: MagixEndpointFactory