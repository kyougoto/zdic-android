package com.kyougoto.zdic.ui
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyougoto.zdic.data.model.CiYu
import com.kyougoto.zdic.data.ZdicUrl
import com.kyougoto.zdic.data.model.HanZi
import com.kyougoto.zdic.data.model.RadicalNode
import com.kyougoto.zdic.data.model.SearchHit
import com.kyougoto.zdic.ui.theme.Accent
import com.kyougoto.zdic.util.RichText

@Composable
fun MainScreen(vm: MainViewModel = viewModel()) {
    val snackbar = remember { SnackbarHostState() }
    // 系统返回键：非首页时回到上一页/首页，首页时保持默认(退出)
    BackHandler(enabled = vm.screen != Screen.Home) { vm.back() }
    vm.message?.let { m ->
        LaunchedEffect(m) {
            snackbar.showSnackbar(m)
            vm.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { if (vm.screen != Screen.Home) TopBar(onBack = { vm.back() }) else {} },
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (val s = vm.screen) {
                Screen.Home -> HomeScreen(
                    onSearch = { vm.search(it) },
                    onOpenRadical = { vm.openRadical() },
                    onBrowse = { url, title -> vm.openIndex(url, title) },
                    loading = vm.isLoading,
                )
                is Screen.Zi -> ZiScreen(s.zi, onTerm = { vm.openTerm(it) })
                is Screen.Word -> WordScreen(s.w, onTerm = { vm.openTerm(it) })
                is Screen.RadicalList -> RadicalGrid(s.radicals, onPick = { vm.openRadicalChars(it.label) })
                is Screen.Browse -> BrowserScreen(url = s.url, onBack = { vm.back() }, onWord = { vm.openBrowsed(it) })
                is Screen.RadicalChars -> CharList(s.radical, s.chars, onPick = { vm.openCursor(it.display) })
            }
            if (vm.isLoading && vm.screen == Screen.Home) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onBack: () -> Unit) {
    TopAppBar(
        title = { Text("汉典") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
    )
}

@Composable
fun HomeScreen(onSearch: (String) -> Unit, onOpenRadical: () -> Unit, onBrowse: (String, String) -> Unit, loading: Boolean) {
    var query by remember { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Spacer(Modifier.height(32.dp)) }
        item {
            Text(
                "汉 典",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = FontFamily.Serif,
                color = Accent,
            )
        }
        item { Spacer(Modifier.height(8.dp)) }
        item { Text("第三方 Android 汉典客户端 · 字典 / 词典", style = MaterialTheme.typography.bodyMedium) }
        item { Spacer(Modifier.height(28.dp)) }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("输入汉字、词语或成语…") },
                label = { Text("查询") },
            )
        }
        item { Spacer(Modifier.height(12.dp)) }
        item {
            Button(
                onClick = { onSearch(query) },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("立刻查询") }
        }
        item { Spacer(Modifier.height(20.dp)) }
        item { Text("· 检字 / 索引 ·", style = MaterialTheme.typography.labelLarge, color = Accent) }
        item { Spacer(Modifier.height(8.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val idx = listOf(
                    Triple("部首", { onOpenRadical() }, "部首检字"),
                    Triple("拼音", { onBrowse(ZdicUrl.pinyinIndex(), "拼音索引") }, "拼音索引"),
                    Triple("康熙", { onBrowse(ZdicUrl.kangxi(), "康熙部首") }, "康熙部首"),
                )
                idx.forEach { (label, act, _) ->
                    OutlinedButton(
                        onClick = act,
                        modifier = Modifier.weight(1f),
                    ) { Text(label) }
                }
            }
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val idx2 = listOf(
                    Triple("繁体部首", { onBrowse(ZdicUrl.radicalIndex(simplified = false), "繁体部首索引") }, ""),
                    Triple("部件检索", { onBrowse(ZdicUrl.chaiZi(), "汉字拆分查询") }, ""),
                )
                idx2.forEach { (label, act, _) ->
                    OutlinedButton(
                        onClick = act,
                        modifier = Modifier.weight(1f),
                    ) { Text(label) }
                }
            }
        }
        item { Spacer(Modifier.height(6.dp)) }
        item { Text("选字后自动在 App 内查详情", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline) }
    }
}

@Composable
fun ZiScreen(zi: HanZi, onTerm: (String) -> Unit) {
    val meta = remember(zi) { buildMeta(zi) }
    LazyColumn(Modifier.fillMaxSize()) {
        item { ZiHeader(zi, meta, onTerm) }
        items(zi.sections.size) { i ->
            val sec = zi.sections[i]
            val paras = remember(sec) { RichText.toParagraphs(sec.html) }
            SectionTitle(sec.title)
            paras.forEach { SegRich(it) }
            HorizontalDivider()
        }
    }
}
@Composable
private fun ZiHeader(zi: HanZi, meta: List<Pair<String, String>>, onTerm: (String) -> Unit) {
    Column(Modifier.padding(16.dp)) {
        // 大字 + 巨型字形
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                zi.zi,
                fontSize = 110.sp,
                lineHeight = 130.sp,
                fontFamily = CJKSerif,
                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                textAlign = TextAlign.Center,
            )
        }
        if (zi.pinyin.isNotEmpty()) {
            val pyText = zi.pinyin.joinToString("  ")
            Text(
                pyText,
                fontSize = 28.sp,
                color = Accent,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }
        if (zi.zhuyin.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                zi.zhuyin.take(6).forEach { v -> Text("　" + v, fontSize = 15.sp, color = secondaryText(), modifier=Modifier) }
            }
        }
        Spacer(Modifier.height(12.dp))
        // 基本信息两列式排版
        if (meta.isNotEmpty()) {
            val l = meta.size
            chunkRows(meta).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                    row.forEach { (k, v) ->
                        Column(Modifier.weight(1f)) {
                            Text(k, fontSize = 12.sp, color = secondaryText())
                            Text(v, fontSize = 16.sp, fontWeight = FontWeight.Medium, fontFamily = CJKSerif)
                        }
                    }
                }
            }
        }
        // 异体 / 繁 等其他字形 → 可点跳转
        if (zi.variants.isNotEmpty()) {
            MetaDivider("其他字形")
            val grouped = zi.variants.take(30).groupBy { it.type.ifBlank { "字形" } }
            grouped.forEach { (type, vs) ->
                LabeledChips(type.ifBlank { "字形" }, vs.map { it.zi.ifBlank { it.type } }, CJKSerif) { t -> onTerm(t) }
            }
        }
        // 由该字组成的词 / 相关词语 → 可点跳转
        val words = zi.relatedCi.filter { it.length > 1 }.distinct().take(40)
        if (words.isNotEmpty()) {
            MetaDivider("包含「" + zi.zi + "」的词")
            LabeledChips("组词", words, serif = false) { t -> onTerm(t) }
        }
    }
}
@Composable
fun WordScreen(w: CiYu, onTerm: (String) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(w.term, fontSize = 44.sp, fontFamily = CJKSerif, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                if (w.pinyin.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(w.pinyin.joinToString("  "), color = Accent, fontSize = 18.sp)
                }
                if (w.related.isNotEmpty()) {
                    val seen = linkedSetOf<String>()
                    val kw = w.related.filter { it != w.term && it.isNotBlank() && it.length <= 6 }
                    if (kw.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        LabeledChips("相关", kw.distinct().take(24), serif = false) { t -> onTerm(t) }
                    }
                }
            }
        }
        w.sections.forEach { sec ->
            item {
                SectionTitle(sec.title)
                val paras = remember(sec) { RichText.toParagraphs(sec.html) }
                paras.forEach { SegRich(it) }
                HorizontalDivider()
            }
        }
    }
}
@Composable
private fun SegRich(line: String) {
    Text(
        line,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 5.dp, bottom = 2.dp),
        fontSize = 16.sp,
        lineHeight = 27.sp,
            )
}
@Composable
private fun chunkRows(meta: List<Pair<String, String>>): List<List<Pair<String, String>>> {
    val out = mutableListOf<List<Pair<String, String>>>()
    var i = 0
    while (i < meta.size) { out.add(meta.subList(i, (i + 2).coerceAtMost(meta.size))); i += 2 }
    return out
}
@Composable
private fun MetaDivider(title: String) {
    HorizontalDivider(Modifier.padding(vertical = 10.dp))
    Text(title, style = MaterialTheme.typography.titleSmall, color = Accent, modifier = Modifier.padding(bottom = 6.dp))
}
@Composable
private fun LabeledChips(title: String, items: List<String>, serif: Boolean = true, onTap: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 2.dp)) {
        if (title.isNotBlank()) Text(title, fontSize = 13.sp, color = secondaryText(), modifier = Modifier.padding(bottom = 6.dp))
        var key = items.joinToString("|") + serif
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { item ->
                Surface(
                    onClick = { onTap(item) },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier,
                ) {
                    Text(item,
                        fontFamily = if (serif) CJKSerif else FontFamily.Default,
                        fontSize = if (serif) 21.sp else 14.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
@Composable
private fun secondaryText(): Color = MaterialTheme.colorScheme.secondary
private val CJKSerif: FontFamily = FontFamily.Serif

@Composable
fun RadicalGrid(radicals: List<RadicalNode>, onPick: (RadicalNode) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(8),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(radicals) { r ->
            Card(onClick = { onPick(r) }) {
                Box(Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                    Text(r.label, fontSize = 22.sp, fontFamily = CJKSerif)
                }
            }
        }
    }
}
@Composable
fun CharList(radical: String, chars: List<SearchHit>, onPick: (SearchHit) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        item { Text("部首「$radical」下的字", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp)) }
        val grid = mutableListOf<List<SearchHit>>()
        var i = 0
        while (i < chars.size) { grid.add(chars.subList(i, (i + 10).coerceAtMost(chars.size))); i += 10 }
        grid.forEach { rowItems ->
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    rowItems.forEach { h ->
                        Text(h.display, Modifier.weight(1f).clickable { onPick(h) }, fontSize = 22.sp, textAlign = TextAlign.Center, fontFamily = CJKSerif)
                    }
                }
            }
        }
    }
}
@Composable
private fun SectionTitle(t: String) {
    Text(
        t,
        style = MaterialTheme.typography.titleLarge,
        color = Accent,
        modifier = Modifier.padding(16.dp, 18.dp, 16.dp, 6.dp),
    )
}

@Composable
private fun SegmentText(line: String) {
    Text(
        line,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
        fontSize = 16.sp,
        lineHeight = 24.sp,
    )
}

@Composable
private fun TokenChips(title: String, values: List<String>) {
    Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = MaterialTheme.colorScheme.secondary, fontSize = 13.sp)
        values.take(6).forEach { v ->
            Spacer(Modifier.width(8.dp))
            Text(v, fontWeight = FontWeight.Medium)
        }
    }
}

private fun buildMeta(zi: HanZi): List<Pair<String, String>> = listOf(
    "部首" to zi.bushou,
    "部外" to zi.buwai,
    "总笔画" to zi.zongbihua,
    "统一码" to zi.tongyima,
    "笔顺" to zi.bishun,
    "结构" to zi.jiegou,
    "字形分析" to zi.zixing,
    "简体/繁体" to zi.fanjianzi,
).filter { it.second.isNotEmpty() }


@Composable

fun BrowserScreen(url: String, onBack: () -> Unit, onWord: (String) -> Unit) {
    // 用系统 WebView 承载 ZDIC 页面（不接管其 HTML 响应，避免站点 CSS/JS 失效）。
    // 1) 点击页内 /hans/{字} → 解码后 onWord 交回 App 原生详情。
    // 2) onPageFinished 后注入一段覆盖样式：把页面做“宣纸化”，隐藏站点导航栏/广告。
    //    用 JSONObject.quote 对 CSS 可靠转义后作为 JS 字符串注入前端。
    val css =
        "html,body{background:#F8F6F0 !important;color:#1f2937 !important;margin:0 !important}" +
        "header,.site-header,.site-header__inner,.header-wrap,.topbar,.top-bar,.top-bar__inner," +
        ".top-bar__nav,.main-nav,.nav,.drawer,.drawer__panel,.drawer__overlay,.drawer__section," +
        ".dropdown,.dropdown__panel,.dropdown__trigger,#header,#topnav,.header-actions," +
        ".search-bar,.searchbox,.ads,.adsbygoogle,.banner-ad,.banner,ins," +
        "footer,.footer,.site-footer,.copyright,.fb-modal,#feedback{display:none !important}" +
        "body{padding:12px !important}" +
        ".bs-content a,td a,.char-card a,.dict-section__body a{color:#B03A2E !important;font-size:21px !important;line-height:1.9 !important}" +
        "a.pck{color:#3E5C46 !important;font-size:22px !important}"
    val cssJson = JSONObject.quote(css.toString())
    val injection =
        "(function(){" +
        "var s=document.createElement('style');s.type='text/css';" +
        "s.appendChild(document.createTextNode(" + cssJson + "));" +
        "document.documentElement.appendChild(s);" +
        "})();"
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                webViewClient = object : WebViewClient() {
                    // 点某字详情 → 交回原生渲染（不让它在 WebView 内开官方详情）
                    override fun shouldOverrideUrlLoading(view: WebView?, urlStr: String?): Boolean {
                        val href = urlStr ?: return false
                        val mk = "/hans/"
                        val idx = href.indexOf(mk)
                        if (idx >= 0) {
                            var term = href.substring(idx + mk.length)
                            val q = term.indexOf('?')
                            if (q >= 0) term = term.substring(0, q)
                            term = term.trimEnd('/')
                            val decoded = try { java.net.URLDecoder.decode(term, "UTF-8") } catch (e: Exception) { term }
                            if (decoded.isNotEmpty()) { onWord(decoded); return true }
                        }
                        return false
                    }
                    // 主文档加载完成后注入样式，隐藏导航/广告并铺宣纸底色
                    override fun onPageFinished(view: WebView?, urlStr: String?) {
                        super.onPageFinished(view, urlStr)
                        try { view?.evaluateJavascript(injection, null) }
                        catch (_: Exception) { }
                    }
                }
                loadUrl(url)
            }
        }
    )
}
