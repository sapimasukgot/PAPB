package com.dave.moneymanager

import com.dave.moneymanager.data.model.Transaction
import com.dave.moneymanager.service.NotificationParser
import org.junit.Assert.*
import org.junit.Test

class NotificationParserTest {

    @Test
    fun testOutgoingTransferGoPay() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Transfer ke Dave sebesar Rp 50.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Food & Dining", result.category)
        assertEquals("To: Dave", result.note)
    }

    @Test
    fun testIncomingTransferGoPayClone_pattern1() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "GoPay Clone",
            content = "Transfer dari Dave sebesar Rp 50.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Reimburse", result.category)
        assertEquals("From: Dave", result.note)
    }

    @Test
    fun testIncomingTransferGoPayClone_pattern2() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "GoPay Clone",
            content = "Transfer Rp 50.000 dari Dave berhasil"
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Reimburse", result.category)
    }

    @Test
    fun testIncomingTransferGoPayClone_pattern3() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay Clone",
            content = "Kamu menerima transfer sebesar Rp 50.000 dari Dave Pratama",
            isClonedUser = true
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave Pratama", result.destination)
    }

    @Test
    fun testOutgoingTransferFromGoPayToCloneRecipient() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Transfer ke GoPay Clone Dave sebesar Rp 50.000 berhasil"
        )
        assertNotNull(result)

        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
    }

    @Test
    fun testTransferReceiptWithPromoCashbackFooterNotIgnored() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Transfer ke Dave sebesar Rp 50.000 berhasil. Dapatkan cashback hingga 10% dan Koin Shopee"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
    }

    @Test
    fun testCashbackIgnored() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Kamu dapat cashback Rp 5.000 dari transaksi kemarin"
        )
        assertNull(result)
    }

    @Test
    fun testEnglishGoPayTransferExpense() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "You've sent Rp50,000 to Dave"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("To: Dave", result.note)
    }

    @Test
    fun testEnglishGoPayPaymentExpense() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Payment of Rp25,000 at Starbucks was successful"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(25000.0, result.amount, 0.001)
        assertEquals("Starbucks", result.destination)
        assertEquals("Food & Dining", result.category)
    }

    @Test
    fun testEnglishGoPayTransferIncome1() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "GoPay Clone",
            content = "You've received a transfer of Rp50,000 from Dave"
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Reimburse", result.category)
        assertEquals("From: Dave", result.note)
    }

    @Test
    fun testEnglishGoPayTransferIncome2() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "GoPay Clone",
            content = "Transfer from Dave of Rp50,000 was successful"
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Reimburse", result.category)
    }

    @Test
    fun testEnglishGoPayTransferIncome3() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "GoPay Clone",
            content = "Dave sent you Rp50,000"
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Reimburse", result.category)
    }

    @Test
    fun testRealEnglishGoPaySuccessfullyTransferredBalance() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "You have successfully transferred your GoPay balance of Rp50,000 to Dave"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
    }

    @Test
    fun testIndonesianSaldoBerhasilDikirim() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "Transfer Berhasil",
            content = "Saldo kamu berhasil dikirim ke Dave sebesar Rp50.000"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
    }

    @Test
    fun testGoPayCloneInParallelSpace() {
        val result = NotificationParser.parse(
            packageName = "com.parallel.space.gojek",
            title = "GoPay Clone",
            content = "Dave sent you Rp50,000",
            isClonedUser = true
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
    }

    @Test
    fun testRealGoPayTransferReceivedInYourGoPay() {

        val result = NotificationParser.parse(
            packageName = "com.gojek.gopay.clone",
            title = "GoPay",
            content = "Rp50,000 has been received in your GoPay"
        )
        assertNotNull("Should parse 'has been received in your GoPay' notification", result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
    }

    @Test
    fun testRealGoPayTransferReceivedFromSender() {

        val result = NotificationParser.parse(
            packageName = "com.gojek.gopay.clone",
            title = "GoPay",
            content = "Rp50,000 has been received in your GoPay from Dave"
        )
        assertNotNull("Should parse 'has been received in your GoPay from Dave'", result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("From: Dave", result.note)
    }

    @Test
    fun testGoPay2NotificationResolvedAsGoPayClone() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay 2",
            content = "Pembayaran sebesar Rp 35.000 berhasil di Kopi Kenangan"
        )
        assertNotNull("Should parse GoPay 2 title notification", result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(35000.0, result.amount, 0.001)
    }

    @Test
    fun testRealGoPaySucessfullyTransferred() {

        val result = NotificationParser.parse(
            packageName = "com.gojek.gopay",
            title = "GoPay",
            content = "Rp50,000 has been sucessfully transferred to Dave"
        )
        assertNotNull("Should parse 'sucessfully transferred' (GoPay typo) notification", result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("To: Dave", result.note)
    }

    @Test
    fun testRealGoPaySuccessfullyTransferredNoTypo() {

        val result = NotificationParser.parse(
            packageName = "com.gojek.gopay",
            title = "GoPay",
            content = "Rp50,000 has been successfully transferred to Dave"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
    }

    @Test
    fun testEmojiPromoFilteredOut() {

        val promo1 = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay Promo",
            content = "🎉 Dapatkan cashback Rp50.000 dengan belanja min Rp100.000! 🔥"
        )
        assertNull("Notification with emoji 🎉 and 🔥 must return null", promo1)

        val promo2 = NotificationParser.parse(
            packageName = "com.shopee.id",
            title = "ShopeePay",
            content = "Bonus saldo Rp20.000 menantimu! Klaim sekarang 🎁✨"
        )
        assertNull("Notification with emoji 🎁 and ✨ must return null", promo2)

        val promo3 = NotificationParser.parse(
            packageName = "src.bni.mbanking",
            title = "BNI Mobile",
            content = "Promo Gajian! Diskon hingga Rp100.000 di merchant pilihan 💳🥳"
        )
        assertNull("Notification with emoji 💳 and 🥳 must return null", promo3)

        assertNull("Money bag emoji 💰 must be ignored", NotificationParser.parse(
            "com.gojek.app", "GoPay", "💰 Ada bonus saldo Rp50.000 untukmu hari ini!"
        ))

        assertNull("Banknote emoji 💵 must be ignored", NotificationParser.parse(
            "com.shopee.id", "ShopeePay", "Dapatkan 💵 Rp25.000 gratis dengan transaksi sekarang"
        ))

        assertNull("Money wings emoji 💸 must be ignored", NotificationParser.parse(
            "ovo.id", "OVO", "Transfer bebas biaya admin 💸 nikmati cashback Rp10.000"
        ))

        assertNull("Coin emoji 🪙 must be ignored", NotificationParser.parse(
            "com.shopee.id", "ShopeePay", "Kumpulkan 🪙 koin dan tukarkan saldo Rp30.000"
        ))

        assertNull("Money face emoji 🤑 must be ignored", NotificationParser.parse(
            "com.cimbniaga.octomobile", "OCTO Mobile", "Cuan melimpah 🤑 buka rekening dapat Rp100.000"
        ))

        assertNull("Dollar sign emoji 💲 must be ignored", NotificationParser.parse(
            "com.gojek.app", "GoPay", "Diskon spesial 💲 potongan s.d Rp40.000"
        ))

        assertNull("ATM emoji 🏧 must be ignored", NotificationParser.parse(
            "src.bni.mbanking", "BNI Mobile", "Tarik tunai di 🏧 dapat cashback Rp15.000"
        ))

        assertNull("Receipt emoji 🧾 must be ignored", NotificationParser.parse(
            "com.cimbniaga.octomobile", "OCTO Mobile", "Bayar tagihan 🧾 dapat reward Rp20.000"
        ))

        assertNull("Bank emoji 🏦 must be ignored", NotificationParser.parse(
            "id.co.bni.wondr", "wondr by BNI", "Nabung di 🏦 dapat bonus saldo Rp50.000"
        ))

        assertNull("Angpao emoji 🧧 must be ignored", NotificationParser.parse(
            "com.gojek.app", "GoPay", "Bagi-bagi angpao 🧧 dapat cashback Rp10.000"
        ))
    }

    @Test
    fun testBNIDebitExpense() {
        val result = NotificationParser.parse(
            packageName = "src.bni.mbanking",
            title = "BNI Mobile Banking",
            content = "Transaksi debit Rp150.000 dari rekening 1234567890 ke Tokopedia berhasil"
        )
        assertNotNull("Should parse BNI debit transaction", result)
        assertEquals("Bank BNI", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(150000.0, result.amount, 0.001)
        assertEquals("Tokopedia", result.destination)
    }

    @Test
    fun testWondrBNICreditIncome() {
        val result = NotificationParser.parse(
            packageName = "id.co.bni.wondr",
            title = "wondr by BNI",
            content = "Transaksi kredit Rp500.000 masuk ke rekening dari PT Teknologi Bersama"
        )
        assertNotNull("Should parse wondr by BNI credit transaction", result)
        assertEquals("Bank BNI", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(500000.0, result.amount, 0.001)
        assertEquals("PT Teknologi Bersama", result.destination)
    }

    @Test
    fun testCIMBDebitExpense() {
        val result = NotificationParser.parse(
            packageName = "com.cimbniaga.octomobile",
            title = "OCTO Mobile",
            content = "Transaksi debit sebesar Rp200.000 dari rekening CIMB Niaga Anda berhasil"
        )
        assertNotNull("Should parse CIMB Niaga debit transaction", result)
        assertEquals("Bank CIMB", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(200000.0, result.amount, 0.001)
    }

    @Test
    fun testOctoPayQRISExpense() {
        val result = NotificationParser.parse(
            packageName = "com.cimbniaga.octomobile",
            title = "OCTO Mobile",
            content = "Pembayaran QRIS OctoPay sebesar Rp35.000 di Kopi Kenangan berhasil"
        )
        assertNotNull("Should parse OctoPay QRIS transaction under OCTO Mobile package", result)
        assertEquals("OctoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(35000.0, result.amount, 0.001)
        assertEquals("Kopi Kenangan", result.destination)
    }

    @Test
    fun testOctoPayTopUpIncome() {
        val result = NotificationParser.parse(
            packageName = "com.cimbniaga.octomobile",
            title = "OCTO Mobile",
            content = "Top up saldo OctoPay sebesar Rp100.000 berhasil ditambahkan"
        )
        assertNotNull("Should parse OctoPay top up transaction", result)
        assertEquals("OctoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(100000.0, result.amount, 0.001)
    }

    @Test
    fun testSocialMediaTwitterNotificationIgnored() {
        val result = NotificationParser.parse(
            packageName = "com.twitter.android",
            title = "X • @johndoe",
            content = "Gua barusan bayar makan siang pakai GoPay sebesar Rp 50.000 enak banget!"
        )
        assertNull("Twitter/X notification mentioning GoPay and amount MUST NOT be parsed as transaction", result)
        assertFalse(NotificationParser.isTargetApp("com.twitter.android", "X • @johndoe", "Gua barusan bayar pakai GoPay"))
    }

    @Test
    fun testWhatsAppMessageMentioningPaymentIgnored() {
        val result = NotificationParser.parse(
            packageName = "com.whatsapp",
            title = "WhatsApp • Budi",
            content = "Bro, transfer ke gopay lu Rp 50.000 udah masuk ya"
        )
        assertNull("WhatsApp chat mentioning GoPay MUST NOT be parsed as transaction", result)
        assertFalse(NotificationParser.isTargetApp("com.whatsapp", "WhatsApp", "transfer ke gopay"))
    }

    @Test
    fun testFinancialAppWhitelist_ValidEMoney() {
        assertTrue(NotificationParser.isFinancialApp("com.gojek.app"))
        assertTrue(NotificationParser.isFinancialApp("com.gojek.gopay"))
        assertTrue(NotificationParser.isFinancialApp("com.shopee.id"))
        assertTrue(NotificationParser.isFinancialApp("com.shopeepay.id"))
        assertTrue(NotificationParser.isFinancialApp("ovo.id"))
        assertTrue(NotificationParser.isFinancialApp("com.grabtaxi.passenger"))
        assertTrue(NotificationParser.isFinancialApp("id.dana"))
        assertTrue(NotificationParser.isFinancialApp("com.telkom.tcash"))
    }

    @Test
    fun testFinancialAppWhitelist_ValidMBanking() {
        assertTrue(NotificationParser.isFinancialApp("src.bni.mbanking"))
        assertTrue(NotificationParser.isFinancialApp("id.co.bni.wondr"))
        assertTrue(NotificationParser.isFinancialApp("com.cimbniaga.octomobile"))
        assertTrue(NotificationParser.isFinancialApp("com.bca"))
        assertTrue(NotificationParser.isFinancialApp("mybca.id"))
        assertTrue(NotificationParser.isFinancialApp("id.bmri.livin"))
        assertTrue(NotificationParser.isFinancialApp("id.co.bri.brimo"))
        assertTrue(NotificationParser.isFinancialApp("id.co.bankbsi.mobile"))
        assertTrue(NotificationParser.isFinancialApp("com.jago.bank"))
        assertTrue(NotificationParser.isFinancialApp("com.seabank.id"))
    }

    @Test
    fun testFinancialAppWhitelist_NonFinancialAppsRejected() {
        assertFalse(NotificationParser.isFinancialApp("com.twitter.android"))
        assertFalse(NotificationParser.isFinancialApp("com.instagram.android"))
        assertFalse(NotificationParser.isFinancialApp("com.facebook.katana"))
        assertFalse(NotificationParser.isFinancialApp("com.whatsapp"))
        assertFalse(NotificationParser.isFinancialApp("org.telegram.messenger"))
        assertFalse(NotificationParser.isFinancialApp("com.android.chrome"))
        assertFalse(NotificationParser.isFinancialApp("com.google.android.youtube"))
        assertFalse(NotificationParser.isFinancialApp("com.google.android.apps.messaging"))
        assertFalse(NotificationParser.isFinancialApp("com.tokopedia.tkpd"))
        assertFalse(NotificationParser.isFinancialApp("com.detik.portal"))
        assertFalse(NotificationParser.isFinancialApp("com.supercell.clashofclans"))
    }

    @Test
    fun testClonedContainerOnlyApprovedIfFinancialTitle() {

        assertTrue(NotificationParser.isFinancialApp("com.lbe.parallel.intl", "GoPay"))
        assertTrue(NotificationParser.isFinancialApp("com.excelliance.multiaccounts", "ShopeePay"))

        assertFalse(NotificationParser.isFinancialApp("com.lbe.parallel.intl", "Twitter"))
        assertFalse(NotificationParser.isFinancialApp("com.excelliance.multiaccounts", "WhatsApp"))
    }

    @Test
    fun testBCAMobileExpense() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Pembayaran QRIS sebesar Rp 25.000 di Indomaret berhasil"
        )
        assertNotNull(result)
        assertEquals("BCA", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(25000.0, result.amount, 0.001)
        assertEquals("Indomaret", result.destination)
    }

    @Test
    fun testOutgoingTransferBCA_defaultsToFoodAndBeverages() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "m-Transfer: BERHASIL transfer ke Rekening 1234567890 a.n. BUDI sebesar Rp 50.000"
        )
        assertNotNull(result)
        assertEquals("BCA", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Food & Dining", result.category)
    }

    @Test
    fun testOutgoingTransferShopeePay_defaultsToFoodAndDining() {
        val result = NotificationParser.parse(
            packageName = "com.shopeepay.id",
            title = "ShopeePay",
            content = "Transfer Dana Berhasil! Kamu telah mengirim Rp50.000 ke Rekening Budi"
        )
        assertNotNull(result)
        assertEquals("ShopeePay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Food & Dining", result.category)
    }

    @Test
    fun testOutgoingTransferBNI_defaultsToFoodAndDining() {
        val result = NotificationParser.parse(
            packageName = "src.bni.mbanking",
            title = "BNI Mobile Banking",
            content = "Transfer Berhasil ke 12345678 a.n. SITI sebesar Rp 30.000"
        )
        assertNotNull(result)
        assertEquals("Bank BNI", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(30000.0, result.amount, 0.001)
        assertEquals("Food & Dining", result.category)
    }

    @Test
    fun testOutgoingTransferMandiri_defaultsToFoodAndDining() {
        val result = NotificationParser.parse(
            packageName = "id.bmri.livin",
            title = "Livin' by Mandiri",
            content = "Transfer Berhasil ke 12345678 a.n. SITI sebesar Rp 30.000"
        )
        assertNotNull(result)
        assertEquals("Bank Mandiri", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(30000.0, result.amount, 0.001)
        assertEquals("Food & Dining", result.category)
    }

    @Test
    fun testOutgoingTransferEnglish_defaultsToFoodAndDining() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "You have successfully transferred your GoPay balance of Rp75,000 to Dave"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(75000.0, result.amount, 0.001)
        assertEquals("Food & Dining", result.category)
    }

    @Test
    fun testOutgoingTransferWithSpecificCategoryOverridesDefault() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer ke Rekening 123456 untuk pembayaran tagihan PLN listrik sebesar Rp 150.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("BCA", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(150000.0, result.amount, 0.001)
        assertEquals("Bills & Utilities", result.category)
    }

    @Test
    fun testIndonesianActiveSenderExtraction() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "GoPay Clone",
            content = "Dave baru aja ngirim kamu saldo GoPay sebesar Rp 50.000",
            isClonedUser = true
        )
        assertNotNull(result)
        assertEquals("GoPay Clone", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
        assertEquals("Reimburse", result.category)
        assertEquals("From: Dave", result.note)
    }

    @Test
    fun testIndonesianAtasNamaSenderExtraction() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "m-Transfer: Rekening 1234567890 menerima transfer dari Rekening 987654 a.n. BUDI SANTOSO sebesar Rp 100.000"
        )
        assertNotNull(result)
        assertEquals("BCA", result!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, result.type)
        assertEquals(100000.0, result.amount, 0.001)
        assertEquals("BUDI SANTOSO", result.destination)
        assertEquals("Reimburse", result.category)
        assertEquals("From: BUDI SANTOSO", result.note)
    }

    @Test
    fun testSenderInTitle() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app.clone",
            title = "Dave Carnegie",
            content = "Transfer saldo sebesar Rp 75.000 berhasil masuk ke akunmu",
            isClonedUser = true
        )
        assertNotNull(result)
        assertEquals("Dave Carnegie", result!!.destination)
        assertEquals("Reimburse", result.category)
        assertEquals("From: Dave Carnegie", result.note)
    }

    @Test
    fun testBankIncomingTransferCategorizedAsReimburse() {
        val bcaResult = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer masuk sebesar Rp 250.000 dari BUDI berhasil ke rekening Anda"
        )
        assertNotNull(bcaResult)
        assertEquals("BCA", bcaResult!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, bcaResult.type)
        assertEquals("Reimburse", bcaResult.category)
        assertNotEquals("Other Income", bcaResult.category)

        val mandiriResult = NotificationParser.parse(
            packageName = "id.bmri.livin",
            title = "Livin by Mandiri",
            content = "Dana masuk sebesar Rp 500.000 ke rekening Anda"
        )
        assertNotNull(mandiriResult)
        assertEquals("Bank Mandiri", mandiriResult!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, mandiriResult.type)
        assertEquals("Reimburse", mandiriResult.category)
        assertNotEquals("Other Income", mandiriResult.category)

        val bniResult = NotificationParser.parse(
            packageName = "src.bni.mbanking",
            title = "BNI Mobile",
            content = "Transaksi kredit sebesar Rp 150.000 berhasil diproses"
        )
        assertNotNull(bniResult)
        assertEquals("Bank BNI", bniResult!!.targetBankName)
        assertEquals(Transaction.TYPE_INCOME, bniResult.type)
        assertEquals("Reimburse", bniResult.category)
        assertNotEquals("Other Income", bniResult.category)
    }

    @Test
    fun testSentKeywordExpense() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Sent Rp 75.000 to Dave Pratama"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(75000.0, result.amount, 0.001)
        assertEquals("Dave Pratama", result.destination)
    }

    @Test
    fun testTransferSuccessfuKeywordExpense() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "m-Transfer successful Rp 100.000 to Dave"
        )
        assertNotNull(result)
        assertEquals("BCA", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(100000.0, result.amount, 0.001)
        assertEquals("Dave", result.destination)
    }

    @Test
    fun testDestinationChurch_GKI() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Transfer ke GKI sebesar Rp 100.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("GoPay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(100000.0, result.amount, 0.001)
        assertEquals("GKI", result.destination)
        assertEquals("Church", result.category)
    }

    @Test
    fun testDestinationChurch_GBI() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer ke GBI Rock sebesar Rp 50.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("BCA", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(50000.0, result.amount, 0.001)
        assertEquals("GBI Rock", result.destination)
        assertEquals("Church", result.category)
    }

    @Test
    fun testDestinationChurch_GMS() {
        val result = NotificationParser.parse(
            packageName = "com.shopeepay.id",
            title = "ShopeePay",
            content = "Transfer Dana Berhasil! Kamu telah mengirim Rp75.000 ke Rekening GMS"
        )
        assertNotNull(result)
        assertEquals("ShopeePay", result!!.targetBankName)
        assertEquals(Transaction.TYPE_EXPENSE, result.type)
        assertEquals(75000.0, result.amount, 0.001)
        assertEquals("GMS", result.destination)
        assertEquals("Church", result.category)
    }

    @Test
    fun testDestinationHealth_Hospital() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer ke RS Siloam sebesar Rp 250.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Health", result!!.category)
        assertEquals("RS Siloam", result.destination)
    }

    @Test
    fun testDestinationHealth_Pharmacy() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Pembayaran di Apotek Kimia Farma sebesar Rp 60.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Health", result!!.category)
    }

    @Test
    fun testDestinationEntertainment_Cinema() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Transfer ke Cinema XXI sebesar Rp 110.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Entertainment", result!!.category)
        assertEquals("Cinema XXI", result.destination)
    }

    @Test
    fun testDestinationTransportation_SPBU() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Pembayaran di SPBU Pertamina sebesar Rp 50.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Transportation", result!!.category)
    }

    @Test
    fun testDestinationShopping_Indomaret() {
        val result = NotificationParser.parse(
            packageName = "com.shopeepay.id",
            title = "ShopeePay",
            content = "Pembayaran di Indomaret sebesar Rp 45.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Shopping", result!!.category)
        assertEquals("Indomaret", result.destination)
    }

    @Test
    fun testDestinationFitness_Gym() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer ke Celebrity Fitness sebesar Rp 350.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Fitness", result!!.category)
        assertEquals("Celebrity Fitness", result.destination)
    }

    @Test
    fun testDestinationTravel_OTA() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer ke Traveloka sebesar Rp 1.200.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Travel", result!!.category)
        assertEquals("Traveloka", result.destination)
    }

    @Test
    fun testDestinationCharity_Donation() {
        val result = NotificationParser.parse(
            packageName = "com.gojek.app",
            title = "GoPay",
            content = "Transfer ke Kitabisa sebesar Rp 100.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Charity / Donations", result!!.category)
        assertEquals("Kitabisa", result.destination)
    }

    @Test
    fun testDestinationPets_PetShop() {
        val result = NotificationParser.parse(
            packageName = "com.bca",
            title = "BCA Mobile",
            content = "Transfer ke Pet Shop Cat Lover sebesar Rp 85.000 berhasil"
        )
        assertNotNull(result)
        assertEquals("Pets", result!!.category)
        assertEquals("Pet Shop Cat Lover", result.destination)
    }
}

