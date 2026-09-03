package com.kyougoto.zdic.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HanZi(
    val zi: String = "",
    val pinyin: List<String> = emptyList(),
    val zhuyin: List<String> = emptyList(),
    val fanjianzi: String = "",
    val bushou: String = "",
    val buwai: String = "",
    val zongbihua: String = "",
    val tongyima: String = "",
    val bishun: String = "",
    val jiegou: String = "",
    val zixing: String = "",
    val glyphSvgUrl: String = "",
    val glyphGifUrl: String = "",
    val variants: List<Variant> = emptyList(),
    val sections: List<ZiSection> = emptyList(),
    val relatedCi: List<String> = emptyList(),
    val sameSoundChars: List<String> = emptyList(),
    val sameBushouChars: List<String> = emptyList(),
)

@Serializable
data class Variant(
    val zi: String = "",
    val type: String = "",
    val link: String = "",
)

@Serializable
data class ZiSection(
    val id: String = "",
    val title: String = "",
    val html: String = "",
)

@Serializable
data class ZiSense(
    val label: String = "",
    val text: String = "",
)
