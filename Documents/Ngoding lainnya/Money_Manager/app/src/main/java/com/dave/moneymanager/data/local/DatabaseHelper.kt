package com.dave.moneymanager.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.dave.moneymanager.data.model.Bank
import com.dave.moneymanager.data.model.Budget
import com.dave.moneymanager.data.model.ScrapedNotificationLog
import com.dave.moneymanager.data.model.ShieldContact
import com.dave.moneymanager.data.model.ShieldEntry
import com.dave.moneymanager.data.model.ShieldHistoryItem
import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionCategory
import com.dave.moneymanager.data.model.TransactionEditHistory

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "money_manager.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_BANKS = "banks"
        const val COL_BANK_ID = "id"
        const val COL_BANK_NAME = "name"
        const val COL_BANK_ACCOUNT_NO = "account_number"
        const val COL_BANK_BALANCE = "balance"
        const val COL_BANK_TYPE = "type"
        const val COL_BANK_COLOR = "color_hex"
        const val COL_BANK_ICON = "icon_name"
        const val COL_BANK_DISPLAY_ORDER = "display_order"

        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_TX_ID = "id"
        const val COL_TX_BANK_ID = "bank_id"
        const val COL_TX_TARGET_BANK_ID = "target_bank_id"
        const val COL_TX_TYPE = "type"
        const val COL_TX_AMOUNT = "amount"
        const val COL_TX_CATEGORY = "category"
        const val COL_TX_NOTE = "note"
        const val COL_TX_TIMESTAMP = "timestamp"
        const val COL_TX_IS_AUTO_SCRAPED = "is_auto_scraped"
        const val COL_TX_SOURCE_APP = "source_app"
        const val COL_TX_DESTINATION = "destination"

        const val TABLE_NOTIF_LOGS = "notification_logs"
        const val COL_LOG_ID = "id"
        const val COL_LOG_PKG = "package_name"
        const val COL_LOG_APP = "app_name"
        const val COL_LOG_TITLE = "title"
        const val COL_LOG_CONTENT = "content"
        const val COL_LOG_TIMESTAMP = "timestamp"
        const val COL_LOG_STATUS = "status"
        const val COL_LOG_AMOUNT = "amount"
        const val COL_LOG_DETECTED_TYPE = "detected_type"
        const val COL_LOG_TARGET_BANK = "target_bank"
        const val COL_LOG_DESTINATION = "destination"

        const val TABLE_BUDGETS = "budgets"
        const val COL_BUDGET_ID = "id"
        const val COL_BUDGET_CATEGORY = "category"
        const val COL_BUDGET_LIMIT = "monthly_limit"
        const val COL_BUDGET_MONTH_YEAR = "month_year"

        const val TABLE_CATEGORIES = "categories"
        const val COL_CAT_ID = "id"
        const val COL_CAT_NAME = "name"
        const val COL_CAT_ICON = "icon"
        const val COL_CAT_COLOR = "color_hex"
        const val COL_CAT_IS_EXPENSE = "is_expense"

        const val TABLE_SHIELDS = "shields"
        const val COL_SHIELD_ID = "id"
        const val COL_SHIELD_TX_ID = "transaction_id"
        const val COL_SHIELD_FRIEND = "friend_name"
        const val COL_SHIELD_AMOUNT = "amount"
        const val COL_SHIELD_NOTE = "note"
        const val COL_SHIELD_TIMESTAMP = "timestamp"
        const val COL_SHIELD_IS_SETTLED = "is_settled"
        const val COL_SHIELD_SETTLED_AT = "settled_at"
        const val COL_SHIELD_SETTLED_BANK_ID = "settled_bank_id"
        const val COL_SHIELD_SETTLED_BANK_NAME = "settled_bank_name"
        const val COL_SHIELD_AVATAR_URI = "avatar_uri"

        const val TABLE_SHIELD_HISTORY = "shield_history"
        const val COL_SHIELD_HIST_ID = "id"
        const val COL_SHIELD_HIST_SHIELD_ID = "shield_id"
        const val COL_SHIELD_HIST_TX_ID = "transaction_id"
        const val COL_SHIELD_HIST_FRIEND = "friend_name"
        const val COL_SHIELD_HIST_AMOUNT = "amount"
        const val COL_SHIELD_HIST_NOTE = "note"
        const val COL_SHIELD_HIST_TIMESTAMP = "timestamp"

        const val TABLE_SHIELD_CONTACTS = "shield_contacts"
        const val COL_CONTACT_ID = "_id"
        const val COL_CONTACT_NAME = "name"
        const val COL_CONTACT_AVATAR = "avatar_uri"
        const val COL_CONTACT_CREATED_AT = "created_at"

        const val TABLE_TX_EDIT_HISTORY = "transaction_edit_history"
        const val COL_TX_EDIT_ID = "id"
        const val COL_TX_EDIT_TX_ID = "transaction_id"
        const val COL_TX_EDIT_TIMESTAMP = "edit_timestamp"
        const val COL_TX_EDIT_OLD_AMOUNT = "old_amount"
        const val COL_TX_EDIT_NEW_AMOUNT = "new_amount"
        const val COL_TX_EDIT_DIFF_AMOUNT = "diff_amount"
        const val COL_TX_EDIT_OLD_CAT = "old_category"
        const val COL_TX_EDIT_NEW_CAT = "new_category"
        const val COL_TX_EDIT_OLD_BANK = "old_bank_name"
        const val COL_TX_EDIT_NEW_BANK = "new_bank_name"
        const val COL_TX_EDIT_OLD_NOTE = "old_note"
        const val COL_TX_EDIT_NEW_NOTE = "new_note"
        const val COL_TX_EDIT_SUMMARY = "change_summary"

        @Volatile
        private var instance: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: DatabaseHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBanksTable = """
            CREATE TABLE $TABLE_BANKS (
                $COL_BANK_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BANK_NAME TEXT NOT NULL,
                $COL_BANK_ACCOUNT_NO TEXT,
                $COL_BANK_BALANCE REAL NOT NULL DEFAULT 0,
                $COL_BANK_TYPE TEXT NOT NULL,
                $COL_BANK_COLOR INTEGER NOT NULL,
                $COL_BANK_ICON TEXT NOT NULL,
                $COL_BANK_DISPLAY_ORDER INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        val createTxTable = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_TX_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TX_BANK_ID INTEGER NOT NULL,
                $COL_TX_TARGET_BANK_ID INTEGER,
                $COL_TX_TYPE TEXT NOT NULL,
                $COL_TX_AMOUNT REAL NOT NULL,
                $COL_TX_CATEGORY TEXT NOT NULL,
                $COL_TX_NOTE TEXT,
                $COL_TX_TIMESTAMP INTEGER NOT NULL,
                $COL_TX_IS_AUTO_SCRAPED INTEGER NOT NULL DEFAULT 0,
                $COL_TX_SOURCE_APP TEXT,
                $COL_TX_DESTINATION TEXT
            )
        """.trimIndent()

        val createNotifLogsTable = """
            CREATE TABLE $TABLE_NOTIF_LOGS (
                $COL_LOG_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_LOG_PKG TEXT,
                $COL_LOG_APP TEXT,
                $COL_LOG_TITLE TEXT,
                $COL_LOG_CONTENT TEXT,
                $COL_LOG_TIMESTAMP INTEGER NOT NULL,
                $COL_LOG_STATUS TEXT NOT NULL,
                $COL_LOG_AMOUNT REAL,
                $COL_LOG_DETECTED_TYPE TEXT,
                $COL_LOG_TARGET_BANK TEXT,
                $COL_LOG_DESTINATION TEXT
            )
        """.trimIndent()

        db.execSQL(createBanksTable)
        db.execSQL(createTxTable)
        db.execSQL(createNotifLogsTable)

        val createBudgetsTable = """
            CREATE TABLE $TABLE_BUDGETS (
                $COL_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_BUDGET_CATEGORY TEXT NOT NULL,
                $COL_BUDGET_LIMIT REAL NOT NULL,
                $COL_BUDGET_MONTH_YEAR TEXT NOT NULL,
                UNIQUE($COL_BUDGET_CATEGORY, $COL_BUDGET_MONTH_YEAR) ON CONFLICT REPLACE
            )
        """.trimIndent()
        db.execSQL(createBudgetsTable)

        val createCategoriesTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CAT_NAME TEXT UNIQUE NOT NULL,
                $COL_CAT_ICON TEXT NOT NULL,
                $COL_CAT_COLOR INTEGER NOT NULL,
                $COL_CAT_IS_EXPENSE INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createCategoriesTable)

        val createShieldsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_SHIELDS (
                $COL_SHIELD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SHIELD_TX_ID INTEGER,
                $COL_SHIELD_FRIEND TEXT NOT NULL,
                $COL_SHIELD_AMOUNT REAL NOT NULL,
                $COL_SHIELD_NOTE TEXT,
                $COL_SHIELD_TIMESTAMP INTEGER NOT NULL,
                $COL_SHIELD_IS_SETTLED INTEGER NOT NULL DEFAULT 0,
                $COL_SHIELD_SETTLED_AT INTEGER,
                $COL_SHIELD_SETTLED_BANK_ID INTEGER,
                $COL_SHIELD_SETTLED_BANK_NAME TEXT,
                $COL_SHIELD_AVATAR_URI TEXT
            )
        """.trimIndent()
        db.execSQL(createShieldsTable)

        val createShieldHistoryTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_SHIELD_HISTORY (
                $COL_SHIELD_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SHIELD_HIST_SHIELD_ID INTEGER NOT NULL,
                $COL_SHIELD_HIST_TX_ID INTEGER,
                $COL_SHIELD_HIST_FRIEND TEXT NOT NULL,
                $COL_SHIELD_HIST_AMOUNT REAL NOT NULL,
                $COL_SHIELD_HIST_NOTE TEXT,
                $COL_SHIELD_HIST_TIMESTAMP INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createShieldHistoryTable)

        val createShieldContactsTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_SHIELD_CONTACTS (
                $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CONTACT_NAME TEXT UNIQUE COLLATE NOCASE NOT NULL,
                $COL_CONTACT_AVATAR TEXT,
                $COL_CONTACT_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createShieldContactsTable)

        val createTxEditHistoryTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TX_EDIT_HISTORY (
                $COL_TX_EDIT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TX_EDIT_TX_ID INTEGER NOT NULL,
                $COL_TX_EDIT_TIMESTAMP INTEGER NOT NULL,
                $COL_TX_EDIT_OLD_AMOUNT REAL NOT NULL,
                $COL_TX_EDIT_NEW_AMOUNT REAL NOT NULL,
                $COL_TX_EDIT_DIFF_AMOUNT REAL NOT NULL,
                $COL_TX_EDIT_OLD_CAT TEXT,
                $COL_TX_EDIT_NEW_CAT TEXT,
                $COL_TX_EDIT_OLD_BANK TEXT,
                $COL_TX_EDIT_NEW_BANK TEXT,
                $COL_TX_EDIT_OLD_NOTE TEXT,
                $COL_TX_EDIT_NEW_NOTE TEXT,
                $COL_TX_EDIT_SUMMARY TEXT NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTxEditHistoryTable)

        insertInitialBanks(db)
        insertInitialCategories(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_SHIELD_CONTACTS (
                    $COL_CONTACT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_CONTACT_NAME TEXT UNIQUE COLLATE NOCASE NOT NULL,
                    $COL_CONTACT_AVATAR TEXT,
                    $COL_CONTACT_CREATED_AT INTEGER NOT NULL
                )
            """.trimIndent())
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_TX_EDIT_HISTORY (
                    $COL_TX_EDIT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_TX_EDIT_TX_ID INTEGER NOT NULL,
                    $COL_TX_EDIT_TIMESTAMP INTEGER NOT NULL,
                    $COL_TX_EDIT_OLD_AMOUNT REAL NOT NULL,
                    $COL_TX_EDIT_NEW_AMOUNT REAL NOT NULL,
                    $COL_TX_EDIT_DIFF_AMOUNT REAL NOT NULL,
                    $COL_TX_EDIT_OLD_CAT TEXT,
                    $COL_TX_EDIT_NEW_CAT TEXT,
                    $COL_TX_EDIT_OLD_BANK TEXT,
                    $COL_TX_EDIT_NEW_BANK TEXT,
                    $COL_TX_EDIT_OLD_NOTE TEXT,
                    $COL_TX_EDIT_NEW_NOTE TEXT,
                    $COL_TX_EDIT_SUMMARY TEXT NOT NULL
                )
            """.trimIndent())

            db.execSQL("""
                INSERT OR IGNORE INTO $TABLE_SHIELD_CONTACTS ($COL_CONTACT_NAME, $COL_CONTACT_AVATAR, $COL_CONTACT_CREATED_AT)
                SELECT TRIM($COL_SHIELD_FRIEND), $COL_SHIELD_AVATAR_URI, MIN($COL_SHIELD_TIMESTAMP)
                FROM $TABLE_SHIELDS
                WHERE $COL_SHIELD_FRIEND IS NOT NULL AND TRIM($COL_SHIELD_FRIEND) != ''
                GROUP BY LOWER(TRIM($COL_SHIELD_FRIEND))
            """.trimIndent())

            db.execSQL("""
                INSERT OR IGNORE INTO $TABLE_SHIELD_CONTACTS ($COL_CONTACT_NAME, $COL_CONTACT_AVATAR, $COL_CONTACT_CREATED_AT)
                SELECT TRIM($COL_SHIELD_HIST_FRIEND), NULL, MIN($COL_SHIELD_HIST_TIMESTAMP)
                FROM $TABLE_SHIELD_HISTORY
                WHERE $COL_SHIELD_HIST_FRIEND IS NOT NULL AND TRIM($COL_SHIELD_HIST_FRIEND) != ''
                GROUP BY LOWER(TRIM($COL_SHIELD_HIST_FRIEND))
            """.trimIndent())

            db.execSQL("""
                UPDATE $TABLE_SHIELD_CONTACTS
                SET $COL_CONTACT_AVATAR = (
                    SELECT s.$COL_SHIELD_AVATAR_URI
                    FROM $TABLE_SHIELDS s
                    WHERE LOWER(TRIM(s.$COL_SHIELD_FRIEND)) = LOWER($TABLE_SHIELD_CONTACTS.$COL_CONTACT_NAME)
                      AND s.$COL_SHIELD_AVATAR_URI IS NOT NULL
                    ORDER BY s.$COL_SHIELD_TIMESTAMP DESC LIMIT 1
                )
                WHERE $COL_CONTACT_AVATAR IS NULL
            """.trimIndent())
        } catch (_: Exception) {}
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_SHIELDS (
                    $COL_SHIELD_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_SHIELD_TX_ID INTEGER,
                    $COL_SHIELD_FRIEND TEXT NOT NULL,
                    $COL_SHIELD_AMOUNT REAL NOT NULL,
                    $COL_SHIELD_NOTE TEXT,
                    $COL_SHIELD_TIMESTAMP INTEGER NOT NULL,
                    $COL_SHIELD_IS_SETTLED INTEGER NOT NULL DEFAULT 0,
                    $COL_SHIELD_SETTLED_AT INTEGER,
                    $COL_SHIELD_SETTLED_BANK_ID INTEGER,
                    $COL_SHIELD_SETTLED_BANK_NAME TEXT,
                    $COL_SHIELD_AVATAR_URI TEXT
                )
            """.trimIndent())
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_SHIELDS ADD COLUMN $COL_SHIELD_AVATAR_URI TEXT")
        } catch (_: Exception) {}

        try {
            val dupCursor = db.rawQuery("""
                SELECT LOWER($COL_SHIELD_FRIEND), COUNT(*)
                FROM $TABLE_SHIELDS
                WHERE $COL_SHIELD_IS_SETTLED = 0
                GROUP BY LOWER($COL_SHIELD_FRIEND)
                HAVING COUNT(*) > 1
            """.trimIndent(), null)
            val dupNames = mutableListOf<String>()
            dupCursor.use {
                while (it.moveToNext()) {
                    dupNames.add(it.getString(0))
                }
            }
            for (name in dupNames) {
                val recordsCursor = db.query(
                    TABLE_SHIELDS,
                    null,
                    "LOWER($COL_SHIELD_FRIEND) = ? AND $COL_SHIELD_IS_SETTLED = 0",
                    arrayOf(name),
                    null,
                    null,
                    "$COL_SHIELD_TIMESTAMP ASC"
                )
                val entries = mutableListOf<ShieldEntry>()
                recordsCursor.use {
                    while (it.moveToNext()) {
                        entries.add(cursorToShield(it))
                    }
                }
                if (entries.size > 1) {
                    val first = entries.first()
                    val totalAmount = entries.sumOf { it.amount }
                    val combinedNote = entries.map { it.note.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(", ")
                    val latestTimestamp = entries.maxOf { it.timestamp }

                    val cv = ContentValues().apply {
                        put(COL_SHIELD_AMOUNT, totalAmount)
                        put(COL_SHIELD_NOTE, combinedNote)
                        put(COL_SHIELD_TIMESTAMP, latestTimestamp)
                    }
                    db.update(TABLE_SHIELDS, cv, "$COL_SHIELD_ID = ?", arrayOf(first.id.toString()))

                    for (i in 1 until entries.size) {
                        db.delete(TABLE_SHIELDS, "$COL_SHIELD_ID = ?", arrayOf(entries[i].id.toString()))
                    }
                }
            }
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE $TABLE_TRANSACTIONS ADD COLUMN $COL_TX_DESTINATION TEXT")
        } catch (_: Exception) {}
        try {
            db.execSQL("ALTER TABLE $TABLE_NOTIF_LOGS ADD COLUMN $COL_LOG_DESTINATION TEXT")
        } catch (_: Exception) {}
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_BUDGETS (
                    $COL_BUDGET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_BUDGET_CATEGORY TEXT NOT NULL,
                    $COL_BUDGET_LIMIT REAL NOT NULL,
                    $COL_BUDGET_MONTH_YEAR TEXT NOT NULL,
                    UNIQUE($COL_BUDGET_CATEGORY, $COL_BUDGET_MONTH_YEAR) ON CONFLICT REPLACE
                )
            """.trimIndent())
        } catch (_: Exception) {}
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                    $COL_CAT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_CAT_NAME TEXT UNIQUE NOT NULL,
                    $COL_CAT_ICON TEXT NOT NULL,
                    $COL_CAT_COLOR INTEGER NOT NULL,
                    $COL_CAT_IS_EXPENSE INTEGER NOT NULL
                )
            """.trimIndent())
            val catCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null)
            var catCount = 0
            if (catCursor.moveToFirst()) catCount = catCursor.getInt(0)
            catCursor.close()
            if (catCount == 0) {
                insertInitialCategories(db)
            }
            ensureShieldCategoryAndMigrate(db)

            val categoryRenames = listOf(
                Pair("incoming transfer", "Reimburse"),
                Pair("food & beverages", "Food & Dining"),
                Pair("shopping & groceries", "Shopping"),
                Pair("healthcare", "Health"),
                Pair("salary & wages", "Salary"),
                Pair("bonus & allowance", "Bonus"),
                Pair("top up & deposit", "Top Up"),
                Pair("cashback & rewards", "Cashback")
            )
            for ((oldName, newName) in categoryRenames) {
                db.execSQL("UPDATE $TABLE_CATEGORIES SET $COL_CAT_NAME = ? WHERE LOWER($COL_CAT_NAME) = LOWER(?)", arrayOf(newName, oldName))
                db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COL_TX_CATEGORY = ? WHERE LOWER($COL_TX_CATEGORY) = LOWER(?)", arrayOf(newName, oldName))
                db.execSQL("UPDATE $TABLE_BUDGETS SET $COL_BUDGET_CATEGORY = ? WHERE LOWER($COL_BUDGET_CATEGORY) = LOWER(?)", arrayOf(newName, oldName))
            }

            val allStandardCategories = TransactionCategory.defaultExpenseCategories + TransactionCategory.defaultIncomeCategories
            for (cat in allStandardCategories) {
                val cur = db.rawQuery("SELECT $COL_CAT_ID FROM $TABLE_CATEGORIES WHERE LOWER($COL_CAT_NAME) = LOWER(?)", arrayOf(cat.name))
                val exists = cur.moveToFirst()
                val existingId = if (exists) cur.getLong(0) else null
                cur.close()
                if (existingId == null) {
                    val cv = ContentValues().apply {
                        put(COL_CAT_NAME, cat.name)
                        put(COL_CAT_ICON, cat.icon)
                        put(COL_CAT_COLOR, cat.colorHex)
                        put(COL_CAT_IS_EXPENSE, if (cat.isExpense) 1 else 0)
                    }
                    db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
                } else {
                    val cv = ContentValues().apply {
                        put(COL_CAT_NAME, cat.name)
                        put(COL_CAT_ICON, cat.icon)
                        put(COL_CAT_COLOR, cat.colorHex)
                        put(COL_CAT_IS_EXPENSE, if (cat.isExpense) 1 else 0)
                    }
                    db.update(TABLE_CATEGORIES, cv, "$COL_CAT_ID = ?", arrayOf(existingId.toString()))
                }
            }

            db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COL_TX_DESTINATION = TRIM(SUBSTR($COL_TX_NOTE, 7)) WHERE ($COL_TX_DESTINATION IS NULL OR $COL_TX_DESTINATION = '') AND ($COL_TX_NOTE LIKE 'From: %' OR $COL_TX_NOTE LIKE 'from: %')")
            db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COL_TX_DESTINATION = TRIM(SUBSTR($COL_TX_NOTE, 7)) WHERE ($COL_TX_DESTINATION IS NULL OR $COL_TX_DESTINATION = '') AND ($COL_TX_NOTE LIKE 'Dari: %' OR $COL_TX_NOTE LIKE 'dari: %')")
            db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COL_TX_DESTINATION = TRIM(SUBSTR($COL_TX_NOTE, 5)) WHERE ($COL_TX_DESTINATION IS NULL OR $COL_TX_DESTINATION = '') AND ($COL_TX_NOTE LIKE 'To: %' OR $COL_TX_NOTE LIKE 'to: %')")
            db.execSQL("UPDATE $TABLE_TRANSACTIONS SET $COL_TX_DESTINATION = TRIM(SUBSTR($COL_TX_NOTE, 9)) WHERE ($COL_TX_DESTINATION IS NULL OR $COL_TX_DESTINATION = '') AND ($COL_TX_NOTE LIKE 'Tujuan: %' OR $COL_TX_NOTE LIKE 'tujuan: %')")
        } catch (_: Exception) {}

        try {
            db.execSQL("ALTER TABLE $TABLE_BANKS ADD COLUMN $COL_BANK_DISPLAY_ORDER INTEGER DEFAULT 0")
        } catch (_: Exception) {}

        migrateGopayCloneAndOrdering(db)

        try {

            val extraBanks = listOf(
                Bank(name = "Bank BNI", accountNumber = "0234567890", balance = 0.0, type = Bank.TYPE_BANK, colorHex = 0xFFE05A10L, iconName = "bni"),
                Bank(name = "Bank CIMB", accountNumber = "701234567890", balance = 0.0, type = Bank.TYPE_BANK, colorHex = 0xFFED1C24L, iconName = "cimb"),
                Bank(name = "OctoPay", accountNumber = "081234567890", balance = 0.0, type = Bank.TYPE_EWALLET, colorHex = 0xFF8A1538L, iconName = "octopay")
            )
            for (eb in extraBanks) {
                val c = db.rawQuery("SELECT $COL_BANK_ID FROM $TABLE_BANKS WHERE LOWER($COL_BANK_NAME) = LOWER(?)", arrayOf(eb.name))
                val exists = c.moveToFirst()
                c.close()
                if (!exists) {
                    val cv = ContentValues().apply {
                        put(COL_BANK_NAME, eb.name)
                        put(COL_BANK_ACCOUNT_NO, eb.accountNumber)
                        put(COL_BANK_BALANCE, eb.balance)
                        put(COL_BANK_TYPE, eb.type)
                        put(COL_BANK_COLOR, eb.colorHex)
                        put(COL_BANK_ICON, eb.iconName)
                    }
                    db.insert(TABLE_BANKS, null, cv)
                }
            }
        } catch (_: Exception) {}

        try {
            val toRepair = mutableListOf<Triple<Long, Long, Double>>()
            val detailsToUpdate = mutableMapOf<Long, Pair<String, String>>()

            val repairCursor = db.rawQuery("""
                SELECT $COL_TX_ID, $COL_TX_BANK_ID, $COL_TX_AMOUNT, $COL_TX_NOTE, $COL_TX_DESTINATION, $COL_TX_CATEGORY
                FROM $TABLE_TRANSACTIONS
                WHERE $COL_TX_TYPE = 'EXPENSE' AND (
                    LOWER($COL_TX_DESTINATION) LIKE 'dari %' OR
                    LOWER($COL_TX_DESTINATION) LIKE '%dari %' OR
                    LOWER($COL_TX_NOTE) LIKE '%dari %' OR
                    LOWER($COL_TX_NOTE) LIKE 'from:%' OR
                    $COL_TX_CATEGORY = 'Incoming Transfer' OR
                    $COL_TX_CATEGORY = 'Reimburse'
                )
            """.trimIndent(), null)

            repairCursor.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(COL_TX_ID)
                val bankIdIdx = cursor.getColumnIndexOrThrow(COL_TX_BANK_ID)
                val amountIdx = cursor.getColumnIndexOrThrow(COL_TX_AMOUNT)
                val noteIdx = cursor.getColumnIndexOrThrow(COL_TX_NOTE)
                val destIdx = cursor.getColumnIndexOrThrow(COL_TX_DESTINATION)

                while (cursor.moveToNext()) {
                    val txId = cursor.getLong(idIdx)
                    val bankId = cursor.getLong(bankIdIdx)
                    val amount = cursor.getDouble(amountIdx)
                    val oldNote = cursor.getString(noteIdx) ?: ""
                    val oldDest = cursor.getString(destIdx) ?: ""

                    var cleanDest = oldDest
                        .replace(Regex("^(?i)dari\\s+"), "")
                        .replace(Regex("^(?i)ke\\s+"), "")
                        .trim()
                    if (cleanDest.isEmpty()) {
                        cleanDest = oldNote
                            .replace(Regex("^(?i)To:\\s*"), "")
                            .replace(Regex("^(?i)From:\\s*"), "")
                            .replace(Regex("^(?i)dari\\s+"), "")
                            .trim()
                    }
                    val newNote = if (cleanDest.isNotEmpty()) "From: $cleanDest" else "Reimburse"

                    toRepair.add(Triple(txId, bankId, amount))
                    detailsToUpdate[txId] = Pair(cleanDest, newNote)
                }
            }

            val logCursor = db.rawQuery("""
                SELECT nl.$COL_LOG_AMOUNT, nl.$COL_LOG_TIMESTAMP, nl.$COL_LOG_DESTINATION, b.$COL_BANK_ID, nl.$COL_LOG_CONTENT, nl.$COL_LOG_TITLE
                FROM $TABLE_NOTIF_LOGS nl
                JOIN $TABLE_BANKS b ON LOWER(nl.$COL_LOG_TARGET_BANK) = LOWER(b.$COL_BANK_NAME)
                WHERE (
                    LOWER(nl.$COL_LOG_CONTENT) LIKE '%dari %' OR
                    LOWER(nl.$COL_LOG_TITLE) LIKE '%dari %' OR
                    LOWER(nl.$COL_LOG_CONTENT) LIKE '%menerima transfer%' OR
                    LOWER(nl.$COL_LOG_CONTENT) LIKE '%saldo masuk%' OR
                    LOWER(nl.$COL_LOG_CONTENT) LIKE '%pengembalian dana%' OR
                    LOWER(nl.$COL_LOG_TITLE) LIKE '%pengembalian dana%' OR
                    LOWER(nl.$COL_LOG_CONTENT) LIKE '%refund%' OR
                    LOWER(nl.$COL_LOG_TITLE) LIKE '%refund%'
                )
            """.trimIndent(), null)

            logCursor.use { cursor ->
                val amtIdx = cursor.getColumnIndexOrThrow(COL_LOG_AMOUNT)
                val tsIdx = cursor.getColumnIndexOrThrow(COL_LOG_TIMESTAMP)
                val destIdx = cursor.getColumnIndexOrThrow(COL_LOG_DESTINATION)
                val bIdIdx = cursor.getColumnIndexOrThrow(COL_BANK_ID)
                val contentIdx = cursor.getColumnIndexOrThrow(COL_LOG_CONTENT)
                val titleIdx = cursor.getColumnIndexOrThrow(COL_LOG_TITLE)

                while (cursor.moveToNext()) {
                    val amt = cursor.getDouble(amtIdx)
                    val ts = cursor.getLong(tsIdx)
                    val logDest = cursor.getString(destIdx) ?: ""
                    val bId = cursor.getLong(bIdIdx)
                    val logContent = (cursor.getString(contentIdx) ?: "").lowercase()
                    val logTitle = (cursor.getString(titleIdx) ?: "").lowercase()

                    val txCur = db.rawQuery("""
                        SELECT $COL_TX_ID FROM $TABLE_TRANSACTIONS
                        WHERE $COL_TX_BANK_ID = ? AND $COL_TX_TYPE = 'EXPENSE' AND $COL_TX_AMOUNT = ?
                        AND abs($COL_TX_TIMESTAMP - ?) <= 10000
                    """.trimIndent(), arrayOf(bId.toString(), amt.toString(), ts.toString()))

                    txCur.use { tc ->
                        if (tc.moveToFirst()) {
                            val txId = tc.getLong(0)
                            if (toRepair.none { it.first == txId }) {
                                var cleanDest = logDest.replace(Regex("^(?i)dari\\s+"), "").replace(Regex("^(?i)ke\\s+"), "").trim()
                                if (cleanDest.isEmpty() && (logContent.contains("pengembalian dana") || logTitle.contains("pengembalian dana") || logContent.contains("refund") || logTitle.contains("refund"))) {
                                    cleanDest = "Pengembalian Dana"
                                }
                                val newNote = if (cleanDest.isNotEmpty()) "From: $cleanDest" else "Reimburse"
                                toRepair.add(Triple(txId, bId, amt))
                                detailsToUpdate[txId] = Pair(cleanDest, newNote)
                            }
                        }
                    }
                }
            }

            for ((txId, bankId, amount) in toRepair) {
                val (newDest, newNote) = detailsToUpdate[txId] ?: Pair("", "Reimburse")

                db.execSQL("""
                    UPDATE $TABLE_TRANSACTIONS
                    SET $COL_TX_TYPE = 'INCOME',
                        $COL_TX_CATEGORY = 'Reimburse',
                        $COL_TX_NOTE = ?,
                        $COL_TX_DESTINATION = ?
                    WHERE $COL_TX_ID = ?
                """.trimIndent(), arrayOf(newNote, newDest, txId))

                db.execSQL("""
                    UPDATE $TABLE_BANKS
                    SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ?
                    WHERE $COL_BANK_ID = ?
                """.trimIndent(), arrayOf(amount * 2, bankId))
            }

            db.execSQL("""
                UPDATE $TABLE_NOTIF_LOGS
                SET $COL_LOG_DETECTED_TYPE = 'INCOME'
                WHERE (
                    LOWER($COL_LOG_CONTENT) LIKE '%dari %' OR
                    LOWER($COL_LOG_TITLE) LIKE '%dari %' OR
                    LOWER($COL_LOG_CONTENT) LIKE '%menerima transfer%' OR
                    LOWER($COL_LOG_CONTENT) LIKE '%saldo masuk%' OR
                    LOWER($COL_LOG_CONTENT) LIKE '%dana masuk%' OR
                    LOWER($COL_LOG_CONTENT) LIKE '%uang masuk%' OR
                    LOWER($COL_LOG_CONTENT) LIKE '%pengembalian dana%' OR
                    LOWER($COL_LOG_TITLE) LIKE '%pengembalian dana%' OR
                    LOWER($COL_LOG_CONTENT) LIKE '%refund%' OR
                    LOWER($COL_LOG_TITLE) LIKE '%refund%' OR
                    LOWER($COL_LOG_DESTINATION) LIKE 'dari %'
                ) AND $COL_LOG_DETECTED_TYPE != 'INCOME'
            """.trimIndent())

            try {
                val promoTxCursor = db.rawQuery("""
                    SELECT $COL_TX_ID, $COL_TX_BANK_ID, $COL_TX_AMOUNT
                    FROM $TABLE_TRANSACTIONS
                    WHERE $COL_TX_IS_AUTO_SCRAPED = 1 AND (
                        ($COL_TX_AMOUNT <= 10.0 AND (
                            LOWER($COL_TX_NOTE) LIKE '%paket%' OR
                            LOWER($COL_TX_NOTE) LIKE '%data%' OR
                            LOWER($COL_TX_NOTE) LIKE '%anime%' OR
                            LOWER($COL_TX_NOTE) LIKE '%klaim%' OR
                            LOWER($COL_TX_NOTE) LIKE '%promo%'
                        )) OR
                        LOWER($COL_TX_NOTE) LIKE '%nonton anime%' OR
                        LOWER($COL_TX_NOTE) LIKE '%klaim paket%'
                    )
                """.trimIndent(), null)

                promoTxCursor.use { ptc ->
                    val txIdIdx = ptc.getColumnIndexOrThrow(COL_TX_ID)
                    val bIdIdx = ptc.getColumnIndexOrThrow(COL_TX_BANK_ID)
                    val amtIdx = ptc.getColumnIndexOrThrow(COL_TX_AMOUNT)

                    while (ptc.moveToNext()) {
                        val tId = ptc.getLong(txIdIdx)
                        val bId = ptc.getLong(bIdIdx)
                        val amt = ptc.getDouble(amtIdx)

                        db.execSQL("""
                            UPDATE $TABLE_BANKS
                            SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ?
                            WHERE $COL_BANK_ID = ?
                        """.trimIndent(), arrayOf(amt, bId))

                        db.execSQL("DELETE FROM $TABLE_TRANSACTIONS WHERE $COL_TX_ID = ?", arrayOf(tId))
                    }
                }

                db.execSQL("""
                    UPDATE $TABLE_NOTIF_LOGS
                    SET $COL_LOG_STATUS = 'IGNORED (Promosi Diabaikan)',
                        $COL_LOG_DETECTED_TYPE = NULL
                    WHERE (
                        LOWER($COL_LOG_CONTENT) LIKE '%klaim%' OR
                        LOWER($COL_LOG_CONTENT) LIKE '%nonton anime%' OR
                        LOWER($COL_LOG_CONTENT) LIKE '%check-in%' OR
                        LOWER($COL_LOG_CONTENT) LIKE '%spesial buat kamu%' OR
                        (LOWER($COL_LOG_CONTENT) LIKE '%paket data rp1%')
                    )
                """.trimIndent())
            } catch (_: Exception) {}

            try {
                val dupTxCursor = db.rawQuery("""
                    SELECT t1.$COL_TX_ID, t1.$COL_TX_BANK_ID, t1.$COL_TX_AMOUNT, t1.$COL_TX_TYPE
                    FROM $TABLE_TRANSACTIONS t1
                    JOIN $TABLE_TRANSACTIONS t2 ON t1.$COL_TX_ID > t2.$COL_TX_ID
                        AND t1.$COL_TX_IS_AUTO_SCRAPED = 1 AND t2.$COL_TX_IS_AUTO_SCRAPED = 1
                        AND t1.$COL_TX_AMOUNT = t2.$COL_TX_AMOUNT
                        AND t1.$COL_TX_TYPE = t2.$COL_TX_TYPE
                        AND abs(t1.$COL_TX_TIMESTAMP - t2.$COL_TX_TIMESTAMP) <= 90000
                        AND (
                            t1.$COL_TX_BANK_ID = t2.$COL_TX_BANK_ID OR
                            (t1.$COL_TX_DESTINATION = t2.$COL_TX_DESTINATION AND t1.$COL_TX_DESTINATION IS NOT NULL) OR
                            (t1.$COL_TX_NOTE = t2.$COL_TX_NOTE AND t1.$COL_TX_NOTE IS NOT NULL)
                        )
                """.trimIndent(), null)

                dupTxCursor.use { dtc ->
                    val txIdIdx = dtc.getColumnIndexOrThrow(COL_TX_ID)
                    val bIdIdx = dtc.getColumnIndexOrThrow(COL_TX_BANK_ID)
                    val amtIdx = dtc.getColumnIndexOrThrow(COL_TX_AMOUNT)
                    val typeIdx = dtc.getColumnIndexOrThrow(COL_TX_TYPE)

                    while (dtc.moveToNext()) {
                        val dupId = dtc.getLong(txIdIdx)
                        val bId = dtc.getLong(bIdIdx)
                        val amt = dtc.getDouble(amtIdx)
                        val txType = dtc.getString(typeIdx)

                        if (txType == Transaction.TYPE_EXPENSE) {
                            db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?", arrayOf(amt, bId))
                        } else if (txType == Transaction.TYPE_INCOME) {
                            db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?", arrayOf(amt, bId))
                        }

                        db.execSQL("DELETE FROM $TABLE_TRANSACTIONS WHERE $COL_TX_ID = ?", arrayOf(dupId))
                    }
                }
            } catch (_: Exception) {}
        } catch (_: Exception) {}

        try {
            db.execSQL("""
                UPDATE $TABLE_TRANSACTIONS
                SET $COL_TX_CATEGORY = 'Reimburse'
                WHERE $COL_TX_TYPE = 'INCOME' AND $COL_TX_CATEGORY = 'Other Income'
            """.trimIndent())
        } catch (_: Exception) {}

        try {
            db.execSQL("DELETE FROM $TABLE_NOTIF_LOGS WHERE $COL_LOG_STATUS NOT LIKE 'PROCESSED%'")
        } catch (_: Exception) {}

        try {
            val dupCursor = db.rawQuery("""
                SELECT LOWER($COL_SHIELD_FRIEND), COUNT(*)
                FROM $TABLE_SHIELDS
                WHERE $COL_SHIELD_IS_SETTLED = 0
                GROUP BY LOWER($COL_SHIELD_FRIEND)
                HAVING COUNT(*) > 1
            """.trimIndent(), null)
            dupCursor.use { c ->
                while (c.moveToNext()) {
                    val lowerName = c.getString(0)
                    val subCur = db.rawQuery("""
                        SELECT $COL_SHIELD_ID, $COL_SHIELD_FRIEND, $COL_SHIELD_AMOUNT, $COL_SHIELD_NOTE
                        FROM $TABLE_SHIELDS
                        WHERE LOWER($COL_SHIELD_FRIEND) = ? AND $COL_SHIELD_IS_SETTLED = 0
                        ORDER BY $COL_SHIELD_TIMESTAMP DESC
                    """.trimIndent(), arrayOf(lowerName))
                    subCur.use { sc ->
                        if (sc.moveToFirst()) {
                            val keepId = sc.getLong(0)
                            var totalAmt = sc.getDouble(2)
                            val notes = mutableListOf<String>()
                            val n1 = sc.getString(3) ?: ""
                            if (n1.isNotBlank()) notes.add(n1.trim())
                            val toDeleteIds = mutableListOf<Long>()
                            while (sc.moveToNext()) {
                                toDeleteIds.add(sc.getLong(0))
                                totalAmt += sc.getDouble(2)
                                val n = sc.getString(3) ?: ""
                                if (n.isNotBlank()) notes.add(n.trim())
                            }
                            val combinedNote = notes.joinToString(", ")
                            db.execSQL("""
                                UPDATE $TABLE_SHIELDS
                                SET $COL_SHIELD_AMOUNT = ?, $COL_SHIELD_NOTE = ?
                                WHERE $COL_SHIELD_ID = ?
                            """.trimIndent(), arrayOf(totalAmt, combinedNote, keepId))
                            for (delId in toDeleteIds) {
                                db.execSQL("DELETE FROM $TABLE_SHIELDS WHERE $COL_SHIELD_ID = ?", arrayOf(delId))
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_SHIELD_HISTORY (
                    $COL_SHIELD_HIST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_SHIELD_HIST_SHIELD_ID INTEGER NOT NULL,
                    $COL_SHIELD_HIST_TX_ID INTEGER,
                    $COL_SHIELD_HIST_FRIEND TEXT NOT NULL,
                    $COL_SHIELD_HIST_AMOUNT REAL NOT NULL,
                    $COL_SHIELD_HIST_NOTE TEXT,
                    $COL_SHIELD_HIST_TIMESTAMP INTEGER NOT NULL
                )
            """.trimIndent())
        } catch (_: Exception) {}

        try {
            val backfillCur = db.rawQuery("""
                SELECT s.$COL_SHIELD_ID, s.$COL_SHIELD_TX_ID, s.$COL_SHIELD_FRIEND, s.$COL_SHIELD_AMOUNT, s.$COL_SHIELD_NOTE, s.$COL_SHIELD_TIMESTAMP
                FROM $TABLE_SHIELDS s
                WHERE NOT EXISTS (
                    SELECT 1 FROM $TABLE_SHIELD_HISTORY h WHERE h.$COL_SHIELD_HIST_SHIELD_ID = s.$COL_SHIELD_ID
                )
            """.trimIndent(), null)
            backfillCur.use { c ->
                while (c.moveToNext()) {
                    val cv = ContentValues().apply {
                        put(COL_SHIELD_HIST_SHIELD_ID, c.getLong(0))
                        if (!c.isNull(1)) put(COL_SHIELD_HIST_TX_ID, c.getLong(1))
                        put(COL_SHIELD_HIST_FRIEND, c.getString(2))
                        put(COL_SHIELD_HIST_AMOUNT, c.getDouble(3))
                        put(COL_SHIELD_HIST_NOTE, c.getString(4) ?: "")
                        put(COL_SHIELD_HIST_TIMESTAMP, c.getLong(5))
                    }
                    db.insert(TABLE_SHIELD_HISTORY, null, cv)
                }
            }
        } catch (_: Exception) {}
    }

    private fun insertInitialBanks(db: SQLiteDatabase) {
        val defaultBanks = listOf(
            Bank(name = "BCA", accountNumber = "5270119823", balance = 2500000.0, type = Bank.TYPE_BANK, colorHex = 0xFF00529CL, iconName = "bca", displayOrder = 0),
            Bank(name = "GoPay Clone", accountNumber = "081234567891", balance = 250000.0, type = Bank.TYPE_EWALLET, colorHex = 0xFF00838FL, iconName = "gopay", displayOrder = 1),
            Bank(name = "GoPay", accountNumber = "081234567890", balance = 350000.0, type = Bank.TYPE_EWALLET, colorHex = 0xFF00AED6L, iconName = "gopay", displayOrder = 2),
            Bank(name = "Bank BNI", accountNumber = "0234567890", balance = 0.0, type = Bank.TYPE_BANK, colorHex = 0xFFE05A10L, iconName = "bni", displayOrder = 3),
            Bank(name = "Bank CIMB", accountNumber = "701234567890", balance = 0.0, type = Bank.TYPE_BANK, colorHex = 0xFFED1C24L, iconName = "cimb", displayOrder = 4),
            Bank(name = "OctoPay", accountNumber = "081234567890", balance = 0.0, type = Bank.TYPE_EWALLET, colorHex = 0xFF8A1538L, iconName = "octopay", displayOrder = 5),
            Bank(name = "ShopeePay", accountNumber = "081234567890", balance = 150000.0, type = Bank.TYPE_EWALLET, colorHex = 0xFFEE4D2DL, iconName = "shopeepay", displayOrder = 6),
            Bank(name = "OVO", accountNumber = "081234567890", balance = 200000.0, type = Bank.TYPE_EWALLET, colorHex = 0xFF4C3494L, iconName = "ovo", displayOrder = 7),
            Bank(name = "Cash", accountNumber = "", balance = 500000.0, type = Bank.TYPE_CASH, colorHex = 0xFF10B981L, iconName = "cash", displayOrder = 8)
        )

        for (bank in defaultBanks) {
            val cv = ContentValues().apply {
                put(COL_BANK_NAME, bank.name)
                put(COL_BANK_ACCOUNT_NO, bank.accountNumber)
                put(COL_BANK_BALANCE, bank.balance)
                put(COL_BANK_TYPE, bank.type)
                put(COL_BANK_COLOR, bank.colorHex)
                put(COL_BANK_ICON, bank.iconName)
                put(COL_BANK_DISPLAY_ORDER, bank.displayOrder)
            }
            db.insert(TABLE_BANKS, null, cv)
        }
    }

    private fun insertInitialCategories(db: SQLiteDatabase) {
        val initial = TransactionCategory.defaultExpenseCategories + TransactionCategory.defaultIncomeCategories
        for (cat in initial) {
            val cv = ContentValues().apply {
                put(COL_CAT_NAME, cat.name)
                put(COL_CAT_ICON, cat.icon)
                put(COL_CAT_COLOR, cat.colorHex)
                put(COL_CAT_IS_EXPENSE, if (cat.isExpense) 1 else 0)
            }
            db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SHIELDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIF_LOGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BANKS")
        onCreate(db)
    }

    fun ensureShieldCategoryAndMigrate(db: SQLiteDatabase = writableDatabase) {
        try {
            val cursor = db.rawQuery("SELECT 1 FROM $TABLE_CATEGORIES WHERE LOWER($COL_CAT_NAME) = 'shield'", null)
            val exists = cursor.use { it.moveToFirst() }
            if (!exists) {
                val cv = ContentValues().apply {
                    put(COL_CAT_NAME, "Shield")
                    put(COL_CAT_ICON, "shield")
                    put(COL_CAT_COLOR, 0xFF10B981)
                    put(COL_CAT_IS_EXPENSE, 1)
                }
                db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
            }

            db.execSQL(
                """
                UPDATE $TABLE_TRANSACTIONS
                SET $COL_TX_CATEGORY = (
                    SELECT $COL_TX_EDIT_OLD_CAT FROM $TABLE_TX_EDIT_HISTORY
                    WHERE $COL_TX_EDIT_TX_ID = $TABLE_TRANSACTIONS.$COL_TX_ID
                      AND $COL_TX_EDIT_OLD_CAT IS NOT NULL
                      AND LOWER($COL_TX_EDIT_OLD_CAT) != 'shield'
                    ORDER BY $COL_TX_EDIT_ID ASC LIMIT 1
                )
                WHERE LOWER($COL_TX_CATEGORY) = 'shield' AND EXISTS (
                    SELECT 1 FROM $TABLE_TX_EDIT_HISTORY
                    WHERE $COL_TX_EDIT_TX_ID = $TABLE_TRANSACTIONS.$COL_TX_ID
                      AND $COL_TX_EDIT_OLD_CAT IS NOT NULL
                      AND LOWER($COL_TX_EDIT_OLD_CAT) != 'shield'
                )
                """.trimIndent()
            )
        } catch (_: Exception) {}
    }

    fun getAllBanks(): List<Bank> {
        val list = mutableListOf<Bank>()
        val db = readableDatabase
        val cursor: Cursor = db.query(TABLE_BANKS, null, null, null, null, null, "$COL_BANK_DISPLAY_ORDER ASC, $COL_BANK_ID ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToBank(it))
            }
        }
        return list
    }

    fun getBankById(id: Long): Bank? {
        val db = readableDatabase
        val cursor = db.query(TABLE_BANKS, null, "$COL_BANK_ID = ?", arrayOf(id.toString()), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToBank(it)
            }
        }
        return null
    }

    fun getBankByName(name: String): Bank? {
        val db = readableDatabase
        val cursor = db.query(TABLE_BANKS, null, "LOWER($COL_BANK_NAME) = LOWER(?)", arrayOf(name.trim()), null, null, null)
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToBank(it)
            }
        }

        if (name.equals("GoPay Clone", ignoreCase = true)) {
            val fallbackCursor = db.query(TABLE_BANKS, null, "LOWER($COL_BANK_NAME) IN ('gopay 2', 'gopay2', 'gopay_2')", null, null, null, null)
            fallbackCursor.use {
                if (it.moveToFirst()) {
                    return cursorToBank(it)
                }
            }
        }
        return null
    }

    fun insertBank(bank: Bank): Long {
        val db = writableDatabase
        val finalDisplayOrder = if (bank.displayOrder > 0) {
            bank.displayOrder
        } else {
            val maxCursor = db.rawQuery("SELECT MAX($COL_BANK_DISPLAY_ORDER) FROM $TABLE_BANKS", null)
            val maxOrder = if (maxCursor.moveToFirst() && !maxCursor.isNull(0)) maxCursor.getInt(0) else 0
            maxCursor.close()
            maxOrder + 1
        }
        val cv = ContentValues().apply {
            put(COL_BANK_NAME, bank.name)
            put(COL_BANK_ACCOUNT_NO, bank.accountNumber)
            put(COL_BANK_BALANCE, bank.balance)
            put(COL_BANK_TYPE, bank.type)
            put(COL_BANK_COLOR, bank.colorHex)
            put(COL_BANK_ICON, bank.iconName)
            put(COL_BANK_DISPLAY_ORDER, finalDisplayOrder)
        }
        return db.insert(TABLE_BANKS, null, cv)
    }

    fun updateBank(bank: Bank): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_BANK_NAME, bank.name)
            put(COL_BANK_ACCOUNT_NO, bank.accountNumber)
            put(COL_BANK_BALANCE, bank.balance)
            put(COL_BANK_TYPE, bank.type)
            put(COL_BANK_COLOR, bank.colorHex)
            put(COL_BANK_ICON, bank.iconName)
            put(COL_BANK_DISPLAY_ORDER, bank.displayOrder)
        }
        return db.update(TABLE_BANKS, cv, "$COL_BANK_ID = ?", arrayOf(bank.id.toString())) > 0
    }

    fun updateBankOrders(bankIdsInOrder: List<Long>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for ((index, bankId) in bankIdsInOrder.withIndex()) {
                val cv = ContentValues().apply {
                    put(COL_BANK_DISPLAY_ORDER, index)
                }
                db.update(TABLE_BANKS, cv, "$COL_BANK_ID = ?", arrayOf(bankId.toString()))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun migrateGopayCloneAndOrdering(db: SQLiteDatabase) {
        try {
            val prefs = context.getSharedPreferences("money_manager_migration", Context.MODE_PRIVATE)
            val alreadyProcessed = prefs.getBoolean("gopay_clone_second_position_v4", false)
            if (alreadyProcessed) return

            val gopay2Cursor = db.rawQuery(
                "SELECT $COL_BANK_ID, $COL_BANK_BALANCE FROM $TABLE_BANKS WHERE LOWER(TRIM($COL_BANK_NAME)) IN ('gopay 2', 'gopay2', 'gopay_2')",
                null
            )
            var gopay2Id: Long? = null
            var gopay2Balance = 0.0
            if (gopay2Cursor.moveToFirst()) {
                gopay2Id = gopay2Cursor.getLong(0)
                gopay2Balance = gopay2Cursor.getDouble(1)
            }
            gopay2Cursor.close()

            val cloneCursor = db.rawQuery(
                "SELECT $COL_BANK_ID, $COL_BANK_BALANCE FROM $TABLE_BANKS WHERE LOWER(TRIM($COL_BANK_NAME)) = 'gopay clone'",
                null
            )
            var cloneId: Long? = null
            if (cloneCursor.moveToFirst()) {
                cloneId = cloneCursor.getLong(0)
            }
            cloneCursor.close()

            if (gopay2Id != null) {
                if (cloneId != null) {
                    if (gopay2Balance != 0.0) {
                        db.execSQL(
                            "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                            arrayOf(gopay2Balance, cloneId)
                        )
                    }
                    db.execSQL(
                        "UPDATE $TABLE_TRANSACTIONS SET $COL_TX_BANK_ID = ? WHERE $COL_TX_BANK_ID = ?",
                        arrayOf(cloneId, gopay2Id)
                    )
                    db.execSQL(
                        "UPDATE $TABLE_TRANSACTIONS SET $COL_TX_TARGET_BANK_ID = ? WHERE $COL_TX_TARGET_BANK_ID = ?",
                        arrayOf(cloneId, gopay2Id)
                    )
                    try {
                        db.execSQL(
                            "UPDATE $TABLE_SHIELDS SET $COL_SHIELD_SETTLED_BANK_ID = ?, $COL_SHIELD_SETTLED_BANK_NAME = 'GoPay Clone' WHERE $COL_SHIELD_SETTLED_BANK_ID = ?",
                            arrayOf(cloneId, gopay2Id)
                        )
                    } catch (_: Exception) {}
                    db.execSQL("DELETE FROM $TABLE_BANKS WHERE $COL_BANK_ID = ?", arrayOf(gopay2Id))
                } else {
                    val cv = ContentValues().apply {
                        put(COL_BANK_NAME, "GoPay Clone")
                        put(COL_BANK_COLOR, 0xFF00838FL)
                        put(COL_BANK_ICON, "gopay")
                    }
                    db.update(TABLE_BANKS, cv, "$COL_BANK_ID = ?", arrayOf(gopay2Id.toString()))
                    cloneId = gopay2Id
                }
            }

            if (cloneId == null) {
                val cv = ContentValues().apply {
                    put(COL_BANK_NAME, "GoPay Clone")
                    put(COL_BANK_ACCOUNT_NO, "081234567891")
                    put(COL_BANK_BALANCE, 250000.0)
                    put(COL_BANK_TYPE, Bank.TYPE_EWALLET)
                    put(COL_BANK_COLOR, 0xFF00838FL)
                    put(COL_BANK_ICON, "gopay")
                    put(COL_BANK_DISPLAY_ORDER, 1)
                }
                cloneId = db.insert(TABLE_BANKS, null, cv)
            }

            val allCursor = db.rawQuery(
                "SELECT $COL_BANK_ID, $COL_BANK_NAME FROM $TABLE_BANKS ORDER BY $COL_BANK_ID ASC",
                null
            )
            val otherBankIds = mutableListOf<Long>()
            var foundCloneId: Long = cloneId
            allCursor.use {
                while (it.moveToNext()) {
                    val bId = it.getLong(0)
                    val bName = it.getString(1)
                    if (bName.equals("GoPay Clone", ignoreCase = true) || bId == cloneId) {
                        foundCloneId = bId
                    } else {
                        otherBankIds.add(bId)
                    }
                }
            }

            val finalOrderedIds = mutableListOf<Long>()
            if (otherBankIds.isNotEmpty()) {
                finalOrderedIds.add(otherBankIds[0])
                finalOrderedIds.add(foundCloneId)
                for (i in 1 until otherBankIds.size) {
                    finalOrderedIds.add(otherBankIds[i])
                }
            } else {
                finalOrderedIds.add(foundCloneId)
            }

            for ((idx, bId) in finalOrderedIds.withIndex()) {
                val cv = ContentValues().apply {
                    put(COL_BANK_DISPLAY_ORDER, idx)
                }
                db.update(TABLE_BANKS, cv, "$COL_BANK_ID = ?", arrayOf(bId.toString()))
            }

            prefs.edit().putBoolean("gopay_clone_second_position_v4", true).apply()
        } catch (_: Exception) {}
    }

    fun deleteBank(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_BANKS, "$COL_BANK_ID = ?", arrayOf(id.toString())) > 0
    }

    fun updateBankBalance(bankId: Long, newBalance: Double): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_BANK_BALANCE, newBalance)
        }
        return db.update(TABLE_BANKS, cv, "$COL_BANK_ID = ?", arrayOf(bankId.toString())) > 0
    }

    fun insertTransaction(tx: Transaction): Long {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(COL_TX_BANK_ID, tx.bankId)
                put(COL_TX_TARGET_BANK_ID, tx.targetBankId)
                put(COL_TX_TYPE, tx.type)
                put(COL_TX_AMOUNT, tx.amount)
                put(COL_TX_CATEGORY, tx.category)
                put(COL_TX_NOTE, tx.note)
                put(COL_TX_TIMESTAMP, tx.timestamp)
                put(COL_TX_IS_AUTO_SCRAPED, if (tx.isAutoScraped) 1 else 0)
                put(COL_TX_SOURCE_APP, tx.sourceApp)
                put(COL_TX_DESTINATION, tx.destination)
            }
            val id = db.insert(TABLE_TRANSACTIONS, null, cv)
            if (id == -1L) return -1L

            when (tx.type) {
                Transaction.TYPE_EXPENSE -> {
                    db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                        arrayOf(tx.amount, tx.bankId))
                }
                Transaction.TYPE_INCOME -> {
                    db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                        arrayOf(tx.amount, tx.bankId))
                }
                Transaction.TYPE_TRANSFER -> {
                    db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                        arrayOf(tx.amount, tx.bankId))
                    if (tx.targetBankId != null) {
                        db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                            arrayOf(tx.amount, tx.targetBankId))
                    }
                }
            }
            db.setTransactionSuccessful()
            return id
        } finally {
            db.endTransaction()
        }
    }

    fun deleteTransaction(tx: Transaction): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val deleted = db.delete(TABLE_TRANSACTIONS, "$COL_TX_ID = ?", arrayOf(tx.id.toString())) > 0
            if (deleted) {

                when (tx.type) {
                    Transaction.TYPE_EXPENSE -> {
                        db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                            arrayOf(tx.amount, tx.bankId))
                    }
                    Transaction.TYPE_INCOME -> {
                        db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                            arrayOf(tx.amount, tx.bankId))
                    }
                    Transaction.TYPE_TRANSFER -> {
                        db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                            arrayOf(tx.amount, tx.bankId))
                        if (tx.targetBankId != null) {
                            db.execSQL("UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                                arrayOf(tx.amount, tx.targetBankId))
                        }
                    }
                }

                deleteShieldsForTransaction(db, tx.id)
                db.delete(TABLE_TX_EDIT_HISTORY, "$COL_TX_EDIT_TX_ID = ?", arrayOf(tx.id.toString()))

                db.setTransactionSuccessful()
            }
            return deleted
        } finally {
            db.endTransaction()
        }
    }

    fun updateTransaction(oldTx: Transaction, newTx: Transaction): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {

            when (oldTx.type) {
                Transaction.TYPE_EXPENSE -> {
                    db.execSQL(
                        "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                        arrayOf(oldTx.amount, oldTx.bankId)
                    )
                }
                Transaction.TYPE_INCOME -> {
                    db.execSQL(
                        "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                        arrayOf(oldTx.amount, oldTx.bankId)
                    )
                }
                Transaction.TYPE_TRANSFER -> {
                    db.execSQL(
                        "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                        arrayOf(oldTx.amount, oldTx.bankId)
                    )
                    if (oldTx.targetBankId != null) {
                        db.execSQL(
                            "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                            arrayOf(oldTx.amount, oldTx.targetBankId)
                        )
                    }
                }
            }

            when (newTx.type) {
                Transaction.TYPE_EXPENSE -> {
                    db.execSQL(
                        "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                        arrayOf(newTx.amount, newTx.bankId)
                    )
                }
                Transaction.TYPE_INCOME -> {
                    db.execSQL(
                        "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                        arrayOf(newTx.amount, newTx.bankId)
                    )
                }
                Transaction.TYPE_TRANSFER -> {
                    db.execSQL(
                        "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE - ? WHERE $COL_BANK_ID = ?",
                        arrayOf(newTx.amount, newTx.bankId)
                    )
                    if (newTx.targetBankId != null) {
                        db.execSQL(
                            "UPDATE $TABLE_BANKS SET $COL_BANK_BALANCE = $COL_BANK_BALANCE + ? WHERE $COL_BANK_ID = ?",
                            arrayOf(newTx.amount, newTx.targetBankId)
                        )
                    }
                }
            }

            val cv = ContentValues().apply {
                put(COL_TX_BANK_ID, newTx.bankId)
                put(COL_TX_TARGET_BANK_ID, newTx.targetBankId)
                put(COL_TX_TYPE, newTx.type)
                put(COL_TX_AMOUNT, newTx.amount)
                put(COL_TX_CATEGORY, newTx.category)
                put(COL_TX_NOTE, newTx.note)
                put(COL_TX_TIMESTAMP, newTx.timestamp)
                put(COL_TX_DESTINATION, newTx.destination)
            }
            val rows = db.update(TABLE_TRANSACTIONS, cv, "$COL_TX_ID = ?", arrayOf(oldTx.id.toString()))
            if (rows > 0) {

                val oldBankName = getBankById(oldTx.bankId)?.name ?: oldTx.bankName
                val newBankName = getBankById(newTx.bankId)?.name ?: newTx.bankName
                val diff = newTx.amount - oldTx.amount
                val changes = mutableListOf<String>()
                val fmt = java.text.NumberFormat.getNumberInstance(java.util.Locale.US)

                if (oldTx.amount != newTx.amount) {
                    val diffStr = if (diff > 0) "+Rp ${fmt.format(diff)}" else "-Rp ${fmt.format(-diff)}"
                    changes.add("Nominal: Rp ${fmt.format(oldTx.amount)} → Rp ${fmt.format(newTx.amount)} ($diffStr)")
                }
                if (oldTx.category != newTx.category) {
                    changes.add("Kategori: ${oldTx.category} → ${newTx.category}")
                }
                if (oldTx.bankId != newTx.bankId) {
                    changes.add("Wallet: $oldBankName → $newBankName")
                }
                if (oldTx.targetBankId != newTx.targetBankId) {
                    val oldTarget = oldTx.targetBankId?.let { getBankById(it)?.name } ?: "-"
                    val newTarget = newTx.targetBankId?.let { getBankById(it)?.name } ?: "-"
                    changes.add("Target: $oldTarget → $newTarget")
                }
                if (oldTx.type != newTx.type) {
                    changes.add("Tipe: ${oldTx.type} → ${newTx.type}")
                }
                if (oldTx.note.trim() != newTx.note.trim()) {
                    changes.add("Catatan diubah")
                }
                if (oldTx.destination?.trim() != newTx.destination?.trim()) {
                    changes.add("Tujuan/Penerima diubah")
                }

                if (changes.isNotEmpty()) {
                    val summary = changes.joinToString("; ")
                    val editCv = ContentValues().apply {
                        put(COL_TX_EDIT_TX_ID, oldTx.id)
                        put(COL_TX_EDIT_TIMESTAMP, System.currentTimeMillis())
                        put(COL_TX_EDIT_OLD_AMOUNT, oldTx.amount)
                        put(COL_TX_EDIT_NEW_AMOUNT, newTx.amount)
                        put(COL_TX_EDIT_DIFF_AMOUNT, diff)
                        put(COL_TX_EDIT_OLD_CAT, oldTx.category)
                        put(COL_TX_EDIT_NEW_CAT, newTx.category)
                        put(COL_TX_EDIT_OLD_BANK, oldBankName)
                        put(COL_TX_EDIT_NEW_BANK, newBankName)
                        put(COL_TX_EDIT_OLD_NOTE, oldTx.note)
                        put(COL_TX_EDIT_NEW_NOTE, newTx.note)
                        put(COL_TX_EDIT_SUMMARY, summary)
                    }
                    db.insert(TABLE_TX_EDIT_HISTORY, null, editCv)
                }

                db.setTransactionSuccessful()
                return true
            }
            return false
        } finally {
            db.endTransaction()
        }
    }

    fun getAllTransactions(): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase
        val query = """
            SELECT t.*, b1.$COL_BANK_NAME AS source_bank_name, b2.$COL_BANK_NAME AS target_bank_name
            FROM $TABLE_TRANSACTIONS t
            LEFT JOIN $TABLE_BANKS b1 ON t.$COL_TX_BANK_ID = b1.$COL_BANK_ID
            LEFT JOIN $TABLE_BANKS b2 ON t.$COL_TX_TARGET_BANK_ID = b2.$COL_BANK_ID
            ORDER BY t.$COL_TX_TIMESTAMP DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToTransaction(it))
            }
        }
        return list
    }

    fun getFilteredTransactions(
        bankId: Long? = null,
        startTimeMillis: Long? = null,
        endTimeMillis: Long? = null,
        type: String? = null
    ): List<Transaction> {
        val list = mutableListOf<Transaction>()
        val db = readableDatabase

        val conditions = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (bankId != null) {
            conditions.add("(t.$COL_TX_BANK_ID = ? OR t.$COL_TX_TARGET_BANK_ID = ?)")
            args.add(bankId.toString())
            args.add(bankId.toString())
        }

        if (startTimeMillis != null) {
            conditions.add("t.$COL_TX_TIMESTAMP >= ?")
            args.add(startTimeMillis.toString())
        }

        if (endTimeMillis != null) {
            conditions.add("t.$COL_TX_TIMESTAMP <= ?")
            args.add(endTimeMillis.toString())
        }

        if (type != null && type != "ALL") {
            conditions.add("t.$COL_TX_TYPE = ?")
            args.add(type)
        }

        val whereClause = if (conditions.isNotEmpty()) "WHERE " + conditions.joinToString(" AND ") else ""

        val query = """
            SELECT t.*, b1.$COL_BANK_NAME AS source_bank_name, b2.$COL_BANK_NAME AS target_bank_name
            FROM $TABLE_TRANSACTIONS t
            LEFT JOIN $TABLE_BANKS b1 ON t.$COL_TX_BANK_ID = b1.$COL_BANK_ID
            LEFT JOIN $TABLE_BANKS b2 ON t.$COL_TX_TARGET_BANK_ID = b2.$COL_BANK_ID
            $whereClause
            ORDER BY t.$COL_TX_TIMESTAMP DESC
        """.trimIndent()

        val cursor = db.rawQuery(query, if (args.isNotEmpty()) args.toTypedArray() else null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToTransaction(it))
            }
        }
        return list
    }

    fun getEditHistoryForTransaction(txId: Long): List<TransactionEditHistory> {
        val list = mutableListOf<TransactionEditHistory>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TX_EDIT_HISTORY,
            null,
            "$COL_TX_EDIT_TX_ID = ?",
            arrayOf(txId.toString()),
            null,
            null,
            "$COL_TX_EDIT_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TransactionEditHistory(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_TX_EDIT_ID)),
                        transactionId = it.getLong(it.getColumnIndexOrThrow(COL_TX_EDIT_TX_ID)),
                        editTimestamp = it.getLong(it.getColumnIndexOrThrow(COL_TX_EDIT_TIMESTAMP)),
                        oldAmount = it.getDouble(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_AMOUNT)),
                        newAmount = it.getDouble(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_AMOUNT)),
                        diffAmount = it.getDouble(it.getColumnIndexOrThrow(COL_TX_EDIT_DIFF_AMOUNT)),
                        oldCategory = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_CAT)),
                        newCategory = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_CAT)),
                        oldBankName = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_BANK)),
                        newBankName = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_BANK)),
                        oldNote = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_NOTE)),
                        newNote = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_NOTE)),
                        changeSummary = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_SUMMARY))
                    )
                )
            }
        }
        return list
    }

    fun getAllEditHistories(): Map<Long, List<TransactionEditHistory>> {
        val map = mutableMapOf<Long, MutableList<TransactionEditHistory>>()
        val db = readableDatabase
        try {
            val cursor = db.query(
                TABLE_TX_EDIT_HISTORY,
                null,
                null,
                null,
                null,
                null,
                "$COL_TX_EDIT_TIMESTAMP DESC"
            )
            cursor.use {
                while (it.moveToNext()) {
                    val item = TransactionEditHistory(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_TX_EDIT_ID)),
                        transactionId = it.getLong(it.getColumnIndexOrThrow(COL_TX_EDIT_TX_ID)),
                        editTimestamp = it.getLong(it.getColumnIndexOrThrow(COL_TX_EDIT_TIMESTAMP)),
                        oldAmount = it.getDouble(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_AMOUNT)),
                        newAmount = it.getDouble(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_AMOUNT)),
                        diffAmount = it.getDouble(it.getColumnIndexOrThrow(COL_TX_EDIT_DIFF_AMOUNT)),
                        oldCategory = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_CAT)),
                        newCategory = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_CAT)),
                        oldBankName = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_BANK)),
                        newBankName = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_BANK)),
                        oldNote = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_OLD_NOTE)),
                        newNote = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_NEW_NOTE)),
                        changeSummary = it.getString(it.getColumnIndexOrThrow(COL_TX_EDIT_SUMMARY))
                    )
                    map.getOrPut(item.transactionId) { mutableListOf() }.add(item)
                }
            }
        } catch (_: Exception) {}
        return map
    }

    fun insertNotificationLog(log: ScrapedNotificationLog): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_LOG_PKG, log.packageName)
            put(COL_LOG_APP, log.appName)
            put(COL_LOG_TITLE, log.title)
            put(COL_LOG_CONTENT, log.content)
            put(COL_LOG_TIMESTAMP, log.timestamp)
            put(COL_LOG_STATUS, log.status)
            put(COL_LOG_AMOUNT, log.amount)
            put(COL_LOG_DETECTED_TYPE, log.detectedType)
            put(COL_LOG_TARGET_BANK, log.targetBank)
            put(COL_LOG_DESTINATION, log.destination)
        }
        return db.insert(TABLE_NOTIF_LOGS, null, cv)
    }

    fun getAllNotificationLogs(onlyProcessed: Boolean = true): List<ScrapedNotificationLog> {
        val list = mutableListOf<ScrapedNotificationLog>()
        val db = readableDatabase
        val selection = if (onlyProcessed) "$COL_LOG_STATUS LIKE ?" else null
        val selectionArgs = if (onlyProcessed) arrayOf("PROCESSED%") else null
        val cursor = db.query(TABLE_NOTIF_LOGS, null, selection, selectionArgs, null, null, "$COL_LOG_TIMESTAMP DESC", "100")
        cursor.use {
            while (it.moveToNext()) {
                val destIdx = it.getColumnIndex(COL_LOG_DESTINATION)
                val destination = if (destIdx >= 0 && !it.isNull(destIdx)) it.getString(destIdx) else null

                list.add(
                    ScrapedNotificationLog(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_LOG_ID)),
                        packageName = it.getString(it.getColumnIndexOrThrow(COL_LOG_PKG)) ?: "",
                        appName = it.getString(it.getColumnIndexOrThrow(COL_LOG_APP)) ?: "",
                        title = it.getString(it.getColumnIndexOrThrow(COL_LOG_TITLE)) ?: "",
                        content = it.getString(it.getColumnIndexOrThrow(COL_LOG_CONTENT)) ?: "",
                        timestamp = it.getLong(it.getColumnIndexOrThrow(COL_LOG_TIMESTAMP)),
                        status = it.getString(it.getColumnIndexOrThrow(COL_LOG_STATUS)) ?: "",
                        amount = if (!it.isNull(it.getColumnIndexOrThrow(COL_LOG_AMOUNT))) it.getDouble(it.getColumnIndexOrThrow(COL_LOG_AMOUNT)) else null,
                        detectedType = it.getString(it.getColumnIndexOrThrow(COL_LOG_DETECTED_TYPE)),
                        targetBank = it.getString(it.getColumnIndexOrThrow(COL_LOG_TARGET_BANK)),
                        destination = destination
                    )
                )
            }
        }
        return list
    }

    fun deleteDebugNotificationLogs(): Int {
        val db = writableDatabase
        return db.delete(TABLE_NOTIF_LOGS, "$COL_LOG_STATUS NOT LIKE ?", arrayOf("PROCESSED%"))
    }

    fun clearNotificationLogs(): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_NOTIF_LOGS, null, null) > 0
    }

    fun setOrUpdateBudget(category: String, monthlyLimit: Double, monthYear: String): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_BUDGET_CATEGORY, category)
            put(COL_BUDGET_LIMIT, monthlyLimit)
            put(COL_BUDGET_MONTH_YEAR, monthYear)
        }
        return db.insertWithOnConflict(TABLE_BUDGETS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun getBudgets(monthYear: String): List<Budget> {
        val db = readableDatabase
        val list = mutableListOf<Budget>()
        val cursor = db.query(
            TABLE_BUDGETS,
            null,
            "$COL_BUDGET_MONTH_YEAR = ?",
            arrayOf(monthYear),
            null,
            null,
            "$COL_BUDGET_LIMIT DESC"
        )
        cursor.use {
            val idIdx = it.getColumnIndexOrThrow(COL_BUDGET_ID)
            val catIdx = it.getColumnIndexOrThrow(COL_BUDGET_CATEGORY)
            val limitIdx = it.getColumnIndexOrThrow(COL_BUDGET_LIMIT)
            val myIdx = it.getColumnIndexOrThrow(COL_BUDGET_MONTH_YEAR)
            while (it.moveToNext()) {
                list.add(
                    Budget(
                        id = it.getLong(idIdx),
                        category = it.getString(catIdx),
                        monthlyLimit = it.getDouble(limitIdx),
                        monthYear = it.getString(myIdx)
                    )
                )
            }
        }
        return list
    }

    fun deleteBudget(id: Long): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_BUDGETS, "$COL_BUDGET_ID = ?", arrayOf(id.toString())) > 0
    }

    fun getAvailableBudgetMonths(): List<String> {
        val list = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT $COL_BUDGET_MONTH_YEAR FROM $TABLE_BUDGETS ORDER BY $COL_BUDGET_MONTH_YEAR DESC",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getString(0))
            }
        }
        return list
    }

    fun importBudgetsFromMonth(fromMonthYear: String, toMonthYear: String, replaceAll: Boolean = false): Int {
        val db = writableDatabase
        val sourceBudgets = getBudgets(fromMonthYear)
        if (sourceBudgets.isEmpty()) return 0

        db.beginTransaction()
        try {
            if (replaceAll) {
                db.delete(TABLE_BUDGETS, "$COL_BUDGET_MONTH_YEAR = ?", arrayOf(toMonthYear))
            }
            var count = 0
            for (budget in sourceBudgets) {
                val cv = ContentValues().apply {
                    put(COL_BUDGET_CATEGORY, budget.category)
                    put(COL_BUDGET_LIMIT, budget.monthlyLimit)
                    put(COL_BUDGET_MONTH_YEAR, toMonthYear)
                }
                val res = db.insertWithOnConflict(TABLE_BUDGETS, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                if (res != -1L) count++
            }
            db.setTransactionSuccessful()
            return count
        } finally {
            db.endTransaction()
        }
    }

    fun getAllCategories(): List<TransactionCategory> {
        ensureShieldCategoryAndMigrate()
        val list = mutableListOf<TransactionCategory>()
        val db = readableDatabase
        val cursor = db.query(TABLE_CATEGORIES, null, null, null, null, null, "$COL_CAT_ID ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    TransactionCategory(
                        id = it.getLong(it.getColumnIndexOrThrow(COL_CAT_ID)),
                        name = it.getString(it.getColumnIndexOrThrow(COL_CAT_NAME)),
                        icon = it.getString(it.getColumnIndexOrThrow(COL_CAT_ICON)),
                        colorHex = it.getLong(it.getColumnIndexOrThrow(COL_CAT_COLOR)),
                        isExpense = it.getInt(it.getColumnIndexOrThrow(COL_CAT_IS_EXPENSE)) == 1
                    )
                )
            }
        }
        return list
    }

    fun insertCategory(cat: TransactionCategory): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_CAT_NAME, cat.name.trim())
            put(COL_CAT_ICON, cat.icon)
            put(COL_CAT_COLOR, cat.colorHex)
            put(COL_CAT_IS_EXPENSE, if (cat.isExpense) 1 else 0)
        }
        return db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun updateCategory(oldName: String, updated: TransactionCategory): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val cv = ContentValues().apply {
                put(COL_CAT_NAME, updated.name.trim())
                put(COL_CAT_ICON, updated.icon)
                put(COL_CAT_COLOR, updated.colorHex)
                put(COL_CAT_IS_EXPENSE, if (updated.isExpense) 1 else 0)
            }
            var rows = db.update(TABLE_CATEGORIES, cv, "LOWER($COL_CAT_NAME) = LOWER(?)", arrayOf(oldName.trim()))
            if (rows == 0) {

                db.insertWithOnConflict(TABLE_CATEGORIES, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
                rows = 1
            }

            if (!oldName.trim().equals(updated.name.trim(), ignoreCase = true)) {
                db.execSQL(
                    "UPDATE $TABLE_TRANSACTIONS SET $COL_TX_CATEGORY = ? WHERE LOWER($COL_TX_CATEGORY) = LOWER(?)",
                    arrayOf(updated.name.trim(), oldName.trim())
                )
                db.execSQL(
                    "UPDATE $TABLE_BUDGETS SET $COL_BUDGET_CATEGORY = ? WHERE LOWER($COL_BUDGET_CATEGORY) = LOWER(?)",
                    arrayOf(updated.name.trim(), oldName.trim())
                )
            }
            db.setTransactionSuccessful()
            return rows > 0
        } finally {
            db.endTransaction()
        }
    }

    fun deleteCategory(name: String): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("DELETE FROM $TABLE_BUDGETS WHERE LOWER($COL_BUDGET_CATEGORY) = LOWER(?)", arrayOf(name.trim()))
            val rows = db.delete(TABLE_CATEGORIES, "LOWER($COL_CAT_NAME) = LOWER(?)", arrayOf(name.trim()))
            db.setTransactionSuccessful()
            return rows > 0
        } finally {
            db.endTransaction()
        }
    }

    private fun cursorToBank(c: Cursor): Bank {
        val displayOrderIdx = c.getColumnIndex(COL_BANK_DISPLAY_ORDER)
        val displayOrder = if (displayOrderIdx != -1 && !c.isNull(displayOrderIdx)) c.getInt(displayOrderIdx) else 0
        return Bank(
            id = c.getLong(c.getColumnIndexOrThrow(COL_BANK_ID)),
            name = c.getString(c.getColumnIndexOrThrow(COL_BANK_NAME)),
            accountNumber = c.getString(c.getColumnIndexOrThrow(COL_BANK_ACCOUNT_NO)) ?: "",
            balance = c.getDouble(c.getColumnIndexOrThrow(COL_BANK_BALANCE)),
            type = c.getString(c.getColumnIndexOrThrow(COL_BANK_TYPE)),
            colorHex = c.getLong(c.getColumnIndexOrThrow(COL_BANK_COLOR)),
            iconName = c.getString(c.getColumnIndexOrThrow(COL_BANK_ICON)),
            displayOrder = displayOrder
        )
    }

    private fun cursorToTransaction(c: Cursor): Transaction {
        val targetBankIdIdx = c.getColumnIndexOrThrow(COL_TX_TARGET_BANK_ID)
        val targetBankId = if (!c.isNull(targetBankIdIdx)) c.getLong(targetBankIdIdx) else null
        val sourceBankName = c.getString(c.getColumnIndexOrThrow("source_bank_name")) ?: "Bank #${c.getLong(c.getColumnIndexOrThrow(COL_TX_BANK_ID))}"
        val targetBankName = if (targetBankId != null) c.getString(c.getColumnIndexOrThrow("target_bank_name")) else null

        val destIdx = c.getColumnIndex(COL_TX_DESTINATION)
        val destination = if (destIdx >= 0 && !c.isNull(destIdx)) c.getString(destIdx) else null

        return Transaction(
            id = c.getLong(c.getColumnIndexOrThrow(COL_TX_ID)),
            bankId = c.getLong(c.getColumnIndexOrThrow(COL_TX_BANK_ID)),
            bankName = sourceBankName,
            targetBankId = targetBankId,
            targetBankName = targetBankName,
            type = c.getString(c.getColumnIndexOrThrow(COL_TX_TYPE)),
            amount = c.getDouble(c.getColumnIndexOrThrow(COL_TX_AMOUNT)),
            category = c.getString(c.getColumnIndexOrThrow(COL_TX_CATEGORY)),
            note = c.getString(c.getColumnIndexOrThrow(COL_TX_NOTE)) ?: "",
            timestamp = c.getLong(c.getColumnIndexOrThrow(COL_TX_TIMESTAMP)),
            isAutoScraped = c.getInt(c.getColumnIndexOrThrow(COL_TX_IS_AUTO_SCRAPED)) == 1,
            sourceApp = c.getString(c.getColumnIndexOrThrow(COL_TX_SOURCE_APP)),
            destination = destination
        )
    }

    fun getAllShields(): List<ShieldEntry> {
        val list = mutableListOf<ShieldEntry>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SHIELDS, null, null, null, null, null, "$COL_SHIELD_TIMESTAMP DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToShield(it))
            }
        }
        return list
    }

    fun getShieldsByTransactionId(txId: Long): List<ShieldEntry> {
        val list = mutableListOf<ShieldEntry>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SHIELDS,
            null,
            "$COL_SHIELD_TX_ID = ?",
            arrayOf(txId.toString()),
            null,
            null,
            "$COL_SHIELD_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToShield(it))
            }
        }
        return list
    }

    fun getActiveShieldByFriendName(friendName: String): ShieldEntry? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SHIELDS,
            null,
            "LOWER($COL_SHIELD_FRIEND) = LOWER(?) AND $COL_SHIELD_IS_SETTLED = 0",
            arrayOf(friendName.trim()),
            null,
            null,
            "$COL_SHIELD_TIMESTAMP DESC",
            "1"
        )
        cursor.use {
            if (it.moveToFirst()) {
                return cursorToShield(it)
            }
        }
        return null
    }

    fun insertOrAccumulateShield(entry: ShieldEntry): Long {
        val pastAvatar = getAvatarByFriendName(entry.friendName)
        val resolvedAvatar = entry.avatarUri ?: pastAvatar
        val entryWithAvatar = entry.copy(avatarUri = resolvedAvatar)

        saveOrUpdateShieldContact(entry.friendName, resolvedAvatar)

        val existing = getActiveShieldByFriendName(entry.friendName)
        val shieldId: Long
        if (existing != null) {
            val combinedAmount = existing.amount + entry.amount
            val combinedNote = when {
                existing.note.isNotBlank() && entry.note.isNotBlank() -> "${existing.note}, ${entry.note}"
                entry.note.isNotBlank() -> entry.note
                else -> existing.note
            }
            val combinedAvatar = resolvedAvatar ?: existing.avatarUri
            val updated = existing.copy(
                amount = combinedAmount,
                note = combinedNote,
                timestamp = entry.timestamp,
                avatarUri = combinedAvatar
            )
            updateShield(updated)
            shieldId = existing.id
        } else {
            shieldId = insertShield(entryWithAvatar)
        }

        if (shieldId > 0) {
            insertShieldHistory(
                ShieldHistoryItem(
                    shieldId = shieldId,
                    transactionId = entry.transactionId,
                    friendName = entry.friendName.trim(),
                    amount = entry.amount,
                    note = entry.note.trim(),
                    timestamp = entry.timestamp
                )
            )
        }
        return shieldId
    }

    fun insertShield(entry: ShieldEntry): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            if (entry.transactionId != null) put(COL_SHIELD_TX_ID, entry.transactionId)
            put(COL_SHIELD_FRIEND, entry.friendName.trim())
            put(COL_SHIELD_AMOUNT, entry.amount)
            put(COL_SHIELD_NOTE, entry.note.trim())
            put(COL_SHIELD_TIMESTAMP, entry.timestamp)
            put(COL_SHIELD_IS_SETTLED, if (entry.isSettled) 1 else 0)
            if (entry.settledAt != null) put(COL_SHIELD_SETTLED_AT, entry.settledAt)
            if (entry.settledBankId != null) put(COL_SHIELD_SETTLED_BANK_ID, entry.settledBankId)
            if (entry.settledBankName != null) put(COL_SHIELD_SETTLED_BANK_NAME, entry.settledBankName)
            if (entry.avatarUri != null) put(COL_SHIELD_AVATAR_URI, entry.avatarUri)
        }
        return db.insert(TABLE_SHIELDS, null, cv)
    }

    fun updateShield(entry: ShieldEntry): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            if (entry.transactionId != null) put(COL_SHIELD_TX_ID, entry.transactionId) else putNull(COL_SHIELD_TX_ID)
            put(COL_SHIELD_FRIEND, entry.friendName.trim())
            put(COL_SHIELD_AMOUNT, entry.amount)
            put(COL_SHIELD_NOTE, entry.note.trim())
            put(COL_SHIELD_TIMESTAMP, entry.timestamp)
            put(COL_SHIELD_IS_SETTLED, if (entry.isSettled) 1 else 0)
            if (entry.settledAt != null) put(COL_SHIELD_SETTLED_AT, entry.settledAt) else putNull(COL_SHIELD_SETTLED_AT)
            if (entry.settledBankId != null) put(COL_SHIELD_SETTLED_BANK_ID, entry.settledBankId) else putNull(COL_SHIELD_SETTLED_BANK_ID)
            if (entry.settledBankName != null) put(COL_SHIELD_SETTLED_BANK_NAME, entry.settledBankName) else putNull(COL_SHIELD_SETTLED_BANK_NAME)
            if (entry.avatarUri != null) put(COL_SHIELD_AVATAR_URI, entry.avatarUri) else putNull(COL_SHIELD_AVATAR_URI)
        }
        val ok = db.update(TABLE_SHIELDS, cv, "$COL_SHIELD_ID = ?", arrayOf(entry.id.toString())) > 0
        if (ok) {
            try {
                db.execSQL(
                    "UPDATE $TABLE_SHIELD_HISTORY SET $COL_SHIELD_HIST_FRIEND = ? WHERE $COL_SHIELD_HIST_SHIELD_ID = ?",
                    arrayOf(entry.friendName.trim(), entry.id)
                )
                saveOrUpdateShieldContact(entry.friendName, entry.avatarUri)
            } catch (_: Exception) {}
        }
        return ok
    }

    fun updateShieldAvatar(shieldId: Long, friendName: String, avatarUri: String?): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            if (avatarUri != null) {
                put(COL_SHIELD_AVATAR_URI, avatarUri)
            } else {
                putNull(COL_SHIELD_AVATAR_URI)
            }
        }
        val byName = db.update(TABLE_SHIELDS, cv, "LOWER($COL_SHIELD_FRIEND) = LOWER(?)", arrayOf(friendName.trim()))
        val byId = db.update(TABLE_SHIELDS, cv, "$COL_SHIELD_ID = ?", arrayOf(shieldId.toString()))

        try {
            saveOrUpdateShieldContact(friendName, avatarUri)
        } catch (_: Exception) {}

        return byName > 0 || byId > 0
    }

    fun getAllShieldContacts(): List<ShieldContact> {
        val db = readableDatabase
        val list = mutableListOf<ShieldContact>()
        try {
            val cursor = db.rawQuery(
                "SELECT $COL_CONTACT_ID, $COL_CONTACT_NAME, $COL_CONTACT_AVATAR FROM $TABLE_SHIELD_CONTACTS ORDER BY $COL_CONTACT_NAME COLLATE NOCASE ASC",
                null
            )
            cursor.use {
                while (it.moveToNext()) {
                    list.add(
                        ShieldContact(
                            id = it.getLong(0),
                            name = it.getString(1),
                            avatarUri = it.getString(2)
                        )
                    )
                }
            }
        } catch (_: Exception) {}

        try {
            val sCursor = db.rawQuery(
                "SELECT DISTINCT $COL_SHIELD_FRIEND, $COL_SHIELD_AVATAR_URI FROM $TABLE_SHIELDS WHERE $COL_SHIELD_FRIEND IS NOT NULL AND TRIM($COL_SHIELD_FRIEND) != ''",
                null
            )
            sCursor.use {
                while (it.moveToNext()) {
                    val fName = it.getString(0).trim()
                    val fAvatar = it.getString(1)
                    if (list.none { c -> c.name.equals(fName, ignoreCase = true) }) {
                        list.add(ShieldContact(name = fName, avatarUri = fAvatar))
                    }
                }
            }
        } catch (_: Exception) {}

        return list.sortedBy { it.name.lowercase() }
    }

    fun getAvatarByFriendName(name: String): String? {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return null
        val db = readableDatabase
        try {
            val c = db.rawQuery(
                "SELECT $COL_CONTACT_AVATAR FROM $TABLE_SHIELD_CONTACTS WHERE LOWER($COL_CONTACT_NAME) = LOWER(?) AND $COL_CONTACT_AVATAR IS NOT NULL",
                arrayOf(trimmed)
            )
            c.use {
                if (it.moveToFirst()) return it.getString(0)
            }
        } catch (_: Exception) {}
        try {
            val c = db.rawQuery(
                "SELECT $COL_SHIELD_AVATAR_URI FROM $TABLE_SHIELDS WHERE LOWER($COL_SHIELD_FRIEND) = LOWER(?) AND $COL_SHIELD_AVATAR_URI IS NOT NULL ORDER BY $COL_SHIELD_TIMESTAMP DESC LIMIT 1",
                arrayOf(trimmed)
            )
            c.use {
                if (it.moveToFirst()) return it.getString(0)
            }
        } catch (_: Exception) {}
        return null
    }

    fun saveOrUpdateShieldContact(name: String, avatarUri: String? = null): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        val db = writableDatabase
        try {
            val cursor = db.rawQuery(
                "SELECT $COL_CONTACT_ID, $COL_CONTACT_AVATAR FROM $TABLE_SHIELD_CONTACTS WHERE LOWER($COL_CONTACT_NAME) = LOWER(?)",
                arrayOf(trimmed)
            )
            val exists = cursor.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(0)
                    val currentAvatar = it.getString(1)
                    if (avatarUri != null && avatarUri != currentAvatar) {
                        val cv = ContentValues().apply { put(COL_CONTACT_AVATAR, avatarUri) }
                        db.update(TABLE_SHIELD_CONTACTS, cv, "$COL_CONTACT_ID = ?", arrayOf(id.toString()))
                    }
                    true
                } else false
            }
            if (!exists) {
                val cv = ContentValues().apply {
                    put(COL_CONTACT_NAME, trimmed)
                    put(COL_CONTACT_AVATAR, avatarUri)
                    put(COL_CONTACT_CREATED_AT, System.currentTimeMillis())
                }
                return db.insertWithOnConflict(TABLE_SHIELD_CONTACTS, null, cv, SQLiteDatabase.CONFLICT_IGNORE)
            }
            return 1L
        } catch (_: Exception) {
            return -1L
        }
    }

    fun deleteShield(id: Long): Boolean {
        val db = writableDatabase
        db.delete(TABLE_SHIELD_HISTORY, "$COL_SHIELD_HIST_SHIELD_ID = ?", arrayOf(id.toString()))
        return db.delete(TABLE_SHIELDS, "$COL_SHIELD_ID = ?", arrayOf(id.toString())) > 0
    }

    fun insertShieldHistory(item: ShieldHistoryItem): Long {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SHIELD_HIST_SHIELD_ID, item.shieldId)
            if (item.transactionId != null) put(COL_SHIELD_HIST_TX_ID, item.transactionId)
            put(COL_SHIELD_HIST_FRIEND, item.friendName.trim())
            put(COL_SHIELD_HIST_AMOUNT, item.amount)
            put(COL_SHIELD_HIST_NOTE, item.note.trim())
            put(COL_SHIELD_HIST_TIMESTAMP, item.timestamp)
        }
        return db.insert(TABLE_SHIELD_HISTORY, null, cv)
    }

    fun getAllShieldHistories(): List<ShieldHistoryItem> {
        val list = mutableListOf<ShieldHistoryItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SHIELD_HISTORY, null, null, null, null, null, "$COL_SHIELD_HIST_TIMESTAMP DESC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToShieldHistory(it))
            }
        }
        return list
    }

    fun getShieldHistoryByShieldId(shieldId: Long): List<ShieldHistoryItem> {
        val list = mutableListOf<ShieldHistoryItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SHIELD_HISTORY,
            null,
            "$COL_SHIELD_HIST_SHIELD_ID = ?",
            arrayOf(shieldId.toString()),
            null,
            null,
            "$COL_SHIELD_HIST_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToShieldHistory(it))
            }
        }
        return list
    }

    fun deleteShieldsForTransaction(txId: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            deleteShieldsForTransaction(db, txId)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    private fun deleteShieldsForTransaction(db: SQLiteDatabase, txId: Long) {
        try {

            val cursor = db.query(
                TABLE_SHIELD_HISTORY,
                null,
                "$COL_SHIELD_HIST_TX_ID = ?",
                arrayOf(txId.toString()),
                null,
                null,
                null
            )
            val histories = mutableListOf<ShieldHistoryItem>()
            cursor.use {
                while (it.moveToNext()) {
                    histories.add(cursorToShieldHistory(it))
                }
            }

            db.delete(TABLE_SHIELD_HISTORY, "$COL_SHIELD_HIST_TX_ID = ?", arrayOf(txId.toString()))

            val affectedShieldIds = histories.map { it.shieldId }.distinct()
            for (sId in affectedShieldIds) {
                val remCur = db.query(
                    TABLE_SHIELD_HISTORY,
                    null,
                    "$COL_SHIELD_HIST_SHIELD_ID = ?",
                    arrayOf(sId.toString()),
                    null,
                    null,
                    "$COL_SHIELD_HIST_TIMESTAMP ASC"
                )
                val remainingHist = mutableListOf<ShieldHistoryItem>()
                remCur.use {
                    while (it.moveToNext()) {
                        remainingHist.add(cursorToShieldHistory(it))
                    }
                }

                val newTotal = remainingHist.sumOf { it.amount }
                if (remainingHist.isEmpty() || newTotal <= 0) {

                    db.delete(TABLE_SHIELDS, "$COL_SHIELD_ID = ?", arrayOf(sId.toString()))
                } else {

                    val positiveHist = remainingHist.filter { it.amount > 0 }
                    val newNote = positiveHist.map { it.note.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(", ")
                    val latestTs = remainingHist.maxOf { it.timestamp }
                    val cv = ContentValues().apply {
                        put(COL_SHIELD_AMOUNT, newTotal)
                        put(COL_SHIELD_NOTE, newNote)
                        put(COL_SHIELD_TIMESTAMP, latestTs)
                    }
                    db.update(TABLE_SHIELDS, cv, "$COL_SHIELD_ID = ?", arrayOf(sId.toString()))
                }
            }

            db.delete(TABLE_SHIELDS, "$COL_SHIELD_TX_ID = ?", arrayOf(txId.toString()))
        } catch (_: Exception) {}
    }

    private fun cursorToShieldHistory(c: Cursor): ShieldHistoryItem {
        val txIdIdx = c.getColumnIndex(COL_SHIELD_HIST_TX_ID)
        val txId = if (txIdIdx >= 0 && !c.isNull(txIdIdx)) c.getLong(txIdIdx) else null

        val noteIdx = c.getColumnIndex(COL_SHIELD_HIST_NOTE)
        val note = if (noteIdx >= 0 && !c.isNull(noteIdx)) c.getString(noteIdx) else ""

        return ShieldHistoryItem(
            id = c.getLong(c.getColumnIndexOrThrow(COL_SHIELD_HIST_ID)),
            shieldId = c.getLong(c.getColumnIndexOrThrow(COL_SHIELD_HIST_SHIELD_ID)),
            transactionId = txId,
            friendName = c.getString(c.getColumnIndexOrThrow(COL_SHIELD_HIST_FRIEND)),
            amount = c.getDouble(c.getColumnIndexOrThrow(COL_SHIELD_HIST_AMOUNT)),
            note = note,
            timestamp = c.getLong(c.getColumnIndexOrThrow(COL_SHIELD_HIST_TIMESTAMP))
        )
    }

    fun settleShield(shieldId: Long, bankId: Long?, bankName: String?, settledAt: Long = System.currentTimeMillis()): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SHIELD_IS_SETTLED, 1)
            put(COL_SHIELD_SETTLED_AT, settledAt)
            if (bankId != null) put(COL_SHIELD_SETTLED_BANK_ID, bankId)
            if (bankName != null) put(COL_SHIELD_SETTLED_BANK_NAME, bankName)
        }
        return db.update(TABLE_SHIELDS, cv, "$COL_SHIELD_ID = ?", arrayOf(shieldId.toString())) > 0
    }

    fun unsettleShield(shieldId: Long): Boolean {
        val db = writableDatabase
        val cv = ContentValues().apply {
            put(COL_SHIELD_IS_SETTLED, 0)
            putNull(COL_SHIELD_SETTLED_AT)
            putNull(COL_SHIELD_SETTLED_BANK_ID)
            putNull(COL_SHIELD_SETTLED_BANK_NAME)
        }
        return db.update(TABLE_SHIELDS, cv, "$COL_SHIELD_ID = ?", arrayOf(shieldId.toString())) > 0
    }

    private fun cursorToShield(c: Cursor): ShieldEntry {
        val txIdIdx = c.getColumnIndex(COL_SHIELD_TX_ID)
        val txId = if (txIdIdx >= 0 && !c.isNull(txIdIdx)) c.getLong(txIdIdx) else null

        val settledAtIdx = c.getColumnIndex(COL_SHIELD_SETTLED_AT)
        val settledAt = if (settledAtIdx >= 0 && !c.isNull(settledAtIdx)) c.getLong(settledAtIdx) else null

        val bankIdIdx = c.getColumnIndex(COL_SHIELD_SETTLED_BANK_ID)
        val bankId = if (bankIdIdx >= 0 && !c.isNull(bankIdIdx)) c.getLong(bankIdIdx) else null

        val bankNameIdx = c.getColumnIndex(COL_SHIELD_SETTLED_BANK_NAME)
        val bankName = if (bankNameIdx >= 0 && !c.isNull(bankNameIdx)) c.getString(bankNameIdx) else null

        val avatarUriIdx = c.getColumnIndex(COL_SHIELD_AVATAR_URI)
        val avatarUri = if (avatarUriIdx >= 0 && !c.isNull(avatarUriIdx)) c.getString(avatarUriIdx) else null

        return ShieldEntry(
            id = c.getLong(c.getColumnIndexOrThrow(COL_SHIELD_ID)),
            transactionId = txId,
            friendName = c.getString(c.getColumnIndexOrThrow(COL_SHIELD_FRIEND)),
            amount = c.getDouble(c.getColumnIndexOrThrow(COL_SHIELD_AMOUNT)),
            note = c.getString(c.getColumnIndexOrThrow(COL_SHIELD_NOTE)) ?: "",
            timestamp = c.getLong(c.getColumnIndexOrThrow(COL_SHIELD_TIMESTAMP)),
            isSettled = c.getInt(c.getColumnIndexOrThrow(COL_SHIELD_IS_SETTLED)) == 1,
            settledAt = settledAt,
            settledBankId = bankId,
            settledBankName = bankName,
            avatarUri = avatarUri
        )
    }
}
