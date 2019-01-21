package com.greglaun.lector.data.course

import com.greglaun.lector.data.cache.ArticleContext

data class CourseDetails(val courseContext: CourseContext,
                         val articleContexts: List<ArticleContext>)