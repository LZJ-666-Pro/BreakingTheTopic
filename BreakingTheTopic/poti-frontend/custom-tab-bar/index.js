Component({
  data: {
    selected: 0,
    color: "#9CA3AF",
    selectedColor: "#2979FF",
    list: [
      {
        pagePath: "/pages/index/index",
        iconPath: "/images/1.png",
        selectedIconPath: "/images/1-active.png",
        text: "首页"
      },
      {
        pagePath: "/pages/question/question",
        iconPath: "/images/2.png",
        selectedIconPath: "/images/2-active.png",
        text: "题库"
      },
      {
        pagePath: "/pages/practice/practice",
        iconPath: "/images/3.png",
        selectedIconPath: "/images/3-active.png",
        text: "练习"
      },
      {
        pagePath: "/pages/statistics/statistics",
        iconPath: "/images/4.png",
        selectedIconPath: "/images/4-active.png",
        text: "统计"
      },
      {
        pagePath: "/pages/user/user",
        iconPath: "/images/5.png",
        selectedIconPath: "/images/5-active.png",
        text: "我的"
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
