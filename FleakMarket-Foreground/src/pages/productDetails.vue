<template>
  <div class="bodyd">
    <Search></Search>
    <el-row class="containerDetails">
      <el-col :span="12" :offset="6">
        <div class="pro">
          <!-- 商品统计模块 -->
          <div class="pdtime">
            <span>发布于 : {{data.creattime || '-'}}</span>
            <span style="margin-left: 20px;">浏览人次：{{data.clicks || 0}}次</span>
          </div>
          <!-- 商品区域 -->
          <el-row>
            <!-- 商品图片 -->
            <el-col :span="10">
              <div class="pimg"> <img :src="imgblur || ''" alt="商品图片"> </div>
            </el-col>
            <!-- 商品信息 -->
            <el-col :span="14" class="pdetails">
              <div>
                <div class="pdname">{{data.name || '暂无商品名称'}}</div>
                <div class="newProduct" @click="addLove(data.id)">
                  <i class="el-icon-star-off" v-if="love" style="color: red;"></i>
                  <i class="el-icon-star-off" v-else></i>
                </div>
              </div>
              <table class="pd">
                <tr> <td>价格</td>     <td class="pdprice">¥ {{data.currentprice || 0}}</td> </tr>
                <tr> <td>商品原价</td> <td style="color: #999999;">¥<s> {{data.originalprice || 0}}</s></td> </tr>
                <tr> <td>交易地址</td> <td>{{data.address || '暂无地址'}}</td> </tr>
                <tr> <td>商品成色</td> <td>{{data.condition || '暂无描述'}}</td> </tr>
                <tr> <td>商品状态</td> <td>{{data.status === '1'?"未出售":"已出售"}}</td> </tr>
                <tr> <td>交易方式</td> <td>{{data.deal || '暂无'}}</td> </tr>
                <tr> <td>卖家</td>     <td>{{data.username || '未知卖家'}}</td> </tr>
              </table>
              <div class="pdbutton" v-if="data.deal==='线上交易'">
                <el-button type="success" @click="buy" icon="el-icon-wallet" style="width: 140px;">立即购买</el-button>
                <el-button type="danger"  @click="addCart(data.id)" icon="el-icon-shopping-cart-full" style="width: 140px;">加入购物车</el-button>
              </div>
              <div class="pdbutton" v-if="data.deal==='线下交易'">
                <el-button type="success" @click="showUserInfo(data.uid)" icon="el-icon-wallet">查看卖家联系方式</el-button>
              </div>
            </el-col>
          </el-row>

          <!-- 买家区域 -->
          <el-row class="buyer">
            <el-col>商品描述</el-col>
          </el-row>
          <el-row class="buyer-describe">
            <p>{{data.details || '暂无商品描述'}}</p>
          </el-row>

          <!-- 评论统计模块 -->
          <div class="buyer">
            <span>{{totalCount}}条评论</span>
            <div class="pagesmall" @click="refresh"><i class="el-icon-refresh-right"></i>刷新</div>
          </div>

          <!-- 发布评论模块 -->
          <div class="leaveMessage" v-if="this.User !== ''">
            <div class="meimg"><img :src="User.userimgpath || ''" alt="用户头像"></div>
            <textarea :style="backcolor" maxlength="200" placeholder="请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。" @mouseenter="enter"
              @mouseleave="leave" v-model="textarea"></textarea>
            <div class="leaveMessageBtn">
              <button @click="insertComment(data.id)">提交</button>
            </div>
          </div>

          <!-- 评论模块 -->
          <ul style="min-height: 200px;">
            <li v-for="item in comments.slice((currentPage-1)*PageSize,currentPage*PageSize)" :key="item.id">
              <el-row class="buyer-describe">
                <div class="buyer-headerimg">
                  <img :src="item.userimgpath || ''" alt="评论用户头像" />
                </div>
                <div class="buyer-sayed">
                  <div class="user">{{item.username || '匿名用户'}}</div>
                  <div class="buyer-text">{{item.text}}</div>
                  <div class="buyer-date">{{item.time}}</div>
                </div>
              </el-row>
            </li>
            <!-- 评论统计模块 -->
            <li class="pagebig" v-if="comments.length > 0">
              <div class="buyer-headerimg"></div>
              <el-pagination background
                layout="total,prev, pager, next,jumper"
                :page-size="PageSize"
                prev-text=" 上一页 "
                next-text=" 下一页 "
                :total="totalCount"
                @current-change="CurrentChange"
              >
              </el-pagination>
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>
    <!-- 订单创建弹窗  -->
    <el-dialog
      title="创建订单"
      :visible.sync="outerVisible"
      :destroy-on-close="true"
      width="40%"
    >
      <div>
        <el-row class="buyPro">
          <el-col :span="4" :offset="2">
            <img :src="imgblur || ''" alt="商品图片">
          </el-col>
          <el-col :span="13" :offset="1">
            <p>{{data.name || '暂无商品名称'}}</p>
            <span>{{data.details || '暂无描述'}}</span>
          </el-col>
          <el-col :span="4">
            <p>售价：¥ {{data.currentprice || 0}}</p>
          </el-col>
        </el-row>
        <el-row class="buyPro">
          <el-col :span="20" :offset="1">
            <el-form ref="userbuy" :model="userbuy" :rules="rules" label-width="100px">
              <el-form-item label="收货人名称" prop="username">
                <el-input v-model="userbuy.username"></el-input>
              </el-form-item>
              <el-form-item label="联系方式" prop="phonenumber">
                <el-input v-model.number="userbuy.phonenumber"></el-input>
              </el-form-item>
              <el-form-item label="通知邮箱" prop="mail">
                <el-input v-model="userbuy.mail"></el-input>
              </el-form-item>
              <el-form-item label="收货地址" prop="address">
                <el-input v-model="userbuy.address"></el-input>
              </el-form-item>
              <el-form-item label="订单备注">
                <el-input v-model="remark" placeholder="选填,请先和卖家协商一致"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="createOrder">立即创建</el-button>
                <el-button @click="outerVisible = false">取消</el-button>
              </el-form-item>
            </el-form>
          </el-col>
        </el-row>
      </div>
      <!-- 支付订单弹窗 -->
      <el-dialog
        width="50%"
        title="订单支付"
        :visible.sync="innerVisible"
        :destroy-on-close="true"
        append-to-body
      >
        <el-row>
          <el-col :span="24"> 订单编号：<b>{{order.oid || '未知订单号'}}</b> </el-col>
        </el-row>
        <el-row class="orderInfo">
          <!-- 修复：用script里处理好的时间，避免模板调用moment -->
          <el-col :span="10"> 创建日期：<b>{{order.createtime || defaultTime}}</b> </el-col>
          <el-col :span="5"> 订单状态：{{Number(order.pay)===1?'未支付':'已支付'}} </el-col>
          <el-col :span="5"> 数量：{{order.productnumber || 1}} </el-col>
          <el-col :span="2" style="text-align: right;"> 总计： </el-col>
          <el-col :span="2"> ¥{{order.producttotal || data.currentprice || 0}} </el-col>
        </el-row>
        <div class="productInfo">
          <el-row v-for="item in order.products || [{
            id: data.id,
            images: data.images || [],
            name: data.name,
            details: data.details,
            currentprice: data.currentprice
          }]" :key="item.id || Math.random()">
            <el-col :span="3"> <img :src="item.images[0] || ''" alt="商品图片" /> </el-col>
            <el-col :span="5" class="infoTop"> <b>{{item.name || '暂无名称'}}</b> <br /> <span>{{item.details || '暂无描述'}}</span> </el-col>
            <el-col :span="8" class="infoTop"> <span>工作日、非工作日、全天时段均可配送预计发货后2-4个工作日送达</span> </el-col>
            <el-col :span="6" class="infoTop"> <p>¥{{item.currentprice || 0}}</p> </el-col>
          </el-row>
        </div>
        <el-row class="addressInfo">
          <el-col :span="3" :offset="1"> 配送地址 </el-col>
          <el-col :span="15">{{order.address || userbuy.address || '暂无地址'}}</el-col>
        </el-row>
        <el-row class="addressInfo">
          <el-col :span="3" :offset="1"> 收货人 </el-col>
          <el-col :span="15">{{order.consignee || userbuy.username || '暂无收货人'}}</el-col>
        </el-row>
        <el-row class="addressInfo">
          <el-col :span="3" :offset="1"> 联系方式 </el-col>
          <el-col :span="15">{{order.phone || userbuy.phonenumber || '暂无联系方式'}}</el-col>
        </el-row>
        <el-row class="btnClick">
          <el-col :span="3" :offset="9">
            <el-button type="primary" @click="gotoPay(order)">点击支付</el-button>
          </el-col>
          <el-col :span="3">
            <el-button @click="deletePay(order.oid)">取消订单</el-button>
          </el-col>
        </el-row>
      </el-dialog>
    </el-dialog>
    <FooterBottom></FooterBottom>
  </div>
</template>

<script type="text/javascript">
import Search from "../components/Search.vue";
import FooterBottom from "../components/Footer.vue";
import moment from "moment"; // 导入moment

export default {
  components: {
    Search,
    FooterBottom
  },
  data() {
    return {
      User: {}, // 修复：初始化为对象，避免字符串判断问题
      userbuy: {}, // 修复：初始化为对象
      data: {}, // 核心修复：从''改为{}，避免无法添加images属性
      imgblur: '',
      currentPage: 1,
      layout: 'slot,pager,next',
      textarea: '',
      backcolor: '',
      comments: [], // 修复：初始化为数组，避免length报错
      PageSize: 5,
      totalCount: 0, // 修复：初始化为0，避免空值
      love: false,
      outerVisible: false,
      innerVisible: false,
      remark: '',
      // 初始化订单对象
      order: {
        oid: '',
        createtime: '',
        pay: '1',
        productnumber: 1,
        producttotal: 0,
        address: '',
        consignee: '',
        phone: '',
        products: []
      },
      vendor: {},
      defaultTime: moment().format('YYYY-MM-DD HH:mm:ss'), // 定义默认时间，供模板使用
      rules: {
        username: [
          { required: true, message: '收货人姓名不能为空', trigger: 'blur' },
          { min: 1, max: 10, message: '长度在 1 到 10 个字符', trigger: 'blur' } // 修复：min从0改为1，避免空字符串通过验证
        ],
        phonenumber: [
          { required: true, message: '联系方式不能为空', trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' } // 增加手机号验证
        ],
        mail: [
          { required: true, message: '邮箱不能为空', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' } // 增加邮箱格式验证
        ],
        address: [
          { required: true, message: '收货地址不能为空', trigger: 'blur' },
          { min: 5, message: '地址长度不能少于5个字符', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    window.scrollTo(0, 0);
    // 请求用户信息
    this.$axios.get('/user/selectUserById', {
      params: { id: this.$store.state.userid }
    }).then(res => {
      this.User = res.data || {};
      this.userbuy = res.data || {}; // 兜底：返回空时赋值为空对象
    }).catch(err => {
      console.error('获取用户信息失败：', err);
      this.User = {};
      this.userbuy = {};
    });
    // 加载商品详情
    this.loadproductdetails();
    // 加载用户评论
    this.loadcomments();
    // 点击量+1（增加容错）
    setTimeout(() => {
      if (this.data.id) { // 确保商品ID存在再调用
        this.clickNum();
      }
    }, 500);
  },
  methods: {
    switchImg(imgPath) {
      this.imgblur = imgPath;
    },
    CurrentChange(val) {
      this.currentPage = val;
    },
    enter() {
      this.backcolor = "background:#FFFFFF";
    },
    leave() {
      this.backcolor = this.textarea.length === 0 ? "background:#f4f5f7" :
        "background:#FFFFFF;border: 1px solid #00a1d6;";
    },
    // 加载商品详情（增加容错）
    loadproductdetails() {
      this.$axios.get('/product/selectProductById', {
        params: { id: this.$route.query.id }
      }).then(res => {
        this.data = res.data || {}; // 兜底
        // 修复：解析images前先判断是否存在，避免eval报错
        if (this.data.images && typeof this.data.images === 'string') {
          try {
            // 替换eval为JSON.parse，更安全
            this.data.images = JSON.parse(this.data.images);
          } catch (e) {
            console.error('解析商品图片失败：', e);
            this.data.images = [];
          }
        } else {
          this.data.images = [];
        }
        this.imgblur = this.data.images[0] || '';
      }).catch(err => {
        console.error('加载商品详情失败：', err);
        this.data = {};
        this.imgblur = '';
      });
    },
    // 加载评论（增加容错）
    loadcomments() {
      this.$axios.get('/product/selectCommentById', {
        params: { id: this.$route.query.id }
      }).then(res => {
        this.comments = res.data || [];
        this.totalCount = this.comments.length;
        // 增加判断：评论数不为空再更新
        if (this.data.id) {
          this.updataProductData(this.totalCount);
        }
        this.selectLoveById();
      }).catch(err => {
        console.error('加载评论失败：', err);
        this.comments = [];
        this.totalCount = 0;
      });
    },
    // 更新商品评论数（增加容错，避免后端500）
    updataProductData(length) {
      if (!this.data.id) return; // 商品ID不存在则不请求
      this.$axios.post('/product/updateProductByUser', {
        id: this.data.id,
        comments: length.toString()
      }).catch(err => {
        console.error('更新评论数失败：', err);
        this.$message.warning('更新评论数失败，请稍后重试');
      });
    },
    // 判断商品是否收藏
    selectLoveById() {
      if (!this.data.id || !this.$store.state.userid) return;
      this.$axios.post('/product/selectLoveById', {
        uid: this.$store.state.userid,
        pid: [this.data.id]
      }).then(res => {
        this.love = res.data > 0;
      }).catch(err => {
        console.error('查询收藏状态失败：', err);
        this.love = false;
      });
    },
    // 添加收藏
    addLove(id) {
      if (this.$store.state.userid === 0) {
        this.$message.warning('请登录！');
        return;
      }
      if (this.$store.state.userid === this.data.uid) {
        this.$message.warning('不能收藏自己发布的商品！');
        return;
      }
      if (this.data.status === '2') {
        this.$message.warning('商品已出售！');
        return;
      }
      this.selectLoveById();
      if (!this.love) {
        this.$axios.post('/product/insertProductToLove', {
          uid: this.$store.state.userid,
          pid: id
        }).then(res => {
          if (res.data === 1) {
            this.$message.success('商品收藏成功！');
            this.love = true;
          }
        }).catch(err => {
          console.error('收藏商品失败：', err);
          this.$message.error('收藏失败，请稍后重试');
        });
      } else {
        this.$message.warning('请勿重复收藏！');
      }
    },
    // 展示卖家信息
    showUserInfo(id) {
      this.$axios.get('/user/selectUserById', { params: { id } })
        .then(res => {
          const data = res.data || {};
          this.$alert(
            `<div><span style="font-weight:600;margin-left:30px;">用户名：</span>${data.username || '未知'}</div>
             <div><span style="font-weight:600;margin-left:30px;">联系电话：</span>${data.phonenumber || '未知'}</div>
             <div><span style="font-weight:600;margin-left:30px;">E-mail：</span>${data.mail || '未知'}</div>`,
            '卖家联系方式',
            { dangerouslyUseHTMLString: true }
          );
        }).catch(err => {
          console.error('获取卖家信息失败：', err);
          this.$message.error('获取卖家信息失败');
        });
    },
    // 发布评论
    insertComment(id) {
      if (!this.textarea.trim()) {
        this.$message.error('内容不能为空！');
        return;
      }
      const date = moment().format('YYYY-MM-DD HH:mm:ss');
      this.$axios.post('/product/insertComment', {
        uid: this.$store.state.userid,
        pid: id,
        text: this.textarea.trim(),
        time: date
      }).then(res => {
        if (res.data === 1) {
          this.$message.success('发布成功！');
          this.textarea = '';
          this.leave();
          this.loadcomments();
        }
      }).catch(err => {
        console.error('发布评论失败：', err);
        this.$message.error('发布失败，请稍后重试');
      });
    },
    // 刷新评论
    refresh() {
      this.loadcomments();
      this.$message.success('刷新成功');
    },
    handleClose(done) {
      this.$confirm('确认关闭？')
        .then(_ => done())
        .catch(_ => { });
    },
    handleClose2(done) {
      this.$confirm('确认关闭？')
        .then(_ => done())
        .catch(_ => { });
    },
    // 立即购买
    buy() {
      if (this.$store.state.userid === 0) {
        this.$message.warning('请登录！');
        return;
      }
      if (this.data.status === '2') {
        this.$message.warning('商品已出售！');
        return;
      }
      if (this.$store.state.userid === this.data.uid) {
        this.$message.warning('不能购买自己发布的商品！');
        return;
      }
      this.outerVisible = true;
    },
    // 创建订单
    createOrder() {
      this.$refs.userbuy.validate((valid) => {
        if (!valid) return;

        this.outerVisible = false;
        const orderParams = {
          consignee: this.userbuy.username || '',
          phone: this.userbuy.phonenumber ? this.userbuy.phonenumber.toString() : '',
          mail: this.userbuy.mail || '',
          address: this.userbuy.address || '',
          remark: this.remark || '',
          cid: this.User.id ? Number(this.User.id) : (this.$store.state.userid || 0),
          createperson: this.User.username || '',
          createtime: this.defaultTime, // 使用预定义的时间
          productnumber: "1",
          pid: [Number(this.data.id) || 0],
          producttotal: this.data.currentprice ? this.data.currentprice.toString() : '0'
        };

        this.$axios.post('/order/insertOrderByOrder', orderParams)
          .then(res => {
            const resData = res.data || {};
            const oid = (resData.data && resData.data.oid) || resData.uuid || resData.data;

            if (oid) {
              this.innerVisible = true;
              this.selectOrderById(oid);
            } else {
              this.$message.error("订单创建成功，但未返回订单号！");
              this.outerVisible = true;
            }
          }).catch(err => {
            console.error('创建订单失败：', err);
            const errMsg = (err.response && err.response.data && err.response.data.msg) || '创建订单失败';
            this.$message.error(errMsg);
            this.outerVisible = true;
          });
      });
    },
    // 查询订单详情
    selectOrderById(oid) {
      if (!oid) {
        this.$message.error('订单编号不能为空！');
        return;
      }
      this.$axios.get('/order/selectOrderById', { params: { oid } })
        .then(res => {
          let orderData = res.data.data || {};

          // 解析商品图片（容错）
          if (orderData.products && Array.isArray(orderData.products)) {
            orderData.products.forEach(item => {
              if (item.images && typeof item.images === 'string') {
                try {
                  item.images = JSON.parse(item.images);
                } catch (e) {
                  item.images = [];
                }
              }
            });
          }

          // 兜底赋值
          this.order = {
            oid: orderData.oid || oid,
            createtime: orderData.createtime || this.defaultTime,
            pay: orderData.pay || '1',
            productnumber: orderData.productnumber || 1,
            producttotal: orderData.producttotal || this.data.currentprice,
            address: orderData.address || this.userbuy.address,
            consignee: orderData.consignee || this.userbuy.username,
            phone: orderData.phone || this.userbuy.phonenumber,
            products: orderData.products || [{
              id: this.data.id,
              images: this.data.images || [],
              name: this.data.name,
              details: this.data.details,
              currentprice: this.data.currentprice
            }]
          };
        }).catch(err => {
          console.error('查询订单失败：', err);
          // 完全兜底
          this.order = {
            oid: oid,
            createtime: this.defaultTime,
            pay: '1',
            productnumber: 1,
            producttotal: this.data.currentprice || 0,
            address: this.userbuy.address || '',
            consignee: this.userbuy.username || '',
            phone: this.userbuy.phonenumber || '',
            products: [{
              id: this.data.id || 0,
              images: this.data.images || [],
              name: this.data.name || '暂无名称',
              details: this.data.details || '暂无描述',
              currentprice: this.data.currentprice || 0
            }]
          };
          this.$message.warning("查询订单详情失败，显示基础订单信息");
        });
    },
    // 支付订单
    gotoPay(order) {
      if (!order || !order.oid) {
        this.$message.error('订单信息不完整，无法支付！');
        return;
      }

      this.$confirm('是否确定付款？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.get(`/order/pay/${order.oid}`)
          .then(res => {
            const payHtml = (res.data && res.data.data) ? res.data.data : res.data;
            if (payHtml) {
              document.write(payHtml);
            } else {
              this.$message.error('支付表单生成失败！');
            }
          }).catch(err => {
            console.error('支付请求失败：', err);
            this.$message.error(`支付失败：${err.message}`);
          });
      }).catch(() => {
        this.$message.info('已取消付款');
      });
    },
    // 删除订单
    deletePay(oid) {
      if (!oid) {
        this.$message.error('订单编号不能为空！');
        return;
      }
      this.$confirm('是否取消订单？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$axios.post("/order/deleteOrderByOid", { oid })
          .then(res => {
            if (res.data === 1) {
              this.$message.success('订单已取消!');
              this.innerVisible = false;
            }
          }).catch(err => {
            console.error('取消订单失败：', err);
            this.$message.error('取消订单失败，请稍后重试');
          });
      });
    },
    // 添加购物车
    addCart(pid) {
      if (this.$store.state.userid === 0) {
        this.$message.warning("请登录！");
        return;
      }
      if (this.$store.state.userid === this.data.uid) {
        this.$message.warning("不能购买自己发布的商品！");
        return;
      }
      if (this.data.status === '2') {
        this.$message.warning("商品已出售！");
        return;
      }
      this.$axios.post('/cart/selectCartProductById', {
        pid,
        uid: this.$store.state.userid
      }).then(res => {
        if (res.data === 0) {
          this.$axios.post('/cart/insertCartById', { pid, uid: this.$store.state.userid })
            .then(res => {
              if (res.data === 1) {
                this.$message.success("商品添加成功！");
              }
            }).catch(err => {
              console.error('添加购物车失败：', err);
              this.$message.error('添加失败，请稍后重试');
            });
        } else {
          this.$message.warning("请勿重复添加！");
        }
      }).catch(err => {
        console.error('查询购物车失败：', err);
        this.$message.error('操作失败，请稍后重试');
      });
    },
    // 统计点击量
    clickNum() {
      if (!this.data.id) return;
      // 更新日期统计
      this.$axios.post('/utils/selectDateFromStatis', {
        dates: moment().format('YYYY-MM-DD')
      }).then(res => {
        if (res.data.length === 0) {
          this.$axios.post('/utils/insertDateInStatis', { dates: moment().format('YYYY-MM-DD') });
        } else {
          this.$axios.post('/utils/updateNumInStatis', {
            id: res.data.id,
            visitNum: res.data.visitNum,
            clickNum: (res.data.clickNum + 1)
          });
        }
      }).catch(err => {
        console.error('更新点击量统计失败：', err);
      });
      // 更新商品点击量
      this.$axios.post('/product/updateProductByUser', {
        id: this.data.id,
        clicks: (this.data.clicks || 0) + 1 // 兜底：clicks为空时取0
      }).catch(err => {
        console.error('更新商品点击量失败：', err);
      });
    }
  }
}
</script>

<style scoped="scoped">
/* 原有样式不变，这里省略（和之前一致） */
.containerDetails {
  margin-top: -19px;
  margin-bottom: 20px;
  background-color: #FFFFFF;
}

.logo {
  height: 80px;
}

.logo img {
  height: 100%;
}

.seek div {
  height: 40px;
  display: flex;
  margin-top: 20px;
  background-color: #FCFCFC;
  border: 3px solid rgba(255, 153, 0);
}

.seek div input {
  flex: 1;
  border: 0;
  padding: 0 10px;
  outline: none;
}

.seek div button {
  width: 80px;
  border: 0;
  outline: none;
  color: #FFFFFF;
  font-size: 17px;
  background-color: rgba(255, 153, 0);
}

.pro {
  width: 100%;
  padding: 10px 20px;
  margin-top: 15px;
  margin-bottom: 10px;
  border: 1px solid #EEEEEE;
  border-radius: 10px 10px 10px 10px;
}

.pdtime {
  font-size: 13px;
  color: #CCCCCC;
  line-height: 40px;
  margin-bottom: 15px;
  border-bottom: 1px solid #E8E8E8;
}

.pimg {
  width: 100%;
  height: 400px;
  margin: auto;
  border: 1px solid #eaeaea;
}

.pimg img {
  width: 100%;
}

.pimgs {
  width: 100%;
  margin: auto;
}

.pimgs ul li {
  float: left;
  width: 20%;
  height: 80px;
  cursor: pointer;
  padding: 5px 0;
  margin-top: 10px;
  border-top: 1px solid #E9E9E9;
  border-bottom: 1px solid #E9E9E9;
  text-align-last: center;
}

.pimgs ul li img {
  width: 95%;
  border-radius: 2px;
  transition: all 0.2s;
}

.pimgs ul li img:hover {
  width: 100%;
}

.pdetails {
  padding-left: 80px;
}

.pdname {
  font-size: 30px;
  max-height: 80px;
  width: 80%;
  color: #333333;
  margin: 20px 0;
  float: left;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.newProduct {
  float: right;
  font-size: 25px;
  margin-top: 30px;
  margin-right: 20px;
  padding: 2px;
  text-align-last: center;
  cursor: pointer;
  width: 13%;
}

.pd {
  clear: left;
}

.pd td {
  height: 40px;
  color: #333333;
}

.pd tr td:nth-of-type(1) {
  width: 100px;
  color: #999;
  font-size: 13px;
}

.pdprice {
  color: #FF0036 !important;
  font-size: 25px;
}

.pdbutton {
  width: 100%;
  padding-top: 20px;
  text-align: center;
  display: flex;
}

.pdbutton button {
  margin: 20px;
}

.buyer {
  margin-top: 10px;
  font-size: 18px;
  color: #222;
  line-height: 50px;
  margin-top: 20px;
  border-bottom: 1px solid #e5e9ef;
}

.pagesmall {
  float: right;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  margin-right: 10px;
  margin-top: 10px;
}

.pagesmall:hover {
  color: #00A1D6;
}

.pagebig {
  margin-top: 15px;
  margin-bottom: 10px;
}

.buyer-describe p {
  text-indent: 2em;
  padding: 0 15px;
  margin-bottom: 20px;
  height: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 30px;
}

.buyer-describe {
  min-height: 130px;
}

.buyer-headerimg {
  width: 15%;
  float: left;
  color: #333333;
  padding-top: 20px;
  text-align: center;
}

.buyer-headerimg img {
  height: 70px;
  width: 70px;
  border-radius: 50%;
  margin: 10px;
}

.buyer-sayed {
  width: 80%;
  padding-right: 5%;
  height: 100%;
  line-height: 25px;
  float: right;
  border-bottom: 1px solid #E0E0E0;
  word-wrap: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
}

.buyer-sayed .user {
  margin-top: 20px;
  color: #6d757a;
  cursor: pointer;
}

.buyer-sayed .buyer-text {
  min-height: 60px;
}

.buyer-sayed .buyer-date {
  font-size: 13px;
  color: #99a2aa;
}

.leaveMessage {
  width: 100%;
  height: 200px;
  padding-bottom: 20px;
  border-bottom: 1px solid #EEEEEE;
}

.leaveMessage .meimg {
  height: 100%;
  width: 15%;
  float: left;
}

.meimg img {
  height: 60px;
  width: 60px;
  float: left;
  margin-left: 40px;
  margin-top: 70px;
  border-radius: 50%;
}

.leaveMessage textarea {
  width: 80%;
  height: 100px;
  padding: 10px;
  font-size: 13px;
  color: #555;
  border-radius: 10px;
  margin-top: 30px;
  outline: none;
  resize: none;
  background-color: #f4f5f7;
  border: 1px solid #e5e9ef;
}

.leaveMessage textarea:hover {
  background-color: #FFFFFF;
  border: 1px solid #00a1d6;
}

.leaveMessageBtn {
  width: 83%;
  margin-left: 15%;
}

.leaveMessageBtn button {
  width: 70px;
  height: 30px;
  float: right;
  margin-top: 5px;
  color: #fff;
  outline: none;
  cursor: pointer;
  border-radius: 4px;
  margin-right: 10px;
  border: 1px solid #00a1d6;
  background-color: #00a1d6;
}

.time {
  font-size: 13px;
  color: #999;
}

.bottom {
  margin-top: 13px;
  line-height: 12px;
}

.button {
  padding: 0;
  float: right;
}

.image {
  width: 100%;
  display: block;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both
}

.ListOfUserGoods {
  margin-top: 15px;
  margin-left: 25px;
  font-size: 14px;
  color: #888;
  background-color: #FFFFFF;
  border-radius: 8px;
  padding: 4px 10px;
}

.ListOfUserGoods li {
  margin-top: 20px;
}

.buyPro {
  padding: 15px 0;
  margin-bottom: 10px;
  border: 1px solid #eeeeee;
}

.buyPro img {
  height: 120px;
  width: 120px;
}

.buyPro p {
  margin-top: 0;
  font-size: 18px;
}

.orderInfo {
  padding: 15px 0;
  border-bottom: 1px solid #EEEEEE;
}

.productInfo {
  margin-top: 10px;
  padding: 10px 5px;
  height: 250px;
  overflow: auto;
}

.productInfo img {
  height: 100px;
  width: 100px;
  margin: 5px 10px;
}

.productInfo .el-row {
  padding-top: 10px;
  border-bottom: 1px solid #E0E0E0;
}

.infoTop {
  margin: 10px;
}

.infoTop p {
  text-align: right;
}

.addressInfo {
  height: 40px;
  margin-top: 20px;
  border-bottom: 1px solid #EEEEEE;
}

.btnClick {
  margin-top: 20px;
}
</style>
