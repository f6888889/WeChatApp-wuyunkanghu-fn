const app = getApp();

Page({
  data: {
    points: 0,
    shopItems: [],
    redeemedItems: [],
    currentTab: '全部',
    categories: ['全部', '养生', '生活', '运动'],
    loading: true,
    displayedItems: [],
    isPageReady: false
  },

  // 审核模式检查
  checkReviewMode() {
    if (app.globalData.isReviewMode) {
      console.log('[商城] 审核模式拦截');
      wx.reLaunch({
        url: '/pages/index/index'
      });
      return true;
    }
    return false;
  },

  onLoad() {
    if (this.checkReviewMode()) {
      return;
    }
    this.setData({ isPageReady: true });
    this.loadPoints();
    this.loadShopData();
  },

  onShow() {
    if (this.checkReviewMode()) {
      return;
    }
    if (!this.data.isPageReady) {
      this.setData({ isPageReady: true });
    }
    this.loadPoints();
    this.loadShopData();
  },

  async loadPoints() {
    try {
      const userId = app.getUserId();
      if (!userId) return;
      const res = await app.request(`/checkin/${userId}/status`);
      if (res && res.code === 200 && res.data) {
        this.setData({ points: res.data.points });
      }
    } catch (err) {
      console.warn('加载积分失败', err);
    }
  },

  async loadShopData() {
    this.setData({ loading: true });
    try {
      const userId = app.getUserId();
      const [itemsRes, redeemedRes] = await Promise.all([
        app.request('/shop/items'),
        userId ? app.request(`/shop/redeemed/${userId}`) : Promise.resolve({ code: 200, data: [] })
      ]);

      let shopItems = [];
      let redeemedItems = [];

      if (itemsRes && itemsRes.code === 200 && itemsRes.data) {
        shopItems = itemsRes.data;
      }

      if (redeemedRes && redeemedRes.code === 200 && redeemedRes.data) {
        redeemedItems = redeemedRes.data.map(item => item.id);
      }

      this.setData({
        shopItems,
        redeemedItems,
        loading: false
      }, () => {
        this.filterItems();
      });
    } catch (err) {
      console.error('加载商城数据失败', err);
      this.setData({ loading: false });
    }
  },

  filterItems() {
    const { shopItems, currentTab } = this.data;
    const displayedItems = currentTab === '全部' 
      ? shopItems 
      : shopItems.filter(item => item.category === currentTab);
    
    this.setData({ displayedItems });
  },

  onTabChange(e) {
    const category = e.currentTarget.dataset.category;
    if (this.data.currentTab === category) return;
    
    this.setData({ currentTab: category }, () => {
      this.filterItems();
    });
  },

  onRedeemTap(e) {
    const item = e.currentTarget.dataset.item;
    if (this.data.points < item.points) {
      wx.showToast({ title: '积分不足', icon: 'none' });
      return;
    }

    wx.showModal({
      title: '确认兑换',
      content: `是否使用 ${item.points} 积分兑换「${item.name}」？`,
      success: async (res) => {
        if (res.confirm) {
          await this.doRedeem(item);
        }
      }
    });
  },

  async doRedeem(item) {
    try {
      wx.showLoading({ title: '兑换中...' });
      const userId = app.getUserId();
      const res = await app.request(`/shop/redeem/${userId}/${item.id}`, 'POST');
      wx.hideLoading();

      if (res && res.code === 200 && res.data && res.data.success) {
        wx.showToast({ title: '兑换成功！', icon: 'success', duration: 2000 });
        this.loadPoints();
        this.loadShopData();
      } else {
        wx.showToast({
          title: (res && res.data && res.data.message) || '兑换失败',
          icon: 'none'
        });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('兑换失败', err);
      wx.showToast({ title: '兑换失败', icon: 'none' });
    }
  }
});
