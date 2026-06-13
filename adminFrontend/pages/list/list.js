const app = getApp();

Page({
  data: {
    type: '',
    name: '',
    list: [],
    filteredList: [],
    searchQuery: ''
  },

  onLoad(options) {
    this.setData({
      type: options.type,
      name: options.name
    });
    wx.setNavigationBarTitle({ title: options.name });
  },

  onShow() {
    this.fetchData();
  },

  fetchData() {
    app.request(`/admin/${this.data.type}`)
      .then(res => {
        if (res.code === 200) {
          this.setData({
            list: res.data,
            filteredList: res.data
          });
        }
      });
  },

  onSearch(e) {
    const query = e.detail.value.toLowerCase();
    const filtered = this.data.list.filter(item => {
      const idStr = String(item.id || '').toLowerCase();
      const nameStr = String(item.name || item.nickname || item.title || item.content || '').toLowerCase();
      return idStr.includes(query) || nameStr.includes(query);
    });
    this.setData({
      searchQuery: query,
      filteredList: filtered
    });
  },

  goToAdd() {
    wx.navigateTo({
      url: `/pages/edit/edit?type=${this.data.type}&mode=add`
    });
  },

  goToEdit(e) {
    const id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/edit/edit?type=${this.data.type}&id=${id}&mode=edit`
    });
  },

  onDelete(e) {
    const id = e.currentTarget.dataset.id;
    wx.showModal({
      title: '确认删除',
      content: `确定要删除 ID 为 ${id} 的记录吗？`,
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          app.request(`/admin/${this.data.type}/${id}`, 'DELETE')
            .then(res => {
              if (res.code === 200) {
                wx.showToast({ title: '删除成功' });
                this.fetchData();
              }
            });
        }
      }
    });
  }
});
