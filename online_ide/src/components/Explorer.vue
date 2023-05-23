<template>
  <div class="explorer" @contextmenu.prevent="showMenu">
    <div class="title">EXPLORER</div>
    <el-tree
      :default-expanded-keys="defaultExpandKeys"
      class="tree"
      ref="treeVm"
      node-key="id"
      @node-contextmenu="showMenuByNode"
      @node-click="nodeClick"
      :data="treeItemsProp">
      <template v-slot:default="{data}">
        <span>
            <span v-if="isDirectory(data)" style="color: #5EA294" class="el-icon-folder-opened"></span>
            <span v-else class="el-icon-document" style="color: rgb(65, 184, 131)"></span>
          <span 
            v-show="!(treeNodesMap[data.id].isShowCreateFileInput||treeNodesMap[data.id].isShowRenameInput)"
            style="font-size: 14px"
            >
            {{data.label}}
          </span>
          <input
            @keydown.enter="keyDownEnterCreateFileInput"
            @blur="createFileInputBlur"
            v-inserted-focus
            class="node-name-input"
            v-if="treeNodesMap[data.id].isShowCreateFileInput"
            v-model="data.label">
          <input
            class="node-name-input"
            @blur="keyDownEnterRenameInput(data)"
            @keydown.enter.prevent="keyDownEnterRenameInput(data)"
            v-show="treeNodesMap[data.id].isShowRenameInput"
            v-model="data.label">
        </span>
      </template>
    </el-tree>
    <explorer-menu-component
      @create-file-click="createFileClickListener('file')"
      @create-directory-click="createFileClickListener('directory')"
      @rename-click="renameClickListener"
      @blur.native="hideMenu"
      @delete-click="deleteClick"
      ref="menuVM"
      :menu="menu"
      style="width: 150px">
    </explorer-menu-component>
  </div>
</template>

<script>
import { Tree, Card } from "element-ui";
import explorerMenuComponent from "./ExplorerMenu.vue";
import uuidV1 from 'uuid/v1'
import Menu from './Menu.vue';
import {EventBus} from '../event/EventBus'
const fetch = require('node-fetch');
export default {
  data: function() {
    return {
      menu: {
        isShow: false,
        positionX: "0px",
        positionY: "0px"
      },
      nodeToBeProcessed: null,
      newSystemNode: {},
      maxFileNum: 1000,
      treeNodesMap: Object.fromEntries(
        Array.from({ length: 1000 }, (_, i) => [
          i + 1,
          {
            isShowCreateFileInput: false,
            isShowRenameInput: false,
          },
        ])
      ),
      treeNodeNum: 0,
      isAddNode: false,
      newNode: null,
      node_vm: null,
      defaultExpandKeys: new Array(),
      fileContent: ''
    };
  },
  props: {
    treeItemsProp: { // id label children type
      default: function() {
        return new Array()
      }
    }
  },
  mounted() {
    EventBus.$on('new', (data) => {
    // 调用兄弟组件的函数
      this.createFileClickListener(data);
    });
    EventBus.$on('upload', (file) => {
      this.add(file)
    });
    EventBus.$on('download', () => {
      this.download()
    });
    EventBus.$on('save_result', (data) => {
      this.saveResult(data)
    });
  },
  created() {
    this.getNodeNum() 
  },
  methods: {
    isDirectory: function(node) {
      if(node.type === 'directory') {
        return true
      }
      return false
    },
    keyDownNode(data){
      this.nodeToBeProcessed = data
    },
    saveResult(data){
      this.treeNodeNum++
      this.newNode = {
        id: this.treeNodeNum,
        label: data['fileName'],
        fileId : data['fileId'],
        type: 'file',
        fileContent: data['code']
      }
      fetch('http://localhost:8081/saveresult', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(this.newNode)
      })
      .then(res => res.json())
    },
    download(){
      if (this.nodeToBeProcessed && this.isDirectory(this.nodeToBeProcessed)) {
        return
      }
      console.log(this.nodeToBeProcessed)
      const fileName = this.nodeToBeProcessed.label; // 文件名称
      const fileContent = this.nodeToBeProcessed.fileContent; // 文件内容

      // 创建Blob对象
      const blob = new Blob([fileContent], { type: 'text/plain' });

      // 生成文件的URL
      const fileUrl = URL.createObjectURL(blob);

      // 创建一个隐藏的<a>标签
      const link = document.createElement('a');
      link.href = fileUrl;
      link.download = fileName;

      // 模拟点击下载
      link.click();

      // 释放URL资源
      URL.revokeObjectURL(fileUrl);
    },
    getNodeNum(){
      fetch('http://localhost:8081/system/nodemaxid')
      .then(res => res.json())
      .then(json => {
        this.treeNodeNum = json['data']
      })
    },
    readFile(file) {
      return new Promise((resolve, reject) => {
        const reader = new FileReader();

        reader.onload = (event) => {
        const contents = event.target.result;
        resolve(contents);
        };

        reader.onerror = (event) => {
        reject(event.target.error);
        };

        reader.readAsText(file);
      });
    },
    async add(file){
      if (this.nodeToBeProcessed && !this.isDirectory(this.nodeToBeProcessed)) {
        return
      }
      this.fileContent = await this.readFile(file);
      console.log(this.fileContent)
      this.treeNodeNum++
      this.newNode = {
        id: this.treeNodeNum,
        label: file.name,
        type: 'file',
        children: null,
        fileContent: this.fileContent
      }
      this.$set(
        this.treeNodesMap,
        this.newNode.id,
        {isShowCreateFileInput: false, isShowRenameInput: false}
      )
      this.$refs.treeVm.append(this.newNode, this.nodeToBeProcessed)
      this.newSystemNode = {
        id: this.newNode.id,
        label: this.newNode.label,
        fatherId: this.nodeToBeProcessed ? this.nodeToBeProcessed.id : 0,
        type: this.newNode.type,
        fileContent: this.newNode.fileContent
      }
      console.log(this.newSystemNode)
      fetch('http://localhost:8081/system/nodeinsert', {
        method: 'POST',
        body: JSON.stringify(this.newSystemNode),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => res.json())
      this.hideMenu()
      this.defaultExpandKeys.push(this.newNode.id)
    },
    // 显示菜单
    showMenu: function(eve) {
      this.nodeToBeProcessed = null
      this.menu.positionX = eve.offsetX + "px";
      this.menu.positionY = eve.offsetY + "px";
      this.menu.isShow = true;
      this.$nextTick(() => this.$refs.menuVM.$el.focus());
    },
    // 显示菜单
    showMenuByNode: function(eve, item, node, node_vm) {
      this.nodeToBeProcessed = item;
      this.node_vm = node_vm;
      this.menu.positionX = eve.offsetX + "px";
      this.menu.positionY = eve.clientY - 48 + "px";
      this.menu.isShow = true;
      this.$nextTick(() => this.$refs.menuVM.$el.focus());
    },
    // 隐藏菜单
    hideMenu: function() {
      this.nodeToBeProcessed = null
      this.node_vm = null
      this.menu.isShow = false;
    },
    // 右键移除节点
    deleteClick: function() {
      console.log(this.nodeToBeProcessed)
      if(!this.nodeToBeProcessed) {
        return
      }
      this.removeNode(this.nodeToBeProcessed)
      this.$emit('delete-node', this.nodeToBeProcessed.id)
      this.hideMenu();
    },
    nodeClick: function(node) {
      this.nodeToBeProcessed = node
      this.$emit('node-click', node)
    },
    // 点击创建节点
    createFileClickListener: function(fileType) {
      console.log(this.nodeToBeProcessed)
      if(this.nodeToBeProcessed && !this.isDirectory(this.nodeToBeProcessed)) {
        return
      }
      this.treeNodeNum++
      this.newNode = {
        id: this.treeNodeNum,
        label: '',
        type: fileType,
        children: null,
        fileContent: ''
      }
      this.addNode(this.newNode, this.nodeToBeProcessed)
      this.hideMenu()
      this.defaultExpandKeys.push(this.newNode.id)
      this.$nextTick(() => {
        this.defaultExpandKeys.splice(0)
      })
    },
    renameClickListener: function() {
      if(!this.nodeToBeProcessed) {
        return
      }
      this.treeNodesMap[this.nodeToBeProcessed.id].isShowRenameInput = true
      var input = this.node_vm.$el.getElementsByTagName('input')[0]
      this.$nextTick(function() {
        input.focus()
        input.select()
      })
      this.hideMenu()
    },
    downloadClickListener: function() {
      if(!this.nodeToBeProcessed) {
        return
      }
      this.nodeToBeProcessed.download()
      this.hideMenu()
    },
    // 新增节点
    addNode: function(newNode, parentNode) {
      if(!newNode) {
        return
      }
      this.$set(
        this.treeNodesMap,
        this.newNode.id,
        {isShowCreateFileInput: true, isShowRenameInput: false}
      )
      this.$refs.treeVm.append(newNode, parentNode)
      this.newSystemNode = {
        id: newNode.id,
        label: "default",
        fatherId: parentNode ? parentNode.id : 0,
        type: newNode.type,
        fileContent: ""
      }
      console.log(this.newSystemNode)
      fetch('http://localhost:8081/system/nodeinsert', {
        method: 'POST',
        body: JSON.stringify(this.newSystemNode),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => res.json())
    },
    // 移除节点
    removeNode: function(treeItem) {
      if(!treeItem) {
        return
      }
      this.treeNodesMap[treeItem.id] = undefined
      this.$refs.treeVm.remove(treeItem);
      fetch('http://localhost:8081/system/nodedelete', {
        method: 'POST',
        body: JSON.stringify(treeItem),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => res.json())
    },
    // 创建节点输入框失去焦点
    createFileInputBlur: function() {
      if(!this.newNode) {
        return
      }
      this.treeNodesMap[this.newNode.id].isShowCreateFileInput = false
      console.log(this.newNode)
      fetch('http://localhost:8081/system/noderename', {
        method: 'POST',
        body: JSON.stringify(this.newNode),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => res.json())
      this.newNode = null
    },
    keyDownEnterRenameInput: function(node) {
      if(node.label.trim() === '') {
        return
      }
      this.treeNodesMap[node.id].isShowRenameInput = false
      
      fetch('http://localhost:8081/system/noderename', {
        method: 'POST',
        body: JSON.stringify(node),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => res.json())
      if(this.isAddNode) {
        this.isAddNode = false
        this.newNode = null
        return
      }
      this.$emit('rename-enter', node)
    },
    keyDownEnterCreateFileInput: function() {
      if(this.newNode.label.trim() === '') {
        return
      }
      this.isAddNode = true
      this.$emit('created-file', this.newNode)
      this.createFileInputBlur()
    }
  },
  directives: {
    'inserted-focus': {
      inserted: function(el) {
        setTimeout(() => el.focus(), 1)
      }
    }
  },
  components: {
    elTree: Tree,
    elCard: Card,
    explorerMenuComponent,
    Menu
  }
};
</script>

<style>
.explorer {
  height: 100%;
  width: 470px;
  padding-left: 10px;
  position: relative;
  background-color: rgb(25, 29, 31);
  border-right: 2px solid rgb(17, 21, 24);
}
.title {
  width: 100%;
  padding: 10px 0px;
  padding-left: 10px;
  font-size: 0.875rem;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.6);
}
.tree {
  background-color: rgb(25, 29, 31);
  color: rgba(255, 255, 255, 0.5);
}
.el-tree-node__content {
  height: 30px !important;
  border-left: 4px solid transparent;
}
.is-current>.el-tree-node__content {
  background-color: rgba(81, 194, 143, 0.2) !important;
  color: rgb(227, 230, 232);
  border-color: rgb(38, 109, 77);
}
.el-tree-node__content:hover {
  background-color: rgba(55, 65, 64, 0.314);
  color: rgb(227, 230, 232);
  border-color: rgb(38, 109, 77);
}
.el-tree-node:focus>.el-tree-node__content {
  background-color: rgba(81, 194, 143, 0.2) !important;
  border-color: rgb(38, 109, 77);
  color: rgb(227, 230, 232);
}
.node-name-input {
  background-color: rgb(25, 29, 31);
  font-size: 14px;
  color: rgb(227, 230, 232);
  outline: none;
  border: 1px solid rgb(38, 109, 77);
  padding: 1px;
}
.show-input {
  background-color: rgba(81, 194, 143, 0.2) !important;
}
</style>>