<template>
  <div class="editor">
    <div class="header" v-show="files.length">
      <span
        @click="currentFileId=file.id"
        v-for="file in files"
        :key="file.id"
        class="file-name"
        :class="{'file-active': file.isShow}">
          {{ file.fileName }}<span v-if="file.isChange">*</span>
        <i
          @click.stop="removeFileById(file.id)"
          class="el-icon-close">
        </i>
      </span>
    </div>

    <div 
      class="main"
      v-show="file.isShow"
      v-for="file in files"
      :key="file.id">
      <textarea
          :id="file.id + textareaSuffix"
          v-model="editor_content"
          @input="inputContent"
          @keydown.tab.prevent
          ref="textarea"
          style="height: 325px;"
      ></textarea>
    </div>
    <div v-if="currentFileId">
      <div class="header">
        <span
          @click="currentProcessId=process.id"
          v-for="process in processes"
          :key="process.id"
          class="process-name"
          :class="{'file-active': process.isShow}">
            {{ process.processname }}
        </span>
      </div>

      <div 
        class="main"
        v-show="process.isShow"
        v-for="process in processes"
        :key="process.id">
        <div v-if="process.id!=3">
          <textarea
            :id="process.id + textareaSuffix"
            v-model="process.content_left"
            @input="inputContent"
            @keydown.tab.prevent
            style="height: 325px; width: 500px"
            readonly
        ></textarea>
          <textarea
              :id="process.id + textareaSuffix"
              v-model="process.content_right"
              @input="inputContent"
              @keydown.tab.prevent
              style="height: 325px; width: 650px;"
              readonly  
          ></textarea>
        </div>
        <div v-else>
          <div class="tree-container">
          <div class="tree">
            <ul>
              <tree-node :node="treeData"></tree-node>
            </ul>
          </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import uuidV1 from "uuid/v1";
import {EventBus} from '../event/EventBus'
import TreeNode from './TreeNode.vue';
export default {
  data: function() {
    return {
      treeData: [
        {
          id: 0,
          label: "PROGRAM",
          children: []
        }
      ],
      files: this.filesProp.map(file => {
        return Object.assign(
          {},
          file,{isShow: false, isChange: false, lineNumbers: this.createLineNumbers(file.fileContent)})
      }),
      processes: [
        {
          id: 0,
          processname: "运行日志",
          content_left: "",
          content_right: "",
          isShow: false,
        },
        {
          id: 1,
          processname: "词法分析",
          content_left: "",
          content_right: "",
          isShow: false,
        },
        {
          id: 2,
          processname: "语法分析",
          content_left: "",
          content_right: "",
          isShow: false,
        },
        {
          id: 3,
          processname: "语法树",
          content_left: "",
          content_right: "",
          isShow: false,
        },
        {
          id: 4,
          processname: "语义分析及优化",
          content_left: "",
          content_right: "",
          isShow: false,
        },
        {
          id: 5,
          processname: "中间代码生成",
          content_left: "",
          content_right: "",
          isShow: false,
        },
        {
          id: 6,
          processname: "查找替换",
          content_left: "",
          content_right: "",
          isShow: false,
        }
      ],
      currentFileId: null,
      currentProcessId : null,
      textareaSuffix: '-textarea',
      editor_content: '',
      clip_board: '',
      counter: 0, // 用于保存计数器的值
      curnode:{},
      compile_node: {},
      optimizeRate : 0,
      compile_state: false,
      compile_error: ""
    }
  },
  props: {
    filesProp: { // id, fileName, fileContent, isShow
      // type: 'Array',
      default: function() {
        return new Array()
      }
    },
    currentFileIdProp: {
      default: null
    }
  },
  watch: {
    currentFileId: {
      deep: true,
      handler: function(newId, oldId) {
        if(newId != undefined && newId != null) {
          var file = this.searchFileById(newId)
          if(file) {
            this.$set(file, 'isShow', true)
            this.editor_content = file.fileContent
            setTimeout(() => {
              document.getElementById(newId+this.textareaSuffix).focus()
            }, 1)
          }
        }
        if(oldId != undefined && oldId != null) {
          var file = this.searchFileById(oldId)
          if(file) {
            this.$set(file, 'isShow', false)
          }
        }
      }
    },
    currentProcessId: {
      deep: true,
      handler: function(newId, oldId) {
        console.log(oldId)
        console.log(newId)
        if(newId != undefined && newId != null) {
          var process = this.searchProcessById(newId)
          console.log(process)
          if(process) {
            this.$set(process, 'isShow', true)
          }
        }
        if(oldId != undefined && oldId != null) {
          var process = this.searchProcessById(oldId)
          if(process) {
            this.$set(process, 'isShow', false)
          }
        }
      }
    }
  },
  mounted() {
    this.startChecking();
    EventBus.$on('save', () => {
      this.save()
    })
    EventBus.$on('compile', () => {
      this.compile();
    })
    EventBus.$on('cut', () => {
      this.cut()
    })
    EventBus.$on('copy', () => {
      this.copy()
    })
    EventBus.$on('paste', () => {
      this.paste()
    })
    EventBus.$on('documentation', () => {
      this.appendDocument()
    })
  },
  methods: {
    find(value){
      this.compile_result = ""
      var code = this.editor_content;
      var lines = code.split('\n');
      var lineMap = new Map();
      var lineNum = 1;
      lines.forEach(line => {
        lineMap.set(lineNum, {
          content: line,
          matchnum: 0
        });
        lineNum++;
      });
      console.log(code.length);
      lineNum = 1;
      var flag = false;
      for(var i = 0; i < code.length; i++){
        if(code[i] == '\n'){
          if (flag == true){
            this.processes[6].content_left += "Line " + lineNum + ": " + lineMap.get(lineNum).content + '\n';
          }
          lineNum++;
          flag = false;
        }
        if(code.substring(i, i+value.length) == value){
          var line = lineMap.get(lineNum).content
          var start = line.indexOf(value, lineMap.get(lineNum).matchnum);
          var end = start + value.length;
          flag = true;
          line = line.substring(0, start) + "%" + value + "%" + line.substring(end, line.length);
          lineMap.set(lineNum, {
            content: line,
            matchnum: end
          });
        }
      }
    },
    replace(findValue, replaceValue){
      this.compile_result = ""
      var code = this.editor_content;
      var lines = code.split('\n');
      var lineMap = new Map();
      var lineNum = 1;
      lines.forEach(line => {
        lineMap.set(lineNum, {
          content: line,
          matchnum: 0
        });
        lineNum++;
      });
      console.log(code.length);
      lineNum = 1;
      var flag = false;
      for(var i = 0; i < code.length; i++){
        if(code[i] == '\n'){
          if (flag == true){
            this.processes[6].content_left += "Line " + lineNum + ": " + lineMap.get(lineNum).content + '\n';
          }
          lineNum++;
          flag = false;
        }
        if(code.substring(i, i+findValue.length) == findValue){
          var line = lineMap.get(lineNum).content;
          var start = line.indexOf(findValue, lineMap.get(lineNum).matchnum);
          var end = start + findValue.length;
          flag = true;
          line = line.substring(0, start) + "%" + findValue + "%" + "(" + replaceValue + ")" + line.substring(end);
          this.editor_content = this.editor_content.substring(0, i) + replaceValue + this.editor_content.substring(i+findValue.length);
          lineMap.set(lineNum, {
            content: line,
            matchnum: end
          });
        }
      }
    },
    async getOptimizeRate(){
      await fetch('http://localhost:8081/getOptimizeRate', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          this.optimizeRate = response.msg;
          console.log('Success:', response)
        });
    },
    async getMipsCode(){
      var content = "";
      await fetch('http://localhost:8081/getMipsCode', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[0].content_right = content;
    },
    async getAnalysisHistory(){
      var content = "";
      await fetch('http://localhost:8081/getAnalysisHistory', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[5].content_left = content;
    },
    async getMessageTableHistory(){
      var content = "";
      await fetch('http://localhost:8081/getMessageTableHistory', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[5].content_right = content;
    },
    async getOptimizedCode(){
      var content = "";
      await fetch('http://localhost:8081/getOptimizedCode', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[4].content_right = content;
    },
    async getIntermediateCode(){
      var content = "";
      await fetch('http://localhost:8081/getIntermediateCode', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[4].content_left = content;
    },
    async getSyntasTree(){
      await fetch('http://localhost:8081/getSyntaxTree', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          this.treeData = response.data;
          console.log('Success:', response)
        });
    },
    async getDerivation(){
      var content = "";
      await fetch('http://localhost:8081/getDerivation', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[2].content_right = content;
    },
    async getLR1(){
      var content = "";
      await fetch('http://localhost:8081/getLR1', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[2].content_left = content;
    },
    async getTokenList(){
      var content = "";
      await fetch('http://localhost:8081/getTokenList', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[1].content_right = content;
    },
    async getSymbolTable(){
      var content = "";
      await fetch('http://localhost:8081/getSymbolTable', {
        method: 'GET',
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          content = response.msg;
          console.log('Success:', response)
        });
      this.processes[1].content_left = content;
    },
    async compile(){
      var code = this.editor_content;
      this.save();
      var data = {
        code: code,
      }
      await fetch('http://localhost:8081/compile', {
        method: 'POST',
        body: JSON.stringify(data),
        headers: new Headers({
          'Content-Type': 'application/json'
        })
      }).then(res => res.json())
        .catch(error => console.error('Error:', error))
        .then(response => {
          if (response.code == 200){
            this.processes[0].content_right = response.msg;
            console.log('Success:', response)
            this.compile_state = true;
          }
          else{
            this.compile_error = response.msg;
            console.log('Fail:', response)
            this.compile_state = false;
          }
        });
      if(this.compile_state){
        var savename = this.searchFileById(this.currentFileId).fileName.split('.')[0]+'.s';
        this.compile_node = {
          code: this.processes[0].content_right,
          fileId: this.currentFileId,
          fileName: savename,
        }
        EventBus.$emit('save_result', this.compile_node);
        this.getSymbolTable();
        this.getTokenList();
        this.getLR1();
        this.getDerivation();
        this.getSyntasTree();
        this.getIntermediateCode();
        this.getOptimizedCode();
        this.getMessageTableHistory();
        this.getAnalysisHistory();
        this.getMipsCode();
        this.getOptimizeRate();
        var msg =  "编译文件名称为" + this.searchFileById(this.currentFileId).fileName + "\n"
        msg += "开始编译...\n"
        msg += "词法分析开始...\n"
        msg += "词法分析结束\n"
        msg += "语法分析开始...\n"
        msg += "语法分析结束\n"
        msg += "语义分析开始...\n"
        msg += "语义分析结束\n"
        msg += "中间代码生成开始...\n"
        msg += "中间代码生成结束\n"
        msg += "目标代码生成开始...\n"
        msg += "目标代码生成结束, 优化率为" + this.optimize_rate + "\n"
        msg += "目标代码生成开始...\n"
        msg += "目标代码生成结束\n"
        msg += "编译结束\n"
        msg += "保存文件" + savename + "\n"
        this.processes[0].content_left = msg;
      } else {
        for (var i = 0; i < this.processes.length; i++){
          this.processes[i].content_left = "";
          this.processes[i].content_right = "";
        }
        this.processes[0].content_left = this.compile_error;
      }
    },
    cut() {
      this.$nextTick(() => {
        for(var i = 0; i < this.files.length; i++) {
          if(this.files[i].id == this.currentFileId) {
            var text =  this.$refs.textarea[i];
            break;
          }
        }
        var first = text.selectionStart;
        var last = text.selectionEnd;
        this.clip_board = this.editor_content.substring(first, last);
        this.editor_content = this.editor_content.substring(0, first) + this.editor_content.substring(last);
      })
    },
    copy() {
      this.$nextTick(() => {
        for(var i = 0; i < this.files.length; i++) {
          if(this.files[i].id == this.currentFileId) {
            var text =  this.$refs.textarea[i];
            break;
          }
        }
        var first = text.selectionStart
        var last = text.selectionEnd
        this.clip_board = this.editor_content.substring(first, last)
      })
    },
    paste() {
      this.$nextTick(() => {
        for(var i = 0; i < this.files.length; i++) {
          if(this.files[i].id == this.currentFileId) {
            var text =  this.$refs.textarea[i];
            break;
          }
        }
        var first = text.selectionStart
        var last = text.selectionEnd
        this.editor_content = this.editor_content.substring(0, first) + this.clip_board + this.editor_content.substring(last)
      })
    },
    adjustTextareaHeight() {
      const textarea = this.$refs.textarea;
      if (textarea) {
        textarea.style.height = "auto";
        textarea.style.height = textarea.scrollHeight + "px";
      }
    },
    startChecking() {
      // 使用 setInterval 设置每秒执行一次检测函数
      this.timer = setInterval(() => {
        this.checkEverySecond();
      }, 10);
    },
    checkEverySecond() {
      // 这里是每秒要执行的逻辑
      this.counter++; // 每秒递增计数器的值
      if (this.currentFileId != null) {
        var file = this.searchFileById(this.currentFileId)
        if(file.fileContent != this.editor_content) {
          this.$set(file, 'isChange', true)
        } else {
          this.$set(file, 'isChange', false)
        }
      } 
    },
    save(){
      console.log(this.files)
      console.log(this.currentFileId)
      var file = this.searchFileById(this.currentFileId)
      file.fileContent = this.editor_content
      this.curnode={
        id: this.currentFileId,
        label: file.fileName,
        fatherId: 0,
        type: 'file',
        fileContent: file.fileContent,
      }
      fetch('http://localhost:8081/system/savefile', {
        method: 'POST',
        body: JSON.stringify(this.curnode),
        headers: {
          'Content-Type': 'application/json'
        }
      }).then(res => res.json())
        .then(res => {
          console.log(res)
        })
        .catch(err => console.log(err))
      this.$set(file, 'isChange', false)
    },
    createLineNumbers: function(text) {
      var lineNumbers = [{id: uuidV1(), number: 1}]
      var enterList = text.match(/\n/g)
      var enterQuantity =  enterList ? enterList.length : 0 
      for(var i=0; i<enterQuantity; i++) {
        lineNumbers.push({id: uuidV1(), number: i+2})
      }
      return lineNumbers
    },
    enter: function() {
      var fileLineNumbers = this.searchFileById(this.currentFileId).lineNumbers
      fileLineNumbers.push({id: uuidV1(), number: fileLineNumbers.length+1})
    },
    // 暴力搜索 /n 个数
    inputContent: function(eve) {
      var numbers = this.searchFileById(this.currentFileId).lineNumbers
      var enterQuantity = this.searchFileById(this.currentFileId).fileContent.match(/\n/g)
      enterQuantity = enterQuantity? enterQuantity.length: 0
      var delta = (enterQuantity + 1) - numbers.length
      if(delta > 0) {
        for(var i=0; i<delta; i++) {
          numbers.push({id: uuidV1(), number: numbers.length+1})
        }
      } else if(delta < 0) {
        for(var i=0-delta; i>0; i--) {
          numbers.pop()
        }
      }
    },
    searchProcessById: function(id) {
      for(var i=0; i<this.processes.length; i++) {
        if(this.processes[i].id === id) {
          return this.processes[i]
        }
      }
      return null
    },
    searchFileById: function(id) {
      for(var i=0; i<this.files.length; i++) {
        if(this.files[i].id === id) {
          return this.files[i]
        }
      }
      return null
    },
    removeFileById: function(id) {
      for(var i=0; i<this.files.length; i++) {
        if(this.files[i].id === id) {
          if(this.files.splice(i, 1)[0].id === this.currentFileId) {
            this.currentFileId = 
              this.files.length ? this.files[i%this.files.length].id : null
          }
          return
        }
      }
    },
    appendFile: function(file) {
      if(!file) {
        return
      }
      this.currentFileId = file.id
      if(this.searchFileById(file.id)) {
        return
      }
      file.lineNumbers = this.createLineNumbers(file.fileContent)
      this.files.push(file)
    },
    appendDocument: function() {
      this.currentFileId = 0
      var file = {
        id: 0,
        fileName: 'Help Document',
        fileContent: '',
      }
      this.files.push(file)
    },
    renameFile: function(id, newName) {
      var file = this.searchFileById(id)
      if(file) {
        file.fileName = newName
      }
    }
  },

  created: function() {
    this.currentFileId =
      this.currentFileIdProp ?
        this.currentFileIdProp :
        this.files.length ?
          this.files[0].id : null
  },
  components: {
    TreeNode
  },
  beforeDestroy() {
    // 在组件销毁前清除定时器
    clearInterval(this.timer);
  }
}
</script>

<style scoped>
  .editor {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    font-size: 15px;
    background-color: #1c2022;
    font-family: consolas;
  }
  .header {
    height: 35px;
    width: 100%;
    background-color: rgb(17, 21, 24);
  }
  .main {
    flex-grow: 1;
    height: 325px;
    width: 100%;
    display: flex;
    flex-direction: column;
  }
  .text-area-wrapper {
    flex: 1;
  }
  .file-name {
    display: inline-block;
    width: fit-content;
    padding: 0 20px; /* 添加左右边距来实现比 auto 多 20px 的效果 */
    height: 100%;
    text-align: center;
    line-height: 35px;
    color: rgba(255, 255, 255, 0.5);
    cursor: pointer;
  }
  .file-name i {
    font-size: 10px;
  }
  .process-name {
    display: inline-block;
    width: fit-content;
    padding: 0 40px; /* 添加左右边距来实现比 auto 多 20px 的效果 */
    height: 100%;
    text-align: center;
    line-height: 35px;
    color: rgba(255, 255, 255, 0.5);
    cursor: pointer;
  }
  .process_name i {
    font-size: 10px;
  }
  .file-active {
    color: rgb(255, 255, 255) !important;
    background-color: #1c2022;
    border-bottom: rgb(38, 109, 77) 2px solid
  }
  .sider {
    width: 55px;
    height: auto;
    margin-right: 26px;
    flex: 1;
  }
  .number {
    width: 55px;
    height: 20px;
    line-height: 20px;
    text-align: right;
    color: #858585;
  }
  textarea {
    margin-top: 10px;
    margin-left: 30px;
    width: 97%;
    font-size: 15px;
    font-family: consolas;
    color: #c6c6c6;
    line-height: 20px;
    background-color: #1c2022;
    resize: none;
    border: none;
    outline: none;
    caret-color: #66b9f4;
  }
  textArea::-webkit-scrollbar {
    width: 0;
    height: 0;
  }
  .tree {
    font-family: Arial, sans-serif;
    margin: 1em;
  }

  .tree ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
  }

  .tree li {
    margin: 0;
    padding: 0;
    display: flex;
    align-items: center;
  }

  .tree li::before {
    content: '';
    position: absolute;
    top: 50%;
    left: -25px;
    width: 25px;
    height: 2px;
    background-color: #ccc;
  }

  .tree-container {
    height: 325px;
    width: 1000px;
    overflow: auto; /* 添加滚动条样式 */
  }

  .tree-container::-webkit-scrollbar {
    width: 0;
    height: 0;
  }

  .tree li:first-child::before {
    display: none;
  }
</style>