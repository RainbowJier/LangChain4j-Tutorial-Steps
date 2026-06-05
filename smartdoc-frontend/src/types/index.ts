export interface ChatMessage {
  id: string
  role: 'user' | 'assistant'
  content: string
  timestamp: number
  isStreaming?: boolean
  toolCalls?: ToolCallInfo[]
}

export interface ToolCallInfo {
  name: string
  args: Record<string, string>
  result?: string
}

export interface ChatSession {
  id: string
  title: string
  createdAt: number
  lastMessageAt: number
}

export interface ChatRequest {
  message: string
  sessionId: string
}

export interface ApiResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

export interface FileNode {
  name: string
  path: string
  type: 'file' | 'directory'
  children?: FileNode[]
  content?: string
  language?: string
}

export interface CodeSelection {
  filePath: string
  fileName: string
  content: string
  startLine: number
  endLine: number
}

export interface AiCard {
  id: string
  type: 'suggestion' | 'explanation' | 'code' | 'error' | 'summary'
  title: string
  content: string
  code?: string
  language?: string
  timestamp: number
  isStreaming?: boolean
}

export interface KbCategory {
  id: string
  name: string
  icon: string
  description: string
  docCount: number
}

export interface KbDocument {
  id: string
  title: string
  summary: string
  categoryId: string
  tags: string[]
  updatedAt: number
}

export interface KbSourceRef {
  title: string
  snippet: string
  score?: number
}
