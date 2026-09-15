<template>
  <el-select :model-value="modelValue" filterable remote :remote-method="load" :loading="loading"
    :disabled="disabled" placeholder="请选择基础资料" class="!w-1/1" @update:model-value="select">
    <el-option v-for="item in options" :key="item.id" :value="item.id"
      :label="`${item.name}（${item.code || item.id}）`" />
  </el-select>
</template>
<script setup lang="ts">
import request from '@/config/axios'
const props = defineProps<{ modelValue?: number; name?: string; kind: 'products' | 'employees' | 'warehouses'; disabled?: boolean }>()
const emit = defineEmits<{ 'update:modelValue': [number]; 'update:name': [string] }>()
interface Option { id: number; name: string; code?: string }
const options = ref<Option[]>([])
const loading = ref(false)
let sequence = 0
const load = async (name = '') => {
  const current = ++sequence
  loading.value = true
  try {
    const data: Option[] = await request.get({ url: `/biz/reference/${props.kind}`, params: { name } })
    if (current === sequence) {
      options.value = data
      if (props.modelValue && !data.some((v) => v.id === props.modelValue)) {
        options.value.unshift({ id: props.modelValue, name: props.name || `#${props.modelValue}` })
      }
    }
  } finally { if (current === sequence) loading.value = false }
}
const select = (id: number) => {
  emit('update:modelValue', id)
  emit('update:name', options.value.find((item) => item.id === id)?.name || '')
}
watch(() => [props.modelValue, props.name], () => load(props.name || ''), { immediate: true })
</script>
