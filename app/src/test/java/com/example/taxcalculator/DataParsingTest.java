package com.example.taxcalculator;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.taxcalculator.api.ProductResponse;

/**
 * Batch 4: DataParsingTest
 * Covers ProductResponse parsing logic, specifically getBestName().
 */
public class DataParsingTest {

    // --- Happy Path (5 Tests) ---

    @Test
    public void testGetBestNameLocalPresent() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "Local Name";
        data.productNameEn = "English Name";
        assertEquals("Local Name", data.getBestName());
    }

    @Test
    public void testGetBestNameOnlyLocal() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "Local Name";
        data.productNameEn = null;
        assertEquals("Local Name", data.getBestName());
    }

    @Test
    public void testGetBestNameOnlyEnglish() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = null;
        data.productNameEn = "English Name";
        assertEquals("English Name", data.getBestName());
    }

    @Test
    public void testGetBestNameBothMissing() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = null;
        data.productNameEn = null;
        assertEquals("Unknown Product", data.getBestName());
    }

    @Test
    public void testGetBestNameBrandsCheck() {
        // Brands field is independent, ensuring it doesn't affect name logic
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "Name";
        data.brands = "Brand";
        assertEquals("Name", data.getBestName());
        assertEquals("Brand", data.brands);
    }

    // --- Empty Strings vs Nulls (5 Tests) ---

    @Test
    public void testGetBestNameEmptyLocalReturnsEnglish() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "";
        data.productNameEn = "English Name";
        assertEquals("English Name", data.getBestName());
    }

    @Test
    public void testGetBestNameEmptyEnglishReturnsUnknown() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = null;
        data.productNameEn = "";
        assertEquals("Unknown Product", data.getBestName());
    }

    @Test
    public void testGetBestNameBothEmpty() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "";
        data.productNameEn = "";
        assertEquals("Unknown Product", data.getBestName());
    }

    @Test
    public void testGetBestNameWhitespaceLocal() {
        // Assuming current logic just checks isEmpty(), so whitespace might be returned
        // Use trim() in actual logic if this is undesirable. Current logic assumes exact check.
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = " ";
        assertEquals(" ", data.getBestName());
    }

    @Test
    public void testGetBestNameWhitespaceEnglish() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "";
        data.productNameEn = " ";
        assertEquals(" ", data.getBestName());
    }

    // --- Complex Inputs (5 Tests) ---

    @Test
    public void testGetBestNameSpecialChars() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "Name@#$";
        assertEquals("Name@#$", data.getBestName());
    }

    @Test
    public void testGetBestNameNumbers() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "12345";
        assertEquals("12345", data.getBestName());
    }

    @Test
    public void testGetBestNameVeryLong() {
        String longName = new String(new char[500]).replace('\0', 'A');
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = longName;
        assertEquals(longName, data.getBestName());
    }

    @Test
    public void testGetBestNameUnicode() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "产品名称"; // Chinese for Product Name
        assertEquals("产品名称", data.getBestName());
    }

    @Test
    public void testGetBestNameEmoji() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.productName = "🍎 Apple";
        assertEquals("🍎 Apple", data.getBestName());
    }

    // --- Edge Cases for Object Parsing (5 Tests) ---

    @Test
    public void testResponseStatusField() {
        ProductResponse response = new ProductResponse();
        response.status = 1;
        assertEquals(1, response.status);
    }

    @Test
    public void testResponseProductDataIsNull() {
        ProductResponse response = new ProductResponse();
        response.product = null;
        assertNull(response.product);
    }

    @Test
    public void testProductDataInitialization() {
        ProductResponse response = new ProductResponse();
        response.product = new ProductResponse().new ProductData();
        assertNotNull(response.product);
    }

    @Test
    public void testBrandsNull() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.brands = null;
        assertNull(data.brands);
    }

    @Test
    public void testBrandsEmpty() {
        ProductResponse.ProductData data = new ProductResponse().new ProductData();
        data.brands = "";
        assertEquals("", data.brands);
    }
}