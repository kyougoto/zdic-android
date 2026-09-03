package com.kyougoto.zdic.ui
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyougoto.zdic.data.IndexRepository
import com.kyougoto.zdic.data.LookupResult
import com.kyougoto.zdic.data.ZdicRepository
import com.kyougoto.zdic.data.ZdicUrl
import kotlinx.coroutines.launch
sealed interface Screen {
    data object Home : Screen
    data class Zi(val zi: com.kyougoto.zdic.data.model.HanZi, val query: String) : Screen
    data class Word(val w: com.kyougoto.zdic.data.model.CiYu, val query: String) : Screen
    data class RadicalList(val radicals: List<com.kyougoto.zdic.data.model.RadicalNode>) : Screen
    data class RadicalChars(val radical: String, val chars: List<com.kyougoto.zdic.data.model.SearchHit>) : Screen
    data class Browse(val url: String, val title: String) : Screen
}
class MainViewModel : ViewModel() {
    private val repo = ZdicRepository()
    private val index = IndexRepository
    var screen by mutableStateOf<Screen>(Screen.Home)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)
        private set
    private val hist = ArrayList<Screen>()
    private fun toast(msg: String) { message = msg }
    fun consumeMessage() { message = null }
    /** 页面导航：把当前页压栈，再切到 next（相同的 next 不重复压）。 */
    private fun goto(next: Screen) {
        if (next == screen) return
        hist.add(screen)
        screen = next
        isLoading = false
    }
    fun back() {
        if (hist.isNotEmpty()) screen = hist.removeAt(hist.size - 1)
        else screen = Screen.Home
        isLoading = false
    }
    fun search(query: String) {
        val q = query.trim()
        if (q.isEmpty()) { toast("请输入要查询的内容"); return }
        isLoading = true
        viewModelScope.launch {
            when (val r = repo.lookup(q)) {
                is LookupResult.ZiSuccess -> goto(Screen.Zi(r.data, q))
                is LookupResult.WordSuccess -> goto(Screen.Word(r.data, q))
                is LookupResult.Error -> { toast(r.message) }
            }
            isLoading = false
        }
    }
    fun openRadical() {
        isLoading = true
        viewModelScope.launch {
            val list = try { index.radicals(simplified = true) } catch (e: Exception) { emptyList() }
            if (list.isEmpty()) toast("部首数据加载失败（网络或站点结构变动）")
            else goto(Screen.RadicalList(list))
            isLoading = false
        }
    }
    fun openRadicalChars(radical: String) {
        // 部首下字列表依赖站点前端 AJAX，直接抓取不稳定；改用内置 WebView 打开官方对应页，
        // 让站点自身 JS 渲染出部首下的字，用户点字后再回到本 App 查字接口。
        goto(Screen.Browse(ZdicUrl.radicalChars(radical), "部首检字"))
    }
    /** 打开任意 ZDIC 检字索引（拼音/康熙/繁体部首/部件等），页内选字回原生详情 */
    fun openIndex(url: String, title: String) { goto(Screen.Browse(url, title)) }
    /** 从 WebView 索引点某字 → 原生详情（保留 WebView 在栈中，返回可回索引） */
    fun openBrowsed(word: String) { search(word) }
    fun openCursor(q: String) { search(q) }

    /** 详情页内的字/词/字形点按后跳转 */
    fun openTerm(q: String) { search(q) }
}
