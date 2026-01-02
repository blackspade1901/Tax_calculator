package com.example.taxcalculator;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.TaxManager;

import java.util.HashMap;
import java.util.Map;

/**
 * Batch 3: ProductCalculationTest
 * Covers precise math verification for tax calculations and data integrity.
 * Formula: Tax = (Price * Rate) / (100 + Rate)
 * Updated to ensure clean TaxManager state.
 */
public class ProductCalculationTest {

    @Before
    public void setUp() {
        // Ensure default rates are set (TaxManager is a Singleton)
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(TaxManager.CAT_EXEMPT, 0.0);
        defaults.put(TaxManager.CAT_ESSENTIAL, 5.0);
        defaults.put(TaxManager.CAT_STANDARD, 18.0);
        defaults.put(TaxManager.CAT_LUXURY, 40.0);
        TaxManager.getInstance().updateRates(defaults);
    }

    // --- Math Verification: 0% Tax (5 Tests) ---

    @Test
    public void testCalcExempt100() {
        ProductItem item = new ProductItem("Milk", "Amul", 100.0, TaxManager.CAT_EXEMPT, "123");
        assertEquals(0.0, item.getTaxAmount(), 0.01);
        assertEquals(100.0, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcExemptZeroPrice() {
        ProductItem item = new ProductItem("Free", "Brand", 0.0, TaxManager.CAT_EXEMPT, "123");
        assertEquals(0.0, item.getTaxAmount(), 0.01);
        assertEquals(0.0, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcExemptDecimal() {
        ProductItem item = new ProductItem("Curd", "Amul", 50.50, TaxManager.CAT_EXEMPT, "123");
        assertEquals(0.0, item.getTaxAmount(), 0.01);
        assertEquals(50.50, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcExemptLarge() {
        ProductItem item = new ProductItem("Bulk", "Brand", 10000.0, TaxManager.CAT_EXEMPT, "123");
        assertEquals(0.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcExemptRateCheck() {
        ProductItem item = new ProductItem("Milk", "Brand", 100.0, TaxManager.CAT_EXEMPT, "123");
        assertEquals(0.0, item.getTaxRate(), 0.01);
    }

    // --- Math Verification: 5% Tax (5 Tests) ---

    @Test
    public void testCalcEssential105() {
        // 105 total, 5% tax included.
        // Tax = (105 * 5) / 105 = 5.0
        // Net = 100
        ProductItem item = new ProductItem("Soap", "Lux", 105.0, TaxManager.CAT_ESSENTIAL, "123");
        assertEquals(5.0, item.getTaxAmount(), 0.01);
        assertEquals(100.0, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcEssential210() {
        // 210 total. Tax = (210 * 5)/105 = 10.0
        ProductItem item = new ProductItem("Oil", "Brand", 210.0, TaxManager.CAT_ESSENTIAL, "123");
        assertEquals(10.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcEssentialRoundNumbers() {
        ProductItem item = new ProductItem("Tea", "Brand", 100.0, TaxManager.CAT_ESSENTIAL, "123");
        // Tax = (100 * 5) / 105 = 4.7619... -> 4.76
        assertEquals(4.76, item.getTaxAmount(), 0.01);
        assertEquals(95.24, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcEssentialSmall() {
        ProductItem item = new ProductItem("Sachet", "Brand", 1.0, TaxManager.CAT_ESSENTIAL, "123");
        // Tax = 5/105 = 0.047.. -> 0.05
        assertEquals(0.05, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcEssentialRateCheck() {
        ProductItem item = new ProductItem("Soap", "Brand", 100.0, TaxManager.CAT_ESSENTIAL, "123");
        assertEquals(5.0, item.getTaxRate(), 0.01);
    }

    // --- Math Verification: 18% Tax (5 Tests) ---

    @Test
    public void testCalcStandard118() {
        // 118 total. Tax = (118 * 18)/118 = 18.0
        ProductItem item = new ProductItem("Phone", "Mi", 118.0, TaxManager.CAT_STANDARD, "123");
        assertEquals(18.0, item.getTaxAmount(), 0.01);
        assertEquals(100.0, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcStandard59() {
        // 59 total. Tax = (59 * 18)/118 = 9.0
        ProductItem item = new ProductItem("Cable", "Brand", 59.0, TaxManager.CAT_STANDARD, "123");
        assertEquals(9.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcStandard100() {
        ProductItem item = new ProductItem("Mouse", "Dell", 100.0, TaxManager.CAT_STANDARD, "123");
        // Tax = 1800 / 118 = 15.254... -> 15.25
        assertEquals(15.25, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcStandardHighValue() {
        ProductItem item = new ProductItem("TV", "Sony", 118000.0, TaxManager.CAT_STANDARD, "123");
        assertEquals(18000.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcStandardRateCheck() {
        ProductItem item = new ProductItem("TV", "Brand", 100.0, TaxManager.CAT_STANDARD, "123");
        assertEquals(18.0, item.getTaxRate(), 0.01);
    }

    // --- Math Verification: 40% Tax (5 Tests) ---

    @Test
    public void testCalcLuxury140() {
        // 140 total. Tax = (140 * 40)/140 = 40.0
        ProductItem item = new ProductItem("Soda", "Coke", 140.0, TaxManager.CAT_LUXURY, "123");
        assertEquals(40.0, item.getTaxAmount(), 0.01);
        assertEquals(100.0, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcLuxury70() {
        // 70 total. Tax = (70 * 40)/140 = 20.0
        ProductItem item = new ProductItem("Soda", "Brand", 70.0, TaxManager.CAT_LUXURY, "123");
        assertEquals(20.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcLuxury100() {
        ProductItem item = new ProductItem("Smoke", "Brand", 100.0, TaxManager.CAT_LUXURY, "123");
        // Tax = 4000/140 = 28.571... -> 28.57
        assertEquals(28.57, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcLuxuryDecimal() {
        ProductItem item = new ProductItem("Soda", "Brand", 14.0, TaxManager.CAT_LUXURY, "123");
        assertEquals(4.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcLuxuryRateCheck() {
        ProductItem item = new ProductItem("Soda", "Brand", 100.0, TaxManager.CAT_LUXURY, "123");
        assertEquals(40.0, item.getTaxRate(), 0.01);
    }

    // --- Complex Numbers & Integrity (10 Tests) ---

    @Test
    public void testCalcNegativePrice() {
        // Math should still work technically, even if logic forbids it elsewhere
        ProductItem item = new ProductItem("Error", "Brand", -118.0, TaxManager.CAT_STANDARD, "123");
        // Tax = (-118 * 18)/118 = -18.0
        assertEquals(-18.0, item.getTaxAmount(), 0.01);
        assertEquals(-100.0, item.getNetPrice(), 0.01);
    }

    @Test
    public void testCalcZeroPriceStandard() {
        ProductItem item = new ProductItem("Gift", "Brand", 0.0, TaxManager.CAT_STANDARD, "123");
        assertEquals(0.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testCalcPreciseFloat() {
        // 99.99 * 5 / 105 = 4.7614 -> 4.76
        ProductItem item = new ProductItem("Item", "Brand", 99.99, TaxManager.CAT_ESSENTIAL, "123");
        assertEquals(4.76, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testConstructorName() {
        ProductItem item = new ProductItem("TestName", "Brand", 100.0, "cat", "123");
        assertEquals("TestName", item.getName());
    }

    @Test
    public void testConstructorBrand() {
        ProductItem item = new ProductItem("Name", "TestBrand", 100.0, "cat", "123");
        assertEquals("TestBrand", item.getBrand());
    }

    @Test
    public void testConstructorBarcode() {
        ProductItem item = new ProductItem("Name", "Brand", 100.0, "cat", "TestBar");
        assertEquals("TestBar", item.getBarcode());
    }

    @Test
    public void testGetTotalPrice() {
        ProductItem item = new ProductItem("Name", "Brand", 500.0, "cat", "123");
        assertEquals(500.0, item.getTotalPrice(), 0.01);
    }

    @Test
    public void testCategoryFallbackInCalculation() {
        // "unknown" category -> TaxManager returns 5%
        ProductItem item = new ProductItem("Unknown", "Brand", 105.0, "unknown_id", "123");
        assertEquals(5.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testNullCategoryCalculation() {
        // null category -> TaxManager returns 5%
        ProductItem item = new ProductItem("NullCat", "Brand", 105.0, null, "123");
        assertEquals(5.0, item.getTaxAmount(), 0.01);
    }

    @Test
    public void testNullNameHandling() {
        ProductItem item = new ProductItem(null, "Brand", 100.0, "cat", "123");
        assertNull(item.getName());
    }
}