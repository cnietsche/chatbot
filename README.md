# Chatbot — React + Spring Boot + Gemini

Chatbot full-stack para aprendizado. O `application` guarda o histórico só em memória; o `chatbot-api` chama a Gemini API usando templates de comportamento.

## Pré-requisitos

- Docker e Docker Compose **ou**
- Java 21 + Maven, Node.js 20+ e uma chave da [Gemini API](https://aistudio.google.com/apikey) (free tier)

## Configuração

1. Copie o arquivo de ambiente:

```bash
cp .env.example .env
```

2. Coloque sua chave em `.env`:

```env
GEMINI_API_KEY=sua_chave_aqui
GEMINI_MODEL=gemini-flash-lite-latest
```

O Docker Compose lê automaticamente o arquivo `.env` na raiz. Sem ele, a chave fica vazia.

## Executar com Docker

```bash
docker compose up --build
```

- Application: http://localhost:3000
- chatbot-api: http://localhost:8080

## Executar em desenvolvimento

### chatbot-api

```bash
cd chatbot-api
set GEMINI_API_KEY=sua_chave_aqui
mvn spring-boot:run
```

### application

```bash
cd application
npm install
npm run dev
```

Abra http://localhost:5173 (o Vite faz proxy de `/api` para `http://localhost:8080`).

## Templates de comportamento

Edite ou adicione arquivos em `chatbot-api/src/main/resources/templates/`.

O template ativo é definido por `GEMINI_TEMPLATE` (padrão: `default` → `default.txt`).

As regras de fluxo (pedir nome, validar nome real, etc.) ficam **somente** no template — o `chatbot-api` não implementa máquina de estados.

## API

`POST /api/chat`

```json
{
  "messages": [
    { "role": "user", "content": "Maria" }
  ]
}
```

Envie `messages: []` para a IA gerar a primeira mensagem.

Resposta:

```json
{
  "role": "assistant",
  "content": "Prazer, Maria! Como posso ajudar?",
  "options": []
}
```
