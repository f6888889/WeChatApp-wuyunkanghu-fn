const app = getApp();

// 默认数据
const DEFAULT_DATA = {
  greeting: '早上好呀',
  timeOfDay: 'morning',
  recommendedCourse: {
    id: 'course_001',
    title: '广场舞入门',
    description: '适合初学者的广场舞课程',
    coverImage: '/assets/icons/home.png'
  },
  userProgress: {
    continuousDays: 0,
    totalMinutes: 0,
    onlineMinutes: 0
  },
  allCourses: [
    { id: 'course_001', title: '广场舞入门', description: '适合初学者的广场舞课程', coverImage: '/assets/icons/home.png' },
    { id: 'course_002', title: '健身操基础', description: '简单易学的健身操', coverImage: '/assets/icons/search.png' },
    { id: 'course_003', title: '太极养生', description: '传统健身，修身养性', coverImage: '/assets/icons/profile.png' }
  ],
  userInfo: {
    nickname: '银舞用户'
  },
  isLoading: false,
  isReviewMode: true,
  currentPage: 1,
  pageSize: 6,
  totalPages: 1,
  paginatedCourses: [],
  paginatedSafeArticles: [],
  pageList: [1]
};

Page({
  data: { ...DEFAULT_DATA },

  onLoad() {
    console.log('[首页] onLoad');
    this.updateGreeting();
    this.loadHomeData();
  },

  onShow() {
    console.log('[首页] onShow');
    this.setData({
      isReviewMode: app.globalData.isReviewMode
    });
    if (app.updateTabBarText) {
      app.updateTabBarText();
    }
    this.updateGreeting();
    this.loadHomeData();
  },

  updateGreeting() {
    const hour = new Date().getHours();
    let greeting = '早上好呀';
    let timeOfDay = 'morning';

    if (hour >= 5 && hour < 12) {
      greeting = '早上好呀';
      timeOfDay = 'morning';
    } else if (hour >= 12 && hour < 18) {
      greeting = '下午好呀';
      timeOfDay = 'afternoon';
    } else {
      greeting = '晚上好呀';
      timeOfDay = 'evening';
    }

    this.setData({ greeting, timeOfDay });
  },

  async loadHomeData() {
    console.log('[首页] 开始加载数据');
    
    if (!this.data.recommendedCourse || !this.data.allCourses) {
      this.setData({ ...DEFAULT_DATA });
    }

    try {
      const day = new Date().getDate();
      const recommendId = day % 2 === 0 ? 'course_001' : 'course_002';
      
      const defaultCourses = [
        { id: 'course_001', title: '广场舞入门', description: '适合初学者的广场舞课程', coverImage: '/assets/icons/home.png' },
        { id: 'course_002', title: '健身操基础', description: '简单易学的健身操', coverImage: '/assets/icons/search.png' },
        { id: 'course_003', title: '太极养生', description: '传统健身，修身养性', coverImage: '/assets/icons/profile.png' }
      ];
      
      let courses = defaultCourses;
      let recommended = defaultCourses.find(c => c.id === recommendId) || defaultCourses[0];
      
      this.setData({ 
        recommendedCourse: recommended,
        allCourses: courses,
        isLoading: false
      }, () => {
        this.updatePaginatedData();
      });
      
      try {
        const url = app.globalData.isReviewMode ? '/courses/safe' : '/courses';
        const allCoursesRes = await app.request(url);
        
        if (allCoursesRes && allCoursesRes.code === 200 && allCoursesRes.data && allCoursesRes.data.length > 0) {
          courses = allCoursesRes.data;
          recommended = courses.find(c => c.id === recommendId) || courses[0] || defaultCourses[0];
          
          let updateObj = { 
            recommendedCourse: recommended,
            allCourses: courses
          };
          
          // 在安全模式下，直接使用 courses 数据映射为 safeArticles
          if (app.globalData.isReviewMode) {
            const safeData = courses.map(c => ({
              id: c.id,
              title: c.title,
              coverImage: c.coverImage,
              content: c.description
            }));
            updateObj.safeArticles = safeData;
          }
          
          this.setData(updateObj, () => {
            this.updatePaginatedData();
          });
        }
      } catch (err) {
        console.warn('[首页] 获取课程数据失败，使用默认数据', err);
      }
      
      const userId = app.getUserId();
      if (userId) {
        try {
          const userRes = await app.request(`/users/${userId}`);
          if (userRes && userRes.code === 200 && userRes.data) {
            const user = userRes.data;
            this.setData({
              userProgress: user.learningProgress || {
                continuousDays: 0,
                totalMinutes: 0,
                onlineMinutes: 0
              },
              'userInfo.nickname': user.nickname || '银舞用户'
            });
          }
        } catch (err) {
          console.warn('[首页] 获取用户数据失败', err);
        }
      }

    } catch (err) {
      console.error('[首页] 加载数据失败', err);
      if (!this.data.allCourses || this.data.allCourses.length === 0) {
        this.setData({ ...DEFAULT_DATA });
      }
    }
  },

  updatePaginatedData() {
    const { pageSize, allCourses, safeArticles, isReviewMode } = this.data;
    let { currentPage } = this.data;
    
    if (!isReviewMode) {
      const totalPages = Math.ceil((allCourses || []).length / pageSize) || 1;
      if (currentPage > totalPages) currentPage = totalPages;
      if (currentPage < 1) currentPage = 1;
      const paginatedCourses = (allCourses || []).slice((currentPage - 1) * pageSize, currentPage * pageSize);
      
      const pageList = [];
      for (let i = 1; i <= totalPages; i++) {
        pageList.push(i);
      }
      
      this.setData({
        currentPage,
        totalPages,
        paginatedCourses,
        pageList
      });
    } else {
      const totalPages = Math.ceil((safeArticles || []).length / pageSize) || 1;
      if (currentPage > totalPages) currentPage = totalPages;
      if (currentPage < 1) currentPage = 1;
      const paginatedSafeArticles = (safeArticles || []).slice((currentPage - 1) * pageSize, currentPage * pageSize);
      
      const pageList = [];
      for (let i = 1; i <= totalPages; i++) {
        pageList.push(i);
      }
      
      this.setData({
        currentPage,
        totalPages,
        paginatedSafeArticles,
        pageList
      });
    }
  },

  onPageTap(e) {
    const page = parseInt(e.currentTarget.dataset.page, 10);
    if (page && page !== this.data.currentPage) {
      this.setData({ currentPage: page });
      this.updatePaginatedData();
      wx.pageScrollTo({
        selector: '.all-courses-section',
        duration: 300
      });
    }
  },

  onPrevPage() {
    let { currentPage } = this.data;
    if (currentPage > 1) {
      this.setData({ currentPage: currentPage - 1 });
      this.updatePaginatedData();
      wx.pageScrollTo({
        selector: '.all-courses-section',
        duration: 300
      });
    }
  },

  onNextPage() {
    let { currentPage, totalPages } = this.data;
    if (currentPage < totalPages) {
      this.setData({ currentPage: currentPage + 1 });
      this.updatePaginatedData();
      wx.pageScrollTo({
        selector: '.all-courses-section',
        duration: 300
      });
    }
  },

  goToCourse(e) {
    // 安全模式下允许点击进入详情页，详情页已经做了安全模式下的图文排版适配
    const course = e.currentTarget.dataset.course;
    if (course && course.id) {
      wx.navigateTo({
        url: `/pages/course/course?id=${course.id}`
      });
    }
  },

  goToQuickEntry(e) {
    const path = e.currentTarget.dataset.path;
    if (app.globalData.isReviewMode && (path.includes('shop') || path.includes('health'))) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    if (path) {
      wx.navigateTo({ url: path });
    }
  },

  goToHealth() {
    if (app.globalData.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/health/health'
    });
  },

  getDifficultyText(difficulty) {
    const map = {
      'easy': '简单',
      'medium': '中等',
      'hard': '较难'
    };
    return map[difficulty] || '一般';
  },

  getDifficultyClass(difficulty) {
    const map = {
      'easy': 'tag-easy',
      'medium': 'tag-medium',
      'hard': 'tag-hard'
    };
    return map[difficulty] || '';
  },

  onShareAppMessage() {
    return {
      title: '银舞沐心 - AI赋能舞蹈康养，开启健康新生活',
      path: '/pages/index/index',
      imageUrl: '/assets/images/share-cover.png'
    };
  },

  onShareTimeline() {
    return {
      title: '银舞沐心 - 您的专属舞蹈康养助手',
      query: '',
      imageUrl: '/assets/images/share-cover.png'
    };
  }
});
