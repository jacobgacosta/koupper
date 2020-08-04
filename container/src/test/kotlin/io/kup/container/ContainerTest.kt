package io.kup.container

import io.kotest.core.spec.style.AnnotationSpec
import io.kup.container.scope.*
import io.kup.container.exceptions.MultipleAbstractImplementationsException
import io.kup.container.extensions.instanceOf
import io.kup.container.extensions.singletonOf
import kotlin.test.*

class ContainerTest : AnnotationSpec() {
    @Test
    fun `should bind a concrete class and return new one instances for multiple creations`() {
        val container = KupContainer()

        container.bind(AbstractClass::class) {
            ConcreteClass()
        }

        val concreteClassOfContainer = container.create().instanceOf<AbstractClass>()

        assertTrue {
            concreteClassOfContainer is ConcreteClass
        }

        val concreteClassOfContainer2 = container.create().instanceOf<AbstractClass>()

        assertNotEquals(concreteClassOfContainer, concreteClassOfContainer2)
    }

    @Test
    fun `should bind a concrete class using the alternative bind method`() {
        val container = KupContainer()

        container.bind(AbstractClass::class, ConcreteClass::class)

        val concreteClassOfContainer = container.create().instanceOf<AbstractClass>()

        assertTrue {
            concreteClassOfContainer is ConcreteClass
        }

        val concreteClassOfContainer2 = container.create().instanceOf<AbstractClass>()

        assertNotEquals(concreteClassOfContainer, concreteClassOfContainer2)
    }

    @Test
    fun `should bind a singleton class and return the same one instance over and over`() {
        val container = KupContainer()

        container.singleton(AbstractClass::class) {
            ConcreteClass()
        }

        val concreteClassOfContainer = container.get().singletonOf<AbstractClass>()

        val concreteClassOfContainer2 = container.get().singletonOf<AbstractClass>()

        assertEquals(concreteClassOfContainer, concreteClassOfContainer2)
    }

    @Test
    fun `should bind a singleton class using the alternative singleton method`() {
        val container = KupContainer()

        container.singleton(AbstractClass::class, ConcreteClass::class)

        val concreteClassOfContainer = container.get().singletonOf<AbstractClass>()

        val concreteClassOfContainer2 = container.get().singletonOf<AbstractClass>()

        assertEquals(concreteClassOfContainer, concreteClassOfContainer2)
    }

    @Test
    fun `should listen for a resolved instance by bind method`() {
        val container = KupContainer()

        container.bind(AbstractClass::class, ConcreteClass::class)

        container.listenFor(AbstractClass::class, {
            assertTrue {
                it is ConcreteClass
            }
        })

        container.create().instanceOf<AbstractClass>()
    }

    @Test
    fun `should listen for a resolved instance by singleton method`() {
        val container = KupContainer()

        container.singleton(AbstractClass::class, ConcreteClass::class)

        container.listenFor(AbstractClass::class, {
            assertTrue {
                it is ConcreteClass
            }
        })

        container.create().singletonOf<AbstractClass>()
    }

    @Test
    fun `should create a new instance from a full namespace`() {
        val container = KupContainer()

        container.bind(AbstractClass::class, ConcreteClass::class)

        val concreteClass = container.create().instanceOf("io.kup.container.AbstractClass") as AbstractClass

        assertTrue {
            concreteClass is ConcreteClass
        }
    }

    @Test
    fun `should create a new instance using its simple class name`() {
        val container = KupContainer()

        container.bind(AbstractClass::class, ConcreteClass::class)

        val concreteClass = container.create().instanceOf("AbstractClass") as AbstractClass

        assertTrue {
            concreteClass is ConcreteClass
        }
    }

    @Test
    fun `should auto bind an abstract class to existing concrete classes in the specified scope`() {
        val container = KupContainer("io.kup.container.scope")

        val concreteClass = container.create().instanceOf<SingleAbstract>()

        assertTrue {
            concreteClass is SingleConcrete
        }
    }

    @Test
    fun `should throw exception if try create a instance of an abstract class with multiple concrete classes`() {
        val exception = assertFailsWith<MultipleAbstractImplementationsException> {
            KupContainer("io.kup.container.scope").create().instanceOf<io.kup.container.scope.AbstractClass>()
        }

        assertTrue {
            exception.cause is MultipleAbstractImplementationsException
            "Type[AbstractClass] has multiple instances" == exception.message
        }
    }

    @Test
    fun `should solve a instance with its dependencies resolved automatically`() {
        val parentConcreteClass = KupContainer("io.kup.container.scope").create().instanceOf<ParentAbstractClass>()

        assertTrue {
            parentConcreteClass is ParentConcreteClass
            (parentConcreteClass as ParentConcreteClass).firstAbstractClass is FirstConcreteClass
            ((parentConcreteClass).firstAbstractClass as FirstConcreteClass).thirdAbstractClass is ThirdConcreteClass
        }
    }

    @Test
    fun `should bind a concrete class resolving their nested dependencies using the container`() {
        val container = KupContainer("io.kup.container.scope")

        val parentConcreteClass = ParentConcreteClass::class

        container.bind(ParentAbstractClass::class, parentConcreteClass)

        val resolvedParentConcreteClass = container.create().instanceOf<ParentAbstractClass>()

        assertTrue {
            resolvedParentConcreteClass is ParentConcreteClass
            (resolvedParentConcreteClass as ParentConcreteClass).firstAbstractClass is FirstConcreteClass
            ((resolvedParentConcreteClass).firstAbstractClass as FirstConcreteClass).thirdAbstractClass is ThirdConcreteClass
        }
    }
}
