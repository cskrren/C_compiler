<template>
  <div style="height: 100%; display: flex; flex-direction: column">
    <menu-component></menu-component>
    <div :style="{display: 'flex', height: '100%', flexGrow: 1}">
      <nav></nav>
      <explorer-component
        ref="explorer_vm"
        @created-file="openEditor"
        @node-click="openEditor"
        @rename-enter="renameEditorTag"
        @delete-node="deleteNode"
        :treeItemsProp="nodes">
      </explorer-component>
      <editor-component
        ref='editor_vm'
        :files-prop="files">
      </editor-component>
    </div>
    <footer></footer>
    <div v-if="showModal" class="modal-container">
      <div class="modal" :class="{ 'modal-open': showModal }">
        <p class="prompt">{{ "查找" }}</p>
        <input type="text" class="input-field" v-model="findValue" />
        <div v-if="isReplace">
          <p class="prompt">{{ "替换" }}</p>
          <input type="text" class="input-field" v-model="replaceValue" />
        </div>
        <div class="buttons">
          <button @click="confirm">确认</button>
          <button @click="cancel">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import menuComponent from "./Menu.vue";
import explorerComponent from "./Explorer.vue";
import editorComponent from "./Editor.vue";
import { EventBus } from "../event/EventBus";
const fetch = require('node-fetch');
var files = [
]
var nodes = [
]
export default {
  components: { menuComponent, explorerComponent, editorComponent },
  data: function() {
    return {
      files, 
      nodes,
      showModal: false,
      findValue: '',
      replaceValue: '',
      isReplace: false,
    };
  },
  created() {
    this.getNodelist();
  },
  mounted() {
    EventBus.$on("find", ()=>{
      this.callfind();
    });
    EventBus.$on("replace", ()=>{
      this.callreplace()
    });
    EventBus.$on("changeNode", (id, newcontent)=>{
      this.change(id, newcontent);
    });
  },
  methods: {
    findNodeById(nodes, id) {
      for (var i = 0; i < nodes.length; i++) {
        if (nodes[i].id === id) {
          return nodes[i];
        }
        if (nodes[i].children) {
          var node = this.findNodeById(nodes[i].children, id);
          if (node) {
            return node;
          }
        }
      }
    },
    change(id, newcontent) {
      //根据id递归查找所有node, 修改content
      var node = this.findNodeById(this.nodes, id);
      console.log(node);
      node.fileContent = newcontent;
    },
    openModal() {
      this.showModal = true;
      return new Promise((resolve, reject) => {
        this.resolveCallback = resolve;
      });
    },
    confirm() {
      // 调用 resolveCallback，表示确认按钮被按下
      if (this.resolveCallback) {
        this.resolveCallback();
        this.resolveCallback = null; // 重置回调函数
      }
      this.closeModal();
    },
    cancel() {
      // 处理取消操作
      console.log("Canceled");
      this.closeModal();
    },
    closeModal() {
      this.showModal = false;
      this.inputValue = ""; // 清空输入框内容
    },
    async find() {
      await this.openModal(); // 等待按钮被按下
      console.log("Find: " + this.findValue);
      this.$refs.editor_vm.find(this.findValue);
    },
    async replace() {
      await this.openModal(); // 等待按钮被按下
      console.log("Find: " + this.findValue);
      console.log("Replace: " + this.replaceValue);
      this.$refs.editor_vm.replace(this.findValue, this.replaceValue);
    },
    callfind() {
      this.isReplace = false;
      this.find();
    },
    callreplace() {
      this.isReplace = true;
      this.replace();
    },
    getNodelist() {
      fetch('http://localhost:8081/system/nodelist')
      .then(res => res.json())
      .then(json => {
        this.nodes = json['data'];
        console.log(this.nodes);
      });
    },
    openEditor: function(node) {
      if(node.type === 'directory') {
        return
      }
      this.$refs.editor_vm.appendFile({
        id: node.id,
        fileName: node.label,
        fileContent: node.fileContent ? node.fileContent : '',
        isChange: false
      })
    },
    renameEditorTag: function(node) {
      if(node.type === 'directory') {
        return
      }
      this.$refs.editor_vm.renameFile(node.id, node.label)
    },
    deleteNode: function(id) {
      this.$refs.editor_vm.removeFileById(id)
    }
  }
};
</script>

<style scoped>
nav {
  height: 100%;
  width: 70px;
  background-color: rgb(28, 32, 34);
  border-right: 2px solid rgb(17, 21, 24);
}
footer {
  height: 30px;
  width: 100%;
  background-color: rgb(28, 32, 34);
  border-top: 2px solid rgb(17, 21, 24);
}
.modal-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5); /* 半透明黑色背景 */
  display: flex;
  align-items: center;
  justify-content: center;
}
.modal {
  background-color: #fff;
  padding: 20px;
  margin-bottom: 100px;
  border-radius: 10px;
  transform: scale(0);
  transition: transform 0.3s ease;
}

.modal-open {
  transform: scale(1);
}
.buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.buttons button {
  margin-right: 10px;
  padding: 10px 20px;
  background-color: #007bff;
  color: #fff;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}

.buttons button:last-child {
  margin-right: 0;
}
</style>