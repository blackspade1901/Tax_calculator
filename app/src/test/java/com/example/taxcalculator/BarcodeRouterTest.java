package com.example.taxcalculator;

import org.junit.Test;
import static org.junit.Assert.*;

import com.example.taxcalculator.utils.BarcodeRouter;

/**
 * Batch 2: BarcodeRouterTest
 * Covers prefix routing logic for Books (978), Indian Retail (890), and Global fallback.
 */
public class BarcodeRouterTest {

    // --- Standard Prefixes (5 Tests) ---

    @Test
    public void testRouteIndianRetailStandard() {
        assertEquals(BarcodeRouter.ProductType.INDIAN_RETAIL, BarcodeRouter.getRoute("8901234567890"));
    }

    @Test
    public void testRouteBookStandard() {
        assertEquals(BarcodeRouter.ProductType.BOOK, BarcodeRouter.getRoute("9781234567890"));
    }

    @Test
    public void testRouteGlobalStandard() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("1234567890123"));
    }

    @Test
    public void testRouteGlobalAnotherPrefix() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("5001234567890"));
    }

    @Test
    public void testRouteGlobalZeroStart() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("0123456789012"));
    }

    // --- Short & Exact Matches (5 Tests) ---

    @Test
    public void testRouteIndianRetailExactPrefix() {
        assertEquals(BarcodeRouter.ProductType.INDIAN_RETAIL, BarcodeRouter.getRoute("890"));
    }

    @Test
    public void testRouteBookExactPrefix() {
        assertEquals(BarcodeRouter.ProductType.BOOK, BarcodeRouter.getRoute("978"));
    }

    @Test
    public void testRouteShortStringStart89() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("89"));
    }

    @Test
    public void testRouteShortStringStart97() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("97"));
    }

    @Test
    public void testRouteSingleChar() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("8"));
    }

    // --- Edge Cases: Null & Empty (5 Tests) ---

    @Test
    public void testRouteNullInput() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute(null));
    }

    @Test
    public void testRouteEmptyString() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute(""));
    }

    @Test
    public void testRouteWhitespaceOnly() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("   "));
    }

    @Test
    public void testRouteWhitespacePrefix() {
        // " 890..." should technically not match unless trimmed. Assuming strict startWith logic.
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute(" 890123"));
    }

    @Test
    public void testRouteWhitespaceSuffix() {
        // "890 " might pass startsWith("890")
        assertEquals(BarcodeRouter.ProductType.INDIAN_RETAIL, BarcodeRouter.getRoute("890 123"));
    }

    // --- Mixed Characters (5 Tests) ---

    @Test
    public void testRouteAlphaPrefix() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("A890"));
    }

    @Test
    public void testRouteAlphaSuffixIndian() {
        assertEquals(BarcodeRouter.ProductType.INDIAN_RETAIL, BarcodeRouter.getRoute("890ABC"));
    }

    @Test
    public void testRouteAlphaSuffixBook() {
        assertEquals(BarcodeRouter.ProductType.BOOK, BarcodeRouter.getRoute("978XYZ"));
    }

    @Test
    public void testRouteSymbols() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("#890"));
    }

    @Test
    public void testRouteMixedAlphaNumeric() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("a978"));
    }

    // --- Boundary Prefixes (5 Tests) ---

    @Test
    public void testRouteBoundary977() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("977"));
    }

    @Test
    public void testRouteBoundary979() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("979"));
    }

    @Test
    public void testRouteBoundary891() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("891"));
    }

    @Test
    public void testRouteBoundary889() {
        assertEquals(BarcodeRouter.ProductType.GLOBAL_GENERAL, BarcodeRouter.getRoute("889"));
    }

    @Test
    public void testRouteLongString() {
        String longCode = "890" + new String(new char[100]).replace('\0', '1');
        assertEquals(BarcodeRouter.ProductType.INDIAN_RETAIL, BarcodeRouter.getRoute(longCode));
    }
}