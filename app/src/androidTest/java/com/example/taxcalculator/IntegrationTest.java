package com.example.taxcalculator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.taxcalculator.database.AppDatabase;
import com.example.taxcalculator.database.ProductDao;
import com.example.taxcalculator.models.ProductItem;
import com.example.taxcalculator.utils.TaxManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

/**
 * Integration Test: Database + Logic + Models
 * Verifies that the Room Database correctly stores and retrieves ProductItems,
 * and that the retrieved items maintain their logic integrity (tax calculations).
 */
@RunWith(AndroidJUnit4.class)
public class IntegrationTest {

    private ProductDao productDao;
    private AppDatabase db;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        // Use an in-memory database because the information stored here disappears when the
        // process is killed.
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        productDao = db.productDao();
    }

    @After
    public void closeDb() throws IOException {
        db.close();
    }

    @Test
    public void writeUserAndReadInList() throws Exception {
        // 1. Create a ProductItem with specific details
        // "Essential" category implies 5% tax.
        // Price 105.0
        ProductItem item = new ProductItem("Test Soap", "Test Brand", 105.0, TaxManager.CAT_ESSENTIAL, "123456");
        
        // 2. Insert into DB
        productDao.insert(item);

        // 3. Retrieve from DB
        List<ProductItem> allItems = productDao.getAll();
        
        // 4. Verify list size
        assertEquals(1, allItems.size());
        
        // 5. Verify Data Integrity
        ProductItem retrievedItem = allItems.get(0);
        assertEquals("Test Soap", retrievedItem.getName());
        assertEquals("Test Brand", retrievedItem.getBrand());
        assertEquals(105.0, retrievedItem.getPrice(), 0.01);
        
        // 6. Verify Logic Integration (Tax Calculation works on retrieved object)
        // Tax = (105 * 5) / 105 = 5.0
        assertEquals(5.0, retrievedItem.getTaxAmount(), 0.01);
        assertEquals(100.0, retrievedItem.getNetPrice(), 0.01);
    }

    @Test
    public void testFindByBarcodeIntegration() {
        ProductItem item1 = new ProductItem("A", "B", 100, TaxManager.CAT_EXEMPT, "111");
        ProductItem item2 = new ProductItem("C", "D", 200, TaxManager.CAT_STANDARD, "222");

        productDao.insert(item1);
        productDao.insert(item2);

        ProductItem found = productDao.findByBarcode("222");
        assertNotNull(found);
        assertEquals("C", found.getName());
        assertEquals(18.0, found.getTaxRate(), 0.01); // Standard is 18%
    }
}