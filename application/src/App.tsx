import { FormEvent, useEffect, useRef, useState } from 'react'
import { ChatMessage, sendChat } from './api'
import './App.css'

let conversationBootstrapped = false

function App() {
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const bottomRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    if (conversationBootstrapped) {
      return
    }
    conversationBootstrapped = true

    async function startConversation() {
      setLoading(true)
      setError(null)
      try {
        const reply = await sendChat([])
        setMessages([
          {
            role: 'assistant',
            content: reply.content,
            options: reply.options ?? [],
          },
        ])
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Erro inesperado')
      } finally {
        setLoading(false)
      }
    }

    void startConversation()
  }, [])

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages, loading])

  async function sendUserText(text: string) {
    const trimmed = text.trim()
    if (!trimmed || loading) {
      return
    }

    const historyForApi = messages.map(({ role, content }) => ({ role, content }))
    const nextMessages: ChatMessage[] = [
      ...messages,
      { role: 'user', content: trimmed },
    ]

    setMessages(nextMessages)
    setInput('')
    setLoading(true)
    setError(null)

    try {
      const reply = await sendChat([
        ...historyForApi,
        { role: 'user', content: trimmed },
      ])
      setMessages((prev) => [
        ...prev,
        {
          role: 'assistant',
          content: reply.content,
          options: reply.options ?? [],
        },
      ])
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro inesperado')
    } finally {
      setLoading(false)
    }
  }

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    void sendUserText(input)
  }

  const lastMessage = messages[messages.length - 1]
  const optionButtons =
    !loading && lastMessage?.role === 'assistant' && lastMessage.options?.length
      ? lastMessage.options
      : []

  return (
    <div className="app">
      <header className="header">
        <h1>Chatbot</h1>
        <p>Assistente genérico com Gemini</p>
      </header>

      <main className="chat">
        {messages.map((message, index) => (
          <div
            key={`${message.role}-${index}`}
            className={`bubble ${message.role === 'user' ? 'user' : 'assistant'}`}
          >
            <span className="label">{message.role === 'user' ? 'Você' : 'Bot'}</span>
            <p>{message.content}</p>
          </div>
        ))}

        {loading && (
          <div className="bubble assistant pending">
            <span className="label">Bot</span>
            <p>Digitando…</p>
          </div>
        )}

        {error && <div className="error">{error}</div>}
        <div ref={bottomRef} />
      </main>

      {optionButtons.length > 0 && (
        <div className="options">
          {optionButtons.map((option) => (
            <button
              key={option}
              type="button"
              disabled={loading}
              onClick={() => void sendUserText(option)}
            >
              {option}
            </button>
          ))}
        </div>
      )}

      <form className="composer" onSubmit={handleSubmit}>
        <input
          type="text"
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder={loading ? 'Aguardando resposta…' : 'Digite sua mensagem'}
          disabled={loading}
          autoComplete="off"
        />
        <button type="submit" disabled={loading || !input.trim()}>
          Enviar
        </button>
      </form>
    </div>
  )
}

export default App
