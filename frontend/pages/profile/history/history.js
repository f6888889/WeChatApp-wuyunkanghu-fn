const app = getApp();

Page({
  data: {
    history: [],
    learningProgress: null,
    isLoading: true
  },

  onLoad() {
    this.loadHistory();
  },

  async loadHistory() {
    try {
      this.setData({ isLoading: true });
      const userId = app.getUserId();
      if (!userId) {
        wx.showToast({ title: '请先登录', icon: 'none' });
        wx.navigateBack();
        return;
      }

      const userRes = await app.request(`/users/${userId}`);
      if (userRes.code === 200) {
        const user = userRes.data;
        this.setData({
          history: user.learningHistory || [],
          learningProgress: user.learningProgress || null,
          isLoading: false
        });
      }
    } catch (err) {
      console.error('加载学习记录失败', err);
      this.setData({ isLoading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  goToCourse(e) {
    const courseId = e.currentTarget.dataset.courseid;
    if (courseId) {
      wx.navigateTo({ url: `/pages/course/course?id=${courseId}` });
    }
  },

  goToIndex() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  getProgressPercent(watchDuration, totalDuration) {
    if (!totalDuration || totalDuration === 0) return 0;
    const percent = Math.round((watchDuration / totalDuration) * 100);
    return Math.min(percent, 100);
  }
});
