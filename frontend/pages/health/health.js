const app = getApp();

Page({
  data: {
    healthProfile: {
      hasHypertension: false,
      hasDiabetes: false,
      hasArthritis: false,
      hasHeartDisease: false,
      mobilityLevel: 'good'
    },
    reminders: [],
    todayReminders: [],
    healthTips: [],
    stats: {
      heartRate: 72,
      bloodPressure: '120/80',
      steps: 3500,
      sleepHours: 7.5
    }
  },

  onLoad() {
    if (app.globalData.isReviewMode) {
      wx.showToast({
        title: '功能开发中',
        icon: 'none',
        duration: 2000
      });
      setTimeout(() => {
        wx.reLaunch({
          url: '/pages/index/index'
        });
      }, 1000);
      return;
    }
    this.loadHealthData();
  },

  async loadHealthData() {
    try {
      const [userRes, remindersRes, tipsRes] = await Promise.all([
        app.request(`/users/${app.getUserId()}`),
        app.request('/health/reminders/daily'),
        app.request('/health/tips')
      ]);

      if (userRes.code === 200) {
        this.setData({
          healthProfile: userRes.data.healthProfile || this.data.healthProfile
        });
      }

      if (remindersRes.code === 200) {
        this.setData({
          todayReminders: remindersRes.data
        });
      }

      if (tipsRes.code === 200) {
        this.setData({
          healthTips: tipsRes.data
        });
      }
    } catch (err) {
      console.error('加载健康数据失败', err);
    }
  },

  onReminderToggle(e) {
    const index = e.currentTarget.dataset.index;
    const reminders = this.data.todayReminders;
    reminders[index].completed = !reminders[index].completed;
    this.setData({ todayReminders: reminders });
  },

  goToCourseByTip(e) {
    const tip = e.currentTarget.dataset.tip;
    wx.navigateTo({
      url: `/pages/search/search?keyword=${encodeURIComponent(tip)}`
    });
  },

  // 分享给朋友
  onShareAppMessage() {
    return {
      title: '银舞沐心 - 您的健康守护助手',
      path: '/pages/health/health'
    };
  },

  // 分享到朋友圈
  onShareTimeline() {
    return {
      title: '银舞沐心 - 科学康养，守护健康',
      path: '/pages/health/health'
    };
  }
});
