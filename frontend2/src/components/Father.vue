<!-- src/components/Father.vue -->
<template>
  <div class="mainBox">
    <div class="handleBox">
      <div>父亲（家庭总资产：{{ assets }} 元）</div>
      <button @click="makeMoney(500)">父亲赚钱 500 元</button>
      <button @click="spendMoney(300)">父亲花钱 300 元</button>
    </div>
    <!-- 子组件 -->
    <Son/>
    <Daughter/>
  </div>
</template>

<script lang="ts" setup>
import {ref, defineEmits, watch} from "vue";
import Son from "./Son.vue";
import Daughter from "./Daughter.vue";
import {EventBus} from "../bus.ts";
import {useStore} from "../store";

const store = useStore();
// 使用 defineProps 接收传入的 props
const props = defineProps({
  assets: {
    type: Number,
    required: true,
  },
});
// 使用 ref 定义响应式数据，初始值为 0
const assets = ref(props.assets);

// 使用 watch 监听 props.assets 的变化并更新 ref
watch(() => props.assets, (newValue) => {
  assets.value = newValue;
  updateMoney()

});
watch(() => store.assets, (newValue) => {
  assets.value = newValue;
  updateMoney()

});

// 使用 EventBus 监听事件更新 assets 的值
EventBus.on("daughterToFather", (val: number) => {
  assets.value = val;
  updateMoney()
});

// 父亲和爷爷通信
const emit = defineEmits<{
  (e: "fatherToGrandpa", val: number): void;
}>();

const makeMoney = (money: number) => {
  assets.value += money;
  updateMoney()
};
const spendMoney = (money: number) => {
  assets.value -= money;
  updateMoney()
};
const updateMoney = () => {
  emit("fatherToGrandpa", assets.value);
  EventBus.emit("fatherToDaughter", assets.value);
  store.updateAssets(assets.value);
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

.handleBox {
  display: flex;
  align-items: center;
}
</style>
