App({
  globalData: {
    apiBase: 'https://fn.zbhly.icu:14502/api'
  },
  onLaunch() {
    console.log('管理后台启动');
  },
  request(url, method = 'GET', data = null) {
    const requestUrl = `${this.globalData.apiBase}${url}`;
    return new Promise((resolve, reject) => {
      wx.showLoading({ title: '加载中' });
      wx.request({
        url: requestUrl,
        method,
        data,
        header: { 'Content-Type': 'application/json' },
        success: res => {
          wx.hideLoading();
          if (res.statusCode === 200) {
            resolve(res.data);
          } else {
            wx.showToast({ title: '服务异常', icon: 'none' });
            reject(res);
          }
        },
        fail: err => {
          wx.hideLoading();
          wx.showToast({ title: '网络失败', icon: 'none' });
          reject(err);
        }
      });
    });
  }
});
