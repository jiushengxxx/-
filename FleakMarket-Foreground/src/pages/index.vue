<template>
  <div class="containerIndex">
    <HeaderTop />
    <Search style="margin-top: 25px;" />
    <el-backtop></el-backtop>

    <!-- 公告栏 -->
    <el-row class="notice-row">
      <el-col :span="16" :offset="4">
        <el-alert
          title="本平台仅供蚌埠学院学生进行二手交易，请注意线下交易安全"
          type="success"
          show-icon
          closable
          class="notice-alert"
        />
      </el-col>
    </el-row>

    <!-- 轮播 + 左侧分类 -->
    <el-row class="slide-row">
      <el-col :span="16" :offset="4">
        <div class="slide-container">
          <!-- 左侧分类侧边栏 -->
          <ul class="category-sidebar">
            <li
              v-for="item in fc"
              :key="item.id"
              @click="gotoType(item.id)"
              class="category-item"
              :class="{ active: $route.query.id == item.id }"
            >
              {{ item.name }}
            </li>
          </ul>

          <!-- 轮播图 -->
          <el-carousel
            height="400px"
            arrow="hover"
            :autoplay="true"
            :autoplay-interval="4000"
            indicator-position="bottom"
          >
            <el-carousel-item v-for="(item, index) in Slide" :key="index">
              <img :src="item" class="banner-img" alt="轮播图" />
            </el-carousel-item>
          </el-carousel>
        </div>
      </el-col>
    </el-row>

    <!-- 热门商品 -->
    <el-divider></el-divider>
    <el-row class="hot">
      <el-col :span="4" :offset="4">
        <h2 class="section-title">🔥 热门商品</h2>
      </el-col>
      <el-col class="center" :span="16" :offset="4">
        <div class="hot-product">
          <ul>
            <li v-for="hotpro in hotpros" :key="hotpro.id">
              <p :style="{borderLeft: '4px '+hotpro.color}">{{hotpro.name}}</p>
              <div :style="{borderTop: '1px '+hotpro.color}">
                <ul>
                  <li v-for="item in hotpro.item" @click="gotoDetails(item.id)" :key="item.id">
                    <span style="margin-right: 10px;">{{item.name}}</span>
                    <span style="float: right;">({{item.creattime}})</span>
                  </li>
                </ul>
              </div>
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>

    <!-- 推荐商品（仿造分类页结构） -->
    <el-divider v-if="recommendpro.length"></el-divider>
    <div class="section">
      <h2 class="section-title">⭐ 推荐商品</h2>
      <!-- 仿造分类页的ul+li结构 -->
      <div class="typeProduct" v-if="recommendpro.length">
        <ul>
          <li v-for="item in recommendpro" :key="item.id">
            <div class="containerProduct" @click="gotoDetails(item.id)">
              <!-- 图片区域 + 收藏按钮（保留首页功能） -->
              <div class="imgbox">
                <img
                  :src="getImg(item)"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImgError($event)"
                >
                <button
                  class="collect-btn"
                  @click.stop="addLove(item.id, item.uid)"
                  :title="isSelfProduct(item.uid) ? '不能收藏自己的商品' : '收藏商品'"
                  :disabled="isSelfProduct(item.uid)"
                >
                  <i class="el-icon-star-off"></i> 收藏
                </button>
              </div>
              <!-- 价格（仿造分类页样式） -->
              <p class="productPrice">¥<span class="price">{{ formatPrice(item.currentprice) }}</span></p>
              <!-- 商品名称（仿造分类页样式） -->
              <p class="productName">{{ item.name }}</p>
              <!-- 评价+操作按钮（仿造分类页的userPlay） -->
              <div class="userPlay">
                <span>评价 {{ item.comments || 0 }}</span>
                <span>
                  <button v-if="item.deal==='线上交易'" @click.stop="addCart(item)">加入购物车</button>
                  <button v-else @click.stop="showUserInfo(item.uid)">联系卖家</button>
                </span>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <div class="empty-tip" v-else>暂无推荐商品</div>
    </div>

    <!-- 最新上架（仿造分类页结构） -->
    <el-divider v-if="newpro.length"></el-divider>
    <div class="section">
      <h2 class="section-title">🆕 最新上架</h2>
      <div class="typeProduct" v-if="newpro.length">
        <ul>
          <li v-for="item in newpro" :key="item.id">
            <div class="containerProduct" @click="gotoDetails(item.id)">
              <div class="imgbox">
                <img
                  :src="getImg(item)"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImgError($event)"
                >
                <button
                  class="collect-btn"
                  @click.stop="addLove(item.id, item.uid)"
                  :disabled="isSelfProduct(item.uid)"
                >
                  <i class="el-icon-star-off"></i> 收藏
                </button>
              </div>
              <p class="productPrice">¥<span class="price">{{ formatPrice(item.currentprice) }}</span></p>
              <p class="productName">{{ item.name }}</p>
              <div class="userPlay">
                <span>评价 {{ item.comments || 0 }}</span>
                <span>
                  <button v-if="item.deal==='线上交易'" @click.stop="addCart(item)">加入购物车</button>
                  <button v-else @click.stop="showUserInfo(item.uid)">联系卖家</button>
                </span>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <div class="empty-tip" v-else>暂无最新商品</div>
    </div>

    <!-- 猜你喜欢（仿造分类页结构） -->
    <el-divider v-if="likepro.length"></el-divider>
    <div class="section">
      <h2 class="section-title">👀 猜你喜欢</h2>
      <div class="typeProduct" v-if="likepro.length">
        <ul>
          <li v-for="item in likepro" :key="item.id">
            <div class="containerProduct" @click="gotoDetails(item.id)">
              <div class="imgbox">
                <img
                  :src="getImg(item)"
                  :alt="item.name"
                  loading="lazy"
                  @error="handleImgError($event)"
                >
                <button
                  class="collect-btn"
                  @click.stop="addLove(item.id, item.uid)"
                  :disabled="isSelfProduct(item.uid)"
                >
                  <i class="el-icon-star-off"></i> 收藏
                </button>
              </div>
              <p class="productPrice">¥<span class="price">{{ formatPrice(item.currentprice) }}</span></p>
              <p class="productName">{{ item.name }}</p>
              <div class="userPlay">
                <span>评价 {{ item.comments || 0 }}</span>
                <span>
                  <button v-if="item.deal==='线上交易'" @click.stop="addCart(item)">加入购物车</button>
                  <button v-else @click.stop="showUserInfo(item.uid)">联系卖家</button>
                </span>
              </div>
            </div>
          </li>
        </ul>
      </div>
      <div class="empty-tip" v-else>暂无推荐商品</div>
    </div>

    <FooterBottom />
  </div>
</template>

<script>
import HeaderTop from "../components/Header.vue"
import Search from "../components/Search.vue"
import FooterBottom from "../components/Footer.vue"
import moment from "moment"

export default {
  components: { HeaderTop, Search, FooterBottom },
  data() {
    return {
      Slide: [
        require('@/assets/banner/banner1.png'),
        require('@/assets/banner/banner2.png'),
        require('@/assets/banner/banner3.png')
      ],
      fc: [],
      imgwidth: "30px",
      hotpros: [
        { id: "1", name: "教材、教辅", color: "solid #FF7F24", item: [] },
        { id: "2", name: "手机、电脑", color: "solid #4169E1", item: [] },
        { id: "3", name: "自行车、电动车", color: "solid #48D1CC", item: [] },
        { id: "4", name: "书桌、书架", color: "solid #FF0000", item: [] }
      ],
      newpro: [],
      recommendpro: [],
      likepro: []
    }
  },
  created() {
    this.initPageData()
    this.clickNum()
  },
  methods: {
    // 初始化页面数据
    async initPageData() {
      try {
        // 加载一级分类
        const categoryRes = await this.$axios.get('/classify/flnb')
        this.fc = categoryRes.data || []

        // 加载热门商品
        const arrayHot = [1,2,3,4]
        for (let i = 0; i < arrayHot.length; i++) {
          const hotRes = await this.$axios.get('/product/selectByHot', {
            params: { typeid: arrayHot[i] }
          })
          this.hotpros[i].item = hotRes.data || []
        }

        // 加载最新上架 + 推荐商品
        const [newRes, recommendRes] = await Promise.all([
          this.$axios.get('/product/selectByNew'),
          this.$axios.get('/product/selectByCommend')
        ])

        // 打印参考格式
        const recommendSample = recommendRes.data && recommendRes.data.length > 0 ? recommendRes.data[0] : null;
        if (recommendSample) {
          console.log('✅ 推荐商品的标准图片格式：', recommendSample.images);
          console.log('✅ 推荐商品解析后格式：', this.formatProductData([recommendSample])[0].images);
        }

        // 统一解析商品数据
        this.newpro = this.formatProductData(newRes.data || []);
        this.recommendpro = this.formatProductData(recommendRes.data || []);
        this.likepro = this.newpro.slice(0, 8);

      } catch (e) {
        console.error('页面数据加载失败：', e)
        this.$message.warning('数据加载失败，请刷新重试')
      }
    },

    // 统一解析商品数据（兼容图片格式）
    formatProductData(list) {
      if (!Array.isArray(list)) return []
      return list.map(item => {
        let finalImages = [];
        // 解析图片字段
        if (item.images && item.images.toString().trim() !== '') {
          try {
            let parsed = []
            if (Array.isArray(item.images)) {
              parsed = item.images
            } else if (typeof item.images === 'string') {
              const str = item.images.trim()
              if (str) {
                try {
                  const temp = JSON.parse(str)
                  parsed = Array.isArray(temp) ? temp : [temp]
                } catch (e) {
                  parsed = [str]
                }
              }
            }
            // 兜底处理
            if (!parsed || parsed.length === 0) {
              parsed = ['/images/default.png']
            }
            // 补全图片路径
            const baseUrl = "http://你的服务器IP:端口";
            finalImages = parsed.map(url => {
              return url.startsWith('http') ? url : `${baseUrl}${url}`;
            });
          } catch (e) {
            console.error('转换格式失败：商品ID=', item.id, '原始值=', item.images, '错误=', e);
            finalImages = [];
          }
        }
        // 赋值图片数组
        item.images = finalImages;
        // 格式化时间
        item.creattime = item.creattime ? moment(item.creattime).format('MM-DD HH:mm') : '未知时间';
        return item;
      });
    },

    // 获取商品首图
    getImg(item) {
      if (!item || !item.images || !Array.isArray(item.images) || item.images.length === 0) {
        return 'https://picsum.photos/220/180?random=default';
      }
      return item.images[0];
    },

    // 格式化价格
    formatPrice(price) {
      const num = Number(price)
      return isNaN(num) ? '0.00' : num.toFixed(2)
    },

    // 图片加载失败兜底
    handleImgError(e) {
      e.target.src = 'https://picsum.photos/220/180?random=default'
    },

    // 收藏商品
    async addLove(id, uid) {
      if (this.$store.state.userid === 0 || !this.$store.state.userid) {
        return this.$message.warning('请先登录！')
      }
      if (this.$store.state.userid === uid) {
        return this.$message.warning('不能收藏自己发布的商品！')
      }
      try {
        const checkRes = await this.$axios.post('/product/selectLoveById', {
          uid: this.$store.state.userid,
          pid: id
        })
        if (checkRes.data < 1) {
          const addRes = await this.$axios.post('/product/insertProductToLove', {
            uid: this.$store.state.userid,
            pid: id
          })
          if (addRes.data === 1) {
            this.$message.success('商品收藏成功！')
          } else {
            this.$message.error('收藏失败，请重试')
          }
        } else {
          this.$message.warning('请勿重复收藏！')
        }
      } catch (e) {
        console.error('收藏失败：', e)
        this.$message.error('收藏失败，服务器异常')
      }
    },

    // 判断是否是自己的商品
    isSelfProduct(uid) {
      return this.$store.state.userid && this.$store.state.userid === uid
    },

    // 跳转商品详情
    gotoDetails(pid) {
      if (!pid) return this.$message.warning('商品ID异常')
      this.$router.push({
        name: 'productDetails',
        query: { id: pid }
      })
    },

    // 跳转分类页
    gotoType(id) {
      if (!id) return this.$message.warning('分类ID异常')
      this.$router.push({
        name: 'productType',
        query: { id }
      })
    },

    // 点击量统计
    async clickNum() {
      try {
        const date = moment().format('YYYY-MM-DD')
        const checkRes = await this.$axios.post('/utils/selectDateFromStatis', {
          dates: date
        })
        if (checkRes.data.length === 0) {
          await this.$axios.post('/utils/insertDateInStatis', { dates: date })
        } else {
          await this.$axios.post('/utils/updateNumInStatis', {
            id: checkRes.data.id,
            clickNum: checkRes.data.clickNum + 1
          })
        }
      } catch (e) {
        console.error('统计点击量失败：', e)
      }
    },

    // ===== 新增：仿造分类页的加入购物车功能（避免冲突）=====
    addCart(data) {
      if(this.$store.state.userid !== 0){
        // 判断是否是自己的商品
        if(this.$store.state.userid === data.uid){
          this.$message({
            message: '不能购买自己发布的商品！',
            type: 'warning'
          });
          return null;
        }
        // 判断商品是否下架
        if(data.status === '2'){
          this.$message({
            message: '商品已出售！',
            type: 'warning'
          });
          return null;
        }
        this.$axios.post('/cart/selectCartProductById',{
          pid:data.id,
          uid:this.$store.state.userid
        }).then(res => {
          if(res.data === 0){
            // 添加商品进入购物车
            this.$axios.post('/cart/insertCartById',{
              pid:data.id,
              uid:this.$store.state.userid
            }).then(res => {
              if(res.data === 1){
                this.$message({
                  type: "success",
                  message: "商品添加成功！"
                });
              }
            })
          }else{
            this.$message({
              type: "warning",
              message: "请勿重复添加！"
            });
          }
        });
      }else{
        this.$message({
          type: "warning",
          message: "请登录！"
        });
      }
    },

    // ===== 新增：仿造分类页的联系卖家功能（避免冲突）=====
    showUserInfo(id) {
      this.$axios.get('/user/selectUserById',{
        params: { id:id }
      }).then(res => {
        this.$alert(
          '<div><span  style="font-weight:600;margin-left:30px;">用户名：</span>'+res.data.username+'</div>'+
          '<div><span  style="font-weight:600;margin-left:30px;">联系电话：</span>'+res.data.phonenumber+'</div>'+
          '<div><span  style="font-weight:600;margin-left:30px;">E-mail：</span>'+res.data.mail+'</div>',
          '卖家联系方式',
          { dangerouslyUseHTMLString: true }
        );
      });
    }
  }
}
</script>

<style scoped>
@import url("../css/index.css");

::v-deep * {
  font-family: -apple-system, BlinkMacSystemFont, "SF Pro Display",
    "Helvetica Neue", Arial, sans-serif;
}

.containerIndex {
  background: #f5f5f7;
  min-height: 100vh;
}

/* 公告栏样式 */
.notice-row {
  margin: 12px 0;
}
.notice-alert {
  border-radius: 16px !important;
  background: #ffffff !important;
  color: #1d1d1f;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: none;
}

/* 轮播+分类容器 */
.slide-row {
  margin-bottom: 30px;
}
.slide-container {
  border-radius: 20px;
  position: relative;
  width: 100%;
  height: 400px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  overflow: hidden;
}

/* 左侧分类侧边栏 */
.category-sidebar {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 10;
  width: 200px;
  height: calc(100% - 20px);
  margin: 10px;
  padding: 12px 0;
  backdrop-filter: blur(20px);
  list-style: none;
  border-radius: 20px;
  box-sizing: border-box;
}
.category-item {
  color: #1d1d1f;
  font-weight: 500;
  border-radius: 10px;
  margin: 4px 10px;
  padding: 10px 16px;
}
.category-item:hover {
  background: rgba(0, 0, 0, 0.05);
}
.category-item.active {
  background: #0071e3;
  color: #fff;
}

/* 轮播图样式 */
:deep(.el-carousel__item) {
  background-color: transparent !important;
}
:deep(.el-carousel__mask) {
  background-color: transparent !important;
}
.banner-img {
  width: 100%;
  height: 400px;
  border-radius: 20px;
  object-fit: contain;
  display: block;
}

/* 热门商品样式 */
.hot {
  margin: 20px 0;
}
.hot-product {
  background: #ffffff;
  border-radius: 16px;
  padding: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}
.hot-product ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.hot-product > ul > li {
  margin-bottom: 15px;
}
.hot-product > ul > li > p {
  font-size: 16px;
  font-weight: 600;
  background: #ffffff;
  border-radius: 16px;
  margin: 0 0 8px 0;
  padding: 12px;
  box-shadow: 0 4px 16px rgba(0,0,0,0.04);
}
.hot-product > ul > li > div {
  padding: 10px;
  background: #f8f9fa;
  border-radius: 4px;
}
.hot-product > ul > li > div > ul > li {
  padding: 6px 0;
  font-size: 14px;
  cursor: pointer;
  transition: color 0.2s;
}
.hot-product > ul > li > div > ul > li:hover {
  color: #409eff;
}

/* 商品板块通用样式 */
.section {
  width: 70%;
  margin: 40px auto;
}
.section-title {
  font-size: 22px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 20px;
}

/* ===== 仿造分类页的商品列表样式 ===== */
.typeProduct {
  margin: 0;
  color: #999;
  font-size: 12px;
}
.typeProduct ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.typeProduct li {
  width: calc(20% - 8px);
  margin: 10px 4px;
  border-radius: 10px;
  background-color: #FFFFFF;
  padding-bottom: 10px;
}
/* 商品卡片（融合首页hover效果+分类页样式） */
.containerProduct {
  display: flex;
  flex-direction: column;
  height: auto; /* 自动高度 */
}
.containerProduct:hover {
   transform: translateY(-6px);
   box-shadow: 0 12px 24px rgba(0,0,0,0.12);
}
/* 图片容器（保留收藏按钮） */
.imgbox {
   flex-shrink: 0; /* 图片固定高度 */
   height: 180px;
}
.imgbox img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.collect-btn:hover {
  background: #ff5500;
  color: #fff;
}
.collect-btn:disabled {
  cursor: not-allowed;
  background: #eee;
  color: #999;
}
/* 价格样式（完全仿造分类页） */
.productPrice {
  color: #FF5500;
  font-weight: 700;
  font-size: 16px;
  margin: 10px 12px 4px;
}
.productPrice .price {
  font-size: 16px !important;
}
/* 商品名称样式（完全仿造分类页） */
.productName {
  font-size: 14px;
  color: #333;
  margin: 0 12px 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
/* 操作栏样式（完全仿造分类页） */
.userPlay {
  display: flex;
  justify-content: space-between;
  align-items: center; /* 垂直居中按钮 */
  padding: 0 12px;
  margin-top: auto; /* 操作栏始终贴底 */
  height: auto;
}
.userPlay span {
 color: #999;
  font-size: 12px;
}
.userPlay button {
    float: right !important;
    font-size: 12px;
    color: #FFFFFF;
    background-color: #FF5500;
    margin: 0 10px;
    cursor: pointer;
}

/* 空数据提示 */
.empty-tip {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 14px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}
</style>
