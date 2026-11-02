package com.example.financeapp.data.market

/**
 * Bảng phân ngành cho các mã thanh khoản tốt trên HOSE.
 *
 * API công khai của VNDirect/SSI KHÔNG trả về trường ngành, nên bảng này nằm trong app.
 * Danh mục quét thực tế = giao của bảng này với snapshot SSI rồi lấy top theo giá trị
 * giao dịch, nên mã huỷ niêm yết sẽ tự rơi ra. Cần rà lại khi có mã mới lên sàn.
 */
object StockUniverse {

    /** Số mã tối đa đưa vào một lần quét, xếp theo giá trị giao dịch giảm dần. */
    const val MAX_UNIVERSE_SIZE = 100

    const val INDEX_SYMBOL = "VNINDEX"

    val SECTORS: Map<String, List<String>> = mapOf(
        "Ngân hàng" to listOf("VCB", "TCB", "HDB", "VPB", "MBB", "ACB", "SHB", "CTG", "BID", "STB", "MSB", "TPB", "EIB", "LPB", "OCB", "VIB", "SSB", "NAB"),
        "Chứng khoán" to listOf("SSI", "VIX", "VND", "VCI", "HCM", "FTS", "BSI", "CTS", "AGR", "ORS", "VDS", "TVS", "APG"),
        "Bất động sản" to listOf("VIC", "VHM", "VRE", "NVL", "PDR", "DXG", "KDH", "DIG", "NLG", "HDG", "IJC", "SCR", "TCH", "CRE", "QCG", "HDC", "NTL", "DXS"),
        "Khu công nghiệp" to listOf("KBC", "SZC", "BCM", "SIP", "LHG", "TIP", "D2D", "ITA"),
        "Thép" to listOf("HPG", "HSG", "NKG", "SMC", "TLH", "POM"),
        "Thực phẩm & Đồ uống" to listOf("VNM", "MSN", "SAB", "KDC", "VHC", "ANV", "IDI", "DBC", "HAG", "BAF", "SBT", "LSS", "FMC", "ASM"),
        "Bán lẻ" to listOf("MWG", "PNJ", "FRT", "DGW", "PET"),
        "Công nghệ" to listOf("FPT", "CMG", "ELC", "ITD", "SAM"),
        "Dầu khí" to listOf("BSR", "PVT", "PLX", "PVD", "PSH", "CNG", "PGD", "PGC"),
        "Điện & Năng lượng" to listOf("POW", "REE", "PC1", "GEG", "NT2", "VSH", "TTA", "GEX"),
        "Vận tải & Cảng biển" to listOf("GMD", "VSC", "HAH", "VOS", "VTO", "PVP", "DVP", "STG", "TMS"),
        "Hàng không" to listOf("VJC", "HVN", "SCS", "NCT"),
        "Hóa chất & Phân bón" to listOf("DGC", "DPM", "DCM", "CSV", "BFC", "DRC", "CSM"),
        "Xây dựng & VLXD" to listOf("CTD", "HBC", "VCG", "LCG", "FCN", "HHV", "VGC", "PLC", "BMP", "HT1"),
        "Dệt may" to listOf("MSH", "TCM", "STK", "GIL", "TNG"),
        "Bảo hiểm" to listOf("BVH", "BMI", "MIG", "PTI", "BIC"),
        "Cao su" to listOf("GVR", "PHR", "DPR", "TRC"),
        "Dược phẩm" to listOf("DHG", "IMP", "DBD", "TRA"),
        "Khai khoáng" to listOf("KSB", "DHA", "BMC")
    )

    /** Tra cứu ngược: mã -> ngành. */
    val SYMBOL_TO_SECTOR: Map<String, String> =
        SECTORS.flatMap { (sector, symbols) -> symbols.map { it to sector } }.toMap()

    fun sectorOf(symbol: String): String = SYMBOL_TO_SECTOR[symbol] ?: "Khác"

    fun isCovered(symbol: String): Boolean = SYMBOL_TO_SECTOR.containsKey(symbol)
}