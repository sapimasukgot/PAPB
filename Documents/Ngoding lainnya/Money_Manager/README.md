# 💰 Money Manager (Android Native)

A modern, native Android **Money Manager** application built with **Kotlin** and **Jetpack Compose (Material 3)**. Features automated transaction logging via **Notification Scraping (Auto-Write)** for Indonesian e-wallets (**GoPay**, **GoPay Clone / Dual Apps**, **ShopeePay**, and **OVO**), comprehensive category analytics, full English UI, and full date-time tracking.

---

## 🌟 Key Features & Requirements

### 1. 🌐 Full English User Interface
- Clean, intuitive, and modern UI rendered completely in **English** (all labels, dialogues, categories, filters, error alerts, and simulator actions).

### 2. 📅 Full Date & Time Representation
- Displays complete day name, date, month, and year across all views (e.g. `Friday, 25 September 2026`).
- Precise device-level timestamps (`HH:mm:ss`) recorded and displayed on each transaction.
- Dynamic calendar synchronization for daily, weekly, monthly, and custom date range filters.

### 3. 🏷️ Dedicated Category Segment, Custom Categories & Editing
- **Categories Tab in Bottom Navigation**:
  - View all categories split between **Expenses** and **Income**.
  - **Aggregated Statistics**: Total spending/income per category, transaction count, and visual percentage progress bars.
  - **Category Detail & History**: Tap on any category to view its complete transaction history, neatly grouped by full day, date, month, and year with a back navigation button.
- **✨ Custom Category Creation & Editing (Fitur Buat & Edit Kategori)**:
  - **Create Custom Categories**: Tap the **"+ Category"** button in the Categories screen to add custom expense or income categories.
  - **Edit Any Category**: Tap the **✏️ Edit** button on any category card or in the category history detail header to rename the category, switch its type, change its icon, or customize its color.
  - **Rich Icon & Color Palette**: Choose from 18 curated Material Design icons (Food, Coffee, Transport, Shopping, Bills, Fitness, Travel, Home, Pets, Work, Education, Health, Investment, Salary, Bonus, etc.) and 12 vibrant HSL color themes.
  - **Safe Cascading Renames**: Renaming a category automatically cascades in SQLite to all associated past transactions and monthly budget limits, ensuring historical financial records remain consistent and intact.
  - **System-Wide Reactivity**: Newly created or edited categories are immediately available across the entire app (Add Transaction dialog, Category Breakdown, and Set Budget dialog).


### 4. 📊 Monthly Budgeting & Over-Budget Alerts
- **Monthly Spending Limits**: Set customizable monthly budgets for chosen expense categories (e.g. Food & Beverages, Shopping & Groceries, Transportation, Entertainment).
- **Comprehensive Budget Overview**:
  - **Total Monthly Budget**: Sum of all assigned category limits for the selected month.
  - **Current Spending (Jumlah Pengeluaran Saat Ini)**: Total money spent so far on budgeted categories.
  - **Total Month Expenses**: Overall monthly expenditure across all categories.
  - **Remaining Budget / Deficit**: Exact remaining balance or excess deficit amount.
  - **Visual Progress Bars**: Animated indicators with intuitive status color coding:
    - 🟢 Green: Safe (< 75% utilized)
    - 🟡 Amber: Warning (75% - 99% utilized)
    - 🔴 Red: Exceeded (100%+ over budget)
- **⚠️ Instant Over-Budget Alert Pop-Up**:
  - Automatically pops up as an interactive modal whenever an expense (either entered manually or captured via Auto-Write notification scraping) pushes spending over the category limit.
  - Displays category name, monthly limit, current total spent, overspent amount, and remaining budget deficit.
  - Includes a direct "View Budget" shortcut to inspect and adjust allocations.
- **Month-by-Month Navigation**: Easily review historical months and plan upcoming monthly budgets (`< September 2026 >`).

### 5. 🏛 Accounts & Wallet Management
- **Source & Destination of Funds**: Manage financial accounts including BCA, Mandiri, GoPay, GoPay Clone, ShopeePay, OVO, and Cash.
- **Real-Time Balance Updates**: Expenses deduct balance, income adds balance, and transfers atomically move funds between accounts.
- **Account Customization**: Customize account names, account/card numbers, initial balances, and color palettes.

### 6. ⚡ Auto-Write via Notification Scraping
- Runs in background using Android `NotificationListenerService`.
- **Supported E-Wallets**: GoPay, GoPay Clone (Dual Apps), ShopeePay, and OVO.
- **Smart Regex Parser**: Automatically extracts transaction amounts, transaction types (expense/income), target merchants/recipients (e.g. Starbucks, Indomaret, Budi Santoso), and assigns them to the appropriate wallet and category.
- **🚫 Cashback & Koin/Point Filter**: Notifikasi penerimaan cashback, koin loyalty (GoPay Coins, Koin Shopee), serta reward points (OVO Points) otomatis disaring dan diabaikan (*ignored*) sehingga tidak mengotori riwayat transaksi dan tidak menggelembungkan saldo kas riil.
- **Instant Budget Validation**: Scraped transactions automatically validate against category budgets and trigger the Over-Budget Alert if the limit is breached.
- **Built-in Simulator**: Test parser scenarios with one tap without real monetary transactions.

### 7. 📱 5-Tab Modern Bottom Navigation Bar
1. 🏠 **Dashboard**: Net balance summary, account cards carousel, quick action buttons, recent activity, and **Monthly Budget Quick Glance Card**.
2. 📋 **Transactions**: Multi-account filter, time filter chips, interactive calendar picker, and full date transaction logs.
3. 🏷️ **Categories**: Dual segmented switcher:
   - **Categories Breakdown**: Visual breakdown of expenses/income and tap-to-inspect category transaction history.
   - **Monthly Budget**: Complete budget control center (total budget, current spending, progress bars, edit limits, and month navigation).
4. 🏛 **Accounts**: Manage banks & wallets, update balances, add new accounts, and perform account transfers.
5. ⚡ **Auto-Write**: Notification listener status, Android permission shortcut, and instant test simulator.

### 8. 🔙 Smart Android System Back Button Handling
- **Return to Dashboard**: Pressing the Android system Back button (tombol kembali di pojok kanan bawah perangkat) from any screen (Transactions, Categories, Accounts, Auto-Write) seamlessly returns you to the **Main Dashboard (Halaman Utama)** instead of unexpectedly closing the app.
- **Hierarchical Detail Back**: Pressing Back while inspecting a category's transaction history returns you to the category overview first.
- **Accidental Exit Prevention**: When already on the Main Dashboard, pressing Back shows a friendly *"Press back again to exit"* toast prompt, requiring a second press within 2 seconds to cleanly exit.

---

## 🏗 Struktur Proyek

```
Money_Manager/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/dave/moneymanager/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MoneyManagerApplication.kt
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   ├── Bank.kt
│   │   │   │   │   ├── Transaction.kt
│   │   │   │   │   ├── Category.kt
│   │   │   │   │   ├── Budget.kt             (Budget, CategoryBudgetStatus, BudgetAlertData)
│   │   │   │   │   ├── FilterOptions.kt
│   │   │   │   │   └── ScrapedNotificationLog.kt
│   │   │   │   └── local/
│   │   │   │       ├── DatabaseHelper.kt         (SQLite Database & TABLE_BUDGETS)
│   │   │   │       └── MoneyRepository.kt        (Reactive StateFlow Repository)
│   │   │   ├── service/
│   │   │   │   ├── MoneyNotificationListenerService.kt (Android Notification Listener)
│   │   │   │   └── NotificationParser.kt     (Indonesian E-Wallet Parser)
│   │   │   └── ui/
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt
│   │   │       │   ├── Theme.kt
│   │   │       │   └── Type.kt
│   │   │       ├── components/
│   │   │       │   ├── BankCard.kt
│   │   │       │   ├── StatSummaryCard.kt
│   │   │       │   ├── TransactionItem.kt
│   │   │       │   ├── BottomNavBar.kt
│   │   │       │   ├── AddTransactionDialog.kt
│   │   │       │   ├── AddBankDialog.kt
│   │   │       │   ├── TransferDialog.kt
│   │   │       │   ├── SetBudgetDialog.kt    (Create & Edit Category Limits)
│   │   │       │   └── BudgetAlertDialog.kt  (Global Over-Budget Pop-up Warning)
│   │   │       ├── screens/
│   │   │       │   ├── DashboardScreen.kt    (Includes Monthly Budget Widget)
│   │   │       │   ├── TransactionsScreen.kt
│   │   │       │   ├── CategoriesScreen.kt   (Includes Segments: Categories & Monthly Budget)
│   │   │       │   ├── BankManagementScreen.kt
│   │   │       │   └── AutoScraperScreen.kt
│   │   │       └── viewmodel/
│   │   │           └── MoneyViewModel.kt     (StateFlows & Budget Alerts Logic)
│   │   └── res/
│   └── build.gradle.kts
├── gradle/
└── build.gradle.kts
```

---

---

## 🚀 Panduan Instalasi & Penggunaan Lengkap

Terdapat 3 cara untuk menginstal aplikasi **Money Manager** ke perangkat Android Anda. Pilih cara yang paling mudah:

### 📱 Cara 1: Menginstal Langsung File APK ke HP (Paling Praktis)

1. **Temukan File APK**:
   File APK sudah berhasil dikompilasi dan siap digunakan pada folder:
   ```
   app/build/outputs/apk/debug/app-debug.apk
   ```
2. **Kirim APK ke HP Android Anda**:
   - Anda bisa mengirimkannya lewat **WhatsApp Web**, **Telegram Saved Messages**, **Google Drive**, atau menyalinnya langsung via kabel data USB.
3. **Pasang (Install) di HP**:
   - Buka file `app-debug.apk` yang telah dikirim ke HP Anda.
   - Pilih **Install / Pasang**.
   - *Catatan*: Jika muncul peringatan keamanan *"Sumber tidak dikenal"* (*Install unknown apps*) atau *Google Play Protect*, pilih **"Tetap Install" / "Install anyway"** (hal ini wajar untuk file APK yang di-build dalam mode development/debug).

---

### 💻 Cara 2: Pasang Menggunakan Kabel Data (ADB PowerShell)

Jika HP Anda sudah terhubung ke PC/laptop dengan kabel data:

1. **Aktifkan Opsi Pengembang & USB Debugging di HP**:
   - Buka **Pengaturan HP > Tentang Ponsel** (*About Phone*).
   - Ketuk **Nomor Bentukan** (*Build Number*) sebanyak 7 kali hingga muncul pesan *"Anda adalah seorang pengembang"*.
   - Kembali ke Pengaturan, buka **Opsi Pengembang** (*Developer Options*), lalu aktifkan **USB Debugging**.
2. **Jalankan Perintah ADB di Terminal PC**:
   Buka terminal PowerShell pada direktori proyek ini, lalu jalankan:
   ```powershell
   & "C:\Users\DAVE\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r "app\build\outputs\apk\debug\app-debug.apk"
   ```
3. Aplikasi akan langsung terpasang otomatis di layar utama HP Anda.

---

### 🛠 Cara 3: Menjalankan via Android Studio

1. Buka aplikasi **Android Studio**.
2. Pilih menu **File > Open**, lalu pilih folder proyek ini:
   ```
   c:\Users\DAVE\Documents\Ngoding lainnya\Money_Manager
   ```
3. Tunggu hingga proses *Gradle Sync* selesai.
4. Hubungkan HP fisik Anda atau buka emulator Android Virtual Device (AVD).
5. Klik tombol hijau **Run 'app' ▶** (atau shortcut `Shift + F10`).

---

## ⚙️ Pengaturan Wajib Setelah Aplikasi Terpasang

Agar fitur **Auto-Write Notification Scraping** berjalan otomatis di latar belakang saat transaksi GoPay, ShopeePay, dan OVO terjadi:

### 1. Mengaktifkan Izin Akses Notifikasi (Notification Access)
1. Buka aplikasi **Money Manager**.
2. Masuk ke tab paling kanan: **Auto-Write** ⚡.
3. Anda akan melihat kartu peringatan *"Izin Notifikasi Belum Aktif"*.
4. Klik tombol **"Buka Pengaturan Izin Notifikasi"**.
5. Sistem Android akan membuka layar pengaturan izin notifikasi. Cari aplikasi **Money Manager**, lalu aktifkan tombol switch ke posisi **Aktif (Izinkan / Allow)**.
6. Kembali ke Money Manager, statusnya akan berubah menjadi hijau: *"Izin Akses Notifikasi Aktif"*.

### 2. Pengaturan Hemat Baterai (Penting untuk Xiaomi, Samsung, Oppo, Vivo, Realme)
Sistem operasi Android pada merek-merek tertentu sering mematikan background service untuk menghemat baterai. Agar notifikasi transaksi tetap tercatat saat layar HP mati/terkunci:
1. Buka **Pengaturan HP > Aplikasi > Kelola Aplikasi > Money Manager**.
2. **Penghemat Baterai** (*Battery Saver*): Ubah dari *Optimal* menjadi **"Tidak Ada Pembatasan"** (*No restrictions / Unrestricted*).
3. **Mulai Otomatis** (*Autostart*): Aktifkan izin ini jika tersedia di HP Anda.

---

## 🧪 Cara Menguji Fitur Auto-Write (Tanpa Keluar Uang)

1. Buka tab **Auto-Write** ⚡ di aplikasi Money Manager.
2. Scroll ke bagian kartu **"Simulator / Uji Coba Scraping"**.
3. Coba tekan tombol-tombol pengujian:
   - `Simulasi GoPay: Bayar Rp 25.000 ke Kopi Kenangan`
   - `Simulasi GoPay: Transfer ke Budi Santoso Rp 50.000`
   - `Simulasi GoPay Clone: Bayar Rp 42.000 di Alfamart Kaliurang`
   - `Simulasi ShopeePay: Bayar Rp 75.000 di ShopeeFood`
   - `Simulasi OVO: Transfer Rp 150.000 ke Rekening BCA 5270119823`
   - `Simulasi OVO Masuk: Transfer Rp 100.000 dari SITI RAHMAWATI`
4. Lihat pada bagian **Riwayat Scraping**:
   - Transaksi akan langsung berstatus `PROCESSED`.
   - Nama merchant/penerima atau nomor rekening tujuan akan langsung terdeteksi rapi.
5. Pindah ke tab **Beranda** atau **Transaksi**:
   - Saldo dompet Anda akan otomatis terpotong/bertambah sesuai nominal.
   - Transaksi muncul lengkap dengan badge `⚡ Auto`, jam transaksi device, dan badge tujuan `🎯 Ke: [Nama Tujuan]`.

---

## 📊 Cara Menguji Fitur Monthly Budgeting & Pop-up Peringatan

1. **Buka Segmen Monthly Budget**:
   - Ketuk tab **Categories** di navigasi bawah.
   - Di bagian atas, pilih tab segmented **"Monthly Budget"**.
   - Atau dari tab **Dashboard**, ketuk tombol **"Manage"** pada kartu ringkasan *Monthly Budget*.
2. **Lihat Ringkasan Budget Bulanan**:
   - Di bagian atas kartu utama, Anda dapat melihat:
     - **Total Budget**: Total limit pengeluaran bulanan yang telah dialokasikan.
     - **Current Spending**: Jumlah akumulasi pengeluaran pada kategori yang memiliki budget.
     - **All Expenses**: Total semua pengeluaran bulan ini (termasuk kategori non-budget).
     - **Remaining / Deficit**: Sisa budget yang masih bisa dipakai atau jumlah over-budget.
     - **Progress Bar Animasi**: Berwarna Hijau (<75%), Kuning (75-99%), atau Merah (>=100%).
3. **Mengatur atau Menambah Budget Kategori**:
   - Tekan tombol **"+ Set Budget"** di pojok kanan atas segmen budget.
   - Pilih kategori pengeluaran (misal: *Food & Beverages*, *Shopping & Groceries*, *Transportation*).
   - Masukkan nominal batas bulanan (misal: `Rp 50.000` untuk tes cepat batas terlampaui).
   - Tekan **"Save Budget"**. Kategori akan langsung muncul di daftar dengan limit baru.
4. **Memicu Pop-up Peringatan Over-Budget**:
   - **Tes via Manual**:
     - Buka tab **Dashboard** atau **Transactions**, tekan tombol **"+" (Add Transaction)**.
     - Pilih tipe **Expense**, pilih kategori yang Anda beri limit (misal: *Food & Beverages*), dan masukkan nominal di atas limit (misal: `Rp 100.000`).
     - Tekan **Save**.
     - **Pop-up Peringatan Merah** langsung muncul di layar:
       - Memperingatkan bahwa pengeluaran kategori telah melampaui budget.
       - Menampilkan nama kategori, batas bulanan, total pengeluaran saat ini, dan selisih minus / sisa budget defisit.
       - Terdapat tombol **"View Budget"** untuk langsung menuju segmen pengelolaan budget.
   - **Tes via Auto-Write Simulator**:
     - Buka tab **Auto-Write**, lalu jalankan simulasi GoPay atau ShopeePay (misal: bayar makanan/kopi).
     - Jika transaksi tersebut menyebabkan pengeluaran kategori melebihi budget, pop-up peringatan over-budget juga akan otomatis muncul secara instan!
