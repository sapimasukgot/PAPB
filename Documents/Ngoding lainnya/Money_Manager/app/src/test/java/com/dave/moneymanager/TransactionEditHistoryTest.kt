package com.dave.moneymanager

import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.data.model.TransactionEditHistory
import com.dave.moneymanager.ui.components.FriendShieldDraft
import com.dave.moneymanager.ui.components.isFoodCategory
import com.dave.moneymanager.ui.components.isShieldEligibleCategory
import com.dave.moneymanager.ui.components.mergeDuplicateFriends
import org.junit.Assert.*
import org.junit.Test

class TransactionEditHistoryTest {

    @Test
    fun testTransactionEditHistoryCalculation() {
        val oldAmount = 50000.0
        val newAmount = 75000.0
        val diffAmount = newAmount - oldAmount

        val history = TransactionEditHistory(
            id = 1L,
            transactionId = 100L,
            editTimestamp = System.currentTimeMillis(),
            oldAmount = oldAmount,
            newAmount = newAmount,
            diffAmount = diffAmount,
            oldCategory = "Food",
            newCategory = "Food",
            oldBankName = "BCA",
            newBankName = "BCA",
            changeSummary = "Nominal diubah: Rp 50.000 -> Rp 75.000"
        )

        assertEquals(25000.0, history.diffAmount, 0.001)
        assertTrue(history.diffAmount > 0)
        assertEquals(oldAmount, history.oldAmount, 0.001)
        assertEquals(newAmount, history.newAmount, 0.001)
        assertTrue(history.changeSummary.contains("Rp 50.000 -> Rp 75.000"))
    }

    @Test
    fun testNegativeDiffAmount() {
        val oldAmount = 100000.0
        val newAmount = 40000.0
        val diffAmount = newAmount - oldAmount

        val history = TransactionEditHistory(
            id = 2L,
            transactionId = 100L,
            editTimestamp = System.currentTimeMillis(),
            oldAmount = oldAmount,
            newAmount = newAmount,
            diffAmount = diffAmount,
            changeSummary = "Nominal diubah: Rp 100.000 -> Rp 40.000"
        )

        assertEquals(-60000.0, history.diffAmount, 0.001)
        assertTrue(history.diffAmount < 0)
    }

    @Test
    fun testTransferTransactionCreation() {
        val transferTx = Transaction(
            id = 10L,
            bankId = 1L,
            targetBankId = 2L,
            type = Transaction.TYPE_TRANSFER,
            amount = 150000.0,
            category = "Transfer",
            note = "Transfer ke Mandiri",
            timestamp = System.currentTimeMillis()
        )

        assertEquals(Transaction.TYPE_TRANSFER, transferTx.type)
        assertEquals(1L, transferTx.bankId)
        assertEquals(2L, transferTx.targetBankId)
        assertEquals(150000.0, transferTx.amount, 0.001)
    }

    @Test
    fun testMergeDuplicateFriendShields() {
        val drafts = listOf(
            FriendShieldDraft(id = 1L, name = "Budi", amountText = "20000", note = "Nasi Goreng"),
            FriendShieldDraft(id = 2L, name = "Siti", amountText = "15000", note = "Mie Ayam"),
            FriendShieldDraft(id = 3L, name = "budi", amountText = "10000", note = "Es Teh")
        )

        val merged = mergeDuplicateFriends(drafts)
        assertEquals(2, merged.size)

        val budi = merged.find { it.name.equals("Budi", ignoreCase = true) }
        assertNotNull(budi)
        assertEquals("30000", budi!!.amountText)
        assertTrue(budi.note.contains("Nasi Goreng"))
        assertTrue(budi.note.contains("Es Teh"))
    }

    @Test
    fun testShieldCategoryEligibility() {
        assertTrue(isShieldEligibleCategory("Food & Beverages"))
        assertTrue(isShieldEligibleCategory("Makanan & Minuman"))
        assertTrue(isShieldEligibleCategory("Kopi"))
        assertTrue(isShieldEligibleCategory("Shield"))
        assertFalse(isShieldEligibleCategory("Salary"))
        assertFalse(isShieldEligibleCategory("Transfer"))
    }

    @Test
    fun testShieldMultiWalletDeductions() {
        val deductions = listOf(
            com.dave.moneymanager.data.model.ShieldWalletDeduction(bankId = 1L, bankName = "BCA", amount = 30000.0),
            com.dave.moneymanager.data.model.ShieldWalletDeduction(bankId = 2L, bankName = "GoPay", amount = 20000.0)
        )

        assertEquals(2, deductions.size)
        val totalDeducted = deductions.sumOf { it.amount }
        assertEquals(50000.0, totalDeducted, 0.001)

        val distinctBanks = deductions.map { it.bankId }.distinct()
        assertEquals(deductions.size, distinctBanks.size)
    }

    @Test
    fun testIsSameDay() {
        val cal1 = java.util.Calendar.getInstance()
        val cal2 = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.HOUR_OF_DAY, 2)
        }
        val calDifferentDay = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.DAY_OF_YEAR, -1)
        }

        assertTrue(com.dave.moneymanager.data.model.isSameDay(cal1.timeInMillis, cal2.timeInMillis))
        assertFalse(com.dave.moneymanager.data.model.isSameDay(cal1.timeInMillis, calDifferentDay.timeInMillis))
    }

    @Test
    fun testIsFoodCategoryRecognition() {
        assertTrue(com.dave.moneymanager.data.model.isFoodCategory("Food & Dining"))
        assertTrue(com.dave.moneymanager.data.model.isFoodCategory("Food & Beverages"))
        assertTrue(com.dave.moneymanager.data.model.isFoodCategory("Makanan"))
        assertTrue(com.dave.moneymanager.data.model.isFoodCategory("Kopi"))
        assertTrue(com.dave.moneymanager.data.model.isFoodCategory("Resto"))
        assertFalse(com.dave.moneymanager.data.model.isFoodCategory("Church"))
        assertFalse(com.dave.moneymanager.data.model.isFoodCategory("Health"))
        assertFalse(com.dave.moneymanager.data.model.isFoodCategory("Transportation"))
        assertFalse(com.dave.moneymanager.data.model.isFoodCategory("Shopping"))
    }

    @Test
    fun testSameDayFoodConsolidationLogic() {
        val existingTx = Transaction(
            id = 5L,
            bankId = 1L,
            bankName = "GoPay",
            type = Transaction.TYPE_EXPENSE,
            amount = 35000.0,
            category = "Food & Dining",
            destination = "KFC",
            note = "To: KFC",
            timestamp = System.currentTimeMillis()
        )

        val newExpenseAmount = 25000.0
        val newDestination = "Starbucks"
        val newNote = "To: Starbucks"

        val consolidatedAmount = existingTx.amount + newExpenseAmount
        val mergedDestination = if (!existingTx.destination.isNullOrBlank() && !newDestination.isNullOrBlank()) {
            "${existingTx.destination}, $newDestination"
        } else newDestination

        val mergedNote = "To: $mergedDestination"

        val updatedTx = existingTx.copy(
            amount = consolidatedAmount,
            destination = mergedDestination,
            note = mergedNote
        )

        assertEquals(60000.0, updatedTx.amount, 0.001)
        assertEquals("KFC, Starbucks", updatedTx.destination)
        assertEquals("To: KFC, Starbucks", updatedTx.note)
        assertEquals(existingTx.id, updatedTx.id)
        assertEquals("Food & Dining", updatedTx.category)
    }
}
