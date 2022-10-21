package com.android.gradle.replicator.codegen

import org.junit.Test

class GeneratorDriverKtTest {

    @OptIn(NeedsOptIn::class)
    @Test
    fun testOptInAnnotations() {
        assert(!TestOptIt::class.isNotOptInType())
    }

    @Test
    fun testNoOptInAnnotations() {
        assert(TestNoOptIt::class.isNotOptInType())
    }
}

@RequiresOptIn
annotation class NeedsOptIn

@NeedsOptIn
class TestOptIt

class TestNoOptIt