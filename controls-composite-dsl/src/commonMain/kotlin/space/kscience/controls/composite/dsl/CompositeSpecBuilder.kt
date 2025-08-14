package space.kscience.controls.composite.dsl

import ru.nsk.kstatemachine.statemachine.BuildingStateMachine
import ru.nsk.kstatemachine.statemachine.StateMachine
import space.kscience.controls.composite.dsl.children.ChildConfigBuilder
import space.kscience.controls.composite.model.*
import space.kscience.controls.composite.model.contracts.*
import space.kscience.controls.composite.model.contracts.runtime.DeviceFlows
import space.kscience.controls.composite.model.discovery.BlueprintRegistry
import space.kscience.controls.composite.model.features.Feature
import space.kscience.controls.composite.model.features.LifecycleFeature
import space.kscience.controls.composite.model.features.OperationalFsmFeature
import space.kscience.controls.composite.model.lifecycle.LifecycleContext
import space.kscience.controls.composite.model.meta.DeviceActionSpec
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.ContextAware
import space.kscience.dataforge.context.logger
import space.kscience.dataforge.context.warn
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.MutableMeta
import space.kscience.dataforge.names.Name
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * A DSL marker for building composite device specifications.
 */
@DslMarker
public annotation class CompositeSpecDsl


/**
 * A builder for composing [DeviceBlueprint] instances using a DSL.
 * This class is the primary entry point for defining a device's structure and behavior.
 * Properties and actions are typically declared using delegated properties, which automatically register themselves.
 * This builder is used to configure other aspects like child components, lifecycle, and the device driver.
 *
 * @param D The type of the device contract being specified.
 */
@CompositeSpecDsl
public class CompositeSpecBuilder<D : Device>(
    override val context: Context,
) : ContextAware {

    internal val _properties = mutableMapOf<Name, DevicePropertySpec<D, *>>()
    internal val _actions = mutableMapOf<Name, DeviceActionSpec<D, *, *>>()
    internal val _children = mutableMapOf<Name, ChildComponentConfig>()
    internal val _peerConnections = mutableMapOf<Name, PeerBlueprint<out PeerConnection>>()
    private var _lifecycle: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit = { _, _ -> }
    private var _operationalFsm: (suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit)? = null
    private var _operationalFsmStates: Set<String> = emptySet()
    private var deviceDriver: DeviceDriver<D>? = null
    internal val _features = mutableMapOf<String, Feature>()

    /**
     * A block of code defining the internal reactive logic of the device.
     * This logic will be executed by the runtime when the device is started.
     */
    internal var logic: (suspend D.(flows: DeviceFlows) -> Unit)? = null


    /**
     * Additional metadata for the blueprint itself. This meta is layered at the bottom
     * of the final device's configuration meta.
     */
    public var meta: Meta = Meta.EMPTY

    /**
     * A DSL block to configure the blueprint's metadata.
     */
    public fun meta(block: MutableMeta.() -> Unit) {
        this.meta = Meta(block)
    }

    /**
     * Defines the driver for the device. The factory lambda receives the parent's [Context] and [Meta].
     * The driver is responsible for creating the device instance and handling its physical interactions.
     * A driver **must** be defined for a blueprint to be valid.
     */
    public fun driver(factory: (context: Context, meta: Meta) -> D) {
        this.deviceDriver = DeviceDriver { context, meta -> factory(context, meta) }
    }

    private fun checkNameCollision(name: Name) {
        require(!_properties.containsKey(name)) { "Property with name '$name' is already registered." }
        require(!_actions.containsKey(name)) { "Action with name '$name' is already registered." }
        require(!_children.containsKey(name)) { "Child with name '$name' is already registered." }
        require(!_peerConnections.containsKey(name)) { "Peer connection with name '$name' is already registered." }
    }

    /**
     * Registers a pre-constructed [DevicePropertySpec]. Called automatically by property delegates.
     * @throws IllegalArgumentException if a component with the same name already exists.
     */
    public fun registerProperty(spec: DevicePropertySpec<D, *>) {
        checkNameCollision(spec.name)
        _properties[spec.name] = spec
    }


    /**
     * Registers a pre-constructed [DeviceActionSpec]. Called automatically by action delegates.
     * @throws IllegalArgumentException if a component with the same name already exists.
     */
    public fun registerAction(spec: DeviceActionSpec<D, *, *>) {
        checkNameCollision(spec.name)
        _actions[spec.name] = spec
    }

    /**
     * Registers a pre-configured child component.
     * @throws IllegalArgumentException if a component with the same name already exists.
     */
    public fun registerChild(name: Name, config: ChildComponentConfig) {
        checkNameCollision(name)
        _children[name] = config
    }

    /**
     * Registers a pre-configured peer connection blueprint.
     * @throws IllegalArgumentException if a component with the same name already exists.
     */
    public fun registerPeerConnection(name: Name, blueprint: PeerBlueprint<out PeerConnection>) {
        checkNameCollision(name)
        _peerConnections[name] = blueprint
    }

    /**
     * Declares a peer connection required by this device, using a static list of addresses.
     *
     * @param P The type of the [PeerConnection].
     * @param driver The driver to create the connection instance.
     * @param addresses A vararg list of static [Address] objects.
     * @param failoverStrategy The strategy for handling multiple addresses.
     * @return A [PropertyDelegateProvider] for a [PeerBlueprint].
     */
    public fun <P : PeerConnection> peer(
        driver: PeerDriver<P>,
        vararg addresses: Address,
        failoverStrategy: FailoverStrategy = FailoverStrategy.ORDERED,
    ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, PeerBlueprint<P>>> =
        PropertyDelegateProvider { _, property ->
            val name = Name.parse(property.name)
            val blueprint = SimplePeerBlueprint(
                id = name.toString(),
                addressSource = StaticAddressSource(addresses.toList()),
                failoverStrategy = failoverStrategy,
                driver = driver
            )
            registerPeerConnection(name, blueprint)
            ReadOnlyProperty { _, _ -> blueprint }
        }

    /**
     * Declares a peer connection that uses a discovery service to find its endpoint.
     *
     * @param P The type of the [PeerConnection].
     * @param driver The driver to create the connection instance.
     * @param serviceId The ID of the service to discover.
     * @param failoverStrategy The strategy for handling multiple discovered addresses.
     * @return A [PropertyDelegateProvider] for a [PeerBlueprint].
     */
    public fun <P : PeerConnection> peer(
        driver: PeerDriver<P>,
        serviceId: String,
        failoverStrategy: FailoverStrategy = FailoverStrategy.ROUND_ROBIN,
    ): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, PeerBlueprint<P>>> =
        PropertyDelegateProvider { _, property ->
            val name = Name.parse(property.name)
            val blueprint = SimplePeerBlueprint(
                id = name.toString(),
                addressSource = DiscoveredAddressSource(serviceId),
                failoverStrategy = failoverStrategy,
                driver = driver
            )
            registerPeerConnection(name, blueprint)
            ReadOnlyProperty { _, _ -> blueprint }
        }


    /**
     * Adds a [Feature] to the blueprint, declaring a specific capability.
     */
    public fun feature(feature: Feature) {
        _features[feature.capability] = feature
    }

    /**
     * Configures the device's lifecycle using KStateMachine's DSL.
     * @see space.kscience.controls.composite.dsl.lifecycle.standardLifecycle for a pre-configured lifecycle FSM.
     */
    public fun lifecycle(block: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit) {
        this._lifecycle = block
    }

    /**
     * Configures the device's operational logic using KStateMachine's DSL. This FSM is separate
     * from the lifecycle FSM and is responsible for managing the device's business logic states
     * (e.g., `Idle`, `Moving`, `AcquiringData`).
     *
     * The `states` parameter must provide a non-empty set of all possible state names, which is used
     * to automatically generate the [OperationalFsmFeature].
     *
     * @param states A set of all state names in this FSM. Used for static feature description.
     * @param block The suspendable lambda to configure the state machine.
     */
    public fun operationalFsm(
        states: Set<String>,
        block: suspend BuildingStateMachine.(device: D, context: LifecycleContext<D>) -> Unit
    ) {
        require(states.isNotEmpty()) { "Operational FSM must have at least one state defined." }
        this._operationalFsm = block
        this._operationalFsmStates = states
    }


    /**
     * Defines the internal reactive logic of the device. This block is executed by the runtime
     * when the device starts. It provides access to the device instance (`this`) and a [DeviceFlows]
     * context to get reactive flows for properties.
     *
     * Example:
     * ```
     * logic { flows ->
     *     val positionFlow = flows.flow(position)
     *     val speedFlow = flows.flow(speed)
     *
     *     combine(positionFlow, speedFlow) { pos, spd ->
     *         //...
     *     }.launchIn(this)
     * }
     * ```
     */
    public fun logic(block: suspend D.(flows: DeviceFlows) -> Unit) {
        this.logic = block
    }

    /**
     * A type-safe and recommended way to declare a remote child.
     * It accepts a [PeerBlueprint] instance (obtained from a `peer` delegate), which eliminates the risk of
     * errors from incorrect or non-existent peer connection names.
     *
     * Note: Property bindings are not supported for remote children as they imply a local, reactive data flow.
     *
     * @param name The local name for the remote child proxy.
     * @param blueprint The blueprint of the remote device. Used for type safety and static analysis.
     * @param address The network-wide [Address] of the remote device.
     * @param via The [PeerBlueprint] instance to use for communication.
     * @param configBuilder A DSL block to configure the child's proxy (lifecycle, meta overrides).
     */
    public fun <C : Device> remoteChild(
        name: Name,
        blueprint: DeviceBlueprint<C>,
        address: Address,
        via: PeerBlueprint<out PeerConnection>,
        configBuilder: ChildConfigBuilder<D, C>.() -> Unit = {}
    ) {
        val peerName = this._peerConnections.entries.find { it.value == via }?.key
            ?: error("Peer connection blueprint '${via.id}' is not registered in this spec. It must be declared as a property before being used.")

        val builder = ChildConfigBuilder<D, C>().apply(configBuilder)

        if (builder.buildBindings().bindings.isNotEmpty()) {
            context.logger.warn { "Property bindings for remote child '$name' are not supported and will be ignored." }
        }

        val config = RemoteChildComponentConfig(
            address = address,
            peerName = peerName,
            blueprintId = blueprint.id,
            blueprintVersion = blueprint.version,
            config = builder.lifecycle,
            meta = builder.meta
        )
        registerChild(name, config)
    }


    /**
     * Declares a single child component for the device.
     */
    public fun <C : Device> child(
        name: Name,
        blueprint: DeviceBlueprint<C>,
        configBuilder: ChildConfigBuilder<D, C>.() -> Unit = {},
    ) {
        contract {
            callsInPlace(configBuilder, InvocationKind.EXACTLY_ONCE)
        }
        val builder = ChildConfigBuilder<D, C>().apply(configBuilder)

        val config = LocalChildComponentConfig(
            blueprintId = blueprint.id,
            blueprintVersion = blueprint.version,
            config = builder.lifecycle,
            meta = builder.meta,
            bindings = builder.buildBindings()
        )
        registerChild(name, config)
    }

    /**
     * Declares a collection of child components of the same type.
     *
     * @param C The type of the child device.
     * @param blueprint The blueprint for all children in this collection.
     * @param names A collection of local names for the children.
     * @param configBuilder A DSL block to configure each child. It receives the child's name.
     */
    public fun <C : Device> children(
        blueprint: DeviceBlueprint<C>,
        names: Collection<Name>,
        configBuilder: ChildConfigBuilder<D, C>.(name: Name) -> Unit = {},
    ) {
        names.forEach { childName ->
            val builder = ChildConfigBuilder<D, C>().apply { configBuilder(childName) }
            val config = LocalChildComponentConfig(
                blueprintId = blueprint.id,
                blueprintVersion = blueprint.version,
                config = builder.lifecycle,
                meta = builder.meta,
                bindings = builder.buildBindings()
            )
            registerChild(childName, config)
        }
    }


    /**
     * Builds the final [DeviceBlueprint].
     * This method does NOT perform validation. Validation should be done by the caller
     * using [CompositeSpecValidator.validate] and a [BlueprintRegistry].
     */
    internal fun build(id: String): DeviceBlueprint<D> {
        val driver = this.deviceDriver ?: error("Device driver must be defined for the blueprint '$id'.")

        // Auto-add LifecycleFeature if not present
        if (!_features.containsKey(Device.CAPABILITY)) {
            feature(LifecycleFeature())
        }

        // Auto-add OperationalFsmFeature if an FSM is defined
        if (this._operationalFsm != null && !_features.containsKey("ru.nsk.kstatemachine.statemachine.StateMachine")) {
            val eventNames = _actions.values.flatMap {
                listOfNotNull(
                    it.operationalEventTypeName,
                    it.operationalSuccessEventTypeName,
                    it.operationalFailureEventTypeName
                )
            }.toSet()
            feature(OperationalFsmFeature(states = this._operationalFsmStates, events = eventNames))
        }

        return SimpleDeviceBlueprint(
            id = BlueprintId(id),
            children = _children.toMap(),
            peerConnections = _peerConnections.toMap(),
            properties = _properties.toMap(),
            actions = _actions.toMap(),
            meta = this.meta,
            lifecycle = this._lifecycle,
            operationalFsm = this._operationalFsm,
            logic = this.logic,
            driver = driver,
            features = _features.toMap()
        )
    }
}