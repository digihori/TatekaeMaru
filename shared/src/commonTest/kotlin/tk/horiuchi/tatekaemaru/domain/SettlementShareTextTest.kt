package tk.horiuchi.tatekaemaru.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import tk.horiuchi.tatekaemaru.data.Event
import tk.horiuchi.tatekaemaru.data.Expense
import tk.horiuchi.tatekaemaru.data.GasInfo
import tk.horiuchi.tatekaemaru.data.Member

class SettlementShareTextTest {
    @Test
    fun sharedTextIncludesJapaneseNamesTransfersAndGasDetails() {
        val event = Event(
            name = "日帰り旅行",
            members = listOf(Member("a", "太郎"), Member("b", "花子")),
            expenses = listOf(Expense(
                id = "gas", title = "ガソリン代", amount = 1360,
                payerId = "a", targetMemberIds = setOf("a", "b"),
                gasInfo = GasInfo(120.0, 15.0, 170)
            ))
        )
        assertEquals(
            """
            【日帰り旅行】

            ■ 精算結果
            花子 → 太郎：680円

            ■ 各自負担額
            太郎：680円
            花子：680円

            ■ 支払い明細
            ガソリン代：1,360円（太郎）
              ガス代計算：120.0km ÷ 15.0km/L × 170円/L

            合計：1,360円

            ※たてかえ丸で作成
            """.trimIndent() + "\n",
            SettlementShareText.build(event)
        )
    }

    @Test
    fun emptyEventCanBeSharedWithoutTransfers() {
        val text = SettlementShareText.build(Event(name = "空のイベント"))
        kotlin.test.assertTrue(text.contains("精算はありません\n"))
        kotlin.test.assertTrue(text.contains("合計：0円\n"))
    }
}
