Page({
  data: {
    menuItems: [
      { type: 'users', name: '用户管理', icon: '👤', desc: '管理用户信息、积分、状态' },
      { type: 'shop_items', name: '商城商品', icon: '🛍️', desc: '管理积分商城可兑换商品' },
      { type: 'courses', name: '课程管理', icon: '📚', desc: '管理视频课程、分类、步骤' },
      { type: 'messages', name: '消息管理', icon: '💬', desc: '查看用户反馈与系统消息' },
      { type: 'learning_records', name: '学习记录', icon: '📈', desc: '监控用户学习进度与时长' },
      { type: 'health_reminders', name: '健康提醒', icon: '⏰', desc: '配置健康生活提醒事项' },
      { type: 'health_alerts', name: '健康告警', icon: '⚠️', desc: '查看实时健康预警信息' }
    ]
  },

  goToPath(e) {
    const { type, name } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/list/list?type=${type}&name=${name}`
    });
  }
});
