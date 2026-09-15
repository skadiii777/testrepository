<template>
  <ContentWrap>
    <el-alert type="info" :closable="false" class="mb-10px" show-icon
      title="系统采用单账号单设备登录：同一账号再次登录会使旧会话自动失效；关闭网页后令牌即失效，需重新登录。" />
    <div class="mb-10px">
      <el-tag type="success" size="large">当前在线：{{ list.length }} 个会话</el-tag>
    </div>
  </ContentWrap>

  <ContentWrap>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column label="用户名" align="center" prop="username" min-width="110" />
      <el-table-column label="昵称" align="center" prop="nickname" min-width="110" />
      <el-table-column label="部门" align="center" prop="deptName" min-width="110" />
      <el-table-column label="登录时间" align="center" prop="createTime" :formatter="dateFormatter" width="170px" />
      <el-table-column label="过期时间" align="center" prop="expiresTime" :formatter="dateFormatter" width="170px" />
      <el-table-column label="令牌" align="center" prop="accessTokenMask" min-width="130" />
      <el-table-column label="操作" align="center" width="120" fixed="right">
        <template #default="scope">
          <el-button link type="danger" @click="handleKick(scope.row)"
            v-hasPermi="['system:online-user:delete']">强制下线</el-button>
        </template>
      </el-table-column>
    </el-table>
  </ContentWrap>
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import * as Api from '@/api/system/onlineUser'
import type { OnlineUserVO } from '@/api/system/onlineUser'

defineOptions({ name: 'SystemOnlineUser' })

const message = useMessage()

const loading = ref(true)
const list = ref<Api.OnlineUserVO[]>([])

const getList = async () => {
  loading.value = true
  try {
    list.value = await Api.getOnlineUserList()
  } finally {
    loading.value = false
  }
}

const handleKick = async (row: OnlineUserVO) => {
  try {
    await message.confirm(`确认将用户「${row.nickname || row.username}」强制下线？`)
    await Api.kickOnlineUser(row.userId!, row.userType!)
    message.success('已强制下线')
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
