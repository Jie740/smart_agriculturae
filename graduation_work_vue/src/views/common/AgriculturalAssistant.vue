<template>
  <div class="assistant-page">
    <header class="page-header">
      <div>
        <h1>农业助手</h1>
        <p>保存并管理你的农业咨询记录</p>
      </div>
      <div class="header-actions">
        <span v-if="scopeSummary" class="scope-summary">{{ scopeSummary }}</span>
        <el-button type="primary" plain @click="knowledgeDrawerVisible = true">
          <el-icon><Setting /></el-icon>
          知识库配置
        </el-button>
        <el-button text @click="clearConversation">
          <el-icon><Delete /></el-icon>
          清空当前会话
        </el-button>
      </div>
    </header>

    <main class="page-body">
      <aside class="sidebar">
        <el-button type="primary" class="new-conversation" @click="createConversation">
          <el-icon><Plus /></el-icon>
          新建会话
        </el-button>

        <div ref="conversationListRef" class="conversation-list">
          <div class="list-heading">
            <span>会话记录</span>
            <span class="conversation-count">{{ conversations.length }}</span>
          </div>
          <button
            v-for="conversation in conversations"
            :key="conversation.id"
            type="button"
            :class="['conversation-item', { active: conversation.id === conversationId }]"
            @click="selectConversation(conversation.id)"
          >
            <span class="conversation-main">
              <span class="conversation-title">{{ conversation.title || '新建会话' }}</span>
              <small>{{ formatTime(conversation.updatedTime || conversation.createdTime) }}</small>
            </span>
            <el-dropdown trigger="click" @click.stop>
              <el-button text class="conversation-menu" @click.stop>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="renameConversation(conversation)"
                    >修改标题</el-dropdown-item
                  >
                  <el-dropdown-item divided @click="removeConversation(conversation.id)">
                    删除会话
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </button>
          <el-empty v-if="!conversations.length" description="暂无会话" :image-size="64" />
        </div>

        <div class="quick-questions">
          <div class="list-heading">常见问题</div>
          <button
            v-for="(question, index) in quickQuestions"
            :key="index"
            type="button"
            class="question-item"
            @click="selectQuestion(question)"
          >
            <el-icon><QuestionFilled /></el-icon>
            <span>{{ question }}</span>
          </button>
        </div>
      </aside>

      <section class="chat-panel">
        <div ref="chatHistoryRef" class="chat-history">
          <div v-if="!messages.length" class="empty-state">
            <el-icon :size="44" color="#9fb3c8"><ChatLineRound /></el-icon>
            <h2>欢迎使用农业助手</h2>
            <p>输入问题后，回答会以流式方式显示并自动保存。</p>
          </div>

          <div
            v-for="(message, index) in messages"
            :key="message.id || `${message.role}-${index}`"
            :class="['chat-message', message.role === 'user' ? 'user' : 'assistant']"
          >
            <el-avatar
              :size="36"
              :class="message.role === 'user' ? 'user-avatar' : 'assistant-avatar'"
            >
              <el-icon v-if="message.role === 'user'"><User /></el-icon>
              <el-icon v-else><Cpu /></el-icon>
            </el-avatar>

            <div class="message-card">
              <div class="message-meta">
                <span>{{ message.role === 'user' ? '你' : '农业助手' }}</span>
                <span>{{ formatTime(message.createdTime || message.timestamp) }}</span>
              </div>

              <div v-if="editingMessageId === message.id" class="edit-box">
                <el-input
                  v-model="editDraft"
                  type="textarea"
                  :rows="4"
                  resize="none"
                  placeholder="修改问题后将以新消息重新提问"
                  class="edit-textarea"
                />
                <div class="edit-actions">
                  <!-- <span class="edit-hint">基于当前对话记录重新提问，原消息不会被删除</span> -->
                  <el-button plain @click="cancelEdit">取消</el-button>
                  <el-button type="primary" :loading="sending" @click="submitEdit">
                    <el-icon><RefreshRight /></el-icon>
                    <span>重新生成</span>
                  </el-button>
                </div>
              </div>
              <div v-else class="message-content">
                <div v-if="message.attachments?.length" class="message-attachments">
                  <template
                    v-for="attachment in message.attachments"
                    :key="attachment.url || attachment.name"
                  >
                    <el-image
                      v-if="isImageAttachment(attachment)"
                      class="message-image"
                      :src="resolveAttachmentUrl(attachment.url)"
                      :preview-src-list="[resolveAttachmentUrl(attachment.url)]"
                      :preview-teleported="true"
                      fit="cover"
                    />
                    <a
                      v-else
                      class="message-file"
                      :href="resolveAttachmentUrl(attachment.url)"
                      target="_blank"
                      rel="noreferrer"
                    >
                      <el-icon :color="attachmentIconInfo(attachment).color">
                        <component :is="attachmentIconInfo(attachment).icon" />
                      </el-icon>
                      <span>{{ attachment.name }}</span>
                    </a>
                  </template>
                </div>
                <div
                  v-if="message.role === 'assistant' && message.isStreaming && !message.content"
                  class="loading-dots"
                >
                  <span></span><span></span><span></span>
                </div>
                <div
                  v-if="message.content"
                  class="message-text"
                  v-html="renderMarkdown(message.content)"
                ></div>
                <span
                  v-if="message.role === 'assistant' && message.isStreaming"
                  class="typing-cursor"
                  >|</span
                >
              </div>

              <div v-if="!message.isStreaming && message.content" class="message-actions">
                <el-button text size="small" title="复制" @click="copyMessage(message.content)">
                  <el-icon><CopyDocument /></el-icon>
                </el-button>
                <el-button
                  v-if="message.role === 'user'"
                  text
                  size="small"
                  title="编辑"
                  @click="startEdit(message)"
                >
                  <el-icon><Edit /></el-icon>
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div class="composer">
          <div class="composer-toolbar">
            <el-upload
              action="#"
              :show-file-list="false"
              :multiple="true"
              :disabled="sending"
              :accept="chatAttachmentAccept"
              :before-upload="validateChatFile"
              :http-request="uploadChatAttachment"
            >
              <el-button circle title="上传附件">
                <el-icon><Plus /></el-icon>
              </el-button>
            </el-upload>
            <span class="attachment-hint">图片或文档，单个文件不超过 10MB</span>
            <el-button
              text
              type="primary"
              class="scope-button"
              @click="knowledgeDrawerVisible = true"
            >
              <el-icon><Collection /></el-icon>
              <span class="scope-text">{{ scopeSummary || '选择检索范围' }}</span>
            </el-button>
          </div>
          <div v-if="attachments.length" class="attachment-list">
            <div v-for="attachment in attachments" :key="attachment.id" class="attachment-item">
              <el-image
                v-if="isImageAttachment(attachment)"
                class="attachment-thumb"
                :src="resolveAttachmentUrl(attachment.url)"
                :preview-src-list="[resolveAttachmentUrl(attachment.url)]"
                :preview-teleported="true"
                fit="cover"
              />
              <el-icon v-else class="attachment-file-icon" :color="attachmentIconInfo(attachment).color">
                <component :is="attachmentIconInfo(attachment).icon" />
              </el-icon>
              <span class="attachment-name" :title="attachment.name">{{ attachment.name }}</span>
              <el-button text circle title="删除附件" @click="removeAttachment(attachment.id)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
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
            <div class="composer-right">
              <span v-if="isOverTextLimit" class="text-limit-warning">
                <el-icon><WarningFilled /></el-icon>
                文本已达 {{ inputMessage.length }} 字，超出 {{ MAX_TEXT_LENGTH }} 字上限，请精简后再发送
              </span>
              <el-button v-if="sending" type="danger" @click="stopGeneration">
                <el-icon><VideoPause /></el-icon>
                停止生成
              </el-button>
              <el-button
                v-else
                type="primary"
                :disabled="(!inputMessage.trim() && !attachments.length) || isOverTextLimit"
                @click="sendMessage"
              >
                <el-icon><Promotion /></el-icon>
                发送
              </el-button>
            </div>
          </div>
        </div>
      </section>
    </main>
    <KnowledgeBaseDrawer
      v-model="knowledgeDrawerVisible"
      :selected-knowledge-base-ids="selectedKnowledgeBaseIds"
      :selected-document-ids="selectedDocumentIds"
      @selection-change="handleSelectionChange"
    />
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { marked } from 'marked'
import {
  ChatLineRound,
  Close,
  Collection,
  CopyDocument,
  Cpu,
  DataAnalysis,
  Delete,
  Document,
  Edit,
  EditPen,
  Grid,
  MoreFilled,
  Notebook,
  Plus,
  Promotion,
  QuestionFilled,
  RefreshRight,
  Setting,
  User,
  VideoPause,
  WarningFilled,
} from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import $API from '@/api'
import KnowledgeBaseDrawer from '@/components/rag/KnowledgeBaseDrawer.vue'

marked.setOptions({ breaks: true, gfm: true })

const inputMessage = ref('')
// 与后端 ChatRequestDto.MAX_TEXT_LENGTH 保持一致
const MAX_TEXT_LENGTH = 2000
const isOverTextLimit = computed(() => inputMessage.value.length > MAX_TEXT_LENGTH)
const messages = ref([])
const conversations = ref([])
const conversationId = ref(null)
const sending = ref(false)
const chatHistoryRef = ref(null)
const conversationListRef = ref(null)
const abortController = ref(null)
const editingMessageId = ref(null)
const editDraft = ref('')
const attachments = ref([])
const knowledgeDrawerVisible = ref(false)
const selectedKnowledgeBaseIds = ref([])
const selectedDocumentIds = ref([])
const chatAttachmentAccept =
  '.jpg,.jpeg,.png,.gif,.webp,.bmp,.pdf,.doc,.docx,.txt,.md,.xlsx,.xls,.ppt,.pptx,.csv'
const allowedAttachmentPattern =
  /\.(jpg|jpeg|png|gif|webp|bmp|pdf|doc|docx|txt|md|xlsx|xls|ppt|pptx|csv)$/i
const scopeCounts = ref({ knowledgeBaseCount: 0, documentCount: 0 })
const scopeSummary = computed(() => {
  if (!selectedKnowledgeBaseIds.value.length && !selectedDocumentIds.value.length) return ''
  return `检索范围：${scopeCounts.value.knowledgeBaseCount} 个知识库，${scopeCounts.value.documentCount} 篇文档`
})

const typingState = {
  timer: null,
  queue: '',
  message: null,
  finished: false,
  resolve: null,
}

const quickQuestions = [
  '水稻叶片发黄怎么办？',
  '番茄常见病虫害有哪些？',
  '小麦什么时候播种最好？',
  '土壤酸化怎么改良？',
  '玉米施肥的最佳时间？',
  '果树修剪有什么技巧？',
]

const renderMarkdown = (content) => {
  const escaped = String(content || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // GFM 会把成对的单个 ~ 当删除线,模型常用 ~ 表示数值范围(如 4500~5000),
    // 转义非 ~~ 的孤立波浪号,保留真正的 ~~删除线~~ 语法
    .replace(/(?<!~)~(?!~)/g, '\\~')
  return marked.parse(escaped)
}

const scrollToBottom = () => {
  nextTick(() => {
    const el = chatHistoryRef.value
    if (!el) return
    // 桌面端消息区内部滚动;移动端整页滚动时跟随外层容器,保证新内容可见
    el.scrollTop = el.scrollHeight
    const wrapper = el.closest('.content-wrapper')
    if (wrapper && wrapper.scrollHeight > wrapper.clientHeight) {
      wrapper.scrollTop = wrapper.scrollHeight
    }
  })
}

const formatTime = (value) => {
  if (!value) return ''
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString()
}

const createMessageId = () => `local-${Date.now()}-${Math.random().toString(16).slice(2)}`

const parseMessagePayload = (value) => {
  if (value && typeof value === 'object') {
    return {
      text: value.text || value.content || '',
      attachments: Array.isArray(value.attachments) ? value.attachments : [],
    }
  }
  if (typeof value !== 'string') return { text: '', attachments: [] }
  try {
    const parsed = JSON.parse(value)
    return {
      text: parsed?.text || parsed?.content || '',
      attachments: Array.isArray(parsed?.attachments) ? parsed.attachments : [],
    }
  } catch {
    return { text: value, attachments: [] }
  }
}

const normalizeMessage = (message) => {
  const payload = parseMessagePayload(message.contentJson ?? message.content)
  return {
    ...message,
    role: String(message.role || '').toLowerCase() === 'user' ? 'user' : 'assistant',
    content: payload.text || (payload.attachments.length ? '已上传附件' : ''),
    attachments: payload.attachments,
    isStreaming: false,
    timestamp: message.createdTime || message.timestamp || new Date().toISOString(),
  }
}

const isImageAttachment = (attachment) =>
  String(attachment?.type || '')
    .toLowerCase()
    .startsWith('image/') || /\.(jpg|jpeg|png|gif|webp|bmp)$/i.test(attachment?.name || '')

// 按扩展名匹配文件图标与类型色,未识别的类型回退为通用文档图标
const ATTACHMENT_ICON_MAP = {
  pdf: { icon: Document, color: '#ef4444' },
  doc: { icon: EditPen, color: '#2b6cb0' },
  docx: { icon: EditPen, color: '#2b6cb0' },
  xls: { icon: Grid, color: '#1d9053' },
  xlsx: { icon: Grid, color: '#1d9053' },
  csv: { icon: Grid, color: '#1d9053' },
  md: { icon: Notebook, color: '#7c3aed' },
  txt: { icon: Document, color: '#64748b' },
  ppt: { icon: DataAnalysis, color: '#ea8c2e' },
  pptx: { icon: DataAnalysis, color: '#ea8c2e' },
}
const attachmentIconInfo = (attachment) => {
  const ext = String(attachment?.name || '')
    .split('.')
    .pop()
    .toLowerCase()
  return ATTACHMENT_ICON_MAP[ext] || { icon: Document, color: '#64748b' }
}

// 附件入库的是公网域名地址（img.ajie-ai.xyz），本地开发时该域名不可达，
// 图片缩略图和预览都会加载失败，这里在显示层重写为开发 MinIO 的局域网地址；
// 生产构建保持原地址不变。发送给后端的 imageUrls 仍使用原始 URL。
const DEV_MINIO_ENDPOINT = 'http://192.168.127.128:9000'
const resolveAttachmentUrl = (url) => {
  if (!url || !import.meta.env.DEV) return url
  return String(url).replace(/^https?:\/\/img\.ajie-ai\.xyz/, DEV_MINIO_ENDPOINT)
}

const validateChatFile = (file) => {
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.error(`${file.name} 超过 10MB 限制`)
    return false
  }
  if (!allowedAttachmentPattern.test(file.name)) {
    ElMessage.error('不支持的附件类型')
    return false
  }
  return true
}

const uploadChatAttachment = async ({ file, onSuccess, onError }) => {
  try {
    // 聊天附件只做文件存储：图片存 images/，其它文件存 upload/
    // 目的是让模型读取附件内容，因此不需要勾选知识库，也不写入知识库
    const response = await $API.uploadChatFile(file)
    const url = response.data?.data
    if (!url || typeof url !== 'string') throw new Error('上传接口未返回文件地址')
    attachments.value.push({
      id: createMessageId(),
      name: file.name,
      url,
      type: file.type || '',
      size: file.size,
    })
    onSuccess?.({})
    ElMessage.success(`${file.name} 上传成功`)
  } catch (error) {
    onError?.(error)
    ElMessage.error(error?.response?.data?.message || error?.message || `${file.name} 上传失败`)
  }
}

const removeAttachment = (id) => {
  attachments.value = attachments.value.filter((item) => item.id !== id)
}

const handleSelectionChange = ({
  knowledgeBaseIds = [],
  documentIds = [],
  knowledgeBaseCount,
  documentCount,
}) => {
  selectedKnowledgeBaseIds.value = [...knowledgeBaseIds]
  selectedDocumentIds.value = [...documentIds]
  scopeCounts.value = {
    knowledgeBaseCount: knowledgeBaseCount ?? selectedKnowledgeBaseIds.value.length,
    documentCount: documentCount ?? selectedDocumentIds.value.length,
  }
}

const loadConversations = async () => {
  const response = await $API.listConversations()
  conversations.value = response.data?.data || []
}

const loadMessages = async (id) => {
  if (!id) {
    messages.value = []
    return
  }
  const response = await $API.listConversationMessages(id)
  messages.value = (response.data?.data || []).map(normalizeMessage)
  scrollToBottom()
}

const selectConversation = async (id) => {
  if (sending.value && id !== conversationId.value) stopGeneration()
  conversationId.value = id
  editingMessageId.value = null
  await loadMessages(id)
}

const createConversation = async () => {
  if (sending.value) stopGeneration()
  const response = await $API.createConversation({ title: '新建会话', modelName: 'default' })
  const conversation = response.data?.data
  if (!conversation) return
  conversations.value.unshift(conversation)
  conversationId.value = conversation.id
  messages.value = []
}

const renameConversation = async (conversation) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的会话标题', '修改标题', {
      inputValue: conversation.title || '',
      confirmButtonText: '保存',
      cancelButtonText: '取消',
      inputValidator: (value) => (value.trim() ? true : '标题不能为空'),
    })
    await $API.updateConversation(conversation.id, { title: value.trim() })
    conversation.title = value.trim()
    ElMessage.success('标题已更新')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || '修改失败')
  }
}

const removeConversation = async (id) => {
  try {
    await ElMessageBox.confirm('删除后将无法恢复，是否继续？', '删除会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await $API.deleteConversation(id)
    conversations.value = conversations.value.filter((item) => item.id !== id)
    if (conversationId.value === id) {
      conversationId.value = null
      messages.value = []
    }
    ElMessage.success('会话已删除')
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || '删除失败')
  }
}

const clearConversation = async () => {
  if (!conversationId.value) return
  try {
    await ElMessageBox.confirm('删除当前会话及其消息？', '清空当前会话', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
    await removeConversation(conversationId.value)
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') ElMessage.error(error?.message || '操作失败')
  }
}

const selectQuestion = (question) => {
  inputMessage.value = question
}

const copyMessage = async (content) => {
  try {
    await navigator.clipboard.writeText(content)
    ElMessage.success('复制成功')
  } catch {
    ElMessage.error('复制失败，请检查浏览器权限')
  }
}

const startEdit = (message) => {
  editingMessageId.value = message.id
  editDraft.value = message.content
}

const cancelEdit = () => {
  editingMessageId.value = null
  editDraft.value = ''
}

const resetTypingState = () => {
  if (typingState.timer) {
    clearInterval(typingState.timer)
    typingState.timer = null
  }
  typingState.queue = ''
  typingState.message = null
  typingState.finished = false
  if (typingState.resolve) {
    typingState.resolve()
    typingState.resolve = null
  }
}

const settleTyping = () => {
  if (!typingState.finished || typingState.queue.length > 0) return
  if (typingState.timer) {
    clearInterval(typingState.timer)
    typingState.timer = null
  }
  if (typingState.resolve) {
    typingState.resolve()
    typingState.resolve = null
  }
}

const startTyping = () => {
  if (typingState.timer) return
  typingState.timer = window.setInterval(() => {
    if (!typingState.message) {
      settleTyping()
      return
    }

    if (typingState.queue.length > 0) {
      // 自适应追赶：积压越多每帧输出的字符越多，避免落后于模型流
      const step = Math.max(1, Math.ceil(typingState.queue.length / 8))
      typingState.message.content += typingState.queue.slice(0, step)
      typingState.queue = typingState.queue.slice(step)
      scrollToBottom()
      return
    }

    settleTyping()
  }, 16)
}

const queueTypingText = (message, text) => {
  if (!text) return
  typingState.message = message
  typingState.queue += text
  startTyping()
}

const finishTyping = () => {
  typingState.finished = true
  if (typingState.queue.length > 0) startTyping()
  else settleTyping()
}

const waitForTyping = () => {
  if (typingState.finished && typingState.queue.length === 0) {
    return Promise.resolve()
  }
  return new Promise((resolve) => {
    typingState.resolve = resolve
  })
}

const submitEdit = async () => {
  const content = editDraft.value.trim()
  if (!content || sending.value) return
  if (content.length > MAX_TEXT_LENGTH) {
    ElMessage.warning(`输入文本不能超过 ${MAX_TEXT_LENGTH} 个字符`)
    return
  }
  cancelEdit()
  // 基于当前会话原有消息，把编辑后的内容作为新消息重新提问（不删除任何历史）
  await streamMessage(content)
}

const parseSseEvent = (block) => {
  let event = 'message'
  const data = []
  block.split('\n').forEach((line) => {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    if (line.startsWith('data:')) data.push(line.slice(5).replace(/^ /, ''))
  })
  return { event, data: data.join('\n') }
}

const readSseStream = async (response, onEvent) => {
  const reader = response.body?.getReader()
  if (!reader) throw new Error('响应流不可用')
  const decoder = new TextDecoder()
  let buffer = ''
  try {
    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true }).replace(/\r\n/g, '\n')
      let split = buffer.indexOf('\n\n')
      while (split >= 0) {
        const block = buffer.slice(0, split)
        buffer = buffer.slice(split + 2)
        if (block.trim()) onEvent(parseSseEvent(block))
        split = buffer.indexOf('\n\n')
      }
    }
    buffer += decoder.decode()
    if (buffer.trim()) onEvent(parseSseEvent(buffer.trim()))
  } finally {
    reader.releaseLock()
  }
}

const streamMessage = async (content, replaceMessageId = null, pendingAttachments = []) => {
  if ((!content && !pendingAttachments.length) || sending.value) return
  resetTypingState()
  let assistantMessage
  if (!replaceMessageId) {
    messages.value.push({
      id: createMessageId(),
      role: 'user',
      content: content || '已上传附件',
      attachments: pendingAttachments,
      timestamp: new Date().toISOString(),
      isStreaming: false,
    })
  }
  const pendingAssistantMessage = {
    id: createMessageId(),
    role: 'assistant',
    content: '',
    timestamp: new Date().toISOString(),
    isStreaming: true,
  }
  messages.value.push(pendingAssistantMessage)
  // messages 是深层响应式数组，必须从数组中取出代理对象；
  // 直接修改 push 前的普通对象不会触发 Vue 视图更新。
  assistantMessage = messages.value[messages.value.length - 1]
  sending.value = true
  abortController.value = new AbortController()
  scrollToBottom()

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
        imageUrls: pendingAttachments.filter(isImageAttachment).map((item) => item.url),
        attachments: pendingAttachments,
        knowledgeBaseIds: selectedKnowledgeBaseIds.value,
        documentIds: selectedDocumentIds.value,
        conversationId: conversationId.value,
        replaceMessageId,
      }),
      signal: abortController.value.signal,
    })
    if (!response.ok) throw new Error(`请求失败: ${response.status}`)

    await readSseStream(response, ({ event, data }) => {
      if (event === 'conversation') {
        conversationId.value = Number(data)
      } else if (event === 'error') {
        throw new Error(data || '生成失败')
      } else if (event !== 'done' && event !== 'tool_call') {
        queueTypingText(assistantMessage, data)
      }
    })
    finishTyping()
    await waitForTyping()
    assistantMessage.isStreaming = false
    await loadConversations()
    if (conversationId.value) await loadMessages(conversationId.value)
  } catch (error) {
    if (error?.name !== 'AbortError') {
      assistantMessage.isStreaming = false
      if (!assistantMessage.content)
        assistantMessage.content = error?.message || '生成失败，请稍后重试'
      ElMessage.error(error?.message || '流式请求失败')
    }
  } finally {
    finishTyping()
    abortController.value = null
    sending.value = false
    scrollToBottom()
  }
}

const sendMessage = () => {
  const content = inputMessage.value.trim()
  if ((!content && !attachments.value.length) || sending.value) return
  if (isOverTextLimit.value) {
    ElMessage.warning(`输入文本不能超过 ${MAX_TEXT_LENGTH} 个字符`)
    return
  }
  const pendingAttachments = attachments.value.slice()
  inputMessage.value = ''
  attachments.value = []
  streamMessage(content, null, pendingAttachments)
}

const handleComposerKeydown = (event) => {
  if (event.ctrlKey && event.key === 'Enter') {
    event.preventDefault()
    sendMessage()
  }
}

const stopGeneration = () => {
  if (abortController.value) abortController.value.abort()
  const current = messages.value.findLast((message) => message.isStreaming)
  if (current) {
    current.isStreaming = false
    typingState.message = current
  }
  typingState.queue = ''
  typingState.finished = true
  settleTyping()
  sending.value = false
  abortController.value = null
  ElMessage.info('已停止生成，已保留当前内容')
}

// 滚动条仅在用户实际操作（滚轮/触摸/按下）时短暂显示，自动滚动不触发
const scrollHintCleanups = []
const bindScrollHint = (el) => {
  if (!el) return
  let timer = null
  const show = () => {
    el.classList.add('is-scrolling')
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => el.classList.remove('is-scrolling'), 1000)
  }
  const events = ['wheel', 'pointerdown', 'touchstart']
  events.forEach((name) => el.addEventListener(name, show, { passive: true }))
  scrollHintCleanups.push(() => {
    if (timer) clearTimeout(timer)
    events.forEach((name) => el.removeEventListener(name, show))
  })
}

onMounted(async () => {
  bindScrollHint(chatHistoryRef.value)
  bindScrollHint(conversationListRef.value)
  try {
    await loadConversations()
    if (conversations.value.length) await selectConversation(conversations.value[0].id)
  } catch (error) {
    ElMessage.error(error?.message || '加载会话失败')
  }
})

onBeforeUnmount(() => {
  scrollHintCleanups.forEach((fn) => fn())
  scrollHintCleanups.length = 0
  if (abortController.value) abortController.value.abort()
  resetTypingState()
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
  flex-wrap: wrap;
  justify-content: space-between;
  align-items: center;
  gap: 10px 16px;
  padding: 14px 20px;
  background: #fff;
  border: 1px solid #e3e7ec;
  border-radius: 6px;
}
.page-header h1 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
.page-header p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.scope-summary {
  color: #6b7280;
  font-size: 12px;
  white-space: nowrap;
  padding: 3px 10px;
  border: 1px solid #e3e7ec;
  border-radius: 4px;
  background: #f8fafc;
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
  min-height: 0;
  background: #fff;
  border: 1px solid #e3e7ec;
  border-radius: 6px;
}
.sidebar {
  background: #fafbfc;
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  overflow: hidden;
}
.new-conversation {
  width: 100%;
}
.conversation-list,
.quick-questions {
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.conversation-list {
  flex: 1;
  overflow-y: auto;
}
.quick-questions {
  overflow-y: auto;
}
.list-heading {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #6b7280;
  font-size: 12px;
  font-weight: 600;
  padding: 0 2px;
  margin-bottom: 4px;
}
.conversation-count {
  color: #9ca3af;
  font-weight: 400;
}
.conversation-item {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  border: 0;
  border-radius: 6px;
  background: transparent;
  padding: 8px 10px;
  text-align: left;
  cursor: pointer;
  color: #4b5563;
  transition: background 0.15s ease;
}
.conversation-item:hover {
  background: #f1f3f6;
}
.conversation-item.active {
  background: #e8edf3;
  color: #1f2937;
}
.conversation-item.active .conversation-title {
  font-weight: 600;
}
.conversation-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.conversation-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}
.conversation-main small {
  color: #9ca3af;
  font-size: 11px;
}
.conversation-menu {
  flex: 0 0 auto;
  opacity: 0;
  transition: opacity 0.15s ease;
}
.conversation-item:hover .conversation-menu,
.conversation-item.active .conversation-menu {
  opacity: 1;
}
.question-item {
  display: flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: #6b7280;
  text-align: left;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}
.question-item .el-icon {
  color: #9ca3af;
}
.question-item:hover {
  color: #2563eb;
  background: #f1f3f6;
}
.chat-panel {
  display: flex;
  flex-direction: column;
}
.chat-history {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
}
/* 细滚动条：默认隐藏，仅用户滑动（is-scrolling）或悬停滑块时显示 */
.conversation-list,
.chat-history {
  scrollbar-width: thin;
  scrollbar-color: transparent transparent;
}
.conversation-list.is-scrolling,
.chat-history.is-scrolling {
  scrollbar-color: rgba(100, 116, 139, 0.4) transparent;
}
.conversation-list::-webkit-scrollbar,
.chat-history::-webkit-scrollbar {
  width: 8px;
}
.conversation-list::-webkit-scrollbar-track,
.chat-history::-webkit-scrollbar-track {
  background: transparent;
}
.conversation-list::-webkit-scrollbar-thumb,
.chat-history::-webkit-scrollbar-thumb {
  border: 2px solid transparent;
  border-radius: 4px;
  background-color: transparent;
  background-clip: padding-box;
  transition: background-color 0.25s ease;
}
.conversation-list.is-scrolling::-webkit-scrollbar-thumb,
.chat-history.is-scrolling::-webkit-scrollbar-thumb,
.conversation-list::-webkit-scrollbar-thumb:hover,
.chat-history::-webkit-scrollbar-thumb:hover {
  background-color: rgba(100, 116, 139, 0.45);
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
  margin: 14px 0 6px;
  color: #374151;
  font-size: 16px;
  font-weight: 600;
}
.empty-state p {
  margin: 0;
  font-size: 13px;
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
  background: #4d7c5f;
}
.user-avatar {
  background: #2563eb;
}
.message-card {
  max-width: min(760px, 100%);
  min-width: 0;
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
  padding: 12px 16px;
  border-radius: 8px;
  background: #f5f6f8;
  color: #1f2937;
  line-height: 1.7;
}
.chat-message.user .message-content {
  background: #2563eb;
  color: #fff;
}
.chat-message.user .message-text :deep(a) {
  color: #fff;
}
.chat-message.user .message-file {
  background: rgba(255, 255, 255, 0.16);
}
.edit-box {
  padding: 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(37, 99, 235, 0.35);
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.12);
  line-height: 1.7;
  animation: editBoxIn 0.18s ease-out;
}
.edit-box .edit-textarea :deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #dbeafe inset;
  background: #f8fafc;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #111827;
  transition:
    box-shadow 0.2s ease,
    background 0.2s ease;
}
.edit-box .edit-textarea :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #bfdbfe inset;
}
.edit-box .edit-textarea :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1.5px #2563eb inset;
  background: #fff;
}
.message-attachments {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}
.message-image {
  width: 120px;
  height: 90px;
  border-radius: 6px;
  border: 1px solid rgba(148, 163, 184, 0.35);
}
.message-file {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: min(260px, 100%);
  padding: 6px 8px;
  border-radius: 5px;
  color: inherit;
  background: rgba(15, 23, 42, 0.07);
  text-decoration: none;
}
.message-file span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.message-text {
  overflow-wrap: anywhere;
}
.message-text :deep(p) {
  margin: 0 0 10px;
}
.message-text :deep(p:last-child) {
  margin-bottom: 0;
}
/* 全局 reset 把 font-weight 压成 normal，标题/加粗需要显式恢复 */
.message-text :deep(h1),
.message-text :deep(h2),
.message-text :deep(h3),
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  margin: 16px 0 8px;
  font-weight: 600;
  line-height: 1.4;
  color: inherit;
}
.message-text :deep(h1:first-child),
.message-text :deep(h2:first-child),
.message-text :deep(h3:first-child),
.message-text :deep(h4:first-child),
.message-text :deep(h5:first-child),
.message-text :deep(h6:first-child) {
  margin-top: 0;
}
.message-text :deep(h1) {
  font-size: 20px;
}
.message-text :deep(h2) {
  font-size: 18px;
}
.message-text :deep(h3) {
  font-size: 16px;
}
.message-text :deep(h4),
.message-text :deep(h5),
.message-text :deep(h6) {
  font-size: 15px;
}
.message-text :deep(strong),
.message-text :deep(b) {
  font-weight: 600;
}
.message-text :deep(em),
.message-text :deep(i) {
  font-style: italic;
}
.message-text :deep(a) {
  color: #2563eb;
  text-decoration: underline;
}
.message-text :deep(ul),
.message-text :deep(ol) {
  margin: 8px 0;
  padding-left: 24px;
}
.message-text :deep(ul) {
  list-style: disc;
}
.message-text :deep(ol) {
  list-style: decimal;
}
.message-text :deep(li) {
  margin: 4px 0;
}
.message-text :deep(li > ul),
.message-text :deep(li > ol) {
  margin: 2px 0;
}
.message-text :deep(li::marker) {
  color: #9ca3af;
}
.message-text :deep(hr) {
  border: none;
  border-top: 1px solid rgba(148, 163, 184, 0.5);
  margin: 14px 0;
}
.message-text :deep(blockquote) {
  margin: 8px 0;
  padding: 4px 12px;
  border-left: 3px solid rgba(148, 163, 184, 0.6);
  color: rgba(107, 114, 128, 0.9);
  background: rgba(15, 23, 42, 0.03);
}
.message-text :deep(table) {
  /* fit-content: 列少时外框收缩贴合内容，避免右侧空白；列多时靠 max-width 触发横向滚动 */
  width: fit-content;
  max-width: 100%;
  margin: 12px 0;
  border-collapse: separate;
  border-spacing: 0;
  font-size: 14px;
  display: block;
  overflow-x: auto;
  /* 触屏:允许手指横向拖动表格,不与页面纵向滚动互相抢占 */
  touch-action: pan-x pan-y;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-x: contain;
  background: #fff;
  border: 1px solid rgba(37, 99, 235, 0.2);
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(15, 23, 42, 0.08);
}
/* 加粗且常驻可见,保证触屏/平板能用手指拖动横向滚动条 */
.message-text :deep(table::-webkit-scrollbar) {
  height: 12px;
}
.message-text :deep(table::-webkit-scrollbar-track) {
  background: rgba(148, 163, 184, 0.18);
  border-radius: 6px;
  margin: 0 4px 2px;
}
.message-text :deep(table::-webkit-scrollbar-thumb) {
  background: rgba(100, 116, 139, 0.6);
  border-radius: 6px;
  border: 2px solid transparent;
  background-clip: padding-box;
}
.message-text :deep(th),
.message-text :deep(td) {
  padding: 8px 14px;
  text-align: left;
  vertical-align: top;
  border-bottom: 1px solid rgba(148, 163, 184, 0.28);
  border-right: 1px solid rgba(148, 163, 184, 0.18);
}
.message-text :deep(th:last-child),
.message-text :deep(td:last-child) {
  border-right: none;
}
.message-text :deep(tbody tr:last-child td) {
  border-bottom: none;
}
.message-text :deep(thead th) {
  font-weight: 600;
  color: #fff;
  background: linear-gradient(135deg, #2563eb, #3b82f6);
  white-space: nowrap;
  text-align: center;
  border-bottom: 1px solid rgba(37, 99, 235, 0.5);
}
.message-text :deep(tbody tr:nth-child(even)) {
  background: rgba(37, 99, 235, 0.045);
}
.message-text :deep(tbody tr:hover) {
  background: rgba(37, 99, 235, 0.09);
}
.message-text :deep(td code),
.message-text :deep(th code) {
  font-size: 12.5px;
}
.message-text :deep(pre) {
  margin: 10px 0;
  padding: 12px;
  overflow-x: auto;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 6px;
}
.message-text :deep(code) {
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(15, 23, 42, 0.08);
  font-family: Consolas, Monaco, 'Courier New', monospace;
  font-size: 13px;
}
.message-text :deep(pre code) {
  padding: 0;
  background: transparent;
}
.message-actions {
  display: flex;
  gap: 2px;
  align-self: flex-start;
  opacity: 0;
  transition: opacity 0.15s ease;
}
.chat-message:hover .message-actions {
  opacity: 1;
}
.chat-message.user .message-actions {
  align-self: flex-end;
}
.edit-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
}
.edit-hint {
  flex: 1;
  font-size: 12px;
  color: #94a3b8;
}
.edit-actions :deep(.el-button .el-icon) {
  margin-right: 4px;
}
@keyframes editBoxIn {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.typing-cursor {
  display: inline-block;
  margin-left: 2px;
  color: #94a3b8;
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
  background: #94a3b8;
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
.composer :deep(.el-textarea__inner) {
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 0 0 1px #d9dde3 inset;
  line-height: 1.6;
  transition: box-shadow 0.2s ease;
}
.composer :deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px #c3cad3 inset;
}
.composer :deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px #2563eb inset;
}
.composer-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 32px;
  margin-bottom: 8px;
}
.attachment-hint {
  color: #9ca3af;
  font-size: 12px;
}
.scope-button {
  margin-left: auto;
  max-width: 360px;
  min-width: 0;
}
.scope-text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.attachment-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.attachment-item {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  max-width: min(240px, 100%);
  padding: 4px 6px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #f8fafc;
}
.attachment-thumb {
  width: 32px;
  height: 32px;
  flex: 0 0 auto;
  border-radius: 4px;
}
.attachment-file-icon {
  flex: 0 0 auto;
  color: #64748b;
  font-size: 20px;
}
.attachment-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #475569;
  font-size: 12px;
}
.composer-actions {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.composer-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 12px;
}
.hint {
  color: #9ca3af;
  font-size: 12px;
}
.text-limit-warning {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #e6a23c;
  font-size: 12px;
  font-weight: 500;
}
/* ===== 平板 / 窄窗口:侧栏改为横向双栏,避免被压扁 ===== */
@media (max-width: 1024px) {
  .page-body {
    grid-template-columns: minmax(0, 1fr);
    grid-template-rows: auto minmax(0, 1fr);
    gap: 12px;
  }
  .sidebar {
    height: 230px;
    display: grid;
    grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
    grid-template-rows: auto minmax(0, 1fr);
    grid-template-areas:
      'new quick'
      'list quick';
    gap: 10px 16px;
    padding: 12px;
  }
  .new-conversation {
    grid-area: new;
  }
  .conversation-list {
    grid-area: list;
  }
  .quick-questions {
    grid-area: quick;
  }
}

/* ===== 手机 / 矮屏:整页自然滚动 + 输入区吸底,杜绝组件被截断 ===== */
@media (max-width: 720px), (max-width: 1024px) and (max-height: 560px) {
  .assistant-page {
    height: auto;
    min-height: 100%;
  }
  .page-header {
    padding: 12px 14px;
  }
  .header-actions {
    flex-wrap: wrap;
    justify-content: flex-end;
    row-gap: 6px;
  }
  .scope-summary {
    display: none;
  }
  .page-body {
    display: flex;
    flex-direction: column;
    gap: 10px;
    padding-top: 10px;
  }
  .sidebar {
    display: flex;
    flex-direction: column;
    gap: 10px;
    height: auto;
    padding: 12px;
  }
  /* 会话记录压缩为单行横滑胶囊,不再占据聊天区上方的大块空间 */
  .conversation-list {
    flex: none;
    max-height: none;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    gap: 8px;
    overflow-x: auto;
    overflow-y: hidden;
    padding-bottom: 2px;
  }
  .conversation-list .list-heading {
    display: none;
  }
  .conversation-item {
    flex: 0 0 auto;
    width: auto;
    max-width: 220px;
    border: 1px solid #e3e7ec;
    border-radius: 999px;
    background: #fff;
    padding: 6px 10px;
  }
  .conversation-item.active {
    border-color: #2563eb;
    background: #e8f0fe;
  }
  .conversation-main {
    flex-direction: row;
    align-items: center;
    gap: 6px;
  }
  .conversation-main small {
    display: none;
  }
  .conversation-title {
    font-size: 12px;
  }
  .quick-questions {
    flex-direction: row;
    flex-wrap: nowrap;
    gap: 8px;
    overflow-x: auto;
    overflow-y: hidden;
    padding-bottom: 2px;
  }
  .quick-questions .list-heading {
    flex: 0 0 auto;
    margin-bottom: 0;
  }
  .question-item {
    flex: 0 0 auto;
    border: 1px solid #e3e7ec;
    border-radius: 999px;
    background: #fff;
    padding: 5px 10px;
    white-space: nowrap;
  }
  .chat-history {
    flex: none;
    min-height: 0;
    overflow: visible;
    padding: 14px 12px;
    gap: 14px;
  }
  .message-card {
    max-width: calc(100% - 48px);
  }
  .message-content {
    padding: 10px 12px;
  }
  .empty-state {
    min-height: 200px;
  }
  .composer {
    position: sticky;
    bottom: 0;
    z-index: 5;
    background: #fff;
    padding: 10px 12px;
    border-radius: 0 0 6px 6px;
  }
  .composer-toolbar {
    gap: 6px;
  }
  .attachment-hint {
    display: none;
  }
  .scope-button {
    max-width: calc(100% - 44px);
  }
  .composer-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
  .hint {
    display: none;
  }
  .composer-right {
    width: 100%;
  }
  .text-limit-warning {
    flex: 1;
    min-width: 0;
    white-space: normal;
    line-height: 1.4;
    text-align: right;
  }
}

/* ===== 小屏手机:头部按钮等分,消息区进一步收紧 ===== */
@media (max-width: 480px) {
  .page-header h1 {
    font-size: 15px;
  }
  .page-header p {
    font-size: 12px;
  }
  .header-actions {
    width: 100%;
  }
  .header-actions .el-button {
    flex: 1;
    margin-left: 0;
    padding: 8px 10px;
  }
  .chat-message {
    gap: 8px;
  }
  .message-card {
    max-width: calc(100% - 44px);
  }
  .message-meta {
    font-size: 11px;
  }
}

/* ===== 触屏设备无 hover:菜单与操作按钮常显 ===== */
@media (hover: none) {
  .conversation-menu,
  .message-actions {
    opacity: 1;
  }
}
</style>
