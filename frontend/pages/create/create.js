const app = getApp();

const DEFAULT_SAFE_ARTICLES = [
  { id: 'course_001', title: '山水情歌', content: '在优美的山水情歌中翩翩起舞，动作舒缓流畅，旨在通过柔和的下肢摆动增强膝关节稳定性，适合喜欢抒情曲风的舞者。', coverImage: 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E5%B1%B1%E6%B0%B4%E6%83%85%E6%AD%8C%E5%9B%BE%E7%89%87.png' },
  { id: 'course_002', title: '美丽的草原我的家', content: '模仿雄鹰展翅的动作，在悠扬的草原旋律中充分拉伸肩颈，缓解长期伏案或低头带来的不适，提升个人气质。', coverImage: 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E7%BE%8E%E4%B8%BD%E7%9A%84%E8%8D%89%E5%8E%9F%E6%88%91%E7%9A%84%E5%AE%B6%E5%9B%BE%E7%89%87.png' },
  { id: 'course_003', title: '中老年健身操', content: '节奏明快、动作简单的基础健身操，适合零基础中老年朋友，旨在快速提升全身活力，增强协调性。', coverImage: 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E4%B8%AD%E8%80%81%E5%B9%B4%E5%81%A5%E8%BA%AB%E6%93%8D%E5%9B%BE%E7%89%87.png' },
  { id: 'course_004', title: '醉酒的蝴蝶', content: '经典广场舞曲目，动作轻盈优美，如同蝴蝶飞舞。通过欢快的节奏调节心情，辅助平稳血压，让你成为焦点。', coverImage: 'https://java-chashaobao-ai.oss-cn-beijing.aliyuncs.com/%E5%9B%BD%E5%88%9B%E5%9B%BE%E7%89%87/%E9%86%89%E9%85%92%E7%9A%84%E8%9D%B4%E8%9D%B6%E5%9B%BE%E7%89%87.png' }
];

Page({
  data: {
    videoPath: '',
    videoTitle: '',
    videoDesc: '',
    isUploading: false,
    uploadProgress: 0,
    safeArticles: DEFAULT_SAFE_ARTICLES,
    isReviewMode: true
  },

  onShow() {
    this.setData({
      isReviewMode: app.globalData.isReviewMode
    });
    if (app.updateTabBarText) {
      app.updateTabBarText();
    }
    this.loadSafeArticles();
  },

  async loadSafeArticles() {
    try {
      const url = this.data.isReviewMode ? '/courses/safe' : '/courses';
      const res = await app.request(url);
      if (res && res.code === 200 && res.data && res.data.length > 0) {
        const safeData = res.data.map(c => ({
          id: c.id,
          title: c.title,
          coverImage: c.coverImage,
          content: c.description
        }));
        this.setData({ safeArticles: safeData });
      } else if (this.data.isReviewMode) {
        this.setData({ safeArticles: DEFAULT_SAFE_ARTICLES });
      }
    } catch (err) {
      console.warn('[创作] 获取安全文章失败，使用默认数据', err);
      if (this.data.isReviewMode) {
        this.setData({ safeArticles: DEFAULT_SAFE_ARTICLES });
      }
    }
  },

  onChooseVideo() {
    if (this.data.isReviewMode) {
      wx.showToast({
        title: '上传功能暂未开放',
        icon: 'none'
      });
      return;
    }

    wx.chooseVideo({
      sourceType: ['album'],
      maxDuration: 60,
      success: (res) => {
        const tempFilePath = res.tempFilePath;
        const fileType = res.fileType;

        if (fileType && fileType.toLowerCase() !== 'h264') {
          wx.showToast({
            title: '仅支持常规格式',
            icon: 'none'
          });
          return;
        }

        const extension = tempFilePath.split('.').pop().toLowerCase();
        const allowedExtensions = ['mp4', 'mov', 'avi', 'mkv'];
        if (!allowedExtensions.includes(extension)) {
          wx.showToast({
            title: '仅支持常规格式',
            icon: 'none'
          });
          return;
        }

        this.setData({
          videoPath: tempFilePath
        });
      },
      fail: () => {
        wx.showToast({
          title: '选择文件失败',
          icon: 'none'
        });
      }
    });
  },

  onTitleInput(e) {
    this.setData({
      videoTitle: e.detail.value
    });
  },

  onDescInput(e) {
    this.setData({
      videoDesc: e.detail.value
    });
  },

  async onUpload() {
    if (this.data.isReviewMode) {
      wx.showToast({
        title: '功能维护中',
        icon: 'none'
      });
      return;
    }

    if (!this.data.videoPath) {
      wx.showToast({
        title: '请先选择文件',
        icon: 'none'
      });
      return;
    }

    if (!this.data.videoTitle) {
      wx.showToast({
        title: '请输入标题',
        icon: 'none'
      });
      return;
    }

    this.setData({
      isUploading: true,
      uploadProgress: 0
    });

    try {
      const userId = app.getUserId();
      const uploadResult = await app.uploadFile('/video/upload', this.data.videoPath, 'file', {
        userId: userId,
        title: this.data.videoTitle,
        description: this.data.videoDesc
      });

      wx.showToast({
        title: '上传成功',
        icon: 'success'
      });

      setTimeout(() => {
        this.setData({
          videoPath: '',
          videoTitle: '',
          videoDesc: '',
          isUploading: false,
          uploadProgress: 0
        });
      }, 1500);

    } catch (error) {
      console.error('Upload error:', error);
      wx.showToast({
        title: '上传失败，请重试',
        icon: 'none'
      });
      this.setData({
        isUploading: false
      });
    }
  },

  goToCourse(e) {
    const course = e.currentTarget.dataset.course;
    if (course && course.id) {
      wx.navigateTo({
        url: `/pages/course/course?id=${course.id}`
      });
    }
  }
});
