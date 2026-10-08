package dev.sathish.learningdashboard.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculatorTest {

    @Test
    fun `rounds to nearest whole percent`() {
        assertEquals(65, ProgressCalculator.percent(completed = 13, total = 20))
        assertEquals(38, ProgressCalculator.percent(completed = 6, total = 16)) // 37.5
        assertEquals(33, ProgressCalculator.percent(completed = 1, total = 3))
    }

    @Test
    fun `handles edges without crashing`() {
        assertEquals(0, ProgressCalculator.percent(completed = 0, total = 0))
        assertEquals(100, ProgressCalculator.percent(completed = 28, total = 28))
        assertEquals(100, ProgressCalculator.percent(completed = 30, total = 28))
    }
}
