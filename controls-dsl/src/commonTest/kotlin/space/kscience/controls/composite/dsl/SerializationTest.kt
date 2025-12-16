package space.kscience.controls.composite.dsl

import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import ru.nsk.kstatemachine.event.Event
import space.kscience.controls.composite.old.*
import space.kscience.controls.composite.old.contracts.toBlueprintId
import space.kscience.controls.composite.old.features.*
import space.kscience.controls.composite.old.messages.*
import space.kscience.controls.composite.old.plans.*
import space.kscience.controls.composite.old.serialization.controlsJson
import space.kscience.controls.core.Address
import space.kscience.controls.core.faults.ValidationFault
import space.kscience.dataforge.meta.Meta
import space.kscience.dataforge.meta.get
import space.kscience.dataforge.meta.string
import space.kscience.dataforge.names.asName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * A test suite to verify the polymorphic serialization of all core models in `controls-composite-old`.
 * This ensures that the communication and persistence layers can reliably handle all defined types,
 * which is critical for the framework's distributed and persistent nature.
 */
//all passed
class SerializationTest {

    @Serializable
    private object TestEvent : Event

    /**
     * Verifies that all subtypes of [DeviceMessage] are correctly serialized and deserialized.
     * This is critical for the message bus and any communication layer.
     */
    @Test
    fun testDeviceMessageSerialization() {
        val messages = listOf(
            PropertyChangedMessage(Clock.System.now(), "testProp", Meta(123), Address("hub", "device")),
            DescriptionMessage(Clock.System.now(), Meta.EMPTY, emptyList(), emptyList(), Address("hub", "device"), requestId = "rq-1"),
            LifecycleStateChangedMessage(Clock.System.now(), "Stopped", "Running", Address("hub", "device")),
            DeviceErrorMessage(Clock.System.now(), SerializableDeviceFailure("TestError", "A test error occurred"), Address("hub", "device"), requestId = null),
            PredicateChangedMessage(Clock.System.now(), "isReady", true, Address("hub", "device")),
            BinaryReadyNotification(Clock.System.now(), "content-123", Meta.EMPTY, Address("hub", "device")),
            BinaryDataRequest(Clock.System.now(), "content-123", Address("hub", "requester"), Address("hub", "provider"), "rq-2"),
            DeviceAttachedMessage(Clock.System.now(), "child".asName(), "com.example.child".toBlueprintId(), Address("hub", "parent")),
            DeviceDetachedMessage(Clock.System.now(), "child".asName(), Address("hub", "parent"))
        )

        messages.forEach { original ->
            val json = controlsJson.encodeToString(DeviceMessage.serializer(), original)
            val restored = controlsJson.decodeFromString(DeviceMessage.serializer(), json)
            assertEquals(original, restored, "Failed to serialize/deserialize ${original::class.simpleName}")
        }
    }

    /**
     * Verifies that all subtypes of [ActionSpec] are correctly serialized, including nested plans.
     * This is crucial for the [TransactionPlan] functionality.
     */
    @Test
    fun testActionSpecSerialization() {
        val testAddress = Address("hub", "device")
        val specs = listOf(
            AttachActionSpec(testAddress, "com.example.device".toBlueprintId()),
            DetachActionSpec(testAddress),
            StartActionSpec(testAddress),
            StopActionSpec(testAddress),
            WritePropertyActionSpec(testAddress, "prop".asName(), Meta(123)),
            SequenceActionSpec(listOf(DelayActionSpec(1.seconds))),
            ParallelActionSpec(listOf(DelayActionSpec(1.seconds))),
            DelayActionSpec(2.seconds),
            AwaitPredicateActionSpec(testAddress, "isReady".asName(), 5.seconds),
            AwaitSignalActionSpec("user.confirm.action", "Please confirm"),
            InvokeActionSpec(testAddress, "myAction".asName(), Meta{"arg" put 1}),
            ConditionalActionSpec(
                PredicateSpec(testAddress, "isReady".asName()),
                thenPlan = TransactionPlan(SequenceActionSpec(emptyList())),
                elsePlan = TransactionPlan(SequenceActionSpec(emptyList()))
            ),
            LoopActionSpec("myList".asName(), "item", TransactionPlan(SequenceActionSpec(emptyList()))),
            RunWorkspaceTaskSpec("myTask", Meta.EMPTY, "result".asName())
        )

        specs.forEach { original ->
            val json = controlsJson.encodeToString(ActionSpec.serializer(), original)
            val restored = controlsJson.decodeFromString(ActionSpec.serializer(), json)
            assertEquals(original, restored, "Failed to serialize/deserialize ${original::class.simpleName}")
        }
    }

    /**
     * Verifies that all subtypes of [PropertyBinding] are correctly serialized.
     */
    @Test
    fun testPropertyBindingSerialization() {
        val bindings = listOf(
            ConstPropertyBinding("target".asName(), Meta(123)),
            ParentPropertyBinding("target".asName(), "source".asName()),
            TransformedPropertyBinding("target".asName(), "source".asName(), ToStringTransformerDescriptor)
        )

        bindings.forEach { original ->
            val json = controlsJson.encodeToString(PropertyBinding.serializer(), original)
            val restored = controlsJson.decodeFromString(PropertyBinding.serializer(), json)
            assertEquals(original, restored, "Failed to serialize/deserialize ${original::class.simpleName}")
        }
    }

    /**
     * Verifies that all standard [Feature] subtypes are correctly serialized.
     * This test is critical because Feature is an open polymorphic interface.
     */
    @Test
    fun testFeatureSerialization() {
        val features = listOf(
            LifecycleFeature(),
            ReconfigurableFeature(),
            StatefulFeature(),
            DataSourceFeature("kotlin.String"),
            TaskExecutorFeature(listOf("myTask")),
            OperationalFsmFeature(setOf("IDLE"), setOf("start")),
            BinaryDataFeature(listOf("image/jpeg")),
            PlanExecutorFeature(),
            IntrospectionFeature(true),
            RemoteMirrorFeature(emptyList()),
            OperationalGuardsFeature(emptyList())
        )

        val serializer = PolymorphicSerializer(Feature::class)

        features.forEach { original ->
            val json = controlsJson.encodeToString(serializer, original)
            val restored = controlsJson.decodeFromString(serializer, json)
            assertEquals(original, restored, "Failed to serialize/deserialize ${original::class.simpleName}")
        }
    }

    /**
     * Verifies that all [GuardSpec] subtypes are correctly serialized.
     */
    @Test
    fun testGuardSpecSerialization() {
        val guards = listOf(
            TimedPredicateGuardSpec("isReady".asName(), 5.seconds, "MyEvent", Meta.EMPTY, setOf("Running")),
            ValueChangeGuardSpec("temperature".asName(), 10, "isStable", Meta{"delta" put 0.1}, "StableEvent")
        )

        guards.forEach { original ->
            val json = controlsJson.encodeToString(GuardSpec.serializer(), original)
            val restored = controlsJson.decodeFromString(GuardSpec.serializer(), json)
            assertEquals(original, restored, "Failed to serialize/deserialize ${original::class.simpleName}")
        }
    }

    /**
     * Verifies that [SerializableDeviceFailure] correctly serializes a nested [DeviceFault].
     */
    @Test
    fun testDeviceFaultSerialization() {
        val fault = ValidationFault(Meta{"field" put "name"})
        val failure = SerializableDeviceFailure("MyError", "Validation failed", fault = fault)

        val json = controlsJson.encodeToString(failure)
        val restored = controlsJson.decodeFromString<SerializableDeviceFailure>(json)

        assertEquals(failure, restored)

        val restoredFault = restored.fault
        assertIs<ValidationFault>(restoredFault)
        assertEquals("name", restoredFault.details["field"].string)
    }

}