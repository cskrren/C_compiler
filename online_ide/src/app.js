import Vue from 'vue'
import {createRouter} from './router/router'
import App from './App.vue'

import TreeNode from './components/TreeNode.vue'
Vue.component("tree-node", TreeNode)

// Vue.use(ElementUi)
export function createApp() {
    const router = createRouter()
    const app = new Vue({
        router,
        render: h => h(App)
    })
    return {router, app}
}