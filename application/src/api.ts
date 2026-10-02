export type ChatRole = 'user' | 'assistant'

export interface ChatMessage {
  role: ChatRole
  content: string
  options?: string[]
}

export interface ChatResponse {
  role: ChatRole
  content: string
  options: string[]
}

export async function sendChat(messages: ChatMessage[]): Promise<ChatResponse> {
  const response = await fetch('/api/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ messages }),
  })

  if (!response.ok) {
    let detail = 'Falha ao falar com o assistente'
    try {
      const body = await response.json()
      if (body?.error) {
        detail = body.error
      }
    } catch {
      // ignore parse errors
    }
    throw new Error(detail)
  }

  return response.json()
}
