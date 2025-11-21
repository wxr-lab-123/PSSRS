// pages/login/login.js
const userApi = require('../../api/user.js')
const smsApi = require('../../api/sms.js')

Page({
  data: {
    activeTab: 'login',
    // 登录表单
    loginPhone: '',
    loginPassword: '',
    // 注册表单
    registerName: '',
    registerPhone: '',
    registerCode: '',
    registerIdCard: '',
    registerPassword: '',
    registerConfirmPassword: '',
    agreed: false,
    // 重置密码表单
    resetPhone: '',
    resetCode: '',
    resetPassword: '',
    resetConfirmPassword: '',
    // 验证码相关
    codeSending: false,
    resetCodeSending: false,
    codeCountdown: 0,
    resetCodeCountdown: 0,
    countdownTimer: null,
    resetCountdownTimer: null,
    // 来源页面
    fromPage: ''
  },

  onLoad(options) {
    // 保存来源页面
    if (options.from) {
      this.setData({
        fromPage: options.from
      })
    }
  },

  // 切换Tab
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab
    this.setData({
      activeTab: tab
    })
  },

  // 登录表单输入
  onLoginPhoneInput(e) {
    this.setData({ loginPhone: e.detail.value })
  },

  onLoginPasswordInput(e) {
    this.setData({ loginPassword: e.detail.value })
  },

  // 注册表单输入
  onRegisterNameInput(e) {
    this.setData({ registerName: e.detail.value })
  },

  onRegisterPhoneInput(e) {
    this.setData({ registerPhone: e.detail.value })
  },

  onRegisterCodeInput(e) {
    this.setData({ registerCode: e.detail.value })
  },

  onRegisterIdCardInput(e) {
    this.setData({ registerIdCard: e.detail.value })
  },

  onRegisterPasswordInput(e) {
    this.setData({ registerPassword: e.detail.value })
  },

  onRegisterConfirmPasswordInput(e) {
    this.setData({ registerConfirmPassword: e.detail.value })
  },

  // 协议勾选
  onAgreementChange(e) {
    this.setData({
      agreed: e.detail.value.length > 0
    })
  },

  // 发送注册验证码
  sendRegisterCode() {
    const { registerPhone } = this.data

    // 验证手机号
    if (!registerPhone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }

    if (!/^1[3-9]\d{9}$/.test(registerPhone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    // 发送验证码
    this.setData({ codeSending: true })
    
    smsApi.sendRegisterCode(registerPhone)
      .then(res => {
        wx.showToast({
          title: '验证码已发送',
          icon: 'success'
        })
        
        // 开始倒计时
        this.startCountdown()
      })
      .catch(err => {
        console.error('发送验证码失败:', err)
      })
      .finally(() => {
        this.setData({ codeSending: false })
      })
  },

  // 开始倒计时
  startCountdown() {
    this.setData({ codeCountdown: 60 })
    
    this.data.countdownTimer = setInterval(() => {
      const countdown = this.data.codeCountdown - 1
      
      if (countdown <= 0) {
        clearInterval(this.data.countdownTimer)
        this.setData({ codeCountdown: 0 })
      } else {
        this.setData({ codeCountdown: countdown })
      }
    }, 1000)
  },

  // 处理登录
  handleLogin() {
    const { loginPhone, loginPassword } = this.data

    // 表单验证
    if (!loginPhone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }

    if (!/^1[3-9]\d{9}$/.test(loginPhone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    if (!loginPassword) {
      wx.showToast({
        title: '请输入密码',
        icon: 'none'
      })
      return
    }

    // 调用登录API
    userApi.login(loginPhone, loginPassword)
      .then(res => {
        // 保存token和用户信息
        // 后端返回格式：{code: 0, msg: "success", data: "token字符串"}
        const token = res.data
        
        if (!token) {
          wx.showToast({
            title: '登录失败：未获取到token',
            icon: 'none'
          })
          return
        }
        
        wx.setStorageSync('token', token)
        
        // 获取用户详细信息
        return userApi.getUserInfo()
      })
      .then(userRes => {
        // 保存用户信息（包含姓名等）
        const userInfo = {
          phone: loginPhone,
          loginTime: new Date().getTime(),
          ...userRes.data // 合并后端返回的用户信息（包含name等字段）
        }
        wx.setStorageSync('userInfo', userInfo)

        wx.showToast({
          title: '登录成功',
          icon: 'success'
        })

        // 跳转到我的页面
        setTimeout(() => {
          wx.switchTab({
            url: '/pages/mine/mine',
            success: () => {
              console.log('跳转到我的页面成功')
            },
            fail: (err) => {
              console.error('跳转失败:', err)
            }
          })
        }, 1500)
      })
      .catch(err => {
        console.error('登录失败:', err)
        wx.showToast({
          title: (err && (err.msg || err.message)) || '登录失败，请检查网络或联系管理员',
          icon: 'none',
          duration: 2500
        })
      })
  },

  // 处理注册
  handleRegister() {
    const { registerName, registerPhone, registerCode, registerIdCard, registerPassword, registerConfirmPassword, agreed } = this.data

    // 表单验证
    if (!registerName) {
      wx.showToast({
        title: '请输入姓名',
        icon: 'none'
      })
      return
    }

    if (!registerPhone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }

    if (!/^1[3-9]\d{9}$/.test(registerPhone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    if (!registerCode) {
      wx.showToast({
        title: '请输入验证码',
        icon: 'none'
      })
      return
    }

    if (registerCode.length !== 6) {
      wx.showToast({
        title: '验证码格式不正确',
        icon: 'none'
      })
      return
    }

    if (!registerIdCard) {
      wx.showToast({
        title: '请输入身份证号',
        icon: 'none'
      })
      return
    }

    if (!/^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/.test(registerIdCard)) {
      wx.showToast({
        title: '身份证号格式不正确',
        icon: 'none'
      })
      return
    }

    if (!registerPassword) {
      wx.showToast({
        title: '请设置密码',
        icon: 'none'
      })
      return
    }

    if (registerPassword.length < 6 || registerPassword.length > 16) {
      wx.showToast({
        title: '密码长度为6-16位',
        icon: 'none'
      })
      return
    }

    if (registerPassword !== registerConfirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      })
      return
    }

    if (!agreed) {
      wx.showToast({
        title: '请阅读并同意用户协议',
        icon: 'none'
      })
      return
    }

    // 调用注册API
    userApi.register({
      name: registerName,
      phone: registerPhone,
      code: registerCode,
      idCard: registerIdCard,
      password: registerPassword,
      confirmPassword: registerConfirmPassword
    })
      .then(res => {
        wx.showToast({
          title: '注册成功',
          icon: 'success'
        })

        // 切换到登录Tab
        setTimeout(() => {
          this.setData({
            activeTab: 'login',
            loginPhone: registerPhone,
            loginPassword: ''
          })
        }, 1500)
      })
      .catch(err => {
        console.error('注册失败:', err)
      })
  },

  // 忘记密码 - 切换到重置密码tab
  forgotPassword() {
    this.setData({
      activeTab: 'reset'
    })
  },

  // 微信快捷登录
  wechatLogin() {
    wx.showLoading({ title: '登录中...' })

    // 获取微信登录凭证
    wx.login({
      success: (res) => {
        if (res.code) {
          // 调用后端微信登录接口
          userApi.wechatLogin(res.code)
            .then(result => {
              wx.hideLoading()
              
              // 后端返回格式：{code: 0, msg: "success", data: "token字符串"}
              const token = result.data
              
              if (!token) {
                wx.showToast({
                  title: '登录失败：未获取到token',
                  icon: 'none'
                })
                return
              }
              
              // 保存token
              wx.setStorageSync('token', token)
              
              // 获取用户详细信息
              return userApi.getUserInfo()
            })
            .then(userRes => {
              // 保存用户信息（包含姓名等）
              const userInfo = {
                loginTime: new Date().getTime(),
                loginType: 'wechat',
                ...userRes.data // 合并后端返回的用户信息（包含name等字段）
              }
              wx.setStorageSync('userInfo', userInfo)

              wx.showToast({
                title: '登录成功',
                icon: 'success'
              })
              
              setTimeout(() => {
                wx.switchTab({
                  url: '/pages/mine/mine'
                })
              }, 1500)
            })
            .catch(err => {
              wx.hideLoading()
              console.error('微信登录失败:', err)
              wx.showToast({
                title: (err && (err.msg || err.message)) || '微信登录失败',
                icon: 'none'
              })
            })
        } else {
          wx.hideLoading()
          wx.showToast({
            title: '获取微信授权失败',
            icon: 'none'
          })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({
          title: '微信登录失败',
          icon: 'none'
        })
      }
    })
  },

  // 查看用户协议
  viewAgreement() {
    wx.showModal({
      title: '用户协议',
      content: '这里是用户协议的内容...',
      showCancel: false
    })
  },

  // 查看隐私政策
  viewPrivacy() {
    wx.showModal({
      title: '隐私政策',
      content: '这里是隐私政策的内容...',
      showCancel: false
    })
  },

  // 重置密码表单输入
  onResetPhoneInput(e) {
    this.setData({ resetPhone: e.detail.value })
  },

  onResetCodeInput(e) {
    this.setData({ resetCode: e.detail.value })
  },

  onResetPasswordInput(e) {
    this.setData({ resetPassword: e.detail.value })
  },

  onResetConfirmPasswordInput(e) {
    this.setData({ resetConfirmPassword: e.detail.value })
  },

  // 发送重置密码验证码
  sendResetCode() {
    const { resetPhone } = this.data

    // 验证手机号
    if (!resetPhone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }

    if (!/^1[3-9]\d{9}$/.test(resetPhone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    // 发送验证码
    this.setData({ resetCodeSending: true })
    
    // 调用发送重置密码验证码API
    smsApi.sendResetPasswordCode(resetPhone)
      .then(res => {
        wx.showToast({
          title: '验证码已发送',
          icon: 'success'
        })
        
        // 开始倒计时
        this.startResetCountdown()
      })
      .catch(err => {
        console.error('发送验证码失败:', err)
        wx.showToast({
          title: err.msg || '发送验证码失败',
          icon: 'none'
        })
      })
      .finally(() => {
        this.setData({ resetCodeSending: false })
      })
  },

  // 开始重置密码验证码倒计时
  startResetCountdown() {
    this.setData({ resetCodeCountdown: 60 })
    
    this.data.resetCountdownTimer = setInterval(() => {
      const countdown = this.data.resetCodeCountdown - 1
      
      if (countdown <= 0) {
        clearInterval(this.data.resetCountdownTimer)
        this.setData({ resetCodeCountdown: 0 })
      } else {
        this.setData({ resetCodeCountdown: countdown })
      }
    }, 1000)
  },

  // 处理重置密码
  handleResetPassword() {
    const { resetPhone, resetCode, resetPassword, resetConfirmPassword } = this.data

    // 表单验证
    if (!resetPhone) {
      wx.showToast({
        title: '请输入手机号',
        icon: 'none'
      })
      return
    }

    if (!/^1[3-9]\d{9}$/.test(resetPhone)) {
      wx.showToast({
        title: '手机号格式不正确',
        icon: 'none'
      })
      return
    }

    if (!resetCode) {
      wx.showToast({
        title: '请输入验证码',
        icon: 'none'
      })
      return
    }

    if (resetCode.length !== 6) {
      wx.showToast({
        title: '验证码格式不正确',
        icon: 'none'
      })
      return
    }

    if (!resetPassword) {
      wx.showToast({
        title: '请设置新密码',
        icon: 'none'
      })
      return
    }

    if (resetPassword.length < 6 || resetPassword.length > 16) {
      wx.showToast({
        title: '密码长度为6-16位',
        icon: 'none'
      })
      return
    }

    if (resetPassword !== resetConfirmPassword) {
      wx.showToast({
        title: '两次密码不一致',
        icon: 'none'
      })
      return
    }

    // 调用重置密码API
    wx.showLoading({ title: '正在重置密码...' })
    
    // 调用用户API中的重置密码方法
    userApi.resetPassword({
      phone: resetPhone,
      code: resetCode,
      newPassword: resetPassword,
      confirmPassword: resetConfirmPassword
    })
      .then(res => {
        wx.hideLoading()
        wx.showToast({
          title: '密码重置成功',
          icon: 'success',
          duration: 2000,
          success: () => {
            // 重置成功后跳转到登录页
            setTimeout(() => {
              this.setData({
                activeTab: 'login',
                loginPhone: resetPhone,
                loginPassword: ''
              })
            }, 1500)
          }
        })
      })
      .catch(err => {
        wx.hideLoading()
        console.error('重置密码失败:', err)
        wx.showToast({
          title: (err && (err.msg || err.message)) || '密码重置失败',
          icon: 'none',
          duration: 2500
        })
      })
  },

  // 页面卸载时清除定时器
  onUnload() {
    if (this.data.countdownTimer) {
      clearInterval(this.data.countdownTimer)
    }
    if (this.data.resetCountdownTimer) {
      clearInterval(this.data.resetCountdownTimer)
    }
  }
})
