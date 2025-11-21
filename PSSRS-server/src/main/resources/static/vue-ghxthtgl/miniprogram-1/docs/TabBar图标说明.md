# TabBar图标说明

## 图标要求

TabBar需要以下图标文件（放在 `images/` 目录下）：

1. **首页图标**
   - `home.png` - 未选中状态（灰色）
   - `home-active.png` - 选中状态（蓝色）

2. **我的图标**
   - `mine.png` - 未选中状态（灰色）
   - `mine-active.png` - 选中状态（蓝色）

## 图标规格

- **尺寸**: 81px × 81px
- **格式**: PNG（支持透明背景）
- **颜色**: 
  - 未选中: #999999
  - 选中: #1890ff

## 临时方案

如果没有图标文件，可以：

1. **使用在线图标生成器**
   - https://www.iconfont.cn/
   - https://www.flaticon.com/

2. **或者临时移除图标**
   
修改 `app.json`，移除 `iconPath` 和 `selectedIconPath`：

```json
"tabBar": {
  "color": "#999999",
  "selectedColor": "#1890ff",
  "backgroundColor": "#ffffff",
  "borderStyle": "black",
  "list": [
    {
      "pagePath": "pages/index/index",
      "text": "首页"
    },
    {
      "pagePath": "pages/mine/mine",
      "text": "我的"
    }
  ]
}
```

## 推荐图标

可以从以下网站下载免费图标：
- https://www.iconfont.cn/ (阿里巴巴矢量图标库)
- https://www.flaticon.com/ (Flaticon)
- https://icons8.com/ (Icons8)

搜索关键词：
- 首页: home, house, 主页
- 我的: user, profile, person, 用户
