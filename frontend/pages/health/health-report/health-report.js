const app = getApp();

Page({
  data: {
    loading: true,
    error: null,
    report: null,
    userId: '',
    alertStatus: null,
    emergencyContact: null,
    isReviewMode: true,
    isPageReady: false
  },

  // 审核模式检查
  checkReviewMode() {
    if (app.globalData.isReviewMode) {
      console.log('[健康报告] 审核模式拦截');
      wx.reLaunch({
        url: '/pages/index/index'
      });
      return true;
    }
    return false;
  },

  onLoad(options) {
    if (this.checkReviewMode()) {
      return;
    }
    this.setData({ 
      isReviewMode: app.globalData.isReviewMode,
      isPageReady: true
    });
    if (options.userId) {
      this.setData({ userId: options.userId });
      this.loadHealthReport(options.userId);
      this.loadAlertStatus(options.userId);
    } else {
      const userId = app.getUserId();
      if (userId) {
        this.setData({ userId });
        this.loadHealthReport(userId);
        this.loadAlertStatus(userId);
      } else {
        this.setData({
          loading: false,
          error: '用户ID不存在'
        });
      }
    }
  },

  onShow() {
    if (this.checkReviewMode()) {
      return;
    }
    if (!this.data.isPageReady) {
      this.setData({ isPageReady: true });
    }
  },

  async loadHealthReport(userId) {
    try {
      this.setData({ loading: true, error: null });

      wx.showLoading({ title: '加载中...' });
      const res = await app.request(`/health/report/${userId}`);
      wx.hideLoading();

      if (res && res.code === 200 && res.data) {
        this.setData({
          report: res.data,
          loading: false
        });
      } else {
        this.setData({
          loading: false,
          error: res.message || '加载健康报告失败'
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('加载健康报告失败', err);
      this.setData({
        loading: false,
        error: '网络请求失败，请重试'
      });
    }
  },

  async loadAlertStatus(userId) {
    try {
      const unreadRes = await app.request(`/health-alert/user/${userId}/unread`);
      if (unreadRes && unreadRes.code === 200 && unreadRes.data && unreadRes.data.length > 0) {
        this.setData({ alertStatus: unreadRes.data[0] });
      }

      // 审核模式下跳过加载紧急联系人信息
      if (this.data.isReviewMode) {
        return;
      }

      const contactRes = await app.request(`/health-alert/${userId}/emergency-contact`);
      if (contactRes && contactRes.code === 200 && contactRes.data) {
        this.setData({ emergencyContact: contactRes.data });
      }
    } catch (err) {
      console.error('加载预警状态失败', err);
    }
  },

  goToCourse(e) {
    const courseId = e.currentTarget.dataset.courseid;
    if (courseId) {
      wx.navigateTo({
        url: `/pages/course/course?courseId=${courseId}`
      });
    }
  },

  goToEmergency() {
    if (this.data.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/profile/emergency/emergency'
    });
  }
});