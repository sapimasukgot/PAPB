package com.dave.moneymanager.service

import com.dave.moneymanager.data.model.Transaction
import java.util.regex.Pattern

data class ParsedNotificationResult(
    val isValidTransaction: Boolean,
    val appName: String,
    val targetBankName: String,
    val type: String,
    val amount: Double,
    val category: String,
    val note: String,
    val rawText: String,
    val destination: String? = null
)

object NotificationParser {

    val MONEY_EMOJIS = listOf(
        "💰", "💵", "💸", "💳", "🪙", "🤑", "💲", "🏧", "🧾", "🏦",
        "💹", "💴", "💶", "💷", "💱", "📈", "📉", "💎", "🧧", "🏷️",
        "🏷", "🛍️", "🛍", "🛒", "🎰", "👑", "💍", "🏆", "🥇", "🥈", "🥉"
    )

    fun containsEmoji(text: String): Boolean {
        if (text.isEmpty()) return false

        for (emoji in MONEY_EMOJIS) {
            if (text.contains(emoji)) return true
        }

        if (text.contains("$$$") || text.contains("$$")) return true

        var i = 0
        val len = text.length
        while (i < len) {
            val codePoint = text.codePointAt(i)
            val type = Character.getType(codePoint)

            if (type == Character.OTHER_SYMBOL.toInt() ||
                type == Character.SURROGATE.toInt() ||
                type == Character.MODIFIER_SYMBOL.toInt()) {
                return true
            }

            if (codePoint in 0x1F600..0x1F64F ||
                codePoint in 0x1F300..0x1F5FF ||
                codePoint in 0x1F680..0x1F6FF ||
                codePoint in 0x1F1E0..0x1F1FF ||
                codePoint in 0x2600..0x26FF ||
                codePoint in 0x2700..0x27BF ||
                codePoint in 0xFE00..0xFE0F ||
                codePoint in 0x1F900..0x1F9FF ||
                codePoint in 0x1FA70..0x1FAFF ||
                codePoint in 0x2300..0x23FF ||
                codePoint in 0x2B50..0x2B55 ||
                codePoint in 0x203C..0x2049
            ) {
                return true
            }

            i += Character.charCount(codePoint)
        }
        return false
    }

    val NON_FINANCIAL_PACKAGES = setOf(

        "com.twitter.android",
        "com.twitter.android.lite",
        "com.instagram.android",
        "com.instagram.lite",
        "com.instagram.barcelona",
        "com.facebook.katana",
        "com.facebook.lite",
        "com.facebook.orca",
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.google.android.youtube",
        "com.discord",
        "com.reddit.frontpage",
        "com.pinterest",
        "com.linkedin.android",
        "com.snapchat.android",

        "com.whatsapp",
        "com.whatsapp.w4b",
        "org.telegram.messenger",
        "org.thunderdog.challegram",
        "org.telegram.plus",
        "jp.naver.line.android",
        "com.google.android.talk",
        "com.google.android.apps.messaging",
        "com.samsung.android.messaging",
        "com.google.android.gm",
        "com.microsoft.office.outlook",

        "com.android.chrome",
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.opera.mini.native",
        "com.microsoft.emmx",
        "com.brave.browser",
        "com.sec.android.app.sbrowser",
        "com.ucmobile.intl",
        "com.google.android.googlequicksearchbox",

        "com.tokopedia.tkpd",
        "com.bukalapak.android",
        "com.lazada.android",
        "blibli.mobile.commerce",

        "com.detik.portal",
        "com.kompas.app",
        "com.tribun.news",
        "com.cnnindonesia.android",
        "id.kumparan.app",
        "com.idntimes.idntimes",
        "com.liputan6.android",
        "com.google.android.apps.magazines"
    )

    val EMONEY_PACKAGES = setOf(

        "com.gojek.app",
        "com.gojek.gopay",
        "com.gopay.app",
        "com.gojek.app.clone",

        "com.shopee.id",
        "com.shopeepay.id",
        "com.shopee.id.test",

        "ovo.id",
        "com.ovo",
        "com.grabtaxi.passenger",

        "id.dana",

        "com.telkom.tcash",

        "com.astrapay.app",

        "com.indomaret.isaku"
    )

    val MBANKING_PACKAGES = setOf(

        "src.bni.mbanking",
        "id.co.bni.wondr",
        "id.co.bni.wondr.android",
        "id.co.bni.mobile",

        "com.cimbniaga.octomobile",
        "id.co.cimbniaga.octomobile",

        "com.bca",
        "mybca.id",
        "id.co.bca.mybca",

        "id.bmri.livin",
        "com.bankmandiri.mandirimobile",

        "id.co.bri.brimo",

        "id.co.bankbsi.mobile",

        "com.jago.bank",

        "com.seabank.id",

        "com.btpn.dc",

        "id.allobank.mobile",

        "bcadigital.myblu",

        "com.permata.mobile"
    )

    fun isBlacklistedPackage(packageName: String): Boolean {
        val pkg = packageName.lowercase().trim()
        if (NON_FINANCIAL_PACKAGES.contains(pkg)) return true
        return pkg.startsWith("com.twitter.") ||
                pkg.startsWith("com.whatsapp") ||
                pkg.startsWith("org.telegram.") ||
                pkg.startsWith("com.instagram.") ||
                pkg.startsWith("com.facebook.") ||
                pkg.startsWith("com.discord") ||
                pkg.startsWith("com.reddit.")
    }

    private fun isGoPayPackage(pkg: String): Boolean {
        return pkg.contains("gojek") || pkg.contains("gopay")
    }

    private fun isShopeePackage(pkg: String): Boolean {
        return pkg.contains("shopee")
    }

    private fun isOvoPackage(pkg: String, combined: String = ""): Boolean {
        return pkg.contains("ovo") || (pkg.contains("grab") && (combined.contains("ovo") || combined.contains("grabpay")))
    }

    private fun isBniPackage(pkg: String): Boolean {
        return pkg.contains("bni") || pkg.contains("wondr")
    }

    private fun isCimbPackage(pkg: String): Boolean {
        return pkg.contains("cimb") || pkg.contains("octo")
    }

    private fun isBcaPackage(pkg: String): Boolean {
        return pkg == "com.bca" || pkg.contains("mybca") || pkg.contains("bca.mybca")
    }

    private fun isMandiriPackage(pkg: String): Boolean {
        return pkg.contains("bmri.livin") || pkg.contains("mandirimobile")
    }

    private fun isBriPackage(pkg: String): Boolean {
        return pkg.contains("bri.brimo") || pkg.contains("brimo")
    }

    private fun isDanaPackage(pkg: String): Boolean {
        return pkg == "id.dana" || pkg.contains(".dana")
    }

    private fun isContainerPackage(pkg: String): Boolean {
        return pkg.contains("parallel") || pkg.contains("dual") ||
                pkg.contains("multi") || pkg.contains("excelliance") ||
                pkg.contains("polestar") || pkg.contains("clone")
    }

    fun isFinancialApp(packageName: String, title: String = ""): Boolean {
        val pkg = packageName.lowercase().trim()
        if (pkg.isEmpty()) return false

        if (isBlacklistedPackage(pkg)) return false

        if (EMONEY_PACKAGES.contains(pkg) || MBANKING_PACKAGES.contains(pkg)) {
            return true
        }

        if (isGoPayPackage(pkg) || isShopeePackage(pkg) || isOvoPackage(pkg) || isDanaPackage(pkg)) {
            return true
        }

        if (isBniPackage(pkg) || isCimbPackage(pkg) || isBcaPackage(pkg) || isMandiriPackage(pkg) || isBriPackage(pkg)) {
            return true
        }

        if (isContainerPackage(pkg)) {
            val titleLower = title.lowercase().trim()
            val matchesFinancialTitle = titleLower.contains("gopay") ||
                    titleLower.contains("gojek") ||
                    titleLower.contains("shopee") ||
                    titleLower.contains("ovo") ||
                    titleLower.contains("bni") ||
                    titleLower.contains("wondr") ||
                    titleLower.contains("cimb") ||
                    titleLower.contains("octo") ||
                    titleLower.contains("bca") ||
                    titleLower.contains("dana")
            if (matchesFinancialTitle) {
                return true
            }
        }

        return false
    }

    fun isTargetApp(packageName: String, title: String = "", text: String = ""): Boolean {
        return isFinancialApp(packageName, title)
    }

    private fun lowerText(s: String) = s.lowercase()

    fun isCashbackOrCoinNotification(title: String, content: String): Boolean {
        val titleLower = title.lowercase().trim()
        val contentLower = content.lowercase().trim()
        val fullText = "$titleLower $contentLower"

        val isRealMoneyTx = fullText.contains("transfer ke") ||
                fullText.contains("transfer dari") ||
                fullText.contains("transfer saldo") ||
                fullText.contains("transferan") ||
                fullText.contains("kirim saldo") ||
                fullText.contains("kamu telah mengirim") ||
                fullText.contains("kamu telah transfer") ||
                fullText.contains("kamu telah membayar") ||
                fullText.contains("pembayaran di") ||
                fullText.contains("pembayaran ke") ||
                fullText.contains("bayar ke") ||
                fullText.contains("membayar") ||
                fullText.contains("menerima transfer") ||
                fullText.contains("dapat transfer") ||
                fullText.contains("dapet transfer") ||
                fullText.contains("top up") ||
                fullText.contains("topup") ||
                fullText.contains("isi saldo") ||
                fullText.contains("saldo masuk") ||
                fullText.contains("uang masuk") ||
                fullText.contains("dana masuk") ||

                fullText.contains("transfer to") ||
                fullText.contains("transfer from") ||
                fullText.contains("transfer of") ||
                fullText.contains("transferred to") ||
                fullText.contains("transferred from") ||
                fullText.contains("transferred") ||
                fullText.contains("sent") ||
                fullText.contains("transfer successfu") ||
                fullText.contains("sent to") ||
                fullText.contains("you've sent") ||
                fullText.contains("you sent") ||
                fullText.contains("you have sent") ||
                fullText.contains("you've transferred") ||
                fullText.contains("you transferred") ||
                fullText.contains("paid to") ||
                fullText.contains("paid at") ||
                fullText.contains("you've paid") ||
                fullText.contains("you paid") ||
                fullText.contains("payment to") ||
                fullText.contains("payment at") ||
                fullText.contains("payment of") ||
                fullText.contains("payment for") ||
                fullText.contains("received a transfer") ||
                fullText.contains("received from") ||
                fullText.contains("you received") ||
                fullText.contains("you've received") ||
                fullText.contains("transfer received") ||
                fullText.contains("money received") ||
                fullText.contains("sent you") ||
                fullText.contains("has sent you") ||
                fullText.contains("balance increased") ||
                fullText.contains("added to your") ||

                fullText.contains("has been received") ||
                fullText.contains("received in your") ||
                fullText.contains("has been transferred") ||
                fullText.contains("successfully transferred") ||
                fullText.contains("ditambahkan")
        if (isRealMoneyTx) {
            return false
        }

        val titleIgnoreKeywords = listOf(
            "cashback", "cash back", "koin", "coin", "coins",
            "ovo point", "ovo points", "shopee koin", "koin shopee", "gopay coin", "gopay coins"
        )
        if (titleIgnoreKeywords.any { titleLower.contains(it) }) {
            return true
        }

        if (fullText.contains("koin shopee") || fullText.contains("shopee koin") ||
            fullText.contains("gopay coin") || fullText.contains("gopay coins") ||
            fullText.contains("ovo point") || fullText.contains("ovo points") ||
            fullText.contains("tokopedia koin") || fullText.contains("koin tokopedia")) {
            return true
        }

        val coinAmountPattern = Pattern.compile(
            """(?:\b|dapat|dapet|mendapatkan|menerima|bonus|reward|tambah|bertambah|earned|received)\s*[0-9.,]+\s*(?:koin|coin|coins|point|points)\b""",
            Pattern.CASE_INSENSITIVE
        )
        if (coinAmountPattern.matcher(fullText).find()) {
            return true
        }

        val coinReversedPattern = Pattern.compile(
            """(?:koin|coin|coins|point|points)\s*(?:kamu\s*)?(?:sebesar\s*)?[0-9.,]+""",
            Pattern.CASE_INSENSITIVE
        )
        if (coinReversedPattern.matcher(fullText).find()) {
            return true
        }

        val cashbackReceivePattern = Pattern.compile(
            """\b(?:dapat|dapet|mendapatkan|menerima|terima|diberikan|bonus|earned|received)\s+cash\s*back\b""",
            Pattern.CASE_INSENSITIVE
        )
        if (cashbackReceivePattern.matcher(fullText).find()) {
            return true
        }

        val cashbackAmountPattern = Pattern.compile(
            """cash\s*back\s+(?:sebesar\s+|of\s+)?(?:rp\.?\s*[0-9.,]+|[0-9]{3,}(?!\s*%))""",
            Pattern.CASE_INSENSITIVE
        )
        if (cashbackAmountPattern.matcher(fullText).find()) {
            return true
        }

        val cashbackStatusPattern = Pattern.compile(
            """cash\s*back\s+(?:kamu\s+)?(?:telah|berhasil|sukses|was|has been)?\s*(?:masuk|ditambahkan|dikreditkan|diterima|credited|added|received)""",
            Pattern.CASE_INSENSITIVE
        )
        if (cashbackStatusPattern.matcher(fullText).find()) {
            return true
        }

        return false
    }

    fun isPromotionalOrMarketingNotification(title: String, content: String): Boolean {
        val titleLower = title.lowercase().trim()
        val contentLower = content.lowercase().trim()
        val fullLower = "$titleLower $contentLower"

        if (fullLower.contains("pengembalian dana") || fullLower.contains("dana dikembalikan") ||
            fullLower.contains("saldo dikembalikan") || fullLower.contains("refund berhasil") ||
            fullLower.contains("refund successful") || fullLower.contains("refund of")) {
            return false
        }

        val hasConfirmedTransaction = hasTransactionConfirmation(fullLower)
        val isTinyBaitPrice = Regex("""\brp\.?\s*[01]\b(?![.,][0-9]{3}|\d)""", RegexOption.IGNORE_CASE).containsMatchIn(fullLower)
        val isGameOrBait = fullLower.contains("nonton anime") || fullLower.contains("main game") ||
                fullLower.contains("spin & win") || fullLower.contains("check-in") || fullLower.contains("check in") ||
                fullLower.contains("klaim paket") || fullLower.contains("klaim hadiah") || fullLower.contains("klaim kupon")

        if (hasConfirmedTransaction && !isTinyBaitPrice && !isGameOrBait) {
            return false
        }

        val promoKeywords = listOf(
            "klaim", "claim", "klaim sekarang", "claim now", "klaim paket", "klaim hadiah", "klaim kupon", "klaim voucher",
            "check-in", "check in", "checkin", "yuk check-in",
            "nonton anime", "main game", "mainkan", "misi", "mission", "selesaikan misi", "spin & win", "spin wheel", "roda keberuntungan",
            "spesial buat kamu", "spesial untukmu", "khusus untukmu", "khusus buat kamu",
            "just for you", "only for you", "buat kamu:", "buat kamu!", "untukmu:", "untukmu!",
            "mulai dari rp", "mulai rp", "starting from", "start from", "as low as",
            "cuma rp", "hanya rp", "serba rp", "hanya dengan rp", "cuma bayar rp", "hanya bayar rp",
            "diskon hingga", "potongan hingga", "cashback hingga", "hemat hingga", "diskon s.d.", "cashback s.d.",
            "flash sale", "mega sale", "super sale", "cuci gudang", "promo gajian", "waktu indonesia belanja",
            "buruan", "serbu", "jangan lewatkan", "yuk ikutan", "dapatkan promo", "nikmati diskon",
            "cobain sekarang", "mumpung", "kesempatan emas", "kado buat kamu", "kejutan spesial",
            "kode promo", "pakai voucher", "gunakan kode", "voucher diskon", "kupon hemat",
            "ajukan pinjaman", "ajukan paylater", "aktifkan paylater", "aktipkan paylater",
            "limit paylater", "limit pinjaman", "dapatkan limit", "pinjaman cepat cair", "dana siaga",
            "kode otp", "kode verifikasi", "otp anda", "security alert", "jaga kerahasiaan",
            "beri rating", "beri ulasan", "bagikan pengalaman", "kasih bintang",
            "driver sedang menuju", "driver menuju lokasi", "pesanan sedang dipersiapkan", "pesanan sedang diantar"
        )

        for (kw in promoKeywords) {
            if (fullLower.contains(kw)) {
                return true
            }
        }

        if (isTinyBaitPrice) {
            val baitContextWords = listOf("paket", "kuota", "data", "voucher", "diskon", "promo", "anime", "gratis", "free", "cashback")
            if (baitContextWords.any { fullLower.contains(it) }) {
                return true
            }
        }

        val promoRegexes = listOf(
            Regex("""\b(?:paket\s+data|kuota|pulsa)\s+rp\s*[0-9]+""", RegexOption.IGNORE_CASE),
            Regex("""\b(?:diskon|potongan|hemat|cashback)\s+[0-9]+%""", RegexOption.IGNORE_CASE),
            Regex("""\b(?:klaim|dapatkan|serbu|nikmati)\s+(?:sekarang|hadiah|bonus|paket)\b""", RegexOption.IGNORE_CASE)
        )
        for (rgx in promoRegexes) {
            if (rgx.containsMatchIn(fullLower)) {
                return true
            }
        }

        return false
    }

    fun hasTransactionConfirmation(fullText: String): Boolean {
        val textLower = fullText.lowercase().trim()

        if (textLower.contains("pengembalian dana") || textLower.contains("dana dikembalikan") ||
            textLower.contains("saldo dikembalikan") || textLower.contains("refund")) {
            return true
        }

        val confirmationKeywords = listOf(
            "berhasil", "sukses", "successful", "successfully", "sucessfully",
            "telah dibayar", "telah dipotong", "telah dikirim", "telah didebit", "telah terpotong",
            "kamu telah membayar", "kamu telah mengirim", "kamu telah transfer", "kamu berhasil bayar", "kamu berhasil transfer",
            "pembayaran sebesar", "pembayaran qris", "transaksi qris", "pembayaran di", "pembayaran ke",
            "transfer sebesar", "transfer ke", "transfer saldo ke", "kirim saldo ke",
            "you've paid", "you paid", "you have paid", "you've sent", "you sent", "you have sent", "you've transferred",
            "sent you", "sent rp",
            "charged", "charge of", "been charged", "debited", "debit rekening", "terpotong dari", "dipotong dari",
            "telah diterima", "telah masuk", "telah ditambahkan", "telah dikredit",
            "kamu menerima", "kamu dapat transfer", "kamu dapet transfer",
            "transfer dari", "transfer saldo dari", "saldo masuk", "dana masuk", "uang masuk",
            "top up sebesar", "top up berhasil", "isi saldo berhasil", "penambahan saldo",
            "you've received", "you received", "you have received", "has been received", "money received",
            "credited to", "kredit rekening", "setoran tunai", "transaksi kredit", "kredit rp", "masuk ke rekening",
            "ngirim kamu", "ngirim saldo", "mengirim kamu", "mengirimkan kamu",
            "db:", "cr:", "debit:", "kredit:", "m-transfer", "qris"
        )

        return confirmationKeywords.any { textLower.contains(it) }
    }

    fun parse(packageName: String, title: String, content: String, isClonedUser: Boolean = false): ParsedNotificationResult? {

        if (!isFinancialApp(packageName, title)) {
            return null
        }

        val fullText = "$title $content".trim()
        val textLower = fullText.lowercase()

        if (containsEmoji(title) || containsEmoji(content)) {
            return null
        }

        if (isCashbackOrCoinNotification(title, content)) {
            return null
        }

        if (isPromotionalOrMarketingNotification(title, content)) {
            return null
        }

        if (!hasTransactionConfirmation(fullText)) {
            return null
        }

        val (appName, targetBank) = detectAppAndBank(packageName, title, content, isClonedUser)
        if (targetBank.isEmpty()) return null

        val amount = extractAmount(fullText) ?: return null
        if (amount <= 0) return null

        val isIncome = detectIsIncome(textLower)
        val type = if (isIncome) Transaction.TYPE_INCOME else Transaction.TYPE_EXPENSE

        val destination = extractDestinationOrSender(title, content, isIncome)

        val isBank = isBankAppOrTarget(packageName, targetBank)
        val category = detectCategory(textLower, isIncome, isBank, destination)

        val note = if (!destination.isNullOrEmpty()) {
            if (isIncome) "From: $destination" else "To: $destination"
        } else {
            if (isIncome) "$targetBank Income" else "$targetBank Transaction"
        }

        return ParsedNotificationResult(
            isValidTransaction = true,
            appName = appName,
            targetBankName = targetBank,
            type = type,
            amount = amount,
            category = category,
            note = note,
            rawText = fullText,
            destination = destination
        )
    }

    private fun detectAppAndBank(packageName: String, title: String, content: String, isClonedUser: Boolean = false): Pair<String, String> {
        val pkg = packageName.lowercase().trim()
        if (!isFinancialApp(pkg, title)) return Pair("", "")

        val titleLower = title.lowercase().trim()
        val contentLower = content.lowercase().trim()
        val combined = "$titleLower $contentLower"
        val isContainer = isContainerPackage(pkg)

        if (isGoPayPackage(pkg) || (isContainer && (titleLower.contains("gopay") || titleLower.contains("gojek")))) {
            val isClone = isClonedUser ||
                    pkg.contains("clone") || pkg.contains("dual") || pkg.contains("parallel") || pkg.contains("multi") ||
                    pkg.contains("excelliance") || titleLower.contains("clone") ||
                    titleLower.contains("gopay 2") || titleLower.contains("gopay2") || titleLower.contains("gopay (2)")
            val appLabel = if (isClone) "GoPay Clone" else "GoPay"
            val targetBank = if (isClone) "GoPay Clone" else "GoPay"
            return Pair(appLabel, targetBank)
        }

        if (isShopeePackage(pkg) || (isContainer && titleLower.contains("shopee"))) {
            return Pair("ShopeePay", "ShopeePay")
        }

        if (isOvoPackage(pkg, combined) || (isContainer && titleLower.contains("ovo"))) {
            return Pair("OVO", "OVO")
        }

        if (isCimbPackage(pkg) || (isContainer && (titleLower.contains("cimb") || titleLower.contains("octo")))) {
            if (combined.contains("octopay") || combined.contains("octo pay")) {
                return Pair("OctoPay", "OctoPay")
            }
            return Pair("Bank CIMB", "Bank CIMB")
        }

        if (isBniPackage(pkg) || (isContainer && (titleLower.contains("bni") || titleLower.contains("wondr")))) {
            return Pair("Bank BNI", "Bank BNI")
        }

        if (isBcaPackage(pkg) || (isContainer && titleLower.contains("bca"))) {
            return Pair("BCA", "BCA")
        }

        if (isMandiriPackage(pkg) || (isContainer && (titleLower.contains("mandiri") || titleLower.contains("livin")))) {
            return Pair("Bank Mandiri", "Bank Mandiri")
        }

        if (isBriPackage(pkg) || (isContainer && (titleLower.contains("bri") || titleLower.contains("brimo")))) {
            return Pair("Bank BRI", "Bank BRI")
        }

        if (isDanaPackage(pkg) || (isContainer && titleLower.contains("dana"))) {
            return Pair("DANA", "DANA")
        }

        return Pair("", "")
    }

    private fun detectIsIncome(textLower: String): Boolean {

        val isExplicitRefund = textLower.contains("pengembalian dana") ||
                textLower.contains("dana dikembalikan") ||
                textLower.contains("saldo dikembalikan") ||
                textLower.contains("uang dikembalikan") ||
                textLower.contains("dikembalikan") ||
                textLower.contains("refund") ||
                textLower.contains("refund berhasil") ||
                textLower.contains("refund of")
        if (isExplicitRefund) {
            return true
        }

        val hasChargeKeywords = textLower.contains("charged") ||
                textLower.contains("been charged") ||
                textLower.contains("have been charged") ||
                textLower.contains("has been charged") ||
                textLower.contains("charge of") ||
                textLower.contains("dikenakan biaya") ||
                textLower.contains("dibebankan") ||
                textLower.contains("terpotong") ||
                textLower.contains("dipotong") ||
                textLower.contains("terdebit") ||
                textLower.contains("didebit")

        if (hasChargeKeywords) {
            return false
        }

        val incomingExplicitKeywords = listOf(

            "pengembalian dana", "dana dikembalikan", "saldo dikembalikan", "uang dikembalikan",
            "transfer dari", "transfer saldo dari", "transferan dari", "kiriman dari", "pengiriman dari",
            "dapat transfer", "dapet transfer", "dapat transferan", "dapet transferan",
            "menerima transfer", "terima transfer", "menerima saldo", "menerima uang",
            "menerima dana", "diterima dari", "diterima",
            "transfer masuk", "saldo masuk", "dana masuk", "uang masuk",
            "transaksi kredit", "kredit rekening", "rekening dikredit", "rekening dikreditkan",
            "kredit sebesar", "cr:", "cr ", "cr.", "setoran tunai",
            "masuk ke saldo", "masuk ke gopay", "masuk ke shopeepay", "masuk ke ovo", "masuk ke octopay", "masuk ke akun",
            "top up", "topup", "top-up", "isi saldo", "pengisian saldo", "isi ulang",
            "tambah saldo", "penambahan saldo", "saldo bertambah",
            "berhasil ditambahkan", "berhasil ditambah",
            "kamu dapat transfer", "kamu dapet transfer", "refund",
            "ngirim kamu", "ngirim ke kamu", "ngirimkan ke kamu",
            "mengirim kamu", "mengirimkan kamu", "mengirim ke kamu", "mengirimkan ke kamu",
            "mentransfer kamu", "mentransfer ke kamu",
            "transfer ke kamu", "transfer untuk kamu", "transfer buat kamu",
            "reimburse", "reimbursement",

            "transfer from", "transferred from", "received from", "received a transfer",
            "received transfer", "you received", "you've received", "you have received",
            "money received", "transfer received", "received money", "received funds",
            "sent you ", "has sent you ", "transferred you ", "incoming transfer",
            "transferred to you", "sent to you",
            "balance increased", "increased by", "added to your", "credited to your",
            "deposit of", "deposited to", "deposited",

            "has been received", "has been received in your", "received in your",
            "been received in your gopay", "been received in"
        )
        if (incomingExplicitKeywords.any { textLower.contains(it) || textLower.endsWith(it.trimEnd()) }) {
            return true
        }

        val regexIncomeFrom = Regex("""\b(?:transfer|transferred|transferan|kiriman|saldo|dana|uang|money|funds|balance|received)\b.*?\b(?:dari|from)\s+""", RegexOption.IGNORE_CASE)
        val regexSentYou = Regex("""^[A-Za-z0-9\s.'_-]+\s+(?:has\s+)?sent\s+you\b""", RegexOption.IGNORE_CASE)
        val regexActiveIncome = Regex("""(?:^|[\.\!\?]\s*|\b)[A-Za-z0-9\s.'_-]+\s+(?:telah\s+|baru\s+(?:aja|saja)\s+)?(?:mengirim(?:kan)?|kirim|ngirim|mentransfer|transfer|sent)\s+(?:kamu\b|ke\s+kamu\b|you\b|to\s+you\b)""", RegexOption.IGNORE_CASE)
        if ((regexIncomeFrom.containsMatchIn(textLower) || regexSentYou.containsMatchIn(textLower) || regexActiveIncome.containsMatchIn(textLower)) &&
            !textLower.contains("dipotong dari") &&
            !textLower.contains("terpotong dari") &&
            !textLower.contains("debit dari") &&
            !textLower.contains("deducted from") &&
            !textLower.contains("charged") &&
            !textLower.contains("from your balance") &&
            !textLower.contains("from your account") &&
            !textLower.contains("from your gopay") &&
            !textLower.contains("from your shopeepay") &&
            !textLower.contains("from your ovo") &&
            !textLower.contains("from gopay") &&
            !textLower.contains("from shopeepay") &&
            !textLower.contains("from ovo") &&
            !textLower.contains("from your wallet") &&
            !textLower.contains("order from") &&
            !textLower.contains("pesanan dari") &&
            !textLower.contains("purchase from") &&
            !textLower.contains("potong dari") &&
            !textLower.contains("dari saldo") &&
            !textLower.contains("dari rekening")) {
            return true
        }

        val outgoingExplicit = listOf(

            "you've been charged", "you have been charged", "have been charged", "has been charged",
            "been charged", "charged for", "charged to", "charged by", "charge of", "charged", "charge",

            "transfer ke", "transfer saldo ke", "kirim ke", "kirim saldo ke",
            "kamu telah transfer", "kamu telah mengirim", "kamu telah membayar",
            "kamu berhasil transfer", "kamu berhasil kirim", "kamu berhasil bayar",
            "berhasil transfer", "berhasil kirim", "berhasil bayar", "berhasil dikirim",
            "saldo berhasil dikirim", "saldo kamu berhasil", "pengiriman saldo",
            "pembayaran ke", "pembayaran di", "pembayaran untuk",
            "pembayaran qris", "transaksi qris", "qris sebesar", "qris di", "qris ke",
            "transaksi debit", "debit rekening", "debit sebesar", "rekening didebit",
            "dikenakan biaya", "dibebankan", "terpotong", "dipotong", "terdebit", "didebit",
            "tarik tunai", "penarikan dana", "pembayaran tagihan", "db:", "db ",
            "bayar ke", "bayar di", "membayar ke", "membayar di", "membayar",
            "pembelian di", "beli di", "pesanan di", "pesanan dari", "ke rekening", "ke bank",

            "transfer to", "transferred to", "transfer of",
            "you've transferred", "you transferred", "you have transferred",
            "successfully transferred", "successfully sent", "successfully paid",
            "sucessfully transferred", "has been transferred", "been transferred to",
            "transferred your", "sent your", "balance was sent", "balance has been sent",
            "balance has been transferred",
            "you've sent", "you sent", "you have sent", "sent to",
            "sent", "transfer successfu", "transfer successful",
            "you've paid", "you paid", "you have paid", "paid to", "paid at", "paid for",
            "payment to", "payment at", "payment for", "payment of",
            "purchase at", "purchase from", "order at", "order from", "to bank account", "to account"
        )
        val hasExplicitOutgoingKeywords = outgoingExplicit.any { textLower.contains(it) } &&
                !textLower.contains("ke saldo kamu") &&
                !textLower.contains("ke akun kamu") &&
                !textLower.contains("ke gopay kamu") &&
                !textLower.contains("ke shopeepay kamu") &&
                !textLower.contains("ke ovo kamu") &&
                !textLower.contains("ke octopay kamu") &&
                !textLower.contains("to your balance") &&
                !textLower.contains("to your gopay") &&
                !textLower.contains("to your account")

        if (hasExplicitOutgoingKeywords) {
            return false
        }

        val regexOutgoingTransfer = Regex("""\b(?:transfer|transferred|kirim|kiriman|send|sent|paid|bayar|dikirim)\b.*?\b(?:ke|to)\s+""", RegexOption.IGNORE_CASE)
        val isRegexOutgoing = regexOutgoingTransfer.containsMatchIn(textLower) &&
                !textLower.contains("ke saldo kamu") &&
                !textLower.contains("ke akun kamu") &&
                !textLower.contains("ke gopay kamu") &&
                !textLower.contains("ke shopeepay kamu") &&
                !textLower.contains("ke ovo kamu") &&
                !textLower.contains("ke octopay kamu") &&
                !textLower.contains("to your balance") &&
                !textLower.contains("to your gopay") &&
                !textLower.contains("to your account")

        if (isRegexOutgoing) {
            return false
        }

        if ((textLower.contains("dari ") || textLower.contains("from ")) &&
            !textLower.contains("dipotong dari") &&
            !textLower.contains("terpotong dari") &&
            !textLower.contains("debit dari") &&
            !textLower.contains("deducted from") &&
            !textLower.contains("charged") &&
            !textLower.contains("from your balance") &&
            !textLower.contains("from your account") &&
            !textLower.contains("from your gopay") &&
            !textLower.contains("from your shopeepay") &&
            !textLower.contains("from your ovo") &&
            !textLower.contains("from gopay") &&
            !textLower.contains("from shopeepay") &&
            !textLower.contains("from ovo") &&
            !textLower.contains("from your wallet") &&
            !textLower.contains("order from") &&
            !textLower.contains("pesanan dari") &&
            !textLower.contains("purchase from") &&
            !textLower.contains("potong dari") &&
            !textLower.contains("dari saldo") &&
            !textLower.contains("dari rekening") &&
            !textLower.contains("sumber dana")
        ) {
            return true
        }

        val generalIncomeKeywords = listOf(
            "masuk", "bertambah", "ditambahkan", "kamu dapat", "kamu dapet", "diisi",
            "received", "added to", "credited to"
        )
        if (generalIncomeKeywords.any { textLower.contains(it) }) {
            return true
        }

        return false
    }

    fun extractAmount(text: String): Double? {

        val contextPattern = Pattern.compile(
            """(?:sebesar|sebesar\s+rp\.?|transfer\s+of|payment\s+of|top\s*up\s+(?:of\s+)?|isi\s+saldo|tambah\s+saldo|bertambah|ditambah|increased\s+by|added|bayar|paid|membayar|transfer|transferred|kirim|mengirim|sent|menerima|received|charged\s+(?:of\s+)?|charge\s+of)\s+(?:rp\.?\s*)?([0-9]{1,3}(?:[.,][0-9]{3})+(?:[.,][0-9]{2})?|[0-9]{4,})""",
            Pattern.CASE_INSENSITIVE
        )
        val contextMatcher = contextPattern.matcher(text)
        if (contextMatcher.find()) {
            val numStr = contextMatcher.group(1)
            val amt = numStr?.let { cleanAmountString(it) }
            if (amt != null && amt > 0) return amt
        }

        val rpPattern = Pattern.compile(
            """(?:rp|idr)\.?\s*([0-9]{1,3}(?:[.,][0-9]{3})+(?:[.,][0-9]{2})?|[0-9]+(?:[.,][0-9]{2})?)""",
            Pattern.CASE_INSENSITIVE
        )
        val matcher = rpPattern.matcher(text)
        if (matcher.find()) {
            val numberStr = matcher.group(1) ?: return null
            return cleanAmountString(numberStr)
        }

        val fallbackPattern = Pattern.compile(
            """(?:sebesar|bayar|paid|payment|transfer|transferred|sent|received|top\s*up|isi\s+saldo|charged)\s+([0-9]{1,3}(?:[.,][0-9]{3})+|[0-9]{4,})""",
            Pattern.CASE_INSENSITIVE
        )
        val fallbackMatcher = fallbackPattern.matcher(text)
        if (fallbackMatcher.find()) {
            val numberStr = fallbackMatcher.group(1) ?: return null
            return cleanAmountString(numberStr)
        }

        return null
    }

    fun cleanAmountString(str: String): Double? {
        return try {
            var s = str.trim()

            if (s.endsWith(",00") || s.endsWith(".00")) {
                s = s.substring(0, s.length - 3)
            }

            if (s.contains(".") && s.contains(",")) {
                if (s.lastIndexOf(".") > s.lastIndexOf(",")) {

                    s = s.replace(",", "")
                } else {

                    s = s.replace(".", "").replace(",", ".")
                }
            } else if (s.contains(",")) {

                if (s.matches(Regex(""".*,\d{3}(?:,\d{3})*"""))) {
                    s = s.replace(",", "")
                } else {

                    s = s.replace(",", ".")
                }
            } else if (s.contains(".")) {

                if (s.matches(Regex(""".*\.\d{3}(?:\.\d{3})*"""))) {
                    s = s.replace(".", "")
                }
            }
            s.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    fun extractDestinationOrSender(title: String, content: String, isIncome: Boolean): String? {
        val textsToSearch = listOf(content, title)

        if (isIncome) {
            val combined = "$title $content".lowercase()
            if (combined.contains("pengembalian dana") || combined.contains("dana dikembalikan") ||
                combined.contains("saldo dikembalikan") || combined.contains("refund")) {
                return "Pengembalian Dana"
            }

            val patterns = listOf(

                Pattern.compile("""(?:a\.?n\.?|atas\s+nama|a/n)\s+([A-Za-z0-9\s&.'_-]{2,35}?)(?:\s+sebesar|\s+sejumlah|\s+rp\.?|\s+idr|\s+berhasil|\s+sukses|\s+pada|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:^|[\.\!\?]\s*|\b)([A-Za-z0-9&.'_-]+(?:\s+[A-Za-z0-9&.'_-]+)?)\s+(?:telah\s+|baru\s+(?:aja|saja)\s+)?(?:mengirim(?:kan)?|kirim|ngirim|mentransfer|transfer|sent)\s+(?:kamu\b|ke\s+kamu\b|you\b|to\s+you\b)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:^|[\.\!\?]\s*|\b)([A-Za-z0-9&.'_-]+(?:\s+[A-Za-z0-9&.'_-]+)?)\s+(?:has\s+)?sent\s+you\b""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:ditransfer\s+oleh|dikirim\s+oleh|transferred\s+by|sent\s+by)\s+([A-Za-z0-9\s&.'_-]{2,35}?)(?:\s+sebesar|\s+sejumlah|\s+rp|\s+idr|\s+berhasil|\s+sukses|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:pengirim|nama\s+pengirim|sender|sender\s+name)\s*[:=-]?\s*([A-Za-z0-9\s&.'_-]{2,35}?)(?:\s+sebesar|\s+sejumlah|\s+rp|\s+idr|\s+berhasil|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:transfer\s+dari|transferan\s+dari|kiriman\s+dari|diterima\s+dari|dana\s+masuk\s+dari|saldo\s+masuk\s+dari|uang\s+masuk\s+dari|transaksi\s+masuk\s+dari|kredit\s+dari|dari|pengirim|transfer\s+from|transferred\s+from|received\s+from|from)\s+([A-Za-z0-9\s&.'_-]{2,35}?)(?:\s+sebesar|\s+sejumlah|\s+rp\.?|\s+idr|\s+berhasil|\s+sukses|\s+telah|\s+of|\s+was|\s+to|\s+ke|\s+pada|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:has\s+been\s+received|been\s+received)\s+in\s+your\s+\w+(?:\s+\w+)?\s+from\s+([A-Za-z0-9\s&.'_-]{2,35}?)(?:\s+sebesar|\s+sejumlah|\s+rp\.?|\s+idr|\s+berhasil|\s+of|\s+was|\s+to|\s+ke|\s+pada|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:top\s*up\s+(?:saldo\s+)?(?:of\s+rp\.?\s*[0-9.,]+\s+)?via)\s+([A-Za-z0-9\s&.'_-]{2,30}?)(?:\s+sebesar|\s+berhasil|\s+was|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE)
            )

            for (text in textsToSearch) {
                if (text.isBlank()) continue
                for (p in patterns) {
                    val m = p.matcher(text)
                    if (m.find()) {
                        val raw = m.group(1)?.trim()
                        val cleaned = cleanTargetEntity(raw)
                        if (!cleaned.isNullOrEmpty()) return cleaned
                    }
                }
            }

            if (isSenderContactTitle(title)) {
                val cleaned = cleanTargetEntity(title)
                if (!cleaned.isNullOrEmpty()) return cleaned
            }
        } else {

            val patterns = listOf(

                Pattern.compile("""(?:(?:you(?:'ve|\s+have)?\s+been|has\s+been|been)?\s*charged\s+(?:rp\.?\s*[0-9.,]+\s+)?(?:for\s+(?:your\s+)?(?:transaction|order|payment|purchase)\s+)?(?:at|to|from|by|in)\s+)([A-Za-z0-9\s&.'_()-]{2,40}?)(?:\s+sebesar|\s+berhasil|\s+pada|\s+using|\s+menggunakan|\s+was|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),
                Pattern.compile("""(?:order\s+from|pesanan\s+dari)\s+([A-Za-z0-9\s&.'_()-]{2,40}?)(?:\s+sebesar|\s+berhasil|\s+pada|\s+was|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:transfer\s+(?:saldo\s+)?(?:ke\s+rekening\s+|ke\s+bank\s+|ke\s+)|transfer\s+(?:of\s+rp\.?\s*[0-9.,]+\s+)?to\s+|transferred\s+(?:(?:saldo|balance|your\s+gopay\s+balance|rp\.?\s*[0-9.,]+)\s+)?to\s+|kirim\s+(?:saldo\s+)?ke\s+|sent\s+(?:(?:saldo|balance|rp\.?\s*[0-9.,]+)\s+)?to\s+)\s*([A-Za-z0-9\s&.'_()-]{2,40}?)(?:\s+sebesar|\s+berhasil|\s+pada|\s+menggunakan|\s+of|\s+was|\s+using|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:membayar|paid)\s+(?:rp\.?\s*[0-9.,]+\s+)?(?:menggunakan\s+[A-Za-z0-9\s]+\s+|using\s+[A-Za-z0-9\s]+\s+)?(?:ke|di|to|at)\s+([A-Za-z0-9\s&.'_()-]{2,40}?)(?:\s+sebesar|\s+berhasil|\s+pada|\s+was|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:pembayaran\s+(?:qris\s+)?|transaksi\s+(?:qris\s+)?|qris\s+|pembayaran|bayar|transaksi|payment)\s+(?:(?:sebesar|of)\s+rp\.?\s*[0-9.,]+\s+)?(?:di|ke|untuk|at|to|for)\s+([A-Za-z0-9\s&.'_()-]{2,40}?)(?:\s+sebesar|\s+berhasil|\s+pada|\s+menggunakan|\s+was|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE),

                Pattern.compile("""(?:\s+ke\s+|\s+di\s+|\s+to\s+|\s+at\s+)([A-Za-z0-9\s&.'_()-]{2,35}?)(?:\s+sebesar|\s+berhasil|\s+menggunakan|\s+was|\s+using|\s*$|\.|\,)""", Pattern.CASE_INSENSITIVE)
            )

            for (text in textsToSearch) {
                if (text.isBlank()) continue
                for (p in patterns) {
                    val m = p.matcher(text)
                    if (m.find()) {
                        val raw = m.group(1)?.trim()
                        val cleaned = cleanTargetEntity(raw)
                        if (!cleaned.isNullOrEmpty()) return cleaned
                    }
                }
            }

            val specificServices = listOf(
                "ShopeeFood", "GoFood", "GrabFood", "GoRide", "GrabBike", "GoCar", "GrabCar",
                "Indomaret", "Alfamart", "Tokopedia", "Shopee", "Kopi Kenangan", "Starbucks", "Solaria",
                "Cinema XXI", "XXI", "CGV", "Cinepolis", "RS Siloam", "Kimia Farma", "Apotek K-24",
                "Celebrity Fitness", "Traveloka", "Tiket.com", "Kitabisa"
            )
            for (service in specificServices) {
                if (content.contains(service, ignoreCase = true) || title.contains(service, ignoreCase = true)) {
                    return service
                }
            }

            val churchMatch = Regex("""\b(GKI|GBI|GMS|HKBP|GPIB|JPCC|Tiberias|NDC|Bethany|Bethel)\b""", RegexOption.IGNORE_CASE).find("$title $content")
            if (churchMatch != null) {
                return churchMatch.value.uppercase()
            }
        }

        return null
    }

    private fun isSenderContactTitle(title: String): Boolean {
        if (title.isBlank()) return false
        val t = title.lowercase().trim()
        val nonSenderTitles = listOf(
            "gopay", "gopay clone", "gopay 2", "gopay2", "gojek", "shopee", "shopeepay", "ovo", "dana", "octopay",
            "bca", "bca mobile", "mybca", "bni", "bni mobile", "bni mobile banking", "wondr", "wondr by bni",
            "livin", "livin' by mandiri", "mandiri", "bank mandiri", "bri", "brimo", "bank bri",
            "cimb", "octo mobile", "bank cimb", "transfer", "transfer berhasil", "transfer masuk",
            "transaksi", "transaksi berhasil", "saldo masuk", "dana masuk", "uang masuk",
            "notifikasi", "pembayaran", "payment", "successful", "berhasil", "m-transfer", "qris",
            "bank", "promo", "info", "sent", "transfer successfu", "transfer successful"
        )
        if (nonSenderTitles.any { t == it || t.startsWith("$it ") || t.endsWith(" $it") }) {
            return false
        }
        return title.length in 2..35 && !title.matches(Regex("""^[0-9\s.,-]+$"""))
    }

    private fun cleanTargetEntity(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        var s = raw.trim()

        s = s.replace(Regex("^(?i)(?:dari|ke|from|to|at)\\s+"), "")

        val anMatch = Regex("(?i)(?:a\\.?n\\.?|atas\\s+nama|a/n)\\s+([A-Za-z0-9\\s&.'_-]+)").find(s)
        if (anMatch != null) {
            s = anMatch.groupValues[1].trim()
        }

        s = s.replace(Regex("^(?i)(?:rekening|rek|bank)\\s+(?:[0-9-]+\\s*)?"), "").trim()

        s = s.replace(Regex("(?i)\\s+(?:gopay|gopay clone|gopay 2|shopeepay|ovo|bni|cimb|octopay)$"), "").trim()

        s = s.replace(Regex("(?i)\\s+(?:sebesar|sejumlah)?\\s*(?:rp|idr)\\.?\\s*[0-9.,]+.*$"), "").trim()
        s = s.replace(Regex("(?i)\\s+sebesar\\s+.*$"), "").trim()

        val noiseWords = listOf(
            "berhasil", "sebesar", "sejumlah", "menggunakan", "saldo", "aplikasi", "rp", "idr", "pada", "shopeepay", "gopay", "ovo", "bni", "cimb", "octopay",
            "was", "successful", "successfully", "using", "balance", "app", "account", "on", "at", "for", "of", "to", "from", "rekening", "dana",
            "kamu", "you", "anda", "saya", "aku"
        )
        if (noiseWords.contains(s.lowercase())) return null

        s = s.trimEnd('.', ',', '!', '?', '-', ' ')
        if (s.length < 2) return null

        if (s.matches(Regex("""^[0-9\s-]+$"""))) return null

        return s
    }

    fun isOutgoingTransfer(textLower: String): Boolean {

        val transferKeywords = listOf(
            "transfer ke", "transfer saldo ke", "kirim ke", "kirim saldo ke", "kiriman ke", "pengiriman ke",
            "transfer to", "transferred to", "transfer of", "transferred your",
            "kamu telah transfer", "kamu telah mengirim",
            "kamu berhasil transfer", "kamu berhasil kirim",
            "berhasil transfer", "berhasil kirim", "berhasil dikirim",
            "saldo berhasil dikirim", "saldo kamu berhasil", "pengiriman saldo",
            "you've transferred", "you transferred", "you have transferred",
            "successfully transferred", "sucessfully transferred",
            "has been transferred", "been transferred to",
            "balance was sent", "balance has been sent", "balance has been transferred",
            "you've sent", "you sent", "you have sent", "sent to",
            "sent", "transfer successfu", "transfer successful",
            "m-transfer", "transfer dana", "transfer keluar", "kirim uang",
            "ke rekening", "ke bank", "to bank account", "to account"
        )
        if (transferKeywords.any { textLower.contains(it) }) return true

        val regexOutgoingTransfer = Regex(
            """\b(?:transfer|transferred|kirim|kiriman|send|sent|dikirim)\b.*?\b(?:ke|to)\s+""",
            RegexOption.IGNORE_CASE
        )
        if (regexOutgoingTransfer.containsMatchIn(textLower)) return true

        if (textLower.contains("transfer") || textLower.contains("transferred") ||
            textLower.contains("kirim") || textLower.contains("mengirim") ||
            Regex("""\bsent\b""", RegexOption.IGNORE_CASE).containsMatchIn(textLower)
        ) {
            return true
        }

        return false
    }

    fun isBankAppOrTarget(packageName: String, targetBank: String): Boolean {
        val pkg = packageName.lowercase().trim()
        val tb = targetBank.lowercase().trim()
        return MBANKING_PACKAGES.contains(pkg) ||
                isBcaPackage(pkg) || isMandiriPackage(pkg) || isBniPackage(pkg) ||
                isBriPackage(pkg) || isCimbPackage(pkg) ||
                tb.startsWith("bank") || tb == "bca" || tb == "cimb" || tb == "bni" || tb == "mandiri" || tb == "bri" ||
                tb.contains("bank")
    }

    fun detectCategory(textLower: String, isIncome: Boolean, isBank: Boolean = false, destination: String? = null): String {
        if (isIncome) {

            if (isBank) {
                return when {
                    textLower.contains("gaji") || textLower.contains("salary") || textLower.contains("wage") -> "Salary"
                    textLower.contains("invest") || textLower.contains("reksadana") || textLower.contains("saham") || textLower.contains("stock") -> "Investment"
                    else -> "Reimburse"
                }
            }
            return when {
                textLower.contains("top up") || textLower.contains("topup") || textLower.contains("deposit") -> "Top Up"
                textLower.contains("cashback") || textLower.contains("hadiah") || textLower.contains("reward") -> "Cashback"
                textLower.contains("gaji") || textLower.contains("salary") || textLower.contains("wage") -> "Salary"
                textLower.contains("invest") || textLower.contains("reksadana") || textLower.contains("saham") || textLower.contains("stock") -> "Investment"
                else -> "Reimburse"
            }
        } else {
            val destLower = (destination ?: "").lowercase().trim()
            val corpus = "$destLower $textLower".trim()

            val churchRegex = Regex("""\b(?:gki|gbi|gms|hkbp|gpib|jpcc|tiberias|ndc|bethany|bethel|gsja|gkpb|gii|gereja|church|paroki|keuskupan|persembahan|perpuluhan|kolekte|tithe|offering)\b""", RegexOption.IGNORE_CASE)
            if (churchRegex.containsMatchIn(corpus)) {
                return "Church"
            }

            val healthRegex = Regex("""\b(?:apotek|apotik|pharmacy|kimia farma|k-24|k24|halodoc|alodokter|dokter|doctor|dokter gigi|klinik|clinic|siloam|mayapada|mitra keluarga|hermina|prodia|pramita|puskesmas|optik|optik seis|optik melawai|rs|rumah sakit|hospital|obat|medicine|health|healthcare)\b""", RegexOption.IGNORE_CASE)
            if (healthRegex.containsMatchIn(corpus)) {
                return "Health"
            }

            val transportRegex = Regex("""\b(?:goride|gocar|grabbike|grabcar|maxim|indrive|bluebird|blue bird|mybluebird|mrt|lrt|transjakarta|tj|krl|kai|kereta api|whoosh|kcic|damri|jaklingko|spbu|pertamina|shell|bp akr|vivo|parkir|parking|sky parking|secure parking|tol|toll|jasamarga|jasa marga|e-toll|bensin|fuel|gasoline|ojek|transport)\b""", RegexOption.IGNORE_CASE)
            if (transportRegex.containsMatchIn(corpus)) {
                return "Transportation"
            }

            val billsRegex = Regex("""\b(?:pln|listrik|token listrik|pdam|air pdam|bpjs|bpjs kesehatan|bpjs ketenagakerjaan|telkom|indihome|biznet|myrepublic|first media|cbn|pulsa|paket data|kuota|telkomsel|byu|by\.u|indosat|im3|xl axiata|axis|tri|smartfren|pbb|pajak|ipl|tagihan|utility|bill|water|electricity|wifi|internet)\b""", RegexOption.IGNORE_CASE)
            if (billsRegex.containsMatchIn(corpus)) {
                return "Bills & Utilities"
            }

            val entertainmentRegex = Regex("""\b(?:cinema xxi|xxi|cgv|cinepolis|tix id|bioskop|movie|cinema|steam|playstation|nintendo|google play|unipin|codashop|riot games|valorant|mobile legends|genshin|netflix|spotify|youtube premium|disney\+|disney plus|hbo|vidio|prime video|apple music|dufan|ancol|taman safari|timezone|inul vizta|karaoke|game|games)\b""", RegexOption.IGNORE_CASE)
            if (entertainmentRegex.containsMatchIn(corpus)) {
                return "Entertainment"
            }

            val educationRegex = Regex("""\b(?:sekolah|school|universitas|univ|kampus|campus|kuliah|spp|udemy|coursera|ruangguru|zenius|english first|ef|kumon|lia|bimbel|kursus|les|education)\b""", RegexOption.IGNORE_CASE)
            if (educationRegex.containsMatchIn(corpus)) {
                return "Education"
            }

            val fitnessRegex = Regex("""\b(?:gym|fitness|celebrity fitness|fitness first|gold's gym|f45|yoga|pilates|badminton|futsal|tenis|tennis|padel)\b""", RegexOption.IGNORE_CASE)
            if (fitnessRegex.containsMatchIn(corpus)) {
                return "Fitness"
            }

            val petsRegex = Regex("""\b(?:petshop|pet shop|dokter hewan|vet|pet care|grooming|whiskas|royal canin|kucing|anjing)\b""", RegexOption.IGNORE_CASE)
            if (petsRegex.containsMatchIn(corpus)) {
                return "Pets"
            }

            val travelRegex = Regex("""\b(?:traveloka|tiket\.com|agoda|booking\.com|airbnb|hotel|resort|aston|santika|marriott|hilton|ibis|airasia|garuda indonesia|lion air|citilink|batik air|flight|pesawat|tiket pesawat|travel)\b""", RegexOption.IGNORE_CASE)
            if (travelRegex.containsMatchIn(corpus)) {
                return "Travel"
            }

            val charityRegex = Regex("""\b(?:kitabisa|dompet dhuafa|baznas|rumah zakat|act|donasi|donation|sedekah|infaq|zakat|panti asuhan|charity)\b""", RegexOption.IGNORE_CASE)
            if (charityRegex.containsMatchIn(corpus)) {
                return "Charity / Donations"
            }

            val isShopeeMarketplace = (destLower.contains("shopee") && !destLower.contains("shopeepay")) ||
                    textLower.contains("pesanan shopee") || textLower.contains("belanja di shopee")
            val shoppingRegex = Regex("""\b(?:indomaret|alfamart|alfamidi|superindo|hypermart|transmart|carrefour|grand lucky|farmers market|ranch market|lotte mart|hero|papaya fresh|tokopedia|lazada|blibli|tiktok shop|zalora|amazon|uniqlo|zara|h&m|pull&bear|stradivarius|bershka|cotton on|miniso|kkv|sociolla|watsons|guardian|gramedia|ace hardware|informa|ikea|mitra10|mr diy|mr\. diy|decathlon|supermarket|belanja|groceries|grocery|market|mall|toko|store)\b""", RegexOption.IGNORE_CASE)
            if (shoppingRegex.containsMatchIn(corpus) || isShopeeMarketplace) {
                return "Shopping"
            }

            val foodRegex = Regex("""\b(?:gofood|grabfood|shopeefood|starbucks|kopi kenangan|janji jiwa|fore coffee|fore|flash coffee|point coffee|tomoro|chatime|mixue|xi bo ba|kopi tuku|tuku|anomali|excelso|haus|teguk|mcdonald|mcd|kfc|burger king|hokben|a&w|subway|pizza hut|domino's|dominos|richeese|shihlin|solaria|marugame|yoshinoya|bakmi gm|d'cost|dcost|kintan|shaburi|hanamasa|pepper lunch|mie gacoan|gacoan|kopi|coffee|makan|food|resto|restaurant|cafe|kafe|warung|bakso|sate|soto|nasi|ayam|mie|tea|teh|drink|beverage|boba|bakery|roti|bread|cake|martabak|padang|geprek|kuliner|snack|kantin|dining)\b""", RegexOption.IGNORE_CASE)
            if (foodRegex.containsMatchIn(corpus)) {
                return "Food & Dining"
            }

            if (isOutgoingTransfer(textLower)) {
                return "Food & Dining"
            }

            return "Other Expense"
        }
    }
}
