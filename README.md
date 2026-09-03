# Zdic (汉典) Android 第三方客户端

> 汉典 (ZDIC.NET) 专用 **Android 客户端**，纯在线直连抓取。

基于 **Kotlin + Jetpack Compose** 单语言开发的安卓端汉典查询工具，覆盖汉典 **字典** 与 **词典** 的全部查询入口。

## 功能特性

### 字典（单字查询）
保留并实现汉典查字的所有入口：

- 按字直查：读音音标、部首、部外笔画、总笔画、笔顺、部首索引
- 字头信息：拼音、注音、繁体/简体、异体、结构、统一码(Unicode)、五笔、仓颉、四角号码、郑码
- 释义分级：基本解释、详细解释（含义项/书证/例句/常用词组）
- 古籍考据原文：康熙字典、说文解字
- 语音学：音韵方言（国际音标、上古音/中古音、方言读音）
- 字源字形：金文/楚系简帛/说文/楷书演变，书体字形实例
- 同音字/同部首字关联跳转
- 检字索引：部首、拼音、注音、康熙部首、说文部首

### 词典（词语/成语查询）
- 词语查询：词语释义、对照、出处
- 成语查询：成语释义、出处、典故、例句

## 技术架构

```
UI (Jetpack Compose) -> ViewModel(StateFlow) -> Repository -> ZdicApi(Ktor 抓取) + Jsoup 解析
```

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 网络：Ktor Client (OkHttp) + Jsoup
- 异步：Coroutines + Flow

## 构建

环境：Android Studio / JDK 17

```bash
git clone https://github.com/kyougoto/zdic-android.git
cd zdic-android
./gradlew assembleDebug
```

产物：app/build/outputs/apk/debug/app-debug.apk

## 数据来源与免责
- 本应用为汉典网站第三方查询客户端，内容在用户主动查询时实时抓取公开网页解析所得。
- 数据版权归 汉典 zdic.net 所有，仅作学术与个人查询用途。
- 本应用不缓存、不存储、不再分发抓取内容。

## License

[MIT](LICENSE)

---
by kyougoto