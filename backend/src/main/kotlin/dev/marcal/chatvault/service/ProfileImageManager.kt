package dev.marcal.chatvault.service

import org.springframework.core.io.Resource
import java.io.InputStream

interface ProfileImageManager {
    fun updateImage(
        inputStream: InputStream,
        chatId: Long,
    )

    fun getImage(chatId: Long): Resource
}
