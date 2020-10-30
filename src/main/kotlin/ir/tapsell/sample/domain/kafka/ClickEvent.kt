package ir.tapsell.sample.domain.kafka


class ClickEvent(
    val requestId: String,
    val clickTime: Long
)