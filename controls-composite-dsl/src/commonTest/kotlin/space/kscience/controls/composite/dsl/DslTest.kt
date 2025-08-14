package space.kscience.controls.composite.dsl

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope.coroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import space.kscience.controls.composite.dsl.actions.metaAction
import space.kscience.controls.composite.dsl.actions.plan
import space.kscience.controls.composite.dsl.actions.taskAction
import space.kscience.controls.composite.dsl.properties.*
import space.kscience.controls.composite.model.*
import space.kscience.controls.composite.model.contracts.*
import space.kscience.controls.composite.model.contracts.runtime.CompositeDeviceContext
import space.kscience.controls.composite.model.contracts.runtime.ConstructorElement
import space.kscience.controls.composite.model.lifecycle.DeviceLifecycleState
import space.kscience.controls.composite.model.messages.DeviceMessage
import space.kscience.controls.composite.model.meta.DevicePropertySpec
import space.kscience.controls.composite.model.state.MutableDeviceState
import space.kscience.controls.composite.model.state.StatefulDevice
import space.kscience.controls.composite.model.state.StatefulDeviceLogic
import space.kscience.dataforge.context.Context
import space.kscience.dataforge.context.Global
import space.kscience.dataforge.meta.*
import space.kscience.dataforge.names.Name
import space.kscience.dataforge.names.asName
import kotlin.coroutines.CoroutineContext
import kotlin.test.*

// --- Mocks and Test Fixtures ---

private interface TestDevice : Device
private interface TestPlanExecutorDevice : TestDevice, PlanExecutorDevice
private interface TestTaskExecutorDevice : TestDevice, TaskExecutorDevice
private interface TestStatefulDevice : TestDevice, StatefulDevice, CompositeDeviceContext
private interface KitchenSinkDevice : TestStatefulDevice, TestPlanExecutorDevice, TestTaskExecutorDevice

@OptIn(InternalControlsApi::class)
private class TestDeviceImpl(
    override val context: Context,
    override val name: Name,
    override val meta: Meta,
    override val coroutineContext: CoroutineContext,
) : KitchenSinkDevice {
    override val lifecycleState = MutableStateFlow(DeviceLifecycleState.Stopped)
    override val operationalState = null
    override val propertyDescriptors = emptyList<space.kscience.controls.composite.model.meta.PropertyDescriptor>()
    override val actionDescriptors = emptyList<space.kscience.controls.composite.model.meta.ActionDescriptor>()
    override val messageFlow by lazy { MutableSharedFlow<DeviceMessage>() }
    override val clock = kotlin.time.Clock.System
    override fun getChildDevice(name: Name): Device = error("Not for test")

    override suspend fun readProperty(propertyName: Name, context: ExecutionContext): Meta = Meta.EMPTY
    override suspend fun writeProperty(propertyName: Name, value: Meta, context: ExecutionContext) {}
    override suspend fun execute(actionName: Name, argument: Meta?, context: ExecutionContext): Meta? = null

    override val statefulLogic = StatefulDeviceLogic()
    override val constructorElements = emptySet<ConstructorElement>()
    override fun registerElement(constructorElement: ConstructorElement) {}
    override fun unregisterElement(constructorElement: ConstructorElement) {}
    override suspend fun snapshot() = error("Not for test")
    override suspend fun restore(snapshot: space.kscience.controls.composite.model.state.StateSnapshot) = error("Not for test")
    override fun <T> getState(spec: DevicePropertySpec<*, T>) = error("Not for test")
    override fun <T> getMutableState(spec: space.kscience.controls.composite.model.meta.MutableDevicePropertySpec<*, T>): MutableDeviceState<T> =
        error("Not for test")

    override suspend fun executePlan(plan: space.kscience.controls.composite.model.plans.TransactionPlan, context: ExecutionContext): Meta? = null
    override suspend fun executeTask(taskName: Name, input: Meta?, context: ExecutionContext): Meta? = null
}

private enum class TestEnum { ONE, TWO }

private class ChildDeviceSpec : DeviceSpecification<TestDevice>() {
    override val id = "test.child"
    val childProperty by mutableDoubleProperty(read = { 0.0 }, write = { _ -> })
    val readOnlyChildProperty by doubleProperty(read = { 1.0 })

    @OptIn(DelicateCoroutinesApi::class)
    override fun CompositeSpecBuilder<TestDevice>.configure() {
        driver { ctx, meta -> TestDeviceImpl(ctx, "child".asName(), meta, coroutineContext) }
    }
}

class DslTest {

    @Test
    fun `should create a valid blueprint with bindings`() = runTest {
        val childSpec = ChildDeviceSpec()
        val parentSpec = object : DeviceSpecification<TestDevice>() {
            override val id = "test.parent"
            val parentProperty by mutableDoubleProperty(read = { 0.0 }, write = { _ -> })
            private val childBlueprint = compositeDevice(childSpec, Global)

            override fun CompositeSpecBuilder<TestDevice>.configure() {
                driver { ctx, meta -> TestDeviceImpl(ctx, "parent".asName(), meta, coroutineContext) }

                child("constChild".asName(), childBlueprint) {
                    bindings {
                        bind(childSpec.childProperty, 123.0)
                    }
                    meta { "meta.override" put true }
                }

                child("boundChild".asName(), childBlueprint) {
                    bindings {
                        childSpec.childProperty bindsTo parentProperty
                    }
                }
            }
        }

        val blueprint = compositeDevice(parentSpec, Global)
        assertEquals("test.parent", blueprint.id.value)
        assertTrue(blueprint.properties.containsKey("parentProperty".asName()))

        val constChildConfig = blueprint.children["constChild".asName()] as? LocalChildComponentConfig
        assertNotNull(constChildConfig)
        assertEquals("test.child", constChildConfig.blueprintId.value)
        assertEquals(true, constChildConfig.meta["meta.override"].boolean)
        assertEquals(1, constChildConfig.bindings.bindings.size)
        val constBinding = constChildConfig.bindings.bindings.first() as? ConstPropertyBinding
        assertNotNull(constBinding)
        assertEquals(123.0, constBinding.value.double)

        val boundChildConfig = blueprint.children["boundChild".asName()] as? LocalChildComponentConfig
        assertNotNull(boundChildConfig)
        assertEquals(1, boundChildConfig.bindings.bindings.size)
        assertIs<ParentPropertyBinding>(boundChildConfig.bindings.bindings.first())
    }

    @Test
    fun `should fail validation on name collision`() = runTest {
        val specWithCollision = object : DeviceSpecification<TestDevice>() {
            val theName by doubleProperty { 0.0 }
            override fun CompositeSpecBuilder<TestDevice>.configure() {
                driver { _, meta -> TestDeviceImpl(Global, "collision".asName(), meta, coroutineContext) }
                child("theName".asName(), compositeDevice(ChildDeviceSpec(), Global))
            }
        }
        assertFailsWith<IllegalArgumentException> {
            compositeDevice(specWithCollision, Global)
        }
    }

    @Test
    fun `should fail validation on remote child with non-existent peer`() = runTest {
        val spec = object : DeviceSpecification<TestDevice>() {
            override fun CompositeSpecBuilder<TestDevice>.configure() {
                driver { ctx, meta -> TestDeviceImpl(ctx, "remoteInvalid".asName(), meta, coroutineContext) }
                val fakePeer = SimplePeerBlueprint<PeerConnection>("fake.peer", StaticAddressSource(emptyList()), FailoverStrategy.ORDERED) { _, _ -> error("Should not be called") }
                remoteChild(
                    name = "remoteChild".asName(),
                    blueprint = compositeDevice(ChildDeviceSpec(), Global),
                    address = Address("remoteHub", "remoteDevice".asName()),
                    via = fakePeer
                )
            }
        }
        assertFailsWith<IllegalStateException>("Peer connection blueprint 'fake.peer' is not registered in this spec. It must be declared as a property before being used.") {
            compositeDevice(spec, Global)
        }
    }

    @Test
    fun `should build a kitchen sink blueprint without state conflicts`() = runTest {
        val sinkSpec = object : DeviceSpecification<KitchenSinkDevice>() {
            override val id: String = "test.kitchenSink"

            val persistentState by stateProperty(MetaConverter.int, 1)

            val readOnlyProp by stringProperty { "readOnly" }
            val enumProp by enumProperty<_, TestEnum> { TestEnum.ONE }
            val mutableProp by mutableIntProperty(read = { 2 }, write = {})
            val lateBound by lateBoundProperty(MetaConverter.double, 0.0)

            val planAction by plan { write(mutableProp, Address("hub", "device".asName()), 123) }
            val taskAction by taskAction<String, Int, _>("com.example.myTask")
            val metaAction by metaAction { it }
            private val childBlueprint = compositeDevice(ChildDeviceSpec(), Global)

            override fun CompositeSpecBuilder<KitchenSinkDevice>.configure() {
                driver { ctx, meta -> TestDeviceImpl(ctx, "kitchenSink".asName(), meta, coroutineContext) }

                val peer by peer({ _, _ -> error("not called") }, Address("hub", "peer".asName()))

                child("child1".asName(), childBlueprint)
                children(childBlueprint, listOf("child2".asName(), "child3".asName()))
                remoteChild("remoteChild".asName(), childBlueprint, Address("remoteHub", "remoteDevice".asName()), via = peer)

                operationalFsm(setOf("IDLE", "RUNNING")) { _, _ -> }
                logic { }
            }
        }
        val blueprint = compositeDevice(sinkSpec, Global)

        assertEquals(5, blueprint.properties.size)
        assertEquals(blueprint.properties["persistentState".asName()]?.descriptor?.persistent, true)
        assertEquals(3, blueprint.actions.size)
    }
}