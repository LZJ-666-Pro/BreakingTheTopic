Component({
  data: {
    selected: 0,
    color: "#7A7E83",
    selectedColor: "#07C160",
    list: [
      {
        pagePath: "/pages/index/index",
        iconPath: "images/home.png",
        selectedIconPath: "images/home-active.png",
        text: "首页",
        iconName: "home-o"
      },
      {
        pagePath: "/pages/question/question",
        iconPath: "images/question.png",
        selectedIconPath: "images/question-active.png",
        text: "题库",
        iconName: "question-o"
      },
      {
        pagePath: "/pages/practice/practice",
        iconPath: "images/practice.png",
        selectedIconPath: "images/practice-active.png",
        text: "刷题",
        iconName: "edit"
      },
      {
        pagePath: "/pages/user/user",
        iconPath: "images/user.png",
        selectedIconPath: "images/user-active.png",
        text: "我的",
        iconName: "user-o"
      }
    ]
  },
  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset
      const url = data.path
      wx.switchTab({ url })
      this.setData({
        selected: data.index
      })
    }
  }
})
