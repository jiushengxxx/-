<template>
  <div id="app">
    <router-view v-if="isRouterAlive" />
  </div>

</template>

<script>
import moment from "moment";
export default {
  name: 'App',
  provide () {
    return {
      reload: this.reload
    }
  },
  data () {
    return {
      isRouterAlive: true
    }
  },

  created () {
    // 在页面加载时读取 sessionStorage 里的状态信息
    if (sessionStorage.getItem("store")) {
      this.$store.replaceState(Object.assign({}, this.$store.state, JSON.parse(sessionStorage.getItem("store"))))
    }

    // 在页面刷新时将 vuex 信息保存到 sessionStorage
    window.addEventListener("beforeunload", () => {
      sessionStorage.setItem("store", JSON.stringify(this.$store.state))
    });
  },
  methods: {
    reload () {
      this.isRouterAlive = false
      this.$nextTick(() => {
        this.isRouterAlive = true
      })
    }
  }
}
</script>

<style>
html, body, #app {
  margin: 0;
  padding: 0;
  height: 100%;
  min-height: 100vh;
  background: #F9F9F9;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Text", "Helvetica Neue", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #1C1C1E;
}

/* 列表样式 */
ul {
  list-style: none;
  margin: 0;
  padding: 0;
}

/* 链接样式 */
a {
  color: #007AFF;
  text-decoration: none;
  transition: color 0.25s ease;
}
a:hover {
  color: #0056D2;
}

/* 输入框与按钮 */
input, button {
  border: 0;
  outline: none;
  font-size: 14px;
  padding: 8px 12px;
  border-radius: 10px;
  background: #F2F2F7;
  transition: background 0.25s ease;
}
input:focus, button:hover {
  background: #E5E5EA;
}

/* 卡片风格 */
.card {
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  padding: 20px;
}

/* 页面容器 */
.container {
  width: 100%;
  max-width: 1200px;
  margin: auto;
  padding: 20px;
}

/* 商品卡片悬浮动画 */
.hover-card {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.hover-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 28px rgba(0,0,0,0.12);
}
</style>

