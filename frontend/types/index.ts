export interface Chat {
    chatId: number
    chatName: string
    authorName: string
    authorType: string,
    content: string,
    msgCreatedAt: string,
    msgCount: number
}

export interface Attachment {
    name: string
    type: 'IMAGE' | 'VIDEO' | 'AUDIO' | 'PDF' | 'UNKNOWN'
    url: string
}

export class ChatMessage {
    id: number
    chatId: number
    author: string
    authorType: string
    content: string
    attachment: Attachment | null
    createdAt: string

    constructor(data: any, chatId: number) {
        this.id = data.id
        this.chatId = chatId
        this.author = data.author
        this.authorType = data.authorType
        this.content = data.content
        this.createdAt = data.createdAt
        const url = useRuntimeConfig().public.api.getAttachmentByChatIdAndMessageId
            .replace(':chatId', this.chatId.toString())
            .replace(':messageId', this.id.toString())
        this.attachment = AttachmentConstructor(data.attachmentName, url)
    }
}

export function AttachmentConstructor(attachmentName: string | undefined, url: string): Attachment | null {
    if (!attachmentName) {
        return null
    }

    if (/\.(jpg|jpeg|png|gif|webp)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'IMAGE', url: url}
    } else if (/\.(mp4|avi|mov)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'VIDEO', url: url}
    } else if (/\.(mp3|wav|opus)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'AUDIO', url: url}
    } else if (/\.(pdf)$/i.test(attachmentName)) {
        return {name: attachmentName, type: 'PDF', url: url}
    }

    return {name: attachmentName, type: 'UNKNOWN', url: url}
}