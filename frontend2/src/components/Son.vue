<!-- src/components/Son.vue -->
<template>
  <div class="mainBox">
    <div>儿子（家庭总资产：{{ assets }} 元）</div>
    <button @click="makeMoney(200)">儿子赚钱 200 元</button>
    <button @click="spendMoney(100)">儿子花钱 100 元</button>
  </div>
</template>

<script lang="ts" setup>
import {useStore} from "../store";
import {ref, watch} from "vue";

const assets = ref(0);

const store = useStore();

const makeMoney = (money: number) => {
  assets.value += money
  update()
};

const spendMoney = (money: number) => {
  assets.value -= money
  update()
};
const update = () => {
  store.updateAssets(assets.value);
}
watch(() => store.assets, (newValue) => {
  assets.value = newValue;
  update()

});




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
