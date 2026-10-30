本次开发目标：

构建完整的知识库管理中心，实现：

- 多知识库管理
- 知识库分类管理
- 文档管理
- 文件上传与向量化
- 知识库勾选检索
- 文档级勾选检索
- 聊天附件上传

# 功能一：农业助手增加附件上传能力

## UI位置

聊天输入框区域：

当前：

```
[ 输入框 ] [发送]
```

调整为：

```
[ + ] [ 输入框 ] [发送]
```

## 上传按钮

样式：

```
圆形按钮
+
```

采用：

```
Element Plus
el-upload
```

实现。

## 上传能力

支持：

### 图片

```
jpg
jpeg
png
gif
webp
bmp
```

### 文档

以后端实际支持格式为准。

自动读取后端：

```
DocumentParser
```

支持的格式。

通常包括：

```
pdf
doc
docx
txt
md
xlsx
xls
ppt
pptx
csv
```

## 文件限制

单文件：

```
最大40MB
```

支持：

```
多文件上传
```

## 上传后展示

在输入框左上角展示附件区域。

### 图片

显示：

```
缩略图
```

支持删除。

### 文件

显示：

```
文件图标
文件名
```

支持删除。

## 消息发送

发送消息时：

同时提交：

```
{
  "message":"...",
  "attachments":[]
}
```

后端保存附件信息。

# 功能二：新增知识库配置中心

## UI位置

页面右上角：

当前：

```
知识库配置按钮    用户信息
```

新增：

```
[知识库配置] [用户头像]
```

## 按钮样式

要求：

- Hover变色
- 点击高亮
- 与系统主题一致

## 抽屉弹窗

点击：

```
知识库配置
```

右侧：

```
Drawer
```

滑出。

宽度：

```
600~800px
```

# 功能三：知识库类型管理

## PostgreSQL新增表

新增：

```
ai_knowledge_base_type
```

字段：

```
id

type_name

type_code

description

sort_no

status

deleted

create_time

update_time
```

## 初始化数据

当前已有：

```
农业知识库
```

增加：

```
农业
```

类型。

例如：

```
id = 1
type_name = 农业
type_code = AGRICULTURE
```

并更新：

```
ai_knowledge_base
```

增加：

```
type_id
```

字段。

将：

```
农业知识库
```

绑定：

```
农业类型
```

# 功能四：知识库列表管理

## 抽屉展示

展示：

```
知识库名称

知识库类型

文档数量

更新时间
```

要求：

使用：

```
Card
Table
```

布局。

排列整齐。

## 查询条件

支持：

### 类型筛选

```
下拉框
```

### 名称筛选

支持：

```
前缀匹配
模糊匹配
```

例如：

```
农业
农业知识库
农业规范库
```

# 功能五：知识库CRUD

支持：

## 创建知识库

字段：

```
知识库名称

知识库类型

描述
```

## 编辑知识库

支持修改：

```
名称

类型

描述
```

## 删除知识库

采用：

```
逻辑删除
```

删除时：

同步失效：

```
文档
Chunk
向量记录
```

# 功能六：知识库详情页

点击知识库进入详情。

展示：

```
文档列表
```

字段：

```
文档名称

文件类型

Chunk数量

上传时间

更新时间
```

# 功能七：文档管理

支持：

## 上传文档

调用已有：

```
文档上传接口
```

调用：

```
向量化接口
```

自动完成：

```
上传
↓
解析
↓
切片
↓
Embedding
↓
存储pgvector
```

完整流程。

## 文档CRUD

支持：

```
新增

查看

修改名称

删除
```

## 删除要求

采用软删除。

删除文档时：

同步软删除：

```
ai_rag_document

ai_rag_chunk

向量数据
```

保证：

RAG检索无法命中。

# 功能八：知识库选择检索

## 农业助手页面

新增：

```
当前检索知识库
```

区域。

展示：

```
☑ 农业知识库

☑ 种植知识库

☐ 病虫害知识库
```

支持：

```
多选
```

## 检索逻辑

发送消息时：

传递：

```
{
  "knowledgeBaseIds":[1,2]
}
```

后端：

仅检索：

```
已勾选知识库
```

# 功能九：文档级检索控制

## 展开知识库

展示：

```
知识库
 ├─ 文档A
 ├─ 文档B
 ├─ 文档C
```

支持：

```
Tree
```

结构。

## 勾选能力

支持：

```
知识库勾选
文档勾选
```

例如：

```
☑ 农业知识库

  ☑ 玉米种植规范.pdf

  ☐ 水稻病虫害手册.pdf
```

## 检索逻辑

发送：

```
{
  "knowledgeBaseIds":[1],
  "documentIds":[11]
}
```

后端：

优先按：

```
documentIds
```

过滤。

再进行：

```
向量检索
```

# 功能十：RAG检索增强

修改农业助手接口。

新增：

```
knowledgeBaseIds

documentIds
```

参数。

Retriever查询时：

动态追加：

```
knowledge_base_id IN (...)
```

和：

```
document_id IN (...)
```

过滤条件。

确保：

用户勾选什么。

就只检索什么。

# 数据库设计要求

继续使用现有：

```
ai_knowledge_base

ai_rag_document

ai_rag_chunk

ai_rag_retrieval
```

表。

禁止：

重复创建知识库体系。

新增最少字段：

```
type_id

deleted

update_time
```

等必要字段。

# 前端涉及模块

重点检查：

```
RightSidebar.vue

ChatContainer.vue

ChatInput.vue

KnowledgeBaseDrawer.vue

KnowledgeBaseTree.vue

KnowledgeBaseForm.vue

DocumentTable.vue
```

不存在则新增。

# 后端涉及模块

重点检查：

```
KnowledgeBaseController

KnowledgeBaseService

DocumentController

DocumentService

RagService

RetrieverService
```

# 验收标准

必须全部完成：

✓ 聊天支持图片上传

✓ 聊天支持文档上传

✓ 多文件上传

✓ 单文件最大40MB

✓ 知识库配置按钮

✓ Drawer知识库中心

✓ 知识库类型管理

✓ 知识库CRUD

✓ 文档CRUD

✓ 上传自动向量化

✓ 向量同步软删除

✓ 知识库勾选检索

✓ 文档勾选检索

✓ RAG动态过滤

✓ PostgreSQL持久化正常

✓ 前后端联调通过

✓ npm build通过

✓ Maven编译通过

最终输出：

1. 修改文件列表
2. 新增文件列表
3. 新增数据库表
4. 修改数据库字段
5. 新增接口
6. 功能测试结果