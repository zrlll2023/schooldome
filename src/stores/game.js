import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import request from '@/utils/request'

export const useGameStore = defineStore('game', () => {
  const currentGame = ref(null)
  const gameStats = ref({
    totalStudents: 0,
    totalTeachers: 0,
    schoolLevel: 1,
    reputation: 0,
    funds: 0
  })
  const loading = ref(false)

  const schoolName = computed(() => currentGame.value?.schoolName || '未命名学校')
  const gameDay = computed(() => currentGame.value?.day || 1)

  async function fetchCurrentGame() {
    loading.value = true
    try {
      const res = await request.get('/api/game/current')
      currentGame.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function createGame(data) {
    loading.value = true
    try {
      const res = await request.post('/api/game/create', data)
      currentGame.value = res.data
      return res.data
    } finally {
      loading.value = false
    }
  }

  async function fetchGameStats() {
    try {
      const res = await request.get('/api/game/stats')
      gameStats.value = res.data
      return res.data
    } catch (error) {
      console.error('获取游戏统计失败:', error)
    }
  }

  async function nextDay() {
    try {
      const res = await request.post('/api/game/next-day')
      currentGame.value = res.data
      await fetchGameStats()
      return res.data
    } catch (error) {
      console.error('进入下一天失败:', error)
      throw error
    }
  }

  function resetGame() {
    currentGame.value = null
    gameStats.value = {
      totalStudents: 0,
      totalTeachers: 0,
      schoolLevel: 1,
      reputation: 0,
      funds: 0
    }
  }

  return {
    currentGame,
    gameStats,
    loading,
    schoolName,
    gameDay,
    fetchCurrentGame,
    createGame,
    fetchGameStats,
    nextDay,
    resetGame
  }
})
