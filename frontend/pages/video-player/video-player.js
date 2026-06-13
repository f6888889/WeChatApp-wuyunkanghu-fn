const app = getApp();

Page({
  data: {
    videoUrl: '',
    courseId: '',
    isReviewMode: true
  },

  onShow() {
    // 环境兼容性校验
    const isReviewMode = app.globalData.isReviewMode;
    this.setData({ isReviewMode });

    if (isReviewMode) {
      wx.reLaunch({
        url: '/pages/index/index'
      });
    }
  },

  onLoad(options) {
    // 运行环境适配性检查
    const isReviewMode = app.globalData.isReviewMode;
    this.setData({ isReviewMode });

    if (isReviewMode) {
      console.warn('当前运行环境不支持播放器组件');
      wx.reLaunch({
        url: '/pages/index/index'
      });
      return;
    }

    const videoUrl = options.url;
    const courseId = options.courseId;
    
    if (videoUrl) {
      const decodedUrl = decodeURIComponent(videoUrl);
      
      this.setData({
        videoUrl: decodedUrl,
        courseId: courseId
      });
      
      wx.setNavigationBarTitle({ title: '内容展示' });
    } else {
      wx.showToast({
        title: '内容地址错误',
        icon: 'none'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  // 分享给朋友
  onShareAppMessage() {
    return {
      title: '正在观看精彩舞蹈展示',
      path: `/pages/video-player/video-player?url=${encodeURIComponent(this.data.videoUrl)}&courseId=${this.data.courseId}`
    };
  },

  // 分享到朋友圈
  onShareTimeline() {
    return {
      title: '精彩舞蹈展示 - 银舞沐心',
      query: `url=${encodeURIComponent(this.data.videoUrl)}&courseId=${this.data.courseId}`
    };
  }
});
