package com.greglaun.lector.data.cache

class BasicArticleContext(override val id: Long?, override val contextString: String,
                          override val position: String, override val temporary: Boolean)
    : ArticleContext {
    companion object {
        fun fromString(contextString: String): BasicArticleContext {
            return BasicArticleContext(1L, contextString, "", true)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BasicArticleContext

        if (id != other.id) return false
        if (contextString != other.contextString) return false
        if (position != other.position) return false
        if (temporary != other.temporary) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + contextString.hashCode()
        result = 31 * result + position.hashCode()
        result = 31 * result + temporary.hashCode()
        return result
    }


}