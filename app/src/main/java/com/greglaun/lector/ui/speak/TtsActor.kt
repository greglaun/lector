package com.greglaun.lector.ui.speak

import com.greglaun.lector.store.SpeakerAction
import com.greglaun.lector.store.SpeakerState
import com.greglaun.lector.store.Store
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

private val actorContext = newSingleThreadContext("ActorContext")

fun ttsActor(ttsClient: TtsActorClient, ttsStateListener: TtsStateListener, store: Store) =
        CoroutineScope(actorContext).actor<TtsMsg>(
                Dispatchers.Default, 0, CoroutineStart.DEFAULT, null, {
    for (msg in channel) {
        when (msg) {
            // todo
            is SpeakOne -> {
                store.dispatch(SpeakerAction.SpeakAction())
            }
        }
    }
})

// Message types for ttsActor
sealed class TtsMsg
class SpeakOne(val speakerState: CompletableDeferred<SpeakerState>) : TtsMsg()

@Deprecated("Soon to be removed")
class UpdateArticleStateDeprecated(val articleState: ArticleState, val position: String): TtsMsg()
class MarkReady(val articleState: ArticleState): TtsMsg()
