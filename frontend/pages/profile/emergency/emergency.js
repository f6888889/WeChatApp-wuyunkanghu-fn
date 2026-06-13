const app = getApp();

Page({
  data: {
    contact: {
      name: '',
      phone: '',
      relationship: '',
      enabled: false
    },
    relationshipIndex: 0,
    relationships: ['子女', '儿子', '女儿', '配偶', '兄弟姐妹', '其他'],
    userInfo: {},
    currentMinutes: 0,
    recommendedMinutes: 180,
    currentTime: '',
    isPageReady: false // 标记页面是否可以正常显示
  },

  // 审核模式检查（独立函数，可复用）
  checkReviewMode() {
    if (app.globalData.isReviewMode) {
      console.log('[紧急联系人] 审核模式拦截');
      // 立即跳转，不需要延迟
      wx.reLaunch({
        url: '/pages/index/index'
      });
      return true; // 返回 true 表示被拦截
    }
    return false; // 返回 false 表示正常通行
  },

  onLoad() {
    if (this.checkReviewMode()) {
      return; // 被拦截就直接返回
    }
    this.setData({ isPageReady: true });
    this.setCurrentTime();
    this.loadUserInfo();
    this.loadEmergencyContact();
  },

  onShow() {
    // 每次页面显示时都检查
    if (this.checkReviewMode()) {
      return;
    }
    // 如果之前是拦截状态，现在恢复正常
    if (!this.data.isPageReady) {
      this.setData({ isPageReady: true });
      this.setCurrentTime();
      this.loadUserInfo();
      this.loadEmergencyContact();
    }
  },

  setCurrentTime() {
    const now = new Date();
    const time = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;
    this.setData({ currentTime: time });
  },

  loadUserInfo() {
    const userInfo = app.globalData.userInfo || {};
    this.setData({ userInfo });
  },

  loadEmergencyContact() {
    const userId = app.getUserId();
    if (!userId) return;

    wx.request({
      url: `${app.globalData.baseUrl}/health-alert/${userId}/emergency-contact`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data) {
          this.setData({ contact: res.data.data });
          const index = this.data.relationships.indexOf(res.data.data.relationship);
          if (index >= 0) {
            this.setData({ relationshipIndex: index });
          }
        }
      }
    });

    wx.request({
      url: `${app.globalData.baseUrl}/user/${userId}`,
      method: 'GET',
      success: (res) => {
        if (res.data.code === 200 && res.data.data && res.data.data.learningProgress) {
          this.setData({
            currentMinutes: res.data.data.learningProgress.todayOnlineMinutes || 0,
            recommendedMinutes: res.data.data.learningProgress.dailyRecommendedMinutes || 180
          });
        }
      }
    });
  },

  onNameInput(e) {
    this.setData({ 'contact.name': e.detail.value });
  },

  onPhoneInput(e) {
    this.setData({ 'contact.phone': e.detail.value });
  },

  onRelationshipChange(e) {
    const index = e.detail.value;
    this.setData({
      relationshipIndex: index,
      'contact.relationship': this.data.relationships[index]
    });
  },

  onEnabledChange(e) {
    this.setData({ 'contact.enabled': e.detail.value });
  },

  onSaveTap() {
    const userId = app.getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const contact = this.data.contact;

    if (!contact.name) {
      wx.showToast({ title: '请输入联系人姓名', icon: 'none' });
      return;
    }

    if (!contact.phone) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' });
      return;
    }

    if (!/^1[3-9]\d{9}$/.test(contact.phone)) {
      wx.showToast({ title: '请输入正确的手机号', icon: 'none' });
      return;
    }

    if (!contact.relationship) {
      wx.showToast({ title: '请选择与您的关系', icon: 'none' });
      return;
    }

    wx.request({
      url: `${app.globalData.baseUrl}/health-alert/${userId}/emergency-contact`,
      method: 'PUT',
      data: contact,
      header: { 'Content-Type': 'application/json' },
      success: (res) => {
        if (res.data.code === 200) {
          wx.showToast({ title: '保存成功', icon: 'success' });
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        } else {
          wx.showToast({ title: res.data.message || '保存失败', icon: 'none' });
        }
      },
      fail: () => {
        wx.showToast({ title: '网络请求失败', icon: 'none' });
      }
    });
  },

  onShareAppMessage() {
    return {
      title: '银舞沐心 - 紧急联系人设置，守护您的健康安全',
      path: '/pages/profile/emergency/emergency'
    };
  },

  onShareTimeline() {
    return {
      title: '银舞沐心 - 智能预警，守护健康',
      query: ''
    };
  }
});