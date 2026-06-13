const app = getApp();
// 播客数据改为由后端接口获取

Page({
  data: {
    searchQuery: '',
    podcastResults: [],
    allPodcasts: [],
    isSearching: false,
    hasSearched: false,
    isRecording: false,
    isRecommended: true,
    isReviewMode: true,
    aiResponse: '',
    isPodcastSearching: false
  },

  async loadPodcasts(options) {
    this.setData({ isSearching: true, isReviewMode: app.globalData.isReviewMode });
    try {
      const res = await app.request('/podcasts');
      if (res && res.code === 200 && res.data) {
        this.allPodcasts = res.data;
        this.setData({
          allPodcasts: this.allPodcasts,
          podcastResults: [],
          isRecommended: true,
          hasSearched: false,
          isSearching: false,
          aiResponse: '',
          isPodcastSearching: false
        });

        if (options && (options.query || options.keyword)) {
          const query = decodeURIComponent(options.query || options.keyword);
          this.setData({ searchQuery: query });
          this.onSearchConfirm({ detail: { value: query } });
        }
      } else {
        this.setData({ isSearching: false });
      }
    } catch (err) {
      console.error('[搜索页] 加载播客数据失败:', err);
      this.setData({ isSearching: false });
    }
  },

  onLoad(options) {
    console.log('[搜索页] onLoad');
    this.loadPodcasts(options);
  },

  onShow() {
    this.setData({
      isReviewMode: app.globalData.isReviewMode
    });
  },

  onSearchInput(e) {
    this.setData({
      searchQuery: e.detail.value
    });
  },

  doAiChat(query) {
    console.log('[搜索页] AI发问开始(流式):', query);
    
    if (!query || query.trim() === '') {
      return;
    }

    if (this.data.isReviewMode) {
      this.setData({ 
        isSearching: false,
        hasSearched: true, 
        aiResponse: '功能开发中' 
      });
      return;
    }

    this.setData({ isSearching: true, hasSearched: true, aiResponse: '' });

    app.sseRequest(
      `/ai/companion/chat?message=${encodeURIComponent(query)}`,
      (chunk) => {
        this.setData({
          aiResponse: (this.data.aiResponse || '') + chunk
        });
      },
      () => {
        this.setData({ isSearching: false });
      },
      (err) => {
        console.error('[搜索页] AI发问失败:', err);
        this.setData({ 
          aiResponse: '调用AI服务时出现错误，请检查网络或稍后重试。',
          isSearching: false 
        });
      }
    );
  },

  async doSearch(query) {
    console.log('[搜索页] 开始检索播客:', query);
    
    if (!query || query.trim() === '') {
      return;
    }

    this.setData({ 
      isSearching: true, 
      isPodcastSearching: true,
      hasSearched: true, 
      isRecommended: false 
    });

    try {
      const res = await app.request(`/podcasts/search?keyword=${encodeURIComponent(query)}`);
      if (res && res.code === 200 && res.data) {
        const results = res.data || [];

        this.setData({
          podcastResults: results,
          isSearching: false,
          isPodcastSearching: false
        });
      } else {
        this.setData({ 
          isSearching: false,
          isPodcastSearching: false
        });
      }
    } catch (err) {
      console.error('[搜索页] 搜索播客失败:', err);
      this.setData({ 
        isSearching: false,
        isPodcastSearching: false
      });
    }
  },

  onSearchConfirm(e) {
    const query = e.detail?.value || this.data.searchQuery;
    if (!query || !query.trim()) {
      return;
    }

    this.setData({ 
      isSearching: true, 
      hasSearched: true,
      isRecommended: false
    });

    this.doAiChat(query);
    this.doSearch(query);
  },

  onVoiceSearch() {
    if (this.data.isReviewMode) {
      wx.showToast({
        title: '功能开发中',
        icon: 'none'
      });
      return;
    }
    if (this.data.isRecording) {
      this.stopRecording();
    } else {
      this.startRecording();
    }
  },

  startRecording() {
    const recorderManager = wx.getRecorderManager();

    recorderManager.onStart(() => {
      this.setData({ isRecording: true });
      wx.showToast({
        title: '请说话...',
        icon: 'none',
        duration: 10000
      });
      this.recordingTimer = setTimeout(() => {
        this.stopRecording();
      }, 10000);
    });

    recorderManager.onStop((res) => {
      if (this.recordingTimer) {
        clearTimeout(this.recordingTimer);
        this.recordingTimer = null;
      }
      this.setData({ isRecording: false });
      wx.hideToast();
      if (res.duration > 500) {
        this.recognizeVoice(res.tempFilePath);
      } else {
        wx.showToast({ title: '录音太短', icon: 'none' });
      }
    });

    recorderManager.onError((err) => {
      console.error('录音失败', err);
      this.setData({ isRecording: false });
      wx.hideToast();
      wx.showToast({
        title: '录音失败',
        icon: 'none'
      });
    });

    this.recorderManager = recorderManager;
    recorderManager.start({
      format: 'mp3',
      sampleRate: 16000,
      numberOfChannels: 1,
      encodeBitRate: 48000,
      duration: 10000
    });
  },

  stopRecording() {
    if (this.recordingTimer) {
      clearTimeout(this.recordingTimer);
      this.recordingTimer = null;
    }
    if (this.recorderManager) {
      this.recorderManager.stop();
    }
    this.setData({ isRecording: false });
    wx.hideToast();
  },

  async recognizeVoice(filePath) {
    wx.showLoading({ title: '识别中...' });
    try {
      const res = await app.uploadFile('/voice/recognize', filePath, 'file');
      wx.hideLoading();
      if (res && res.code === 200 && res.data && res.data.text) {
        const text = res.data.text;
        this.setData({ searchQuery: text });
        wx.showToast({
          title: '已识别，点击发送',
          icon: 'none'
        });
      } else {
        wx.showToast({
          title: '识别失败',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('语音识别失败', err);
      wx.showToast({
        title: '识别失败',
        icon: 'none'
      });
    }
  },

  goToPodcast(e) {
    const id = e.currentTarget.dataset.id || e.target.dataset.id;
    console.log('[搜索页] 跳转至播客详情, id:', id);
    if (id) {
      wx.navigateTo({
        url: `/pages/podcast-detail/podcast-detail?id=${id}`,
        fail: (err) => {
          console.warn('[搜索页] navigateTo fail, trying redirectTo:', err);
          wx.redirectTo({
            url: `/pages/podcast-detail/podcast-detail?id=${id}`,
            fail: (err2) => {
              console.error('[搜索页] redirectTo also failed:', err2);
              wx.showToast({
                title: '打开失败，请重试',
                icon: 'none'
              });
            }
          });
        }
      });
    } else {
      console.warn('[搜索页] 跳转失败，未找到 id');
      wx.showToast({
        title: '获取播客数据失败',
        icon: 'none'
      });
    }
  }
});