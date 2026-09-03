package com.kyougoto.zdic.data.model

import kotlinx.serialization.Serializable

/** 通用检索条目（词语/成语的候选结果），出现在检索回落列表页。 */
@Serializable
data class SearchHit(
    val display: String = "",
    val pinyin: String = "",
    val brief: String = "",
    val link: String = "",
    val kind: String = "",   // ci / cy / zi
)

/** 词语/成语 / 多字词解析结果（词典部分）。对应 zdic.net/hans/词 的多字页面。 */
@Serializable
data class CiYu(
    val term: String = "",
    val pinyin: List<String> = emptyList(),
    val kind: String = "",          // ci = 词语, cy = 成语
    val glyphUrl: String = "",
    val sections: List<CiSection> = emptyList(),
    val related: List<String> = emptyList(),
)

@Serializable
data class CiSection(
    val id: String = "",
    val title: String = "",
    val html: String = "",
)

/** 检字目录用的部首（used by /zd/bs/ & /zd/fbs/ 页面） */
@Serializable
data class RadicalNode(
    val label: String = "",          // 如 氵
    val strokeCount: Int = 0,        // 可用时
    val link: String = "",          // e.g /zd/bs/?bs=%E6%B0%B5
    val count: String = "",         // 该部首含字数量(若有)
)

/** 检字目录：拼音索引 / 注音索引 用 */
@Serializable
data class PinyinNode(
    val label: String = "",          // a / b …
    val link: String = "",
)
