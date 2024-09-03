<!-- src/components/Grandpa.vue -->
<template>
  <div class="mainBox">
    <div class="handleBox">
      <div>爷爷（家庭总资产：{{ assets }} 元）</div>
      <button @click="makeMoney(1000)">爷爷赚钱 1000 元</button>
      <button @click="spendMoney(500)">爷爷花钱 500 元</button>
    </div>
    <!-- 子组件 -->
    <Father :assets="assets" @fatherToGrandpa="updateMoney" />
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
  import Father from "./Father.vue";

  const assets = ref(0); // 将资产定义为可变状态

  const makeMoney = (money: number) => {
    assets.value += money;
  };

  const spendMoney = (money: number) => {
    assets.value -= money;
  };

  const updateMoney = (newAssets: number) => {
    assets.value = newAssets; // 更新资产值
  };
</script>
<style scoped>
  .mainBox {
    border: 1px solid #000;
    padding: 20px;
    margin: 20px;
    border-radius: 10px;
  }
  button {
    margin-right: 20px;
  }
  .handleBox{
    display: flex;
    align-items: center;
  }
</style>
