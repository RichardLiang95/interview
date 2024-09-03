<!-- src/components/Daughter.vue -->
<template>
  <div class="mainBox">
    <div>女儿（家庭总资产：{{ assets }} 元）</div>
    <button @click="makeMoney(100)">女儿赚钱 100 元</button>
    <button @click="spendMoney(50)">女儿花钱 50 元</button>
  </div>
</template>

<script lang="ts" setup>
import {ref} from "vue";
import {EventBus} from "../bus.ts";
// 使用 ref 定义响应式数据，初始值为 0
const assets = ref<number>(0);

EventBus.on("updateMoney", (newAssets: number) => {
  assets.value = newAssets;
});

const makeMoney = (money: number) => {
  assets.value += money;
  update()
};

const spendMoney = (money: number) => {
  assets.value -= money;
  update()
};
EventBus.on("fatherToDaughter", (val: number) => {
  assets.value = val;
});
const update = () => {
  EventBus.emit("daughterToFather", assets.value);
}
</script>

<style scoped>
.mainBox {
  border: 1px solid #000;
  padding: 20px;
  margin: 20px;
  border-radius: 10px;
  display: flex;
  align-items: center;
}

button {
  margin-right: 20px;
}
</style>
