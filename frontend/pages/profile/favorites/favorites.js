const app = getApp();

Page({
  data: {
    favorites: [],
    isLoading: true
  },

  onLoad() {
    this.loadFavorites();
  },

  async loadFavorites() {
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
        const favoriteIds = userRes.data.favorites || [];
        if (favoriteIds.length === 0) {
          this.setData({ favorites: [], isLoading: false });
          return;
        }

        const coursesRes = await app.request('/courses');
        if (coursesRes.code === 200) {
          const allCourses = coursesRes.data || [];
          const favoriteCourses = allCourses.filter(c => favoriteIds.includes(c.id));
          this.setData({ favorites: favoriteCourses, isLoading: false });
        }
      }
    } catch (err) {
      console.error('加载收藏失败', err);
      this.setData({ isLoading: false });
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  async onRemoveFavorite(e) {
    const courseId = e.currentTarget.dataset.courseid;
    if (!courseId) return;

    try {
      const userId = app.getUserId();
      await app.request(`/users/${userId}/favorites/${courseId}`, 'DELETE');
      wx.showToast({ title: '已取消收藏', icon: 'success' });
      this.loadFavorites();
    } catch (err) {
      console.error('取消收藏失败', err);
      wx.showToast({ title: '操作失败', icon: 'none' });
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

  getDifficultyText(difficulty) {
    const map = { 'easy': '简单', 'medium': '中等', 'hard': '较难' };
    return map[difficulty] || '一般';
  },

  getDifficultyClass(difficulty) {
    const map = { 'easy': 'tag-easy', 'medium': 'tag-medium', 'hard': 'tag-hard' };
    return map[difficulty] || '';
  }
});
