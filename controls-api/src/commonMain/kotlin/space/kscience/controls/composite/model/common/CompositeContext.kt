package space.kscience.controls.composite.model.common

/**
 * A marker interface providing a shared, type-safe context for composite device delegates.
 *
 * This ensures that delegates like `child` can only be used within a valid scope, either during the
 * build phase (`CompositeSpecBuilder`) or at runtime (`CompositeDeviceContext`), preventing
 * runtime errors from incorrect usage.
 */
public interface CompositeContext
