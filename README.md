# 🇮🇳 Smart GST Tax Calculator & Scanner

A smart, crowdsourced barcode scanner that calculates GST tax slabs automatically. 
Built with an **Offline-First** and **Parallel Network** architecture to ensure instant results.

## 🚀 Key Features

* **⚡ Hybrid Parallel Scanning:** Simultaneously races 4 different sources to find product data:
    1.  **Cloud Firestore** (Crowdsourced data - Fastest)
    2.  **OpenFoodFacts API**
    3.  **OpenBeautyFacts API**
    4.  **UPCItemDB** (Backup)
* **☁️ Crowdsourcing Engine:** Every manual entry by a user is uploaded to the cloud, making the scanner smarter for the next user.
* **🏢 Dynamic Tax Slabs:** Configurable tax categories (Exempt, Essential, Standard, Luxury) that auto-calculate based on current Indian GST laws.
* **💾 Offline Persistence:** Uses **Room Database** to cache history. Works perfectly without internet.
* **📷 ML Kit Vision:** Instant barcode detection using Google's ML Kit and CameraX.

## 🛠️ Tech Stack

* **Language:** Java
* **Architecture:** Parallel Execution (Atomic Boolean Locks)
* **Database:** * **Local:** Room Persistence Library (SQLite)
    * **Cloud:** Firebase Firestore (NoSQL)
* **Networking:** Retrofit 2 + Gson
* **Hardware:** CameraX + ML Kit Vision API
* **UI:** Material Design Components

## 📸 How it Works (The "Race" Logic)

1.  **Phase 1 (Cache):** Checks Cloud Firestore. If found, returns immediately ( < 100ms).
2.  **Phase 2 (Race):** If not in cloud, fires requests to Food, Beauty, and Product APIs **simultaneously**.
3.  **Phase 3 (Win):** The first API to return valid data "Wins" the race and cancels the others to save resources.
4.  **Phase 4 (Learn):** If all fail, the user enters data manually, which is instantly uploaded to the Cloud for future users.

## 📝 Setup

1.  Clone the repo.
2.  Add your `google-services.json` file (Firebase) to the `/app` folder.
3.  Build and Run!

---
*Built as a Master of Computer Applications (MCA) project at Goa University.*
