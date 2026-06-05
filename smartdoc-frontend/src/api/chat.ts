import {get, del} from '@/api'
import type {ChatRequest} from '@/types'

export function streamChat(
    request: ChatRequest,
    onToken: (token: string) => void,
    onComplete: () => void,
    onError: (error: Error) => void
): AbortController {
    const controller = new AbortController()

    fetch('/api/chat', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(request),
        signal: controller.signal
    })
        .then(async (response) => {
            if (!response.ok) throw new Error(`HTTP ${response.status}`)

            const reader = response.body!.getReader()
            const decoder = new TextDecoder()
            let buffer = ''

            let eventData: string[] = []

            while (true) {
                const {done, value} = await reader.read()
                if (done) break

                buffer += decoder.decode(value, {stream: true})
                const lines = buffer.split('\n')
                buffer = lines.pop() || ''

                for (const line of lines) {
                    if (line.startsWith('data: ')) {
                        eventData.push(line.substring(6))
                    } else if (line.startsWith('data:')) {
                        eventData.push(line.substring(5))
                    } else if (line === '' && eventData.length > 0) {
                        const data = eventData.join('\n')
                        eventData = []
                        if (data === '[DONE]') {
                            onComplete()
                        } else {
                            onToken(data)
                        }
                    }
                }
            }

            if (eventData.length > 0) {
                const data = eventData.join('\n')
                if (data === '[DONE]') {
                    onComplete()
                } else if (data) {
                    onToken(data)
                }
            }
            onComplete()
        })
        .catch((err) => {
            if (err.name !== 'AbortError') {
                onError(err)
            }
        })

    return controller
}

export async function getChatHistory(sessionId: string): Promise<{role: string; content: string}[]> {
    return get(`/chat/history/${encodeURIComponent(sessionId)}`)
}

export async function clearSession(sessionId: string): Promise<void> {
    await del(`/chat/session/${encodeURIComponent(sessionId)}`)
}
