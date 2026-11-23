package com.ultrahacker.app

import org.junit.Assert.assertTrue
import org.junit.Test

class ResearchLabTest {
    @Test
    fun reportsSuspiciousApiWithLineNumber() {
        val report = ResearchLab.analyzeSource(
            "class Sample {\n    fun run() { Runtime.getRuntime().exec(\"id\") }\n}"
        )
        assertTrue(report.contains("Process execution API"))
        assertTrue(report.contains("line(s) 2"))
        assertTrue(report.contains("not executed"))
    }

    @Test
    fun cleanTextDoesNotClaimSourceIsSafe() {
        val report = ResearchLab.analyzeSource("class Plain { fun value() = 1 }")
        assertTrue(report.contains("No configured indicators were matched"))
        assertTrue(report.contains("does not prove the source is safe"))
    }
}
