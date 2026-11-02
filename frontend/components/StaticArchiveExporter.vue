<script setup lang="ts">
type ExportJob = {
  id: string
  status: 'running' | 'completed' | 'completed_with_warnings' | 'failed'
  phase: string
  processedMessages: number
  totalMessages: number
  copiedFiles: number
  warnings: number
  error?: string
}
const baseUrl = useRuntimeConfig().public.api.archiveExports
const job = ref<ExportJob | null>(null)
const error = ref('')
const starting = ref(false)
const checking = ref(true)
const ready = computed(() => job.value?.status === 'completed' || job.value?.status === 'completed_with_warnings')
const downloadUrl = computed(() => job.value ? `${baseUrl}/${job.value.id}/download` : '')
let timer: ReturnType<typeof setTimeout> | undefined
let disposed = false

async function poll() {
  try {
    job.value = await $fetch<ExportJob | null>(`${baseUrl}/latest`) ?? null
    error.value = ''
  } catch {
    error.value = 'Não foi possível consultar a exportação. Tentaremos novamente; a geração continua no servidor.'
  } finally {
    checking.value = false
    if (!disposed) timer = setTimeout(poll, 2000)
  }
}

async function start() {
  starting.value = true
  error.value = ''
  try {
    job.value = await $fetch<ExportJob>(baseUrl, { method: 'POST' })
  } catch (failure: any) {
    error.value = failure?.status === 409 || failure?.statusCode === 409
      ? 'Há uma exportação ou alteração em andamento. Aguarde e tente novamente.'
      : 'Não foi possível iniciar a exportação. Verifique a conexão e tente novamente.'
  } finally {
    starting.value = false
  }
}

onMounted(poll)
onBeforeUnmount(() => { disposed = true; if (timer) clearTimeout(timer) })
</script>

<template>
  <section class="static-export border-top pt-3 mt-3">
    <h2 class="h5">Exportar acervo como site HTML</h2>
    <p>Baixe todas as conversas, mensagens e arquivos em um ZIP. Extraia tudo e abra index.html no navegador, sem internet ou ChatVault.</p>
    <p class="small">Durante a geração, consultas continuam disponíveis e alterações ficam temporariamente bloqueadas. Você pode fechar esta página e voltar depois.</p>
    <p class="small">A exportação ocupa espaço no servidor e preserva dados pessoais. Confira o relatório de integridade antes de encerrar o serviço.</p>
    <button type="button" class="btn btn-primary" :disabled="checking || starting || job?.status === 'running'" @click="start">
      {{ starting ? 'Iniciando…' : job?.status === 'running' ? 'Exportação em andamento' : 'Gerar exportação HTML' }}
    </button>
    <div class="mt-3" role="status" aria-live="polite">
      <template v-if="job">
        <p>{{ job.phase }} · {{ job.copiedFiles }} arquivos · {{ job.processedMessages }}<template v-if="job.totalMessages"> / {{ job.totalMessages }}</template> mensagens</p>
        <p v-if="job.status === 'completed_with_warnings'" class="alert alert-warning">Concluída com {{ job.warnings }} {{ job.warnings === 1 ? 'pendência' : 'pendências' }}. Consulte report.html dentro do ZIP para identificar anexos ausentes e outros itens.</p>
        <p v-if="job.status === 'failed'" class="alert alert-danger">{{ job.error }}</p>
        <a v-if="ready" class="btn btn-success" :href="downloadUrl">Baixar site HTML (.zip)</a>
      </template>
      <p v-if="error" class="alert alert-warning">{{ error }}</p>
    </div>
  </section>
</template>
