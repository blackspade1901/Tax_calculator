# TrueRate - Smart GST Calculator & Scanner

TrueRate is a robust Android application designed to simplify GST (Goods and Services Tax) calculations for everyday products. It features a smart, crowdsourced barcode scanner that automatically identifies products, retrieves their details, and calculates the precise tax breakdown based on Indian GST laws.

Built with performance in mind, the app utilizes an **Offline-First** approach and a **Parallel Network** architecture to ensure users get results instantly, regardless of network conditions.

## 🚀 Key Features

### ⚡ Hybrid Parallel Scanning
The app employs a sophisticated "Race Logic" to retrieve product data with minimal latency. It simultaneously queries four different sources:
1.  **Cloud Firestore:** Our proprietary crowdsourced database (Fastest).
2.  **OpenFoodFacts API:** For food and beverage items.
3.  **OpenBeautyFacts API:** For cosmetics and personal care.
4.  **UPCItemDB:** A reliable backup for general retail items.

*The first source to return valid data "wins" the race, cancelling the other requests to conserve resources.*

### ☁️ Crowdsourcing Engine
TrueRate gets smarter with every use. If a product is not found in our databases, users can manually enter the details. This data is instantly uploaded to the cloud, making the product recognizable for all future users.

### 🏢 Dynamic Tax Slabs
The app supports configurable tax categories based on current GST standards:
*   **Exempt (0%):** Essentials like milk, bread, and fresh vegetables.
*   **Essential (5%):** Daily-use items like oil, tea, and medicines.
*   **Standard (18%):** Electronics and home appliances.
*   **Luxury (40%):** Sin goods like tobacco and aerated drinks.

### 💾 Offline Persistence
Powered by the **Room Persistence Library**, TrueRate caches your scan history locally. You can access your previous scans and calculations even without an active internet connection.

### 📷 Advanced Scanning
Integrated with **Google ML Kit** and **CameraX**, the app offers instant and accurate barcode detection directly from the device's camera.

## 🧪 Quality Assurance

This project adheres to strict engineering standards with a robust and comprehensive test suite ensuring reliability and accuracy.

* **100+ Unit Tests:** Rigorous testing of all core business logic including:
    * **Tax Engine:** Verifies accurate calculation of Exempt (0%), Essential (5%), Standard (18%), and Luxury (40%) tax slabs with precise floating-point math.
    * **Barcode Router:** Validates routing logic for Indian Retail (890), Books (978), and Global products.
    * **Data Parsing:** Ensures resilience against null values, missing fields, and API inconsistencies.
* **Integration Tests:** Validates the persistence layer, ensuring that the **Room Database** correctly saves, retrieves, and maintains data integrity for scan history.
* **UI/Espresso Tests:** Automated verification of key user flows, including navigation, dialog interactions, and screen visibility on physical devices.
* **Performance:** Achieved a **100% Pass Rate** across all test modules (Unit, Integration, and UI).

## 🛠️ Technical Architecture

* **Language:** Java
* **Architecture pattern:** MVC (Model-View-Controller) with Repository Pattern
* **Database:**
    * **Local:** Room Database (SQLite) for history and caching.
    * **Cloud:** Firebase Firestore (NoSQL) for crowdsourced data.
* **Networking:** Retrofit 2 with Gson for API communication.
* **Concurrency:** Parallel Execution using ExecutorService and Atomic Boolean Locks.
* **Hardware Integration:** CameraX + ML Kit Vision API.
* **UI/UX:** Material Design Components.

## 📸 How It Works

1.  **Scan:** Point the camera at any product barcode.
2.  **Identify:** The app checks the Cloud Cache first. If missing, it initiates a parallel API race to find the product globally.
3.  **Calculate:** Once identified, the app applies the correct GST slab.
4.  **Contribute:** If the product is unknown, your manual entry contributes to the global database.

## 📝 Setup Instructions

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/your-username/TaxCalculator.git
    ```
2.  **Firebase Configuration:**
    *   Create a project in the Firebase Console.
    *   Download the `google-services.json` file.
    *   Place it in the `/app` directory of the project.
3.  **Build:** Open the project in Android Studio and sync Gradle.
4.  **Run:** Deploy the app to a physical device or emulator.

---

*Built as a Master of Computer Applications (MCA) project at Goa University.*
