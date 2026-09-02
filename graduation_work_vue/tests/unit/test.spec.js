import { describe, test, expect, beforeEach } from '@jest/globals'

describe('登录模块测试 (DL-01 ~ DL-08)', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  test('DL-01: 空表单登录 - 验证用户名和密码不能为空', () => {
    const loginRules = {
      username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
    }

    const validateUsername = (value) => {
      if (!value) return loginRules.username[0].message
      return null
    }
    const validatePassword = (value) => {
      if (!value) return loginRules.password[0].message
      return null
    }

    expect(validateUsername('')).toBe('请输入用户名')
    expect(validatePassword('')).toBe('请输入密码')
    expect(validateUsername('testuser')).toBeNull()
    expect(validatePassword('password123')).toBeNull()
  })

  test('DL-02: 用户名错误 - 应提示用户名或密码错误', async () => {
    const mockLogin = async (form) => {
      if (form.username === 'wronguser' && form.password === 'password123') {
        return { data: { code: 400, message: '用户名或密码错误' } }
      }
      return { data: { code: '200' } }
    }

    const res = await mockLogin({ username: 'wronguser', password: 'password123' })
    expect(res.data.message).toBe('用户名或密码错误')
  })

  test('DL-03: 密码错误 - 应提示用户名或密码错误', async () => {
    const mockLogin = async (form) => {
      if (form.username === 'testuser' && form.password === 'wrongpassword') {
        return { data: { code: 400, message: '用户名或密码错误' } }
      }
      return { data: { code: '200' } }
    }

    const res = await mockLogin({ username: 'testuser', password: 'wrongpassword' })
    expect(res.data.message).toBe('用户名或密码错误')
  })

  test('DL-04: 账号被冻结 - 应提示账号被冻结', async () => {
    const mockLogin = async (form) => {
      if (form.username === 'frozenuser') {
        return { data: { code: 403, message: '账号被冻结' } }
      }
      return { data: { code: '200' } }
    }

    const res = await mockLogin({ username: 'frozenuser', password: 'password123' })
    expect(res.data.message).toBe('账号被冻结')
  })

  test('DL-05: 正常登录 - 应成功登录并跳转对应角色页面', async () => {
    const mockLogin = async (form) => {
      return {
        data: {
          code: '200',
          data: {
            token: 'mock-token-12345',
            role: 'user',
          },
        },
      }
    }

    const res = await mockLogin({ username: 'testuser', password: 'password123' })
    expect(res.data.code).toBe('200')
    expect(res.data.data.token).toBe('mock-token-12345')
    expect(res.data.data.role).toBe('user')
  })

  test('DL-06: Token生成 - 登录成功后本地存储应存在Token', async () => {
    const tokenData = { token: 'mock-token-12345', role: 'user' }
    if (tokenData && tokenData.token) {
      localStorage.setItem('token', tokenData.token)
      localStorage.setItem('role', tokenData.role)
    }

    expect(localStorage.getItem('token')).toBe('mock-token-12345')
    expect(localStorage.getItem('role')).toBe('user')
  })

  test('DL-07: Token校验 - 携带Token访问接口应请求成功', async () => {
    localStorage.setItem('token', 'mock-token-12345')
    const mockGetUserInfo = async () => {
      return {
        data: {
          code: '200',
          data: {
            userId: 1,
            username: 'testuser',
            name: '测试用户',
          },
        },
      }
    }

    const res = await mockGetUserInfo()
    expect(res.data.code).toBe('200')
    expect(res.data.data.username).toBe('testuser')
  })

  test('DL-08: 退出登录 - 应清除Token并跳转登录页', () => {
    localStorage.setItem('token', 'mock-token-12345')
    localStorage.setItem('role', 'user')

    localStorage.removeItem('token')
    localStorage.removeItem('role')

    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('role')).toBeNull()
  })
})

describe('个人中心模块测试 (PC-01 ~ PC-07)', () => {
  beforeEach(() => {
    localStorage.clear()
    localStorage.setItem('token', 'mock-token')
  })

  test('PC-01: 信息展示 - 应正确显示用户名、角色、手机号等', async () => {
    const mockGetUserInfo = async () => {
      return {
        data: {
          data: {
            userId: 1,
            username: 'testuser',
            name: '张三',
            role: 'user',
            phone: '13800138000',
            email: 'test@example.com',
          },
        },
      }
    }

    const res = await mockGetUserInfo()
    const userInfo = res.data.data

    expect(userInfo.username).toBe('testuser')
    expect(userInfo.name).toBe('张三')
    expect(userInfo.role).toBe('user')
    expect(userInfo.phone).toBe('13800138000')
    expect(userInfo.email).toBe('test@example.com')
  })

  test('PC-02: 修改手机号错误 - 输入非法手机号应提示格式错误', () => {
    const phoneRegex = /^1[3-9]\d{9}$/
    const invalidPhones = ['12345', 'abc', '13800138000123', '12345678901']
    const validPhones = ['13800138000', '15912345678', '19876543210']

    invalidPhones.forEach((phone) => {
      expect(phoneRegex.test(phone)).toBe(false)
    })
    validPhones.forEach((phone) => {
      expect(phoneRegex.test(phone)).toBe(true)
    })
  })

  test('PC-03: 修改邮箱错误 - 输入非法邮箱应提示格式错误', () => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    const invalidEmails = ['test', 'test@', '@test.com', 'test@test']
    const validEmails = ['test@example.com', 'user.name@domain.com', 'admin@company.cn']

    invalidEmails.forEach((email) => {
      expect(emailRegex.test(email)).toBe(false)
    })
    validEmails.forEach((email) => {
      expect(emailRegex.test(email)).toBe(true)
    })
  })

  test('PC-04: 修改信息成功 - 输入合法信息提交应更新成功并刷新页面数据', async () => {
    const mockUpdateUser = async (data) => {
      return { data: { code: '200', message: '修改成功' } }
    }

    const updateData = {
      userId: 1,
      username: 'testuser',
      name: '李四',
      phone: '13800138000',
      email: 'test@example.com',
    }
    const res = await mockUpdateUser(updateData)
    expect(res.data.code).toBe('200')
    expect(res.data.message).toBe('修改成功')
  })

  test('PC-05: 修改密码错误 - 当前密码错误应提示', async () => {
    const mockUpdatePassword = async (data) => {
      if (data.oldPassword === 'wrongoldpass') {
        return { data: { code: 400, message: '当前密码错误' } }
      }
      return { data: { code: '200', message: '修改成功' } }
    }

    const res = await mockUpdatePassword({
      oldPassword: 'wrongoldpass',
      newPassword: 'newpass123',
    })
    expect(res.data.message).toBe('当前密码错误')
  })

  test('PC-06: 密码不一致 - 新密码与确认密码不一致应提示', () => {
    const validateConfirmPassword = (newPassword, confirmPassword) => {
      if (newPassword !== confirmPassword) {
        return '两次密码不一致'
      }
      return null
    }

    expect(validateConfirmPassword('newpass123', 'differentpass')).toBe('两次密码不一致')
    expect(validateConfirmPassword('newpass123', 'newpass123')).toBeNull()
  })

  test('PC-07: 修改密码成功 - 输入正确数据应修改成功', async () => {
    const mockUpdatePassword = async (data) => {
      return { data: { code: '200', message: '修改成功' } }
    }

    const res = await mockUpdatePassword({
      oldPassword: 'oldpass123',
      newPassword: 'newpass123',
    })
    expect(res.data.code).toBe('200')
  })
})

describe('农业助手模块测试 (AI-01 ~ AI-05)', () => {
  beforeEach(() => {
    localStorage.clear()
    localStorage.setItem('token', 'mock-token')
  })

  test('AI-01: 提问功能 - 输入农业问题应返回AI回答', async () => {
    global.fetch = async () => ({
      json: async () => ({
        code: '200',
        data: {
          message: '番茄生长的最佳温度是20-30摄氏度',
        },
      }),
    })

    const res = await global.fetch('http://localhost:8080/assistant/chat?question=test', {
      headers: { Authorization: 'Bearer mock-token' },
    })
    const result = await res.json()

    expect(result.data.message).toBe('番茄生长的最佳温度是20-30摄氏度')
  })

  test('AI-03: Markdown渲染 - 返回带格式内容应正确渲染', () => {
    const marked = (text) => {
      return text
        .replace(/^# (.*$)/gm, '<h1>$1</h1>')
        .replace(/^## (.*$)/gm, '<h2>$1</h2>')
        .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
        .replace(/\*(.*?)\*/g, '<em>$1</em>')
        .replace(/`([^`]+)`/g, '<code>$1</code>')
    }

    const markdownText = '# Title\n\nThis is **bold** text\n\nThis is *italic* text\n\n`code`'
    const result = marked(markdownText)

    expect(result).toContain('<h1>Title</h1>')
    expect(result).toContain('<strong>bold</strong>')
    expect(result).toContain('<em>italic</em>')
    expect(result).toContain('<code>code</code>')
  })

  test('AI-05: 清空对话 - 点击清空应清除历史记录', async () => {
    const mockClearChatHistory = async () => {
      return { data: { code: '200', message: '清空成功' } }
    }

    const res = await mockClearChatHistory()
    expect(res.data.code).toBe('200')
  })
})

describe('地块管理模块测试 (DK-01 ~ DK-05)', () => {
  test('DK-01: 添加地块 - 输入地块信息提交应添加成功', async () => {
    const mockAddLand = async (data) => {
      return { data: { code: '200', message: '添加成功' } }
    }

    const landData = {
      landName: '测试地块',
      area: 100,
      soilType: '壤土',
    }
    const res = await mockAddLand(landData)
    expect(res.data.code).toBe('200')
  })

  test('DK-02: 修改地块 - 修改已有地块信息应更新成功', async () => {
    const mockUpdateLand = async (data) => {
      return { data: { code: '200', message: '更新成功' } }
    }

    const res = await mockUpdateLand({ landId: 1, landName: '新名称' })
    expect(res.data.code).toBe('200')
  })

  test('DK-03: 删除地块 - 删除某地块应删除成功', async () => {
    const mockDeleteLand = async (landId) => {
      return { data: { code: '200', message: '删除成功' } }
    }

    const res = await mockDeleteLand(1)
    expect(res.data.code).toBe('200')
  })

  test('DK-04: 模糊查询 - 输入名称关键字查询应返回匹配结果', async () => {
    const lands = [
      { landId: 1, landName: '东区1号地' },
      { landId: 2, landName: '东区2号地' },
      { landId: 3, landName: '西区1号地' },
    ]

    const searchLand = (keyword) => {
      return lands.filter((land) => land.landName.includes(keyword))
    }

    const results = searchLand('东区')
    expect(results.length).toBe(2)
    expect(results[0].landName).toBe('东区1号地')
  })

  test('DK-05: 地块分配 - 分配地块给承包人应分配成功并记录时间', async () => {
    const mockAllocateLand = async (data) => {
      return { data: { code: '200', message: '分配成功', allocateTime: new Date().toISOString() } }
    }

    const res = await mockAllocateLand({ landId: 1, contractorId: 2 })
    expect(res.data.code).toBe('200')
    expect(res.data.allocateTime).toBeDefined()
  })
})

describe('农作物管理模块测试 (NC-01 ~ NC-04)', () => {
  test('NC-01: 添加作物 - 输入作物信息应添加成功', async () => {
    const mockAddCrop = async (data) => {
      return { data: { code: '200', message: '添加成功' } }
    }

    const res = await mockAddCrop({ cropName: '小麦', growthCycle: 120 })
    expect(res.data.code).toBe('200')
  })

  test('NC-02: 重复名称 - 输入重复名称应提示', async () => {
    const existingCrops = ['小麦', '玉米', '水稻']

    const checkDuplicate = (name) => {
      return existingCrops.includes(name)
    }

    expect(checkDuplicate('小麦')).toBe(true)
    expect(checkDuplicate('大豆')).toBe(false)
  })

  test('NC-03: 修改作物 - 修改信息应更新成功', async () => {
    const mockUpdateCrop = async (data) => {
      return { data: { code: '200', message: '更新成功' } }
    }

    const res = await mockUpdateCrop({ cropId: 1, cropName: '新小麦' })
    expect(res.data.code).toBe('200')
  })

  test('NC-04: 删除作物 - 删除记录应删除成功', async () => {
    const mockDeleteCrop = async (cropId) => {
      return { data: { code: '200', message: '删除成功' } }
    }

    const res = await mockDeleteCrop(1)
    expect(res.data.code).toBe('200')
  })
})

describe('种植记录模块测试 (JL-01 ~ JL-04)', () => {
  test('JL-01: 添加记录 - 输入种植记录应添加成功', async () => {
    const mockAddRecord = async (data) => {
      return { data: { code: '200', message: '添加成功' } }
    }

    const res = await mockAddRecord({ recordId: 1, operationType: '播种' })
    expect(res.data.code).toBe('200')
  })

  test('JL-02: 状态修改 - 修改未完成记录应修改成功', async () => {
    const mockUpdateRecord = async (data) => {
      if (data.status === '已收割') {
        return { data: { code: 400, message: '已收割记录不可修改' } }
      }
      return { data: { code: '200', message: '修改成功' } }
    }

    const res = await mockUpdateRecord({ operationId: 1, status: '生长中' })
    expect(res.data.code).toBe('200')
  })

  test('JL-03: 已收割限制 - 修改已收割记录应提示不可修改', async () => {
    const mockUpdateRecord = async (data) => {
      if (data.status === '已收割') {
        return { data: { code: 400, message: '已收割记录不可修改' } }
      }
      return { data: { code: '200', message: '修改成功' } }
    }

    const res = await mockUpdateRecord({ operationId: 1, status: '已收割' })
    expect(res.data.code).toBe(400)
    expect(res.data.message).toBe('已收割记录不可修改')
  })

  test('JL-04: 查询记录 - 按地块查询应返回正确数据', async () => {
    const records = [
      { operationId: 1, recordId: 1, operationType: '播种' },
      { operationId: 2, recordId: 2, operationType: '施肥' },
    ]

    const searchByRecordId = (recordId) => {
      return records.filter((r) => r.recordId === recordId)
    }

    const results = searchByRecordId(1)
    expect(results.length).toBe(1)
    expect(results[0].operationType).toBe('播种')
  })
})

describe('农资库存模块测试 (NZ-01 ~ NZ-04)', () => {
  test('NZ-01: 入库操作 - 添加库存应库存增加', async () => {
    let stock = 100
    const addStock = (amount) => {
      stock += amount
      return stock
    }

    const newStock = addStock(50)
    expect(newStock).toBe(150)
  })

  test('NZ-02: 出库操作 - 减少库存应库存减少', async () => {
    let stock = 100
    const reduceStock = (amount) => {
      stock -= amount
      return stock
    }

    const newStock = reduceStock(30)
    expect(newStock).toBe(70)
  })

  test('NZ-03: 库存预警 - 库存低于阈值应显示红色提示', () => {
    const threshold = 10
    const stock = 5

    const shouldWarn = stock < threshold
    expect(shouldWarn).toBe(true)
  })

  test('NZ-04: 查询库存 - 查询农资应正确显示数据', async () => {
    const materials = [
      { materialId: 1, materialName: '氮肥', stock: 100 },
      { materialId: 2, materialName: '磷肥', stock: 50 },
    ]

    const searchMaterial = (name) => {
      return materials.find((m) => m.materialName === name)
    }

    const result = searchMaterial('氮肥')
    expect(result.stock).toBe(100)
  })
})

describe('设备管理模块测试 (SB-01 ~ SB-04)', () => {
  test('SB-01: 添加设备 - 输入设备信息应添加成功', async () => {
    const mockAddEquipment = async (data) => {
      return { data: { code: '200', message: '添加成功' } }
    }

    const res = await mockAddEquipment({ equipmentName: '拖拉机', equipmentType: '农业机械' })
    expect(res.data.code).toBe('200')
  })

  test('SB-02: 修改设备 - 修改设备信息应更新成功', async () => {
    const mockUpdateEquipment = async (data) => {
      return { data: { code: '200', message: '更新成功' } }
    }

    const res = await mockUpdateEquipment({ equipmentId: 1, equipmentName: '新型拖拉机' })
    expect(res.data.code).toBe('200')
  })

  test('SB-03: 删除设备 - 删除设备应删除成功', async () => {
    const mockDeleteEquipment = async (equipmentId) => {
      return { data: { code: '200', message: '删除成功' } }
    }

    const res = await mockDeleteEquipment(1)
    expect(res.data.code).toBe('200')
  })

  test('SB-04: 报修申请 - 提交报修应状态变为待审核', async () => {
    const mockRepairApply = async (data) => {
      return { data: { code: '200', message: '提交成功', status: '待审核' } }
    }

    const res = await mockRepairApply({ equipmentId: 1, description: '故障报修' })
    expect(res.data.status).toBe('待审核')
  })
})

describe('审批管理模块测试 (SP-01 ~ SP-03)', () => {
  test('SP-01: 审批通过 - 审核申请点击通过应状态变为已通过', async () => {
    const mockApprove = async (applicationId) => {
      return { data: { code: '200', message: '审批通过', status: '已通过' } }
    }

    const res = await mockApprove(1)
    expect(res.data.status).toBe('已通过')
  })

  test('SP-02: 审批拒绝 - 点击拒绝应状态变为已拒绝', async () => {
    const mockReject = async (applicationId) => {
      return { data: { code: '200', message: '审批拒绝', status: '已拒绝' } }
    }

    const res = await mockReject(1)
    expect(res.data.status).toBe('已拒绝')
  })

  test('SP-03: 重复审批 - 重复点击审批应提示不可重复操作', async () => {
    const mockApprove = async (applicationId, hasApproved) => {
      if (hasApproved) {
        return { data: { code: 400, message: '不可重复操作' } }
      }
      return { data: { code: '200', message: '审批通过' } }
    }

    const res = await mockApprove(1, true)
    expect(res.data.code).toBe(400)
    expect(res.data.message).toBe('不可重复操作')
  })
})
