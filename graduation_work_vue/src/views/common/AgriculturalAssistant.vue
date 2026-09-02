<template>
  <div class="assistant-page">
    <header class="page-header">
      <div>
        <h1>农业助手</h1>
        <p>前端按打字机效果逐字展示流式回答。</p>
      </div>
      <el-button text @click="clearLocalConversation">
        <el-icon><Delete /></el-icon>
        清空
      </el-button>
    </header>

    <main class="page-body">
      <aside class="sidebar">
        <div class="sidebar-section">
          <div class="section-title">
            <el-icon><Collection /></el-icon>
            <span>常见问题</span>
          </div>
          <button
            v-for="(question, index) in quickQuestions"
            :key="index"
            class="question-item"
            type="button"
            @click="selectQuestion(question)"
          >
            <el-icon><QuestionFilled /></el-icon>
            <span>{{ question }}</span>
          </button>
        </div>

        <div class="sidebar-section tips">
          <div class="section-title">
            <el-icon><InfoFilled /></el-icon>
            <span>说明</span>
          </div>
          <ul>
            <li>支持种植、病虫害、土壤和施肥咨询</li>
            <li>当前只接入流式输出接口</li>
            <li>会话历史读取接口已临时关闭</li>
          </ul>
        </div>
      </aside>

      <section class="chat-panel">
        <div ref="chatHistoryRef" class="chat-history">
          <div v-if="messages.length === 0" class="empty-state">
            <el-icon :size="64" color="#67c23a"><ChatLineRound /></el-icon>
            <h2>欢迎使用农业助手</h2>
            <p>输入问题后，回答会以流式打字机效果逐步显示。</p>
          </div>

          <div
            v-for="message in messages"
            :key="message.id"
            :class="['chat-message', message.role === 'user' ? 'user' : 'assistant']"
          >
            <el-avatar :size="40" :class="message.role === 'user' ? 'user-avatar' : 'assistant-avatar'">
              <el-icon v-if="message.role === 'user'"><User /></el-icon>
              <el-icon v-else><Cpu /></el-icon>
            </el-avatar>

            <div class="message-card">
              <div class="message-meta">
                <span>{{ message.role === 'user' ? '你' : '农业助手' }}</span>
                <span>{{ message.timestamp }}</span>
              </div>

              <div class="message-content">
                <div
                  v-if="message.role === 'assistant' && message.isStreaming && !message.content"
                  class="loading-dots"
                >
                  <span></span>
                  <span></span>
                  <span></span>
                </div>

                <div v-if="message.content" class="message-text" v-html="renderMarkdown(message.content)"></div>
                <span v-if="message.role === 'assistant' && message.isStreaming" class="typing-cursor">|</span>
              </div>
            </div>
          </div>
        </div>

        <div class="composer">
          <el-input
            v-model="inputMessage"
            type="textarea"
            :rows="3"
            placeholder="请输入农业问题，Ctrl + Enter 发送"
            :disabled="sending"
            resize="none"
            @keydown.ctrl.enter.prevent="sendMessage"
          />
          <div class="composer-actions">
            <span class="hint">Ctrl + Enter 发送</span>
            <el-button type="primary" :loading="sending" :disabled="!inputMessage.trim()" @click="sendMessage">
              <el-icon><Promotion /></el-icon>
              发送
            </el-button>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, reactive, ref } from 'vue'
import { marked } from 'marked'
import {
  ChatLineRound,
  Collection,
  Cpu,
  Delete,
  InfoFilled,
  Promotion,
  QuestionFilled,
  User,
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

marked.setOptions({
  breaks: true,
  gfm: true,
})

const inputMessage = ref('')
const messages = ref([])
const sending = ref(false)
const chatHistoryRef = ref(null)
const abortController = ref(null)

const typingState = reactive({
  messageIndex: -1,
  buffer: '',
  timer: null,
  finished: false,
})

const quickQuestions = [
  '水稻叶片发黄怎么办？',
  '番茄常见病虫害有哪些？',
  '小麦什么时候播种最好？',
  '土壤酸化怎么改良？',
  '玉米施肥的最佳时间？',
  '果树修剪有什么技巧？',
]

const renderMarkdown = (content) => marked.parse(content || '')

const scrollToBottom = () => {
  nextTick(() => {
    const el = chatHistoryRef.value
    if (el) {
      el.scrollTop = el.scrollHeight
    }
  })
}

const selectQuestion = (question) => {
  inputMessage.value = question
}

const clearTypingState = () => {
  if (typingState.timer) {
    clearInterval(typingState.timer)
    typingState.timer = null
  }
  typingState.messageIndex = -1
  typingState.buffer = ''
  typingState.finished = false
}

const clearLocalConversation = () => {
  if (abortController.value) {
    abortController.value.abort()
    abortController.value = null
  }
  clearTypingState()
  messages.value = []
  sending.value = false
}

const createMessageId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`

const pushUserMessage = (content) => {
  messages.value.push({
    id: createMessageId(),
    role: 'user',
    content,
    timestamp: new Date().toLocaleString(),
    isStreaming: false,
  })
}

const pushAssistantMessage = () => {
  const message = {
    id: createMessageId(),
    role: 'assistant',
    content: '',
    timestamp: new Date().toLocaleString(),
    isStreaming: true,
  }
  messages.value.push(message)
  typingState.messageIndex = messages.value.length - 1
  return message
}

const startTypingLoop = () => {
  if (typingState.timer) return

  typingState.timer = window.setInterval(() => {
    const current = messages.value[typingState.messageIndex]
    if (!current) {
      clearTypingState()
      return
    }

    if (typingState.buffer.length > 0) {
      const step = Math.min(typingState.buffer.length, 2)
      current.content += typingState.buffer.slice(0, step)
      typingState.buffer = typingState.buffer.slice(step)
      scrollToBottom()
      return
    }

    if (typingState.finished) {
      current.isStreaming = false
      clearTypingState()
      scrollToBottom()
    }
  }, 18)
}

const queueAssistantText = (text) => {
  if (!text) return
  typingState.buffer += text
  startTypingLoop()
}

const waitForTypingToFinish = () =>
  new Promise((resolve) => {
    const timer = window.setInterval(() => {
      if (!typingState.timer && !typingState.buffer && !typingState.finished) {
        clearInterval(timer)
        resolve()
      }
    }, 30)
  })

const parseSseEvent = (block) => {
  const lines = block.split('\n')
  let event = 'message'
  const dataLines = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      event = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^ /, ''))
    }
  }

  const data = dataLines.join('\n')
  return { event, data }
}

const readSseStream = async (response, handlers) => {
  const reader = response.body?.getReader()
  if (!reader) {
      throw new Error('响应流不可用')
  }

  const decoder = new TextDecoder('utf-8')
  let buffer = ''

  try {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')

      let separatorIndex = buffer.indexOf('\n\n')
      while (separatorIndex !== -1) {
        const rawEvent = buffer.slice(0, separatorIndex).trim()
        buffer = buffer.slice(separatorIndex + 2)
        separatorIndex = buffer.indexOf('\n\n')

        if (!rawEvent) continue
        const parsed = parseSseEvent(rawEvent)
        if (parsed) handlers.onEvent(parsed)
      }
    }

    buffer += decoder.decode()
    buffer = buffer.replace(/\r\n/g, '\n').trim()
    if (buffer) {
      const parsed = parseSseEvent(buffer)
      if (parsed) handlers.onEvent(parsed)
    }
  } finally {
    reader.releaseLock()
  }
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || sending.value) return

  pushUserMessage(content)
  inputMessage.value = ''
  pushAssistantMessage()
  scrollToBottom()

  sending.value = true
  abortController.value = new AbortController()

  try {
    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/chat-stream', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Accept: 'text/event-stream',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      body: JSON.stringify({
        text: content,
        imageUrls: [],
        conversationId: null,
      }),
      signal: abortController.value.signal,
    })

    if (response.status === 401) {
      ElMessage.error('登录状态异常，请重新登录后再试')
      return
    }

    if (!response.ok) {
      throw new Error(`请求失败: ${response.status}`)
    }

    await readSseStream(response, {
      onEvent: ({ event, data }) => {
        if (event === 'tool_call') {
          return
        }

        if (event === 'error') {
          const current = messages.value[typingState.messageIndex]
          if (current) {
            current.content = data || '请求失败'
            current.isStreaming = false
          }
          clearTypingState()
          return
        }

        if (event === 'done') {
          typingState.finished = true
          return
        }

        queueAssistantText(data)
      },
    })

    typingState.finished = true
    if (!typingState.timer && !typingState.buffer) {
      const current = messages.value[typingState.messageIndex]
      if (current) {
        current.isStreaming = false
      }
      clearTypingState()
    }

    await waitForTypingToFinish().catch(() => {})
  } catch (error) {
    if (error?.name === 'AbortError') {
      return
    }

    const current = messages.value[typingState.messageIndex]
    if (current) {
      current.content = '农业助手暂时不可用，请稍后重试。'
      current.isStreaming = false
    }
    clearTypingState()
    ElMessage.error(error?.message || '流式请求失败')
  } finally {
    abortController.value = null
    sending.value = false
    scrollToBottom()
  }
}

onBeforeUnmount(() => {
  if (abortController.value) {
    abortController.value.abort()
  }
  clearTypingState()
})
</script>

<style scoped>
.assistant-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.page-header {
  flex: 0 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
}

.page-header h1 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}

.page-header p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}

.page-body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
  padding-top: 16px;
}

.sidebar,
.chat-panel {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
  min-height: 0;
}

.sidebar {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.sidebar-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #111827;
}

.question-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  border: 0;
  background: #f8fafc;
  color: #374151;
  text-align: left;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s ease;
}

.question-item:hover {
  background: #eef6ff;
  color: #2563eb;
}

.tips ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

.tips li {
  position: relative;
  padding-left: 16px;
  margin: 10px 0;
  color: #6b7280;
  font-size: 13px;
}

.tips li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
}

.chat-panel {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.chat-history {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.empty-state {
  height: 100%;
  min-height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #6b7280;
}

.empty-state h2 {
  margin: 16px 0 8px;
  color: #111827;
}

.chat-message {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.chat-message.user {
  flex-direction: row-reverse;
}

.assistant-avatar {
  background: linear-gradient(135deg, #22c55e, #16a34a);
}

.user-avatar {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.message-card {
  max-width: min(760px, 100%);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.chat-message.user .message-card {
  align-items: flex-end;
}

.message-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #9ca3af;
}

.message-content {
  padding: 14px 16px;
  border-radius: 8px;
  background: #f9fafb;
  color: #111827;
  border: 1px solid #e5e7eb;
  line-height: 1.7;
}

.chat-message.user .message-content {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
}

.message-text :deep(p) {
  margin: 0 0 10px;
}

.message-text :deep(p:last-child) {
  margin-bottom: 0;
}

.message-text :deep(pre) {
  margin: 10px 0;
  padding: 12px;
  overflow-x: auto;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 8px;
}

.message-text :deep(code) {
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.08);
}

.message-text :deep(pre code) {
  padding: 0;
  background: transparent;
}

.typing-cursor {
  display: inline-block;
  margin-left: 2px;
  animation: blink 1s steps(1) infinite;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}

.loading-dots {
  display: inline-flex;
  gap: 6px;
  align-items: center;
  min-height: 24px;
}

.loading-dots span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #22c55e;
  animation: bounce 1.2s infinite ease-in-out;
}

.loading-dots span:nth-child(2) {
  animation-delay: 0.15s;
}

.loading-dots span:nth-child(3) {
  animation-delay: 0.3s;
}

@keyframes bounce {
  0%,
  80%,
  100% {
    transform: scale(0.7);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.composer {
  border-top: 1px solid #e5e7eb;
  padding: 14px 16px;
}

.composer-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.hint {
  color: #9ca3af;
  font-size: 12px;
}

@media (max-width: 1024px) {
  .page-body {
    grid-template-columns: 1fr;
  }
}
</style>
