package space.kscience.controls.composite.model.contracts.hub

import kotlinx.coroutines.flow.Flow
import space.kscience.dataforge.data.DataTree
import space.kscience.dataforge.names.Name

/**
 * Represents a structural change in a [DataTree].
 * This is used by [ObservableDataTree] to notify observers about dynamic changes
 * in the device hierarchy, such as attaching or detaching devices.
 */
public sealed interface StructureChangeEvent {
    public val name: Name

    /**
     * An event indicating that a new item (a node or a leaf) has been attached to the tree.
     * @property name The full hierarchical name of the newly attached item.
     */
    public data class ItemAttached(override val name: Name) : StructureChangeEvent

    /**
     * An event indicating that an item has been detached from the tree.
     * @property name The full hierarchical name of the detached item.
     */
    public data class ItemDetached(override val name: Name) : StructureChangeEvent
}

/**
 * A [DataTree] that can notify observers about changes to its structure,
 * such as the addition or removal of devices. This is crucial for UIs and other services
 * that need to react dynamically to the topology of a device hub.
 *
 * @param T The type of data contained in the tree.
 */
public interface ObservableDataTree<T> : DataTree<T> {
    /**
     * A hot [Flow] that emits [StructureChangeEvent]s when the structure of the tree changes.
     */
    public val structureUpdates: Flow<StructureChangeEvent>
}
