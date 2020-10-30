package ir.tapsell.sample.util

import ir.tapsell.sample.domain.cassandra.AdEvent
import ir.tapsell.sample.domain.kafka.ClickEvent
import ir.tapsell.sample.domain.kafka.ImpressionEvent
import java.util.*

class BuildUtil {
    companion object {
        @JvmStatic
        fun buildNewAdEvent(impressionEvent: ImpressionEvent, clickEvent: ClickEvent?): AdEvent {
            var clickTime: Long = -1
            if(Optional.ofNullable(clickEvent).isPresent) {
                clickTime = clickEvent!!.clickTime
            }
            return AdEvent(
                    impressionEvent.requestId,
                    impressionEvent.adId,
                    impressionEvent.adTitle,
                    impressionEvent.advertiserCost,
                    impressionEvent.appId,
                    impressionEvent.appTitle,
                    impressionEvent.impressionTime,
                    clickTime
            )
        }

        @JvmStatic
        fun buildNewAdEvent(impressionEvent: ImpressionEvent): AdEvent {
            return buildNewAdEvent(impressionEvent, null)
        }
    }
}