package space.kscience.controls.composite.model.specs

import kotlinx.serialization.Serializable
import space.kscience.controls.composite.model.meta.requiredString
import space.kscience.controls.composite.model.serialization.SchemeAsMetaSerializer
import space.kscience.controls.composite.model.specs.policy.RetryPolicy
import space.kscience.dataforge.meta.*

/**
 * A reusable, declarative specification for a single event to be posted to a Finite State Machine (FSM).
 * This [Scheme] provides a structured way to define an FSM event, including its type and an optional payload.
 */
@Serializable(with = FsmEventSpec.Serializer::class)
public class FsmEventSpec : Scheme() {
    /**
     * The fully qualified serial name of the event class to be posted.
     * The runtime uses this name for polymorphic deserialization to instantiate the correct,
     * strongly-typed event object before passing it to the state machine.
     * This field is mandatory.
     */
    public var eventTypeName: String by requiredString()

    /**
     * An optional [Meta] object containing payload data for constructing the event instance.
     * The runtime will pass this meta to the event's constructor or factory if needed.
     * This field was renamed from `meta` to `eventMeta` to avoid conflicts with the base `Scheme.meta` property.
     */
    public var eventMeta: Meta? by convertable(MetaConverter.meta)

    public companion object : SchemeSpec<FsmEventSpec>(::FsmEventSpec)
    public object Serializer : SchemeAsMetaSerializer<FsmEventSpec>(Companion)
}

/**
 * A reusable, declarative specification for grouping all FSM integration points for an action.
 * It describes how an action's lifecycle (dispatch, success, failure) should trigger events in the
 * device's operational state machine. This enables a clean separation of action logic from state
 * transition logic.
 */
@Serializable(with = FsmIntegrationSpec.Serializer::class)
public class FsmIntegrationSpec : Scheme() {
    /**
     * An optional [FsmEventSpec] defining the event to post to the operational FSM *before*
     * the action's primary logic is executed. This is useful for transitioning the device
     * into a "busy", "working", or "acquiring" state.
     */
    public var onDispatch: FsmEventSpec? by schemeOrNull(FsmEventSpec)

    /**
     * An optional [FsmEventSpec] defining the event to post after the action's logic
     * completes successfully. This is typically used to transition the device back to an
     * "idle" or "ready" state.
     */
    public var onSuccess: FsmEventSpec? by schemeOrNull(FsmEventSpec)

    /**
     * An optional [FsmEventSpec] defining the event to post if the action's logic fails
     * (e.g., throws an exception). This allows for transitioning the device into an "error"
     * or "failed" state within its operational logic.
     */
    public var onFailure: FsmEventSpec? by schemeOrNull(FsmEventSpec)

    public companion object : SchemeSpec<FsmIntegrationSpec>(::FsmIntegrationSpec)
    public object Serializer : SchemeAsMetaSerializer<FsmIntegrationSpec>(Companion)
}
