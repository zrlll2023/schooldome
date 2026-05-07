<template>
  <<template>
  <div<template>
  <div class="dashboard">
    <el-row :<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon"<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span><template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ game<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label"><template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label"><template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary"<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain size="large" style="width: 100<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain size="large" style="width: 100%; margin-bottom: 12px;">
<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="stat in statsList" :key="stat.key">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :style="{ backgroundColor: stat.color }">
            <el-icon :size="32"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>学校概况</span>
              <el-button type="primary" @click="handleNextDay">
                <el-icon><ArrowRight /></el-icon>
                进入下一天
              </el-button>
            </div>
          </template>
          <div v-if="gameStore.currentGame" class="game-info">
            <div class="info-item">
              <span class="label">学校名称:</span>
              <span class="value">{{ gameStore.schoolName }}</span>
            </div>
            <div class="info-item">
              <span class="label">当前日期:</span>
              <span class="value">第 {{ gameStore.gameDay }} 天</span>
            </div>
            <div class="info-item">
              <span class="label">学校等级:</span>
              <el-tag type="warning">Lv.{{ gameStore.gameStats.schoolLevel }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">学校声望:</span>
              <el-rate
                v-model="reputationStars"
                disabled
                show-score
                text-color="#ff9900"
              />
            </div>
          </div>
          <el-empty v-else description="暂无游戏数据">
            <el-button type="primary" @click="showCreateDialog = true">
              创建新游戏
            </el-button>
          </el-empty>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card>
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain size="large" style="width: 100%; margin-bottom: 12px;">
              <el-icon><User /></el-icon>
              学生管理
            </el-button