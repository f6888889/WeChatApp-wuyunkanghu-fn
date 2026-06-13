const app = getApp();

function formatTime(seconds) {
  if (!seconds || isNaN(seconds)) return '00:00';
  const mins = Math.floor(seconds / 60);
  const secs = Math.floor(seconds % 60);
  return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
}

Page({
  data: {
    podcast: null,
    isPlaying: false,
    currentTimeText: '00:00',
    durationText: '00:00',
    currentTime: 0,
    duration: 0,
    sliderValue: 0,
    isSliding: false
  },

  audioContext: null,

  async onLoad(options) {
    const id = options.id;
    wx.showLoading({ title: '加载中...' });
    try {
      const res = await app.request(`/podcasts/${id}`);
      wx.hideLoading();
      if (res && res.code === 200 && res.data) {
        const podcast = res.data;
        this.setData({
          podcast,
          durationText: podcast.duration || '00:00'
        });

        wx.setNavigationBarTitle({
          title: podcast.title
        });

        this.initAudio(podcast.audioUrl);
      } else {
        this.handleLoadError();
      }
    } catch (err) {
      wx.hideLoading();
      console.error('[播客详情] 获取播客数据失败:', err);
      this.handleLoadError();
    }
  },

  handleLoadError() {
    wx.showToast({
      title: '播客未找到',
      icon: 'none'
    });
    setTimeout(() => {
      wx.navigateBack();
    }, 1500);
  },

  onUnload() {
    this.destroyAudio();
  },

  initAudio(audioUrl) {
    const ctx = wx.createInnerAudioContext();
    ctx.src = audioUrl;
    ctx.autoplay = false;

    ctx.onCanplay(() => {
      // 获取时长，有时初始获取不到，会在播放时更新
      const duration = ctx.duration;
      if (duration && duration > 0) {
        this.setData({
          duration,
          durationText: formatTime(duration)
        });
      }
    });

    ctx.onTimeUpdate(() => {
      if (this.data.isSliding) return;
      const currentTime = ctx.currentTime;
      const duration = ctx.duration || this.data.duration;
      const progress = duration > 0 ? Math.round((currentTime / duration) * 100) : 0;

      this.setData({
        currentTime,
        currentTimeText: formatTime(currentTime),
        sliderValue: progress,
        ...(duration > 0 && duration !== this.data.duration ? { duration, durationText: formatTime(duration) } : {})
      });
    });

    ctx.onPlay(() => {
      this.setData({ isPlaying: true });
    });

    ctx.onPause(() => {
      this.setData({ isPlaying: false });
    });

    ctx.onStop(() => {
      this.setData({ isPlaying: false, currentTime: 0, currentTimeText: '00:00', sliderValue: 0 });
    });

    ctx.onEnded(() => {
      this.setData({ isPlaying: false, currentTime: 0, currentTimeText: '00:00', sliderValue: 0 });
    });

    ctx.onError((res) => {
      console.error('音频播放错误', res);
      wx.showToast({
        title: '音频加载失败',
        icon: 'none'
      });
      this.setData({ isPlaying: false });
    });

    this.audioContext = ctx;
  },

  destroyAudio() {
    if (this.audioContext) {
      this.audioContext.destroy();
      this.audioContext = null;
    }
  },

  togglePlay() {
    if (!this.audioContext) return;
    if (this.data.isPlaying) {
      this.audioContext.pause();
    } else {
      this.audioContext.play();
    }
  },

  onSliderChanging(e) {
    this.setData({
      isSliding: true
    });
  },

  onSliderChange(e) {
    if (!this.audioContext) return;
    const value = e.detail.value;
    const duration = this.audioContext.duration || this.data.duration;
    if (duration > 0) {
      const targetTime = (value / 100) * duration;
      this.audioContext.seek(targetTime);
      this.setData({
        currentTime: targetTime,
        currentTimeText: formatTime(targetTime),
        sliderValue: value,
        isSliding: false
      });
    } else {
      this.setData({
        isSliding: false
      });
    }
  }
});
