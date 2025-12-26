# 仪表盘 API 接口需求文档

## 1. 仪表盘汇总数据 (KPI 指标)
**接口地址:** `GET /api/admin/dashboard/summary`

**描述:** 返回仪表盘顶部的关键绩效指标数据。

**响应示例:**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "today_registrations": 120, // 今日挂号数量
    "today_pending_visits": 45, // 今日待就诊患者数量
    "online_doctors": 12,       // 当前在线医生数量
    "refund_requests": 3        // 待处理的退款申请数量
  }
}
```

## 2. 仪表盘图表数据
**接口地址:** `GET /api/admin/dashboard/charts`

**描述:** 返回仪表盘中“挂号趋势”和“科室挂号占比”的图表数据。

**响应示例:**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "trend": {
      "dates": ["10-01", "10-02", "10-03", "10-04", "10-05", "10-06", "10-07"], // 最近7天的日期 (格式：MM-DD)
      "values": [120, 132, 101, 134, 90, 230, 210] // 对应日期的挂号数量
    },
    "departmentProportion": [
      { "name": "内科", "value": 300 },
      { "name": "外科", "value": 150 },
      { "name": "儿科", "value": 120 },
      { "name": "妇产科", "value": 80 },
      { "name": "眼科", "value": 50 }
    ]
  }
}
```

### 后端实现注意事项
1.  **趋势数据 (Trend Data)**: 应包含包括今天在内的过去7天数据。日期格式请统一为 "MM-DD"。
2.  **占比数据 (Proportion Data)**: 建议返回挂号量排名前 5-8 的科室，其余较小的科室可以合并为“其他”。
3.  **性能要求**: 由于该接口可能会被频繁调用（开启自动刷新时每10秒一次），建议在后端进行适当的缓存优化（如 Redis 缓存）。
