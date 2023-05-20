<template>
  <li>
    <div class="tree-node">
      <div class="tree-node-circle">{{ node.label }}</div>
      <div class="tree-node-arrow" v-if="node.children.length"></div>
    </div>
    <ul>
      <tree-node v-for="childNode in node.children" :key="childNode.id" :node="childNode"></tree-node>
    </ul>
  </li>
</template>

<script>
import TreeNode from './TreeNode.vue';

export default {
  props: ['node'],
  components: {
    TreeNode
  }
};
</script>

<style scoped>

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

  .tree li:first-child::before {
    display: none;
  }
  .tree-node {
    display: flex;
    align-items: center;
    margin-left: 10px;
  }

  .tree-node-circle {
    width: fit-content;
    height: fit-content;
    min-width: 75px;
    padding-top: 5px;
    padding-bottom: 5px;
    padding-left: 10px;
    padding-right: 10px;
    border-radius: 50%;
    background-color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
    color: #000;
    font-weight: bold;
    margin-right: 10px;
  }

  .tree-node-arrow {
    width: auto;
    height: auto;
    border-top: 6px solid transparent;
    border-bottom: 6px solid transparent;
    border-left: 6px solid #ccc;
  }
</style>
