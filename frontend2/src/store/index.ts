// src/store/index.ts
import { defineStore } from 'pinia';

export const useStore = defineStore('main', {
    state: () => ({
        assets: 0,
    }),
    actions: {
        updateAssets(money: number) {
            this.assets = money;
        },

    },
});
