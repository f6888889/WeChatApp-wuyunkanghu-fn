const app = getApp();

Page({
  data: {
    courseId: '',
    course: null,
    isLoading: true,
    isFavorite: false,
    currentStep: 0,
    isPlaying: false,
    isVideoLoading: true,
    watchStartTime: 0,
    totalWatchTime: 0,
    hasRecorded: false,
    isReviewMode: true
  },

  onLoad(options) {
    // 环境适配状态同步校验
    const isReviewMode = app.globalData.isReviewMode;
    this.setData({ isReviewMode });

    if (options.id) {
      this.setData({ courseId: options.id });
      this.loadCourseDetail(options.id);
    }
  },

  onUnload() {
    this.recordLearningHistory();
  },

  onHide() {
    this.recordLearningHistory();
  },

  onShow() {
    this.setData({
      isReviewMode: app.globalData.isReviewMode
    });
  },

  async loadCourseDetail(id) {
    try {
      const res = await app.request(`/courses/${id}`);
      if (res.code === 200) {
        const hasVideo = res.data.videoUrl && res.data.videoUrl.trim() !== '';
        this.setData({
          course: res.data,
          isLoading: false,
          isPlaying: hasVideo,
          isVideoLoading: hasVideo,
          watchStartTime: Date.now()
        });
        wx.setNavigationBarTitle({ title: res.data.title });
      }
    } catch (err) {
      console.error('加载课程详情失败', err);
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      });
    }
  },

  async onFavorite() {
    const courseId = this.data.courseId;
    const isFavorite = this.data.isFavorite;
    const userId = app.getUserId();

    try {
      if (isFavorite) {
        await app.request(`/users/${userId}/favorites/${courseId}`, 'DELETE');
      } else {
        await app.request(`/users/${userId}/favorites/${courseId}`, 'POST');
      }
      this.setData({ isFavorite: !isFavorite });
      wx.showToast({
        title: isFavorite ? '取消收藏' : '收藏成功',
        icon: 'success'
      });
    } catch (err) {
      wx.showToast({
        title: '操作失败',
        icon: 'none'
      });
    }
  },

  onPlayVideo() {
    if (this.data.isReviewMode) {
      wx.showToast({
        title: '功能开发中',
        icon: 'none'
      });
      return;
    }

    if (this.data.course && this.data.course.videoUrl) {
      this.setData({ watchStartTime: Date.now() });
      wx.navigateTo({
        url: `/pages/video-player/video-player?url=${encodeURIComponent(this.data.course.videoUrl)}&courseId=${this.data.courseId}`
      });
    } else {
      wx.showToast({
        title: '暂无内容',
        icon: 'none'
      });
    }
  },

  onStepChange(e) {
    this.setData({
      currentStep: e.detail.current
    });
  },

  goToStep(index) {
    this.setData({ currentStep: index });
  },

  async recordLearningHistory() {
    if (this.data.watchStartTime > 0) {
      const watchTime = Math.round((Date.now() - this.data.watchStartTime) / 60000);
      this.setData({
        totalWatchTime: this.data.totalWatchTime + watchTime,
        watchStartTime: 0
      });
    }

    let totalWatchTime = this.data.totalWatchTime;
    if (totalWatchTime <= 0) {
      totalWatchTime = 1;
    }

    if (this.data.hasRecorded) return;

    const userId = app.getUserId();
    if (!userId) return;

    const course = this.data.course;
    if (!course) return;

    const now = new Date();
    const watchedAt = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')} ${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}`;

    try {
      await app.request(`/users/${userId}/learning-history`, 'POST', {
        courseId: course.id,
        courseTitle: course.title,
        coverImage: course.coverImage,
        watchDuration: totalWatchTime,
        totalDuration: course.duration,
        watchedAt: watchedAt
      });
      this.setData({ hasRecorded: true });
    } catch (err) {
      console.error('记录学习历史失败', err);
    }
  },

  getDifficultyText(difficulty) {
    const map = { 'easy': '简单', 'medium': '中等', 'hard': '较难' };
    return map[difficulty] || '一般';
  },

  getDifficultyClass(difficulty) {
    const map = { 'easy': 'tag-easy', 'medium': 'tag-medium', 'hard': 'tag-hard' };
    return map[difficulty] || '';
  },

  // 分享给朋友
  onShareAppMessage() {
    const course = this.data.course;
    return {
      title: course ? `跟我一起学：${course.title}` : '发现一个很棒的舞蹈康养课程',
      path: `/pages/course/course?id=${this.data.courseId}`,
      imageUrl: course ? course.coverImage : ''
    };
  },

  // 分享到朋友圈
  onShareTimeline() {
    const course = this.data.course;
    return {
      title: course ? `正在学习：${course.title}` : '银舞沐心舞蹈康养',
      query: `id=${this.data.courseId}`,
      imageUrl: course ? course.coverImage : ''
    };
  }
});
