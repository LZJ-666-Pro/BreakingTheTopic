Component({
  data: {
    selected: 0,
    color: "#9CA3AF",
    selectedColor: "#2979FF",
    list: [
      {
        pagePath: "/pages/index/index",
        iconPath: "/images/tabItem/home.png",
        selectedIconPath: "/images/tabItem/home-active.png",
        text: "首页"
      },
      {
        pagePath: "/pages/question/question",
        iconPath: "/images/tabItem/book.png",
        selectedIconPath: "/images/tabItem/book-active.png",
        text: "题库"
      },
      {
        pagePath: "/pages/practice/practice",
        iconPath: "/images/tabItem/pencil.png",
        selectedIconPath: "/images/tabItem/pencil-active.png",
        text: "练习"
      },
      {
        pagePath: "/pages/statistics/statistics",
        iconPath: "/images/tabItem/chart.png",
        selectedIconPath: "/images/tabItem/chart-active.png",
        text: "统计"
      },
      {
        pagePath: "/pages/user/user",
        iconPath: "/images/tabItem/user.png",
        selectedIconPath: "/images/tabItem/user-active.png",
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
