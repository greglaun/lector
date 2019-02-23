package com.greglaun.lector.store

import com.greglaun.lector.ui.speak.ArticleState

sealed class Action
data class UpdateArticleAction(val articleState: ArticleState): Action()
// Starting up action
// Winding down action
// Link clicked
// Settings change in UI
// Fast forward
// Rewind
// Play
// Pause