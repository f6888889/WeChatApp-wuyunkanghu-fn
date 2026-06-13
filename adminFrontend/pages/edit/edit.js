const app = getApp();

Page({
  data: {
    type: '',
    id: '',
    mode: 'add',
    formData: {},
    rawJson: '',
    formFields: []
  },

  onLoad(options) {
    this.setData({
      type: options.type,
      id: options.id || '',
      mode: options.mode
    });

    if (options.mode === 'edit') {
      this.fetchDetail();
    } else {
      this.initNewItem();
    }
  },

  fetchDetail() {
    app.request(`/admin/${this.data.type}`)
      .then(res => {
        if (res.code === 200) {
          const item = res.data.find(i => String(i.id) === String(this.data.id));
          if (item) {
            this.processData(item);
          }
        }
      });
  },

  initNewItem() {
    const newItem = { id: '' };
    // Add some common fields based on type to help the user
    if (this.data.type === 'shop_items') {
      newItem.name = '';
      newItem.points = 0;
      newItem.stock = 0;
    } else if (this.data.type === 'courses') {
      newItem.title = '';
      newItem.description = '';
    }
    this.processData(newItem);
  },

  processData(item) {
    const fields = [];
    for (let key in item) {
      if (typeof item[key] === 'string' && item[key].length < 100) {
        fields.push({ key, label: key, type: 'input' });
      } else if (typeof item[key] === 'number') {
        fields.push({ key, label: key, type: 'input' });
      } else if (typeof item[key] === 'boolean') {
        fields.push({ key, label: key, type: 'switch' });
      } else {
        // Objects or long text
        fields.push({ key, label: key, type: 'textarea' });
      }
    }

    this.setData({
      formData: item,
      rawJson: JSON.stringify(item, null, 2),
      formFields: fields
    });
  },

  onInputChange(e) {
    const { key } = e.currentTarget.dataset;
    const value = e.detail.value;
    const newData = { ...this.data.formData, [key]: value };
    this.setData({
      formData: newData,
      rawJson: JSON.stringify(newData, null, 2)
    });
  },

  onSwitchChange(e) {
    const { key } = e.currentTarget.dataset;
    const value = e.detail.value;
    const newData = { ...this.data.formData, [key]: value };
    this.setData({
      formData: newData,
      rawJson: JSON.stringify(newData, null, 2)
    });
  },

  onRawJsonChange(e) {
    const value = e.detail.value;
    try {
      const parsed = JSON.parse(value);
      this.setData({
        rawJson: value,
        formData: parsed
      });
    } catch (err) {
      this.setData({ rawJson: value });
    }
  },

  onSave() {
    try {
      const finalData = JSON.parse(this.data.rawJson);
      const url = this.data.mode === 'edit' 
        ? `/admin/${this.data.type}/${this.data.id}`
        : `/admin/${this.data.type}`;
      const method = this.data.mode === 'edit' ? 'PUT' : 'POST';

      app.request(url, method, finalData)
        .then(res => {
          if (res.code === 200) {
            wx.showToast({ title: '保存成功' });
            setTimeout(() => wx.navigateBack(), 1500);
          }
        });
    } catch (err) {
      wx.showToast({ title: 'JSON 格式错误', icon: 'none' });
    }
  },

  onCancel() {
    wx.navigateBack();
  }
});
