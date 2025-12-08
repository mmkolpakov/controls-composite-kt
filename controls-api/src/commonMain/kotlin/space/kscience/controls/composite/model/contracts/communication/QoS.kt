package space.kscience.controls.composite.model.contracts.communication

/**
 * Quality of Service levels for peer-to-peer communication, ensuring reliability for critical operations.
 */
public enum class QoS {
    /**
     * "At-most-once": The message is sent, but delivery is not guaranteed. No acknowledgement is expected.
     * Suitable for high-frequency, non-critical data where some loss is acceptable.
     */
    AT_MOST_ONCE,

    /**
     * "At-least-once": Guarantees that the message will be delivered at least once, but duplicates are possible
     * in case of network issues and retries. The runtime must implement acknowledgement and retry mechanisms.
     * This is a good default for most state-changing operations.
     */
    AT_LEAST_ONCE,

    /**
     * "Exactly-once": Guarantees that the message will be delivered exactly once.
     * This is the most reliable but potentially the slowest level, requiring more complex coordination.
     */
    EXACTLY_ONCE
}
