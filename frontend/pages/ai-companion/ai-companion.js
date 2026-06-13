const app = getApp();

// 时间格式化工具：去除 GMT+8 等多余信息，只保留简洁的时间
function formatChatTime(dateInput) {
  if (!dateInput) return '';
  
  let date;
  if (dateInput instanceof Date) {
    date = dateInput;
  } else if (typeof dateInput === 'string') {
    // 处理 ISO 格式
    let timeStr = dateInput;
    if (timeStr.includes('.')) {
      const parts = timeStr.split('.');
      if (parts[1].length > 3) {
        timeStr = parts[0] + '.' + parts[1].substring(0, 3);
      }
    }
    date = new Date(timeStr);
  } else {
    date = new Date(dateInput);
  }

  if (isNaN(date.getTime())) return String(dateInput);

  const now = new Date();
  const isToday = date.toDateString() === now.toDateString();
  
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  if (isToday) {
    return `${hours}:${minutes}:${seconds}`;
  } else {
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${month}-${day} ${hours}:${minutes}`;
  }
}

Page({
  data: {
    messages: [],
    inputMessage: '',
    isRecording: false,
    scrollToId: '',
    currentUserId: '',
    showAddFriendModal: false,
    friendNickname: '',
    chatMode: 'public',
    friends: [],
    currentChatFriend: null,
    privateMessages: [],
    friendRequests: [],
    isReviewMode: true
  },

  onLoad(options) {
    console.log('[消息页面] onLoad', options);
    const userId = app.getUserId();
    const isReviewMode = app.globalData.isReviewMode;
    this.setData({ 
      currentUserId: userId,
      isReviewMode: isReviewMode
    });
    if (!isReviewMode) {
      this.loadMessages();
    }
  },

  onShow() {
    console.log('[消息页面] onShow');
    const userId = app.getUserId();
    const isReviewMode = app.globalData.isReviewMode;
    this.setData({ 
      currentUserId: userId,
      isReviewMode: isReviewMode
    });
    
    if (isReviewMode) {
      return;
    }
    
    this.loadMessages();
    this.loadFriends();
    this.loadFriendRequests();
    
    this.interval = setInterval(() => {
      if (this.data.chatMode === 'public') {
        this.loadMessages();
      } else if (this.data.chatMode === 'private' && this.data.currentChatFriend) {
        this.loadPrivateMessages();
      } else if (this.data.chatMode === 'requests') {
        this.loadFriendRequests();
      }
    }, 3000);
  },

  onHide() {
    if (this.interval) {
      clearInterval(this.interval);
    }
  },

  onSwitchPublicChat() {
    if (this.data.isReviewMode) return;
    this.setData({ chatMode: 'public' });
    this.loadMessages();
  },

  onSwitchPrivateChat() {
    if (this.data.isReviewMode) return;
    this.setData({ chatMode: 'private' });
    this.loadFriends();
  },

  onSwitchRequestsChat() {
    if (this.data.isReviewMode) return;
    this.setData({ chatMode: 'requests' });
    this.loadFriendRequests();
  },

  async loadMessages() {
    if (this.data.isReviewMode) return;
    try {
      const res = await app.request('/messages');
      if (res.code === 200) {
        let messages = res.data || [];
        messages = messages.map(msg => {
          return {
            ...msg,
            createTime: formatChatTime(msg.createTime)
          };
        });

        this.setData({ messages }, () => {
          this.scrollToBottom();
        });
      }
    } catch (err) {
      console.error('[消息] 加载消息失败', err);
    }
  },

  async loadFriends() {
    if (this.data.isReviewMode) return;
    try {
      const userId = app.getUserId();
      if (!userId) return;
      const res = await app.request('/friends/list/' + userId);
      if (res.code === 200) {
        this.setData({ friends: res.data || [] });
      }
    } catch (err) {
      console.error('加载好友列表失败', err);
    }
  },

  async loadFriendRequests() {
    if (this.data.isReviewMode) return;
    try {
      const userId = app.getUserId();
      if (!userId) return;
      const res = await app.request('/friends/request/pending/' + userId);
      if (res.code === 200) {
        this.setData({ friendRequests: res.data || [] });
      }
    } catch (err) {
      console.error('[消息] 加载好友申请失败', err);
    }
  },

  async onAcceptRequest(e) {
    const requestId = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    try {
      const res = await app.request('/friends/request/handle', 'POST', {
        requestId: requestId,
        status: 'ACCEPTED'
      });
      wx.hideLoading();
      if (res.code === 200) {
        wx.showToast({ title: '已同意添加好友', icon: 'success' });
        this.loadFriendRequests();
        this.loadFriends();
      } else {
        wx.showToast({ title: res.message || '处理失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('同意好友申请失败', err);
      wx.showToast({ title: '处理失败', icon: 'none' });
    }
  },

  async onRejectRequest(e) {
    const requestId = e.currentTarget.dataset.id;
    wx.showLoading({ title: '处理中...' });
    try {
      const res = await app.request('/friends/request/handle', 'POST', {
        requestId: requestId,
        status: 'REJECTED'
      });
      wx.hideLoading();
      if (res.code === 200) {
        wx.showToast({ title: '已拒绝申请', icon: 'success' });
        this.loadFriendRequests();
      } else {
        wx.showToast({ title: res.message || '处理失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('拒绝好友申请失败', err);
      wx.showToast({ title: '处理失败', icon: 'none' });
    }
  },

  onSelectFriend(e) {
    const friend = e.currentTarget.dataset.friend;
    this.setData({
      currentChatFriend: friend,
      privateMessages: []
    });
    this.loadPrivateMessages();
  },

  onBackToFriends() {
    this.setData({
      currentChatFriend: null,
      privateMessages: []
    });
    this.loadFriends();
  },

  async loadPrivateMessages() {
    if (this.data.isReviewMode) return;
    const { currentChatFriend, currentUserId } = this.data;
    if (!currentChatFriend || !currentUserId) return;

    try {
      const friendId = currentChatFriend.id;
      const res = await app.request('/messages/private?userId1=' + currentUserId + '&userId2=' + friendId);
      if (res.code === 200) {
        let privateMessages = res.data || [];
        privateMessages = privateMessages.map(msg => {
          return {
            ...msg,
            createTime: formatChatTime(msg.createTime)
          };
        });

        this.setData({ privateMessages }, () => {
          this.scrollToBottom();
        });
      }
    } catch (err) {
      console.error('[通讯录] 加载私聊消息失败', err);
    }
  },

  scrollToBottom() {
    const { messages, privateMessages } = this.data;
    const list = this.data.chatMode === 'public' ? messages : privateMessages;
    if (list.length > 0) {
      const lastMessage = list[list.length - 1];
      this.setData({
        scrollToId: 'msg-' + lastMessage.id
      });
    }
  },

  onInputChange(e) {
    this.setData({ inputMessage: e.detail.value });
  },

  async sendMessage() {
    if (this.data.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    const content = this.data.inputMessage.trim();
    if (!content) return;

    try {
      const userId = app.getUserId();
      const userName = app.getUserNickname() || '用户';

      if (!userId) {
        wx.showLoading({ title: '登录中...' });
        try {
          await app.wxLogin();
        } catch (loginErr) {
          wx.hideLoading();
          wx.showToast({ title: '请先登录', icon: 'none' });
          return;
        }
        wx.hideLoading();
      }

      const finalUserId = app.getUserId() || 'anonymous';
      const res = await app.request('/messages', 'POST', { 
        content, 
        userId: finalUserId, 
        userName, 
        senderType: 'user' 
      });

      this.setData({ inputMessage: '' });
      
      if (res && res.data) {
        const serverMessage = res.data;
        serverMessage.createTime = formatChatTime(serverMessage.createTime);
        
        this.setData({
          messages: [...this.data.messages, serverMessage]
        }, () => {
          this.scrollToBottom();
        });
      } else {
        const newMessage = {
          id: Date.now().toString(),
          content: content,
          senderId: finalUserId,
          senderName: userName,
          senderType: 'user',
          createTime: formatChatTime(new Date())
        };
        
        this.setData({
          messages: [...this.data.messages, newMessage]
        }, () => {
          this.scrollToBottom();
        });
      }

      setTimeout(() => {
        this.loadMessages();
      }, 800);

    } catch (err) {
      console.error('[消息] 发送失败', err);
      wx.showToast({ 
        title: err.message || '发送失败，请重试', 
        icon: 'none',
        duration: 2000
      });
    }
  },

  async sendPrivateMessage() {
    if (this.data.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    const content = this.data.inputMessage.trim();
    if (!content) return;

    const { currentChatFriend, currentUserId } = this.data;
    if (!currentChatFriend) {
      wx.showToast({ title: '请先选择好友', icon: 'none' });
      return;
    }

    try {
      const userId = app.getUserId();
      const userName = app.getUserNickname() || '用户';

      if (!userId) {
        wx.showLoading({ title: '登录中...' });
        try {
          await app.wxLogin();
        } catch (loginErr) {
          wx.hideLoading();
          wx.showToast({ title: '请先登录', icon: 'none' });
          return;
        }
        wx.hideLoading();
      }

      const finalUserId = app.getUserId() || 'anonymous';
      const receiverId = currentChatFriend.id;

      const res = await app.request('/messages/private', 'POST', { 
        content, 
        userId: finalUserId, 
        userName, 
        receiverId 
      });

      this.setData({ inputMessage: '' });

      if (res && res.data) {
        const serverMessage = res.data;
        serverMessage.createTime = formatChatTime(serverMessage.createTime);
        
        this.setData({
          privateMessages: [...this.data.privateMessages, serverMessage]
        }, () => {
          this.scrollToBottom();
        });
      } else {
        const newMessage = {
          id: Date.now().toString(),
          content: content,
          senderId: finalUserId,
          senderName: userName,
          receiverId: receiverId,
          createTime: formatChatTime(new Date())
        };
        
        this.setData({
          privateMessages: [...this.data.privateMessages, newMessage]
        }, () => {
          this.scrollToBottom();
        });
      }

      setTimeout(() => {
        this.loadPrivateMessages();
      }, 800);

    } catch (err) {
      console.error('[通讯录] 发送失败', err);
      wx.showToast({ 
        title: err.message || '发送失败，请重试', 
        icon: 'none',
        duration: 2000
      });
    }
  },

  onMessageLongPress(e) {
    if (this.data.chatMode !== 'public') return;
    
    const message = e.currentTarget.dataset.message;
    const userId = app.getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const isMe = message.senderId === userId;

    if (isMe) {
      // 自己的消息，提供撤回选项
      wx.showActionSheet({
        itemList: ['撤回消息'],
        success: (res) => {
          if (res.tapIndex === 0) {
            wx.showModal({
              title: '撤回消息',
              content: '确定要撤回这条消息吗？',
              success: (modalRes) => {
                if (modalRes.confirm) {
                  this.recallMessage(message.id, userId);
                }
              }
            });
          }
        }
      });
    } else {
      // 别人的消息，提供添加好友选项
      wx.showActionSheet({
        itemList: ['添加好友'],
        success: (res) => {
          if (res.tapIndex === 0) {
            // 检查是否已经是好友
            const isFriend = this.data.friends && this.data.friends.some(f => f.id === message.senderId);
            if (isFriend) {
              wx.showToast({ title: '你们已经是好友了', icon: 'none' });
              return;
            }
            this.addFriendDirectly(message.senderName);
          }
        }
      });
    }
  },

  async addFriendDirectly(nickname) {
    if (this.data.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    const userId = app.getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }
    wx.showLoading({ title: '发送申请中...' });
    try {
      const res = await app.request('/friends/request', 'POST', {
        userId: userId,
        nickname: nickname
      });
      wx.hideLoading();
      if (res.code === 200) {
        wx.showToast({ title: '申请已发送', icon: 'success' });
      } else {
        wx.showToast({ title: res.message || '发送失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('发送好友申请失败', err);
      wx.showToast({ title: err.message || '发送失败', icon: 'none' });
    }
  },

  async recallMessage(messageId, userId) {
    try {
      await app.request('/messages/recall', 'POST', { messageId, userId });
      wx.showToast({ title: '已撤回', icon: 'success' });
      this.loadMessages();
    } catch (err) {
      console.error('撤回消息失败', err);
      wx.showToast({ title: '撤回失败', icon: 'none' });
    }
  },

  onVoiceMessage() {
    if (this.data.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
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
    });

    recorderManager.onStop((res) => {
      this.setData({ isRecording: false });
      if (res.duration > 500) {
        this.recognizeVoice(res.tempFilePath);
      }
    });

    recorderManager.onError((err) => {
      this.setData({ isRecording: false });
      wx.showToast({ title: '录音失败', icon: 'none' });
    });

    this.recorderManager = recorderManager;
    recorderManager.start({
      format: 'mp3',
      sampleRate: 16000,
      numberOfChannels: 1,
      encodeBitRate: 48000,
      duration: 60000
    });
  },

  stopRecording() {
    if (this.recorderManager) {
      this.recorderManager.stop();
    }
    this.setData({ isRecording: false });
  },

  async recognizeVoice(filePath) {
    wx.showLoading({ title: '识别中...' });
    try {
      const res = await app.uploadFile('/voice/recognize', filePath, 'file');
      wx.hideLoading();
      if (res.code === 200 && res.data.text) {
        this.setData({ inputMessage: res.data.text });
      } else {
        wx.showToast({ title: '识别失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('语音识别失败', err);
      wx.showToast({ title: '识别失败', icon: 'none' });
    }
  },

  onAddFriendTap() {
    if (this.data.isReviewMode) {
      wx.showToast({ title: '功能开发中', icon: 'none' });
      return;
    }
    this.setData({
      showAddFriendModal: true,
      friendNickname: ''
    });
  },

  onCloseModal() {
    this.setData({
      showAddFriendModal: false,
      friendNickname: ''
    });
  },

  onFriendNicknameInput(e) {
    this.setData({
      friendNickname: e.detail.value
    });
  },

  async onConfirmAddFriend() {
    const nickname = this.data.friendNickname.trim();
    if (!nickname) {
      wx.showToast({ title: '请输入用户名', icon: 'none' });
      return;
    }

    const userId = app.getUserId();
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    try {
      const res = await app.request('/friends/request', 'POST', {
        userId: userId,
        nickname: nickname
      });

      if (res.code === 200) {
        wx.showToast({ title: '申请已发送', icon: 'success' });
        this.onCloseModal();
      } else {
        wx.showToast({ title: res.message || '发送失败', icon: 'none' });
      }
    } catch (err) {
      console.error('发送好友申请失败', err);
      wx.showToast({ title: '发送失败', icon: 'none' });
    }
  }
});