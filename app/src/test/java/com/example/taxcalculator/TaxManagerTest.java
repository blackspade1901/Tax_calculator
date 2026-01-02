package com.example.taxcalculator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.taxcalculator.utils.TaxManager;
import java.util.HashMap;
import java.util.Map;

/**
 * Batch 1: TaxManagerTest
 * Covers singleton behavior, default rates, cloud updates, and fallback logic.
 * Updated to reset singleton state between tests to prevent test pollution.
 */
public class TaxManagerTest {

    private TaxManager taxManager;

    @Before
    public void setUp() {
        taxManager = TaxManager.getInstance();
        resetTaxRates();
    }

    @After
    public void tearDown() {
        resetTaxRates();
    }

    private void resetTaxRates() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(TaxManager.CAT_EXEMPT, 0.0);
        defaults.put(TaxManager.CAT_ESSENTIAL, 5.0);
        defaults.put(TaxManager.CAT_STANDARD, 18.0);
        defaults.put(TaxManager.CAT_LUXURY, 40.0);
        taxManager.updateRates(defaults);
    }

    // --- Singleton & Basic Checks (5 Tests) ---

    @Test
    public void testSingletonInstanceNotNull() {
        assertNotNull("TaxManager instance should not be null", taxManager);
    }

    @Test
    public void testSingletonIsSameInstance() {
        TaxManager instance2 = TaxManager.getInstance();
        assertSame("TaxManager should return the same instance", taxManager, instance2);
    }

    @Test
    public void testConstantsAreCorrect() {
        assertEquals("exempt", TaxManager.CAT_EXEMPT);
        assertEquals("essential", TaxManager.CAT_ESSENTIAL);
        assertEquals("standard", TaxManager.CAT_STANDARD);
        assertEquals("luxury", TaxManager.CAT_LUXURY);
    }

    // --- Default Rate Checks (5 Tests) ---

    @Test
    public void testGetRateExempt() {
        assertEquals("Exempt should be 0.0", 0.0, taxManager.getRate(TaxManager.CAT_EXEMPT), 0.001);
    }

    @Test
    public void testGetRateEssential() {
        assertEquals("Essential should be 5.0", 5.0, taxManager.getRate(TaxManager.CAT_ESSENTIAL), 0.001);
    }

    @Test
    public void testGetRateStandard() {
        assertEquals("Standard should be 18.0", 18.0, taxManager.getRate(TaxManager.CAT_STANDARD), 0.001);
    }

    @Test
    public void testGetRateLuxury() {
        assertEquals("Luxury should be 40.0", 40.0, taxManager.getRate(TaxManager.CAT_LUXURY), 0.001);
    }

    @Test
    public void testGetRateUnknownDefaultsToFive() {
        assertEquals("Unknown category should default to 5.0", 5.0, taxManager.getRate("unknown_cat"), 0.001);
    }

    // --- Fallback & Edge Cases (10 Tests) ---

    @Test
    public void testGetRateNullInput() {
        assertEquals("Null input should default to 5.0", 5.0, taxManager.getRate(null), 0.001);
    }

    @Test
    public void testGetRateEmptyString() {
        assertEquals("Empty string should default to 5.0", 5.0, taxManager.getRate(""), 0.001);
    }

    @Test
    public void testGetRateSpaceString() {
        assertEquals("Space string should default to 5.0", 5.0, taxManager.getRate(" "), 0.001);
    }

    @Test
    public void testGetRateCaseSensitiveMismatch() {
        // "EXEMPT" != "exempt"
        assertEquals("Case mismatch should default to 5.0", 5.0, taxManager.getRate("EXEMPT"), 0.001);
    }

    @Test
    public void testGetRateSpecialChars() {
        assertEquals("Special chars should default to 5.0", 5.0, taxManager.getRate("@#$"), 0.001);
    }

    @Test
    public void testGetRateNumericString() {
        assertEquals("Numeric string ID should default to 5.0", 5.0, taxManager.getRate("123"), 0.001);
    }

    @Test
    public void testGetRateVeryLongString() {
        String longId = new String(new char[1000]).replace('\0', 'a');
        assertEquals("Long string should default to 5.0", 5.0, taxManager.getRate(longId), 0.001);
    }

    @Test
    public void testGetRatePartiallyCorrect() {
        assertEquals("Partially correct string should default to 5.0", 5.0, taxManager.getRate("exempt_category"), 0.001);
    }

    @Test
    public void testGetRateWithNewlines() {
        assertEquals("String with newline should default to 5.0", 5.0, taxManager.getRate("exempt\n"), 0.001);
    }

    @Test
    public void testGetRateWithTabs() {
        assertEquals("String with tab should default to 5.0", 5.0, taxManager.getRate("\texempt"), 0.001);
    }

    // --- Cloud Update Simulation (5 Tests) ---

    @Test
    public void testUpdateRatesWithValidMap() {
        Map<String, Object> cloudMap = new HashMap<>();
        cloudMap.put(TaxManager.CAT_STANDARD, 20.0); // Changed from 18 to 20
        taxManager.updateRates(cloudMap);
        assertEquals("Standard rate should be updated to 20.0", 20.0, taxManager.getRate(TaxManager.CAT_STANDARD), 0.001);
    }

    @Test
    public void testUpdateRatesWithNewCategory() {
        Map<String, Object> cloudMap = new HashMap<>();
        cloudMap.put("super_luxury", 50.0);
        taxManager.updateRates(cloudMap);
        assertEquals("New category should be 50.0", 50.0, taxManager.getRate("super_luxury"), 0.001);
    }

    @Test
    public void testUpdateRatesWithNullMap() {
        // Should not crash
        taxManager.updateRates(null);
        // Ensure standard still exists
        assertTrue(taxManager.getRate(TaxManager.CAT_STANDARD) > 0);
    }

    @Test
    public void testUpdateRatesWithLongValues() {
        Map<String, Object> cloudMap = new HashMap<>();
        // Firestore sometimes sends Longs, simulating that here:
        cloudMap.put(TaxManager.CAT_EXEMPT, 1L); 
        taxManager.updateRates(cloudMap);
        assertEquals("Exempt rate should be updated via Long input", 1.0, taxManager.getRate(TaxManager.CAT_EXEMPT), 0.001);
    }

    @Test
    public void testUpdateRatesDoesNotClearOthers() {
        Map<String, Object> cloudMap = new HashMap<>();
        cloudMap.put("temp", 99.0);
        taxManager.updateRates(cloudMap);
        // Check temp
        assertEquals(99.0, taxManager.getRate("temp"), 0.001);
        // Check existing still there (Essential)
        assertEquals(5.0, taxManager.getRate(TaxManager.CAT_ESSENTIAL), 0.001);
    }
}