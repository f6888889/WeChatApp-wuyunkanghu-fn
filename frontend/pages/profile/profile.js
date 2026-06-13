const app = getApp();

// 默认数据
const DEFAULT_DATA = {
  isLoggedIn: false,
  userInfo: {
    nickname: '银舞用户'
  },
  learningProgress: {
    continuousDays: 0,
    totalMinutes: 0,
    onlineMinutes: 0,
    dailyRecommendedMinutes: 180
  },
  checkInData: {
    checkedInToday: false,
    continuousDays: 0,
    points: 0,
    todayPoints: 0,
    nextPoints: 10
  },
  favorites: [],
  favoriteCourses: [],
  bodyWarning: false,
  isReviewMode: true,
  menuItems: [
    [
      { id: 'favorites', title: '我的收藏', icon: 'mdi:heart', color: '#E07A5F', path: '/pages/profile/favorites/favorites' },
      { id: 'history', title: '学习记录', icon: 'mdi:history', color: '#81B29A', path: '/pages/profile/history/history' },
      { id: 'emergency', title: '紧急联系人', icon: 'mdi:phone', color: '#E53935', path: '/pages/profile/emergency/emergency' }
    ]
  ]
};

Page({
  data: { ...DEFAULT_DATA },

  onLoad() {
    console.log('[我的页面] onLoad');
    this.checkLoginStatus();
  },

  onShow() {
    console.log('[我的页面] onShow');
    this.setData({
      isReviewMode: app.globalData.isReviewMode
    });
    this.checkLoginStatus();
  },

  checkLoginStatus() {
    const userId = app.getUserId();
    if (userId) {
      this.setData({ isLoggedIn: true });
      this.loadUserData();
    } else {
      this.setData({ 
        isLoggedIn: false,
        userInfo: {
          nickname: '银舞用户'
        },
        learningProgress: {
          continuousDays: 0,
          totalMinutes: 0,
          onlineMinutes: 0,
          dailyRecommendedMinutes: 180
        }
      });
    }
  },

  async onLoginTap() {
    try {
      wx.showLoading({ title: '登录中...' });
      const result = await app.wxLogin();
      wx.hideLoading();
      if (result) {
        this.setData({ isLoggedIn: true });
        this.loadUserData();
        wx.showToast({ title: '登录成功', icon: 'success' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('登录失败详情:', err);
      const errorMsg = err.message || (err.data && err.data.message) || '网络请求失败';
      wx.showToast({ title: `登录失败: ${errorMsg}`, icon: 'none', duration: 2000 });
    }
  },

  async loadUserData() {
    console.log('[我的页面] 开始加载用户数据');
    
    try {
      console.log('[我的页面] 正在请求用户数据...');
      const res = await app.request(`/users/${app.getUserId()}`);
      if (res && res.code === 200 && res.data) {
        const user = res.data;
        const progress = user.learningProgress || {
          totalCourses: 0,
          completedCourses: 0,
          totalMinutes: 0,
          continuousDays: 0,
          onlineMinutes: 0,
          todayOnlineMinutes: 0,
          lastOnlineDate: '',
          dailyRecommendedMinutes: 180
        };

        // 使用 todayOnlineMinutes 或者回退到 onlineMinutes
        const todayMinutes = progress.todayOnlineMinutes !== undefined ? progress.todayOnlineMinutes : progress.onlineMinutes;
        const recommendedMinutes = progress.dailyRecommendedMinutes || 180;
        const bodyWarning = todayMinutes > recommendedMinutes;

        progress.onlineMinutes = todayMinutes;
        progress.dailyRecommendedMinutes = recommendedMinutes;
        
        this.setData({
          userInfo: {
            nickname: user.nickname || '银舞用户'
          },
          learningProgress: progress,
          favorites: user.favorites || [],
          bodyWarning: bodyWarning
        });

        this.loadCheckInStatus();

        if (bodyWarning) {
          wx.showToast({
            title: `今日学习已超${recommendedMinutes / 60}小时，请注意休息！`,
            icon: 'none',
            duration: 3000
          });
        }

        if (user.favorites && user.favorites.length > 0) {
          this.loadFavoriteCourses(user.favorites);
        } else {
          this.setData({ favoriteCourses: [] });
        }
      }
    } catch (err) {
      console.warn('[我的页面] 加载用户数据失败，使用默认数据', err);
    }
  },

  async loadCheckInStatus() {
    try {
      const res = await app.request(`/checkin/${app.getUserId()}/status`);
      if (res && res.code === 200 && res.data) {
        this.setData({
          checkInData: {
            checkedInToday: res.data.checkedInToday,
            continuousDays: res.data.continuousDays,
            points: res.data.points,
            todayPoints: res.data.todayPoints,
            nextPoints: res.data.nextPoints
          }
        });
      }
    } catch (err) {
      console.warn('[我的页面] 加载打卡状态失败', err);
    }
  },

  async onCheckInTap() {
    if (this.data.checkInData.checkedInToday) {
      wx.showToast({ title: '今日已打卡', icon: 'none' });
      return;
    }
    try {
      wx.showLoading({ title: '打卡中...' });
      const res = await app.request(`/checkin/${app.getUserId()}`, 'POST');
      wx.hideLoading();
      if (res && res.code === 200 && res.data && res.data.success) {
        const earnedPoints = res.data.earnedPoints;
        wx.showToast({ title: `打卡成功！+${earnedPoints}积分`, icon: 'success', duration: 2000 });
        this.loadCheckInStatus();
      } else if (res && res.data) {
        wx.showToast({ title: res.data.message || '今日已打卡', icon: 'none' });
        this.loadCheckInStatus();
      }
    } catch (err) {
      wx.hideLoading();
      console.error('打卡失败', err);
      wx.showToast({ title: '打卡失败', icon: 'none' });
    }
  },

  goToShop() {
    if (app.globalData.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/shop/shop'
    });
  },

  goToHealthReport() {
    if (app.globalData.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    const userId = app.getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: `/pages/health/health-report/health-report?userId=${userId}`
    });
  },

  async loadFavoriteCourses(favoriteIds) {
    try {
      const res = await app.request('/courses');
      if (res && res.code === 200 && res.data) {
        const allCourses = res.data;
        const favoriteCourses = allCourses.filter(c => favoriteIds.includes(c.id));
        this.setData({ favoriteCourses });
      }
    } catch (err) {
      console.warn('加载收藏课程失败', err);
      this.setData({ favoriteCourses: [] });
    }
  },

  onMenuTap(e) {
    const item = e.currentTarget.dataset.item;
    // 环境适配：对部分维护中的功能进行提示
    if (app.globalData.isReviewMode && (item.id === 'favorites' || item.id === 'history' || item.id === 'emergency')) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    
    if (item && item.path) {
      wx.navigateTo({ 
        url: item.path,
        fail: () => {
          wx.showToast({ title: '功能开发中', icon: 'none' });
        }
      });
    }
  },

  onNicknameTap() {
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入新昵称',
      success: async (res) => {
        if (res.confirm && res.content) {
          const newNickname = res.content.trim();
          if (!newNickname) return;
          
          this.setData({
            'userInfo.nickname': newNickname
          });
          
          try {
            const userId = app.getUserId();
            const userRes = await app.request(`/users/${userId}`);
            if (userRes && userRes.code === 200 && userRes.data) {
              const user = userRes.data;
              user.nickname = newNickname;
              await app.request(`/users/${userId}`, 'PUT', user);
              wx.showToast({ title: '昵称更新成功', icon: 'success' });
            }
          } catch (err) {
            console.error('更新昵称失败', err);
          }
        }
      }
    });
  },

  onLogoutTap() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userId');
          wx.removeStorageSync('userInfo');
          app.globalData.userId = '';
          app.globalData.token = '';
          app.globalData.userInfo = null;
          this.setData({
            isLoggedIn: false,
            userInfo: {
              nickname: '银舞用户'
            },
            learningProgress: {
              continuousDays: 0,
              totalMinutes: 0,
              onlineMinutes: 0,
              dailyRecommendedMinutes: 180
            },
            checkInData: {
              checkedInToday: false,
              continuousDays: 0,
              points: 0,
              todayPoints: 0,
              nextPoints: 10
            },
            favorites: [],
            favoriteCourses: [],
            bodyWarning: false
          });
          wx.showToast({ title: '已退出登录', icon: 'none' });
        }
      }
    });
  },

  goToCourseDetail(e) {
    // 环境适配：拦截详情页入口
    if (app.globalData.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    const courseId = e.currentTarget.dataset.courseid;
    if (courseId) {
      wx.navigateTo({
        url: `/pages/course/course?id=${courseId}`
      });
    }
  }
});
