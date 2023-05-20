<template>
    <header>
        <div class="menu">
            <div class="menu-item" @click="toggleSubMenu('file')">File
                <div class="submenu" style="width: 180px;" v-show="subMenuOpen === 'file'">
                    <div class="submenu-item" @click="handleNewFile()">New File</div>
                    <div class="submenu-item" @click="handleNewDirectory()">New Directory</div>
                    <div class="submenu-item" @click="handleSaveFile()">Save</div>
                    <div class="submenu-item" @click="handlFileUpload()">Upload</div>
                    <!-- 文件上传的表单 -->
                    <form ref="uploadForm" style="display: none">
                    <input type="file" ref="fileInput" name="file" @change="postfile">
                    </form>
                    <div class="submenu-item" @click="handlFileDownload()">Download</div>
                </div>
            </div>
            <div class="menu-item" @click="toggleSubMenu('edit')">Edit
                <div class="submenu" v-show="subMenuOpen === 'edit'">
                    <div class="submenu-item" @click="handlFileCut()">Cut</div>
                    <div class="submenu-item" @clisk="handlFileCopy()">Copy</div>
                    <div class="submenu-item" @click="handlFilePaste()">Paste</div>
                </div>
            </div>
            <div class="menu-item" @click="toggleSubMenu('edit')">Check
                <div class="submenu" v-show="subMenuOpen === 'edit'">
                    <div class="submenu-item" @click="handlFileFind()">Find</div>
                    <div class="submenu-item" @click="handlFileReplace()">Replace</div>
                </div>
            </div>
            <div class="menu-item" @click="toggleSubMenu('go')">Run
                <div class="submenu" v-show="subMenuOpen === 'go'">
                    <div class="submenu-item" @click="handlFileCompile()">Compile</div>
                </div>
            </div>
            <div class="menu-item" @click="toggleSubMenu('help')">Help
                <div class="submenu" v-show="subMenuOpen === 'help'">
                    <div class="submenu-item" @click="handlDocumentation()">Documentation</div>
                </div>
            </div>
        </div>
    </header>
</template>

<script>
import {EventBus} from '../event/EventBus'
export default {
    data() {
        return {
            subMenuOpen: null
        };
    },
    mounted() {
        // 监听键盘按键
        document.addEventListener('keydown', this.handleKeyDown);
    },
    beforeUnmount() {
        // 移除键盘事件监听器
        document.removeEventListener('keydown', this.handleKeyDown);
    },
    methods: {
        toggleSubMenu(menu) {
            if (this.subMenuOpen === menu) {
                this.subMenuOpen = null; // Close the sub-menu if it's already open
            } else {
                this.subMenuOpen = menu; // Open the sub-menu for the selected menu
            }
        },
        handleNewFile() {
            EventBus.$emit('new', 'file');
        },
        handleNewDirectory() {
            EventBus.$emit('new', 'directory');
        },
        handleSaveFile() {
            EventBus.$emit('save');
        },
        async handlFileUpload() {
            // 通过 $refs 获取 fileInput 元素，并模拟点击事件触发文件选择对话框
            var file = await new Promise((resolve) => {
                this.$refs.fileInput.addEventListener('change', (event) => {
                const selectedFile = event.target.files[0];
                resolve(selectedFile);
                }, { once: true });
                this.$refs.fileInput.click();
            });
            EventBus.$emit('upload', file);
        },
        postfile() {
            this.$refs.fileInput.click();
        },
        handlFileDownload() {
            EventBus.$emit('download');
        },
        handlFileCut() {
            EventBus.$emit('cut');
        },
        handlFileCopy() {
            EventBus.$emit('copy');
        },
        handlFilePaste() {
            EventBus.$emit('paste');
        },
        handlFileFind() {
            EventBus.$emit('find');
        },
        handlFileReplace() {
            EventBus.$emit('replace');
        },
        handlFileCompile() {
            EventBus.$emit('compile');
        },
        handlDocumentation() {
            EventBus.$emit('documentation');
        },
        handleMenuAction(action) {
            // 根据传入的操作执行相应的功能
            if (action === 'new file') {
                // 执行新建文件操作
                this.handleNewFile();
            } else if (action === 'new directory') {
                // 执行新建目录操作
                this.handleNewDirectory();
            } else if (action === 'save') {
                // 执行保存操作
                this.handleSaveFile();
            } else if (action === 'upload') {
                // 执行上传操作
                this.handlFileUpload();
            } else if (action === 'download') {
                // 执行下载操作
                this.handlFileDownload();
            } else if (action === 'cut') {
                // 执行剪切操作
                this.handlFileCut();
            } else if (action === 'copy') {
                // 执行复制操作
                this.handlFileCopy();
            } else if (action === 'paste') {
                // 执行粘贴操作
                this.handlFilePaste();
            } else if (action === 'find') {
                // 执行查找操作
                this.handlFileFind();
            } else if (action === 'replace') {
                // 执行替换操作
                this.handlFileReplace();
            } else if (action === 'compile') {
                // 执行编译操作
                this.handlFileCompile();
            } else if (action === 'documentation') {
                // 打开文档
                this.handlDocumentation();
            }
        },
        handleKeyDown(event) {
            // 检查按下的键是否是要触发的快捷键
            if (event.ctrlKey && event.key === '1') {
                event.preventDefault(); // 阻止默认的新建操作
                this.handleMenuAction('new file'); // 执行新建文件操作
            } else if (event.ctrlKey && event.key === '2') {
                event.preventDefault(); // 阻止默认的新建操作
                this.handleMenuAction('new directory'); // 执行新建目录操作
            } else if (event.ctrlKey && event.key === 's') {
                event.preventDefault(); // 阻止默认的保存操作
                this.handleMenuAction('save'); // 执行保存操作
            } else if (event.ctrlKey && event.key === 'u') {
                event.preventDefault(); // 阻止默认的上传操作
                this.handleMenuAction('upload'); // 执行上传操作
            } else if (event.ctrlKey && event.key === 'd') {
                event.preventDefault(); // 阻止默认的下载操作
                this.handleMenuAction('download'); // 执行下载操作
            } else if (event.ctrlKey && event.key === 'x') {
                event.preventDefault(); // 阻止默认的剪切操作
                this.handleMenuAction('cut'); // 执行剪切操作
            } else if (event.ctrlKey && event.key === 'c') {
                event.preventDefault(); // 阻止默认的复制操作
                this.handleMenuAction('copy'); // 执行复制操作
            } else if (event.ctrlKey && event.key === 'v') {
                event.preventDefault(); // 阻止默认的粘贴操作
                this.handleMenuAction('paste'); // 执行粘贴操作
            } else if (event.ctrlKey && event.key === 'f') {
                event.preventDefault(); // 阻止默认的查找操作
                this.handleMenuAction('find'); // 执行查找操作
            } else if (event.ctrlKey && event.key === 'r') {
                event.preventDefault(); // 阻止默认的替换操作
                this.handleMenuAction('replace'); // 执行替换操作
            } else if (event.ctrlKey && event.key === 'g') {
                event.preventDefault(); // 阻止默认的编译操作
                this.handleMenuAction('compile'); // 执行编译操作
            } else if (event.ctrlKey && event.key === 'h') {
                event.preventDefault(); // 阻止默认的打开文档操作
                this.handleMenuAction('documentation'); // 打开文档
            }
        }
    }
};
</script>

<style scoped>
header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%;
    height: 3rem;
    background-color: rgb(28, 32, 34);
    border-bottom: 2px solid rgb(17, 21, 24);
    font-weight: 400;
    font-size: 1.2rem;
    color: rgb(204, 204, 204);
}

header .menu {
    display: flex;
    align-items: center;
    height: 100%;
}

header .menu .menu-item {
    position: relative; /* 添加相对定位 */
    padding: 4px 12px;
    color: #cccccc;
    cursor: pointer;
    border-radius: 2px;
    font-size: 0.875rem;
}

header .menu .menu-item:hover {
    background-color: rgba(255, 255, 255, 0.1); /* 悬停时的背景颜色 */
}

header .menu .submenu {
    position: absolute;
    top: 100%;
    left: 0;
    display: none;
    background-color: rgb(28, 32, 34);
    padding: 8px 0;
    min-width: 120px; /* 子菜单的最小宽度 */
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1); /* 添加阴影效果 */
    z-index: 9999; /* 提高层级 */
}

header .menu .submenu-item {
    padding: 6px 12px;
    cursor: pointer;
    font-size: 0.875rem;
}

header .menu .submenu-item:hover {
    background-color: #f0f0f0;
}

header .menu .submenu-item:focus {
    outline: none;
}

header .menu .menu-item:hover .submenu {
    display: block;
}
</style>
