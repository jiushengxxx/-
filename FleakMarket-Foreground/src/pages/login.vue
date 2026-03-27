<template>
  <div class="container">
    <div class="view">
    <h2 class="title">欢迎使用</h2>
    <p class="sub">校园二手交易平台</p>
      <el-tabs v-model="activeName" :stretch="true" tab-position="top">
        <el-tab-pane label="登录" name="login">
          <el-form class="Lform" :model="loginForm" :rules="rules" ref="loginForm" label-width="100px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" v-model="loginForm.password" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item> <!-- 登录按钮所在的form-item -->
              <el-button type="primary" @click="toLogin('loginForm')">立即登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form class="Rform" :model="registerForm" :rules="rules" ref="registerForm" label-width="100px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input type="password" v-model="registerForm.password" placeholder="请输入密码"></el-input>
            </el-form-item>
            <el-form-item label="性别" prop="sex">
              <el-radio-group v-model="registerForm.sex">
                <el-radio label="男"></el-radio>
                <el-radio label="女"></el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker type="date" format="yyyy 年 MM 月 dd 日" value-format="yyyy-MM-dd" placeholder="请选择出生日期" v-model="registerForm.birthday"></el-date-picker>
            </el-form-item>
            <el-form-item label="联系方式" prop="phonenumber">
              <el-input v-model="registerForm.phonenumber" placeholder="请输入联系方式"></el-input>
            </el-form-item>
            <el-form-item label="现居地" prop="address">
              <el-input v-model="registerForm.address" placeholder="请输入现居地址"></el-input>
            </el-form-item>
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="registerForm.studentNo" placeholder="请输入学号"></el-input>
            </el-form-item>
            <el-form-item label="邮箱" prop="mail">
              <el-input v-model="registerForm.mail" placeholder="请输入邮箱"></el-input>
            </el-form-item>

            <el-form-item class="verify" label="验证码">
              <el-input v-model="registerForm.verifyCode" placeholder="请输入验证码"></el-input>
              <el-button type="primary" @click="sendMessage('registerForm')" :disabled="disabledBoolean" round>{{sendText}}</el-button>
            </el-form-item>

            <el-form-item class="relo">
              <el-button type="primary" @click="submitForm('registerForm')">注册并登录</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import moment from "moment";

export default {
  data() {
    let validatePhone = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入联系方式'));
      } else if (!(/^1(3|4|5|6|7|8|9)\d{9}$/.test(value))) {
        callback(new Error('手机号码格式出错，请重新输入！'));
      } else {
        callback();
      }
    };

    let validateMail = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入邮箱地址'));
      } else if (!(/^[A-Za-z\d]+([-_.][A-Za-z\d]+)*@([A-Za-z\d]+[-.])+[A-Za-z\d]{2,4}$/).test(value)) {
        callback(new Error('邮箱格式出错，请重新输入！'))
      } else {
        callback();
      }
    };

    let validateStudentNo = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入学号'));
      } else if (!(/^\d{8,12}$/.test(value))) {
        callback(new Error('学号格式出错，请输入8-12位数字！'));
      } else {
        callback();
      }
    };

    return {
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        password: '',
        sex: '',
        birthday: '',
        phonenumber: '',
        address: '',
        studentNo: '',
        mail: '',
        verifyCode: ''
      },
      activeName: this.$route.query.name || 'login',
      verifyCode: '',
      disabledBoolean: false,
      sendText: '获取验证码',
      rules: {
        username: [{
            required: true,
            message: '请输入用户名',
            trigger: 'blur'
          },
          {
            min: 3,
            max: 8,
            message: '长度在 3 到 8 个字符',
            trigger: 'blur'
          }
        ],
        password: [{
            required: true,
            message: '请输入密码',
            trigger: 'change'
          },
          {
            min: 3,
            max: 8,
            message: '长度在 3 到 8 个字符',
            trigger: 'blur'
          }
        ],
        sex: [{
          required: true,
          message: '请选择性别',
          trigger: 'change'
        }],
        birthday: [{
          required: true,
          message: '请选择生日',
          trigger: 'change'
        }],
        phonenumber: [{
          required: true,
          trigger: 'blur',
          validator: validatePhone
        }],
        studentNo: [{
          required: true,
          trigger: 'blur',
          validator: validateStudentNo
        }],
        mail: [{
          required: true,
          trigger: 'blur',
          validator: validateMail
        }],
        address: [{
          required: true,
          message: '请填写现居地址',
          trigger: 'blur'
        }]
      }
    }
  },
  methods: {
    toLogin(loginForm) {
      this.$refs[loginForm].validate((valid) => {
        if (valid) {
          const loading = this.$loading({
            lock: true,
            text: '正在登录...',
            spinner: 'el-icon-loading',
            background: 'rgba(0, 0, 0, 0.7)'
          });
          this.$axios.get('/user/toLogin', {
            params: {
              username: this.loginForm.username,
              password: this.loginForm.password
            }
          }).then(res => {
            loading.close();
            if (res.data != '') {
              this.clickNum();
              this.$store.commit('userInfo', res.data.userid);
              this.$router.push('/').then(() => {
                // 兜底：确保首页滚动正常
                document.body.style.overflow = 'auto';
                document.documentElement.style.overflow = 'auto';
              });
            } else {
              this.$message.error('用户名或密码错误！');
            }
          }).catch(err => {
            loading.close();
            this.$message.error('登录请求失败，请重试！');
            console.error('登录失败：', err);
          });
        }
      });
    },
    submitForm(registerForm) {
      this.$refs[registerForm].validate((valid) => {
        if (valid) {
          if (this.verifyCode == this.registerForm.verifyCode) {
            this.$axios.get('/user/toLogin', {
              params: {
                username: this.registerForm.username
              }
            }).then(res => {
              if (res.data != '') {
                this.$message.error('用户名已注册！');
              } else {
                this.$axios.post('/user/insertUserInfo', {
                  username: this.registerForm.username,
                  phonenumber: this.registerForm.phonenumber,
                  mail: this.registerForm.mail,
                  birthday: this.registerForm.birthday,
                  address: this.registerForm.address,
                  sex: this.registerForm.sex,
                  studentNo: this.registerForm.studentNo
                }).then(res => {
                  this.$axios.post('/user/insertUserToAccount', {
                    userid: res.data,
                    username: this.registerForm.username,
                    password: this.registerForm.password
                  }).then(res => {
                    if (res.data == 1) {
                      this.$message.success('注册成功，请登录！');
                      this.activeName = 'login';
                      this.$refs[registerForm].resetFields();
                    }
                  }).catch(err => {
                    this.$message.error('账户创建失败，请重试！');
                    console.error('注册失败：', err);
                  });
                }).catch(err => {
                  this.$message.error('用户信息保存失败，请重试！');
                  console.error('注册失败：', err);
                });
              }
            }).catch(err => {
              this.$message.error('用户名校验失败，请重试！');
              console.error('注册失败：', err);
            });
          } else {
            this.$message.warning('验证码错误，请重新输入！');
          }
        }
      });
    },
    sendMessage(registerForm) {
      this.$refs[registerForm].validate((valid) => {
        if (valid) {
          if (this.disabledBoolean) {
            return;
          } else {
            this.verifyCode = '';
            this.$message.warning('验证码已发送，请稍等！');
          }
          this.getVerifyCode();
          this.wite(60);
        }
      });
    },
    getVerifyCode() {
      this.$axios.get('/email/sendMail', {
        params: {
          mailTo: this.registerForm.mail
        }
      }).then(res => {
        this.verifyCode = res.data;
      }).catch(err => {
        this.$message.error('验证码发送失败，请检查邮箱！');
        console.error('验证码发送失败：', err);
        this.disabledBoolean = false;
        this.sendText = '获取验证码';
      });
    },
    wite(wait) {
      if (wait == 0) {
        this.disabledBoolean = false;
        this.sendText = "获取验证码"
      } else {
        this.disabledBoolean = true;
        this.sendText = "验证码(" + wait + "s)"
        wait--;
        setTimeout(() => {
          this.wite(wait);
        }, 1000);
      }
    },
    clickNum() {
      this.$axios.post('/utils/selectDateFromStatis', {
        dates: moment().format('YYYY-MM-DD')
      }).then(res => {
        if (res.data.length == 0) {
          this.$axios.post('/utils/insertDateInStatis', {
            dates: moment().format('YYYY-MM-DD'),
          });
        } else {
          this.$axios.post('/utils/updateNumInStatis', {
            id: res.data.id,
            visitNum: res.data.visitNum + 1
          });
        }
      }).catch(err => {
        console.error('统计点击量失败：', err);
      });
    }
  }
}
</script>

<style scoped>
/* 基础重置（只作用于当前组件） */
::v-deep * {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
}

/* 登录页容器 - 企业级写法，背景完全铺满 */
.container {
  width: 100%;
  min-height: 100vh;

  /* 苹果风渐变背景 */
  background: linear-gradient(180deg, #f5f5f7 0%, #eaeaea 100%);

  display: flex;
  justify-content: center;
  align-items: center;
}

/* 表单容器 - 加淡入动画，体验更好 */
.view {

  width: 420px;
  padding: 40px 36px;

  background: rgba(255,255,255,0.85);
  backdrop-filter: blur(20px);

  border-radius: 28px;

  box-shadow:
    0 10px 40px rgba(0,0,0,0.08),
    0 2px 8px rgba(0,0,0,0.04);

  border: 1px solid rgba(255,255,255,0.6);

  transition: all 0.3s ease;
}

/* 淡入动画定义 */
@keyframes fadeIn {
  from {opacity: 0; transform: translateY(20px);}
  to {opacity: 1; transform: translateY(0);}
}

/* 标签页样式 */
.view .el-tabs {
  margin-bottom: 24px;
}
.view .el-tabs__header {
  margin: 0;
  border-bottom: none;
}
.view .el-tabs__nav {
  justify-content: center;
}
.view .el-tabs__item {
  font-size: 15px;
  color: #6e6e73;
}
.view .el-tabs__item.is-active {
  color: #1d1d1f;
  font-weight: 600;
}
.view .el-tabs__active-bar {
  background-color: #0071e3;
  height: 3px;
  border-radius: 3px;
}

/* 标签与输入框间距 */
.view .el-form-item {
  width: 100%;
  padding: 0;
  margin-bottom: 12px !important;
  display: flex;
  align-items: center;
}
.view .el-form-item__label {
  display: block !important;
  width: 80px !important;
  text-align: right !important;
  padding-right: 8px !important;
  font-size: 15px !important;
  color: #1d1d1f !important;
  font-weight: 400 !important;
}
.view .el-form-item__content {
  flex: 1 !important;
  margin-left: 0 !important;
}

/* 输入框样式 */
::v-deep .el-form-item .el-input {
  width: 100%;
}
::v-deep .el-form-item .el-input__wrapper {
  background-color: #f5f5f7;
  border-radius: 16px;
  border: 1px solid transparent;

  height: 44px;

  transition: all 0.25s ease;
}
::v-deep .el-form-item .el-input__inner {
  background-color: transparent;
  border: none;
  font-size: 15px !important;
  color: #1d1d1f;
  height: 42px !important;
  line-height: 42px !important;
  padding: 0;
}
::v-deep .el-form-item .el-input__wrapper.is-focus {
  border: 1px solid #0071e3;
  box-shadow: 0 0 0 4px rgba(0,113,227,0.15);
}

/* 单选框样式 */
::v-deep .el-radio-group {
  width: 100%;
  display: flex;
  justify-content: flex-start;
  gap: 24px;
  background-color: transparent !important;
  padding: 0 !important;
}
::v-deep .el-radio {
  font-size: 15px;
  color: #1d1d1f;
}
::v-deep .el-radio__input.is-checked .el-radio__inner {
  background-color: #0071e3;
  border-color: #0071e3;
}

/* 🔥 登录按钮居中 - 企业级正确写法（不破坏Element UI原有布局） */
::v-deep .Lform .el-form-item:last-child .el-form-item__content {
  text-align: center; /* 核心：让按钮容器居中 */
}
::v-deep .Lform .el-button {
  width: 100%;
  height: 44px;

  border-radius: 22px;

  background: linear-gradient(180deg, #0a84ff, #0071e3);
  border: none;

  font-weight: 500;
  font-size: 15px;

  transition: all 0.25s ease;
}
::v-deep .Lform .el-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(0,113,227,0.25);
}
::v-deep .Lform .el-button:active {
  transform: scale(0.97);
}

/* 验证码输入框与按钮 */
::v-deep .Rform .verify {
  display: flex;
  align-items: center;
  gap: 16px !important;
}
::v-deep .Rform .verify .el-input {
  flex: 1;
}
::v-deep .Rform .verify .el-button {
  width: auto;
  padding: 0 16px;
  height: 42px !important;
  border-radius: 21px;
  background-color: #0071e3;
  border: none;
  font-size: 14px;
}

/* 注册按钮居中（同登录按钮逻辑） */
::v-deep .Rform .relo .el-form-item__content {
  text-align: center;
}
::v-deep .Rform .relo .el-button {
  width: 80% !important;
  height: 44px !important;
  font-size: 16px;
  border-radius: 22px;
  background-color: #0071e3;
  border: none;
  font-weight: 500;
}

/* 响应式适配 */
@media (max-width: 520px) {
  .view {
    width: 90vw;
    padding: 20px 16px;
  }
  .view .el-form-item__label {
  color: #6e6e73 !important;
  font-size: 13px !important;
  }
}
.title {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 6px;
}

.sub {
  text-align: center;
  font-size: 13px;
  color: #6e6e73;
  margin-bottom: 20px;
}
</style>
