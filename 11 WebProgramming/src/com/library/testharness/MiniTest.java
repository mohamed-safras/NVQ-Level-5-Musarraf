package com.library.testharness;

import java.util.function.Supplier;

/**
 * A deliberately small, dependency-free test harness used for the
 * Software Testing unit evidence. It provides the same core ideas as
 * JUnit (test cases, assertions, pass/fail reporting) without requiring
 * a downloaded library, so the whole project stays runnable offline
 * with just `javac`/`java`.
 */
public final class MiniTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void run(String testName, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("[PASS] " + testName);
        } catch (AssertionError e) {
            failed++;
            System.out.println("[FAIL] " + testName + " -> " + e.getMessage());
        } catch (Exception e) {
            failed++;
            System.out.println("[FAIL] " + testName + " -> unexpected exception: " + e);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " (expected=" + expected + ", actual=" + actual + ")");
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void assertThrows(Class<? extends Throwable> expectedType, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable t) {
            if (expectedType.isInstance(t)) return;
            throw new AssertionError(message + " (wrong exception type: " + t.getClass().getSimpleName() + ")");
        }
        throw new AssertionError(message + " (no exception thrown)");
    }

    public static void printSummary() {
        System.out.println("\n================ TEST SUMMARY ================");
        System.out.println("Passed: " + passed + "   Failed: " + failed + "   Total: " + (passed + failed));
        System.out.println("===============================================");
    }

    public static int getFailed() { return failed; }
}
