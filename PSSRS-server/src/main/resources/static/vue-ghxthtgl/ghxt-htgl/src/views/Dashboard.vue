<template>
  <el-row :gutter="16">
    <el-col :span="6"><el-card><div class="kpi"><div>今日挂号</div><b>128</b></div></el-card></el-col>
    <el-col :span="6"><el-card><div class="kpi"><div>待就诊</div><b>57</b></div></el-card></el-col>
    <el-col :span="6"><el-card><div class="kpi"><div>医生在线</div><b>23</b></div></el-card></el-col>
    <el-col :span="6"><el-card><div class="kpi"><div>退款申请</div><b>4</b></div></el-card></el-col>
  </el-row>
</template>

<script setup>
</script>

<style scoped>
.kpi { display:flex; flex-direction:column; gap:8px; text-align:center; padding:12px 0 }
.kpi b { font-size:24px }
</style>


