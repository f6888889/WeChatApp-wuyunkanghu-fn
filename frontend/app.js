// 环境配置
const ENV = {
  production: {
    API_BASE: 'https://fn.zbhly.icu:14502/api',
    RESOURCE_BASE: 'https://fn.zbhly.icu:14502'
  },
  development: {
    API_BASE: 'https://fn.zbhly.icu:14502/api',
    RESOURCE_BASE: 'https://fn.zbhly.icu:14502'
  }
};

// 当前使用的环境 - 默认为生产环境
const CURRENT_ENV = 'development';
const config = ENV[CURRENT_ENV];

const API_BASE = config.API_BASE;
const RESOURCE_BASE = config.RESOURCE_BASE;

App({
  globalData: {
    userId: '',
    apiBase: API_BASE,
    resourceBase: RESOURCE_BASE,
    userInfo: null,
    token: '',
    appLaunchTime: 0,
    onlineTimer: null,
    isReviewMode: CURRENT_ENV === 'production' // 开发环境默认关闭，生产环境默认开启内容保护模式
  },

  onLaunch(options) {
    this.globalData.appLaunchTime = Date.now();
    this.checkReviewMode();
    this.checkUserStatus();
    this.handleLaunchOptions(options);
    this.startOnlineTimer();
  },

  onShow(options) {
    this.globalData.appLaunchTime = Date.now();
    this.handleLaunchOptions(options);
    this.startOnlineTimer();
  },

  handleLaunchOptions(options) {
    console.log('小程序启动参数:', options);
    if (options && options.scene) {
      console.log('场景值:', options.scene);
    }
    // 只要是严格模式且非开发环境，就引导用户通过首页进入，避免开发调试时被重定向
    if (CURRENT_ENV !== 'development' && this.globalData.isReviewMode && options && options.path && options.path !== '' && options.path !== 'pages/index/index') {
      console.log('当前环境强制重定向到首页:', options.path);
      wx.reLaunch({
        url: '/pages/index/index'
      });
    }
  },

  onHide() {
    this.stopOnlineTimer();
    this.recordOnlineTime();
  },

  checkReviewMode() {
    this.request('/config/status', 'GET')
      .then(res => {
        if (res && res.code === 200 && res.data) {
          this.globalData.isReviewMode = res.data.isReviewMode;
          console.log('当前运行环境状态:', this.globalData.isReviewMode);
          this.updateTabBarText();
        }
      })
      .catch(err => {
        console.error('获取运行配置失败', err);
      });
  },

  updateTabBarText() {
    const text = this.globalData.isReviewMode ? '精选' : '创作';
    wx.setTabBarItem({
      index: 2,
      text: text,
      fail: (err) => {
        console.warn('setTabBarItem failed:', err);
      }
    });
  },

  checkUserStatus() {
    const userId = wx.getStorageSync('userId');
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (userId) {
      this.globalData.userId = userId;
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
    }
  },

  wxLogin() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: res => {
          if (res.code) {
            this.request('/auth/login', 'POST', { code: res.code })
              .then(result => {
                if (result && result.data && result.data.user) {
                  this.globalData.userId = result.data.user.id;
                  this.globalData.token = result.data.token;
                  this.globalData.userInfo = result.data.user;
                  wx.setStorageSync('userId', result.data.user.id);
                  wx.setStorageSync('token', result.data.token);
                  wx.setStorageSync('userInfo', result.data.user);
                }
                resolve(result);
              })
              .catch(err => {
                console.error("Login request failed:", err);
                reject(err);
              });
          } else {
            console.error("wx.login code is empty:", res);
            reject({ message: '获取code失败' });
          }
        },
        fail: err => {
          console.error("wx.login failed:", err);
          reject(err);
        }
      });
    });
  },

  uploadFile(url, filePath, name = 'file', formData = {}) {
    const token = this.globalData.token || wx.getStorageSync('token');
    const header = {};
    if (token) {
      header['Authorization'] = `Bearer ${token}`;
    }

    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: `${this.globalData.apiBase}${url}`,
        filePath,
        name,
        formData,
        header,
        success: res => {
          if (res.statusCode === 200) {
            try {
              const data = JSON.parse(res.data);
              if (data.code === 200) {
                resolve(data);
              } else {
                console.error("API 业务错误:", data);
                reject(data);
              }
            } catch (e) {
              console.error("解析响应失败:", e, res.data);
              reject(e);
            }
          } else {
            console.error("HTTP 错误:", res.statusCode, res.data);
            reject(res);
          }
        },
        fail: err => {
          console.error('上传失败:', err);
          reject(err);
        }
      });
    });
  },

  request(url, method = 'GET', data = null) {
    const token = this.globalData.token || wx.getStorageSync('token');
    const header = {
      'Content-Type': 'application/json'
    };
    if (token) {
      header['Authorization'] = `Bearer ${token}`;
    }

    const requestUrl = `${this.globalData.apiBase}${url}`;
    // console.log('[请求]', method, requestUrl, data);

    return new Promise((resolve, reject) => {
      wx.request({
        url: requestUrl,
        method,
        data,
        header: header,
        success: res => {
          // console.log('[响应成功]', res.statusCode, res.data);
          if (res.statusCode === 200) {
            if (res.data && res.data.code !== 200) {
              console.error("API 业务错误:", res.data);
              reject(res.data);
            } else {
              // --- 环境加固：数据流安全清洗 ---
              if (this.globalData.isReviewMode && res.data && res.data.data) {
                const lowerUrl = url.toLowerCase();
                if (lowerUrl.includes('/courses')) {
                  if (Array.isArray(res.data.data)) {
                    res.data.data.forEach(item => { if (item.videoUrl) item.videoUrl = ''; });
                  } else if (res.data.data.videoUrl) {
                    res.data.data.videoUrl = '';
                  }
                }
              }
              // ------------------------------
              resolve(res.data);
            }
          } else if (res.statusCode === 401) {
            wx.removeStorageSync('userId');
            wx.removeStorageSync('token');
            wx.removeStorageSync('userInfo');
            wx.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            });
            reject(res.data);
          } else {
            console.error("HTTP 错误:", res.statusCode, res.data);
            reject(res.data);
          }
        },
        fail: err => {
          console.error('[请求失败]', err);
          console.error('请求地址:', requestUrl);
          wx.showToast({
            title: '网络请求失败，请检查后端服务',
            icon: 'none'
          });
          reject(err);
        }
      });
    });
  },

  getUserId() {
    return this.globalData.userId;
  },

  setUserId(userId) {
    this.globalData.userId = userId;
    wx.setStorageSync('userId', userId);
  },

  getUserNickname() {
    if (this.globalData.userInfo && this.globalData.userInfo.nickname) {
      return this.globalData.userInfo.nickname;
    }
    const userInfo = wx.getStorageSync('userInfo');
    if (userInfo && userInfo.nickname) {
      return userInfo.nickname;
    }
    return '用户';
  },

  recordOnlineTime() {
    if (this.globalData.appLaunchTime > 0) {
      const minutes = Math.round((Date.now() - this.globalData.appLaunchTime) / 60000);
      if (minutes > 0) {
        const userId = this.getUserId();
        if (userId) {
          this.request(`/users/${userId}/online-time`, 'POST', { minutes })
            .then(res => {
              if (res && res.code === 200 && res.data && res.data.pointsEarned > 0) {
                wx.showToast({
                  title: `观看奖励 +${res.data.pointsEarned} 积分`,
                  icon: 'success'
                });
              }
            })
            .catch(err => console.error('记录在线时间失败', err));
        }
      }
      this.globalData.appLaunchTime = 0;
    }
  },

  startOnlineTimer() {
    this.stopOnlineTimer();
    this.globalData.onlineTimer = setInterval(() => {
      const userId = this.getUserId();
      if (userId) {
        this.request(`/users/${userId}/online-time`, 'POST', { minutes: 1 })
          .then(res => {
            if (res && res.code === 200 && res.data && res.data.pointsEarned > 0) {
              wx.showToast({
                title: `观看奖励 +${res.data.pointsEarned} 积分`,
                icon: 'success'
              });
            }
          })
          .catch(err => console.error('记录在线时间失败', err));
      }
    }, 60000);
  },

  stopOnlineTimer() {
    if (this.globalData.onlineTimer) {
      clearInterval(this.globalData.onlineTimer);
      this.globalData.onlineTimer = null;
    }
  },

  // SSE 流式请求方法（使用 enableChunked + onChunkReceived）
  sseRequest(url, onChunk, onComplete, onError) {
    const token = this.globalData.token || wx.getStorageSync('token');
    const header = {};
    if (token) {
      header['Authorization'] = `Bearer ${token}`;
    }

    const requestUrl = `${this.globalData.apiBase}${url}`;
    console.log('[SSE 请求]', requestUrl);

    let buffer = '';
    
    // 自定义 UTF-8 解码器，解决部分设备或微信基础库没有 TextDecoder 的问题
    class Utf8Decoder {
      constructor() {
        this.leftOver = new Uint8Array(0);
      }
      decode(chunkBuffer) {
        const chunk = new Uint8Array(chunkBuffer);
        const totalLen = this.leftOver.length + chunk.length;
        const bytes = new Uint8Array(totalLen);
        bytes.set(this.leftOver);
        bytes.set(chunk, this.leftOver.length);
        let out = "";
        let i = 0;
        while (i < totalLen) {
          const c = bytes[i];
          if (c < 0x80) {
            out += String.fromCharCode(c);
            i++;
          } else if (c < 0xE0) {
            if (i + 1 >= totalLen) break;
            const char2 = bytes[i + 1];
            out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
            i += 2;
          } else if (c < 0xF0) {
            if (i + 2 >= totalLen) break;
            const char2 = bytes[i + 1];
            const char3 = bytes[i + 2];
            out += String.fromCharCode(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | (char3 & 0x3F));
            i += 3;
          } else {
            if (i + 3 >= totalLen) break;
            i += 4;
          }
        }
        this.leftOver = bytes.slice(i);
        return out;
      }
    }

    let decoder;
    let isNativeDecoder = false;
    if (typeof TextDecoder !== 'undefined') {
      try {
        decoder = new TextDecoder('utf-8');
        isNativeDecoder = true;
      } catch (e) {
        console.warn('创建原生 TextDecoder 失败，使用自定义解码器', e);
        decoder = new Utf8Decoder();
      }
    } else {
      decoder = new Utf8Decoder();
    }

    const requestTask = wx.request({
      url: requestUrl,
      method: 'GET',
      header: {
        ...header,
        'Accept': 'text/event-stream'
      },
      enableChunked: true, // 开启分块传输
      responseType: 'arraybuffer', // 接收二进制数据
      success: (res) => {
        console.log('[SSE 完成]', res);
        if (res.statusCode === 200) {
          if (onComplete) {
            onComplete();
          }
        } else {
          console.error('[SSE 状态码错误]', res.statusCode);
          if (onError) {
            onError(new Error(`SSE status code ${res.statusCode}`));
          }
        }
      },
      fail: (err) => {
        console.error('[SSE 失败]', err);
        if (onError) {
          onError(err);
        }
      }
    });

    // 监听分块数据
    requestTask.onChunkReceived((res) => {
      try {
        const chunk = isNativeDecoder ? decoder.decode(res.data, { stream: true }) : decoder.decode(res.data);
        buffer += chunk;
        
        // 增加安全检测：如果数据一上来就是 JSON 错误（比如 {"code": 500, "message": "..."}），说明后端接口报错了
        if (buffer.trim().startsWith('{')) {
          try {
            const errJson = JSON.parse(buffer.trim());
            if (errJson && errJson.code && errJson.code !== 200) {
              console.error('[SSE 业务错误]', errJson);
              if (onError) {
                onError(errJson);
              }
              // 停止请求
              requestTask.abort();
              return;
            }
          } catch (e) {
            // 解析失败说明可能还没接收完整，或者不是 JSON，忽略并继续
          }
        }
        
        // 按单行解析，解决不同平台换行符格式导致的分段解析问题
        const lines = buffer.split('\n');
        buffer = lines.pop() || ''; // 保留未完成的行
        
        for (const line of lines) {
          const trimmedLine = line.trim();
          if (trimmedLine.startsWith('data:')) {
            const dataStr = trimmedLine.slice(5).trim();
            if (dataStr === '[DONE]') {
              console.log('[SSE 流结束]');
              if (onComplete) {
                onComplete();
              }
              return;
            }
            
            try {
              const data = JSON.parse(dataStr);
              const deltaText = data.choices?.[0]?.delta?.content || '';
              if (deltaText) {
                if (onChunk) {
                  onChunk(deltaText);
                }
              }
            } catch (e) {
              console.warn('[SSE 解析错误]', e, dataStr);
            }
          }
        }
      } catch (err) {
        console.error('[SSE 处理 Chunk 报错]', err);
        if (onError) {
          onError(err);
        }
      }
    });

    return requestTask;
  }
});
