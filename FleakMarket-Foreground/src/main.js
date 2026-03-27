// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import Vue from 'vue';
import App from './App';
import router from './router';
import store from './store';
import axios from 'axios';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import moment from 'moment';

// 直接请求后端 Spring Boot，而不是通过本地 Node 代理
axios.defaults.baseURL = 'http://localhost:8080';
axios.defaults.timeout = 60000;

Vue.config.productionTip = false;
Vue.prototype.$axios = axios;
Vue.use(ElementUI);


/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  components: { App },
  template: '<App/>'
})
