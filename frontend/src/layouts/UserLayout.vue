<template>
  <div class="page-shell">
    <div class="container">
      <AppHeader />
      <main>
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/common/AppHeader.vue';
</script>

<style scoped>
main {
  padding-bottom: 56px;
}
</style>
