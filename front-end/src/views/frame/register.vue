<template>
  <div class="register-view">
    <div class="register-box">
      <n-gradient-text class="register-title" :size="26" type="success">
        颜一简易管理系统注册
      </n-gradient-text>
      <n-form
          ref="formRef"
          :model="registerInfo"
          :rules="rules"
          label-placement="left">
        <n-form-item path="userName" label="用户名">
          <n-input v-model:value="registerInfo.userName" placeholder="请输入用户名">
            <template #suffix>
              <component :is="CIcon" :icon="'Accessibility'" color="rgb(224,224,230)"></component>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="nickName" label="昵称">
          <n-input v-model:value="registerInfo.nickName" placeholder="请输入昵称">
            <template #suffix>
              <component :is="CIcon" :icon="'Person'" color="rgb(224,224,230)"></component>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="password" label="密码">
          <n-input
              v-model:value="registerInfo.password"
              type="password"
              show-password-on="mousedown"
              placeholder="请输入密码"
              :maxlength="20"
          />
          <template #feedback>
            <div class="password-requirements">
              <div class="requirement-title">密码必须满足以下条件：</div>
              <div class="requirement-item" :class="{ 'satisfied': passwordStrength.length }">
                <n-icon :color="passwordStrength.length ? '#18a058' : '#d03050'" :size="14">
                  <component :is="CIcon" :icon="passwordStrength.length ? 'CheckmarkCircle' : 'CloseCircle'"></component>
                </n-icon>
                长度在8-20位之间
              </div>
              <div class="requirement-item" :class="{ 'satisfied': passwordStrength.hasNumber }">
                <n-icon :color="passwordStrength.hasNumber ? '#18a058' : '#d03050'" :size="14">
                  <component :is="CIcon" :icon="passwordStrength.hasNumber ? 'CheckmarkCircle' : 'CloseCircle'"></component>
                </n-icon>
                包含至少一个数字
              </div>
              <div class="requirement-item" :class="{ 'satisfied': passwordStrength.hasLowercase }">
                <n-icon :color="passwordStrength.hasLowercase ? '#18a058' : '#d03050'" :size="14">
                  <component :is="CIcon" :icon="passwordStrength.hasLowercase ? 'CheckmarkCircle' : 'CloseCircle'"></component>
                </n-icon>
                包含至少一个小写字母
              </div>
              <div class="requirement-item" :class="{ 'satisfied': passwordStrength.hasUppercase }">
                <n-icon :color="passwordStrength.hasUppercase ? '#18a058' : '#d03050'" :size="14">
                  <component :is="CIcon" :icon="passwordStrength.hasUppercase ? 'CheckmarkCircle' : 'CloseCircle'"></component>
                </n-icon>
                包含至少一个大写字母
              </div>
              <div class="requirement-item" :class="{ 'satisfied': passwordStrength.hasSpecial }">
                <n-icon :color="passwordStrength.hasSpecial ? '#18a058' : '#d03050'" :size="14">
                  <component :is="CIcon" :icon="passwordStrength.hasSpecial ? 'CheckmarkCircle' : 'CloseCircle'"></component>
                </n-icon>
                包含至少一个特殊字符（如：!@#$%^&*）
              </div>
            </div>
          </template>
        </n-form-item>
        <n-form-item path="confirmPassword" label="确认密码">
          <n-input
              v-model:value="registerInfo.confirmPassword"
              type="password"
              show-password-on="mousedown"
              placeholder="请再次输入密码"
              :maxlength="18"
          />
        </n-form-item>
        <n-form-item path="email" label="邮箱">
          <n-input v-model:value="registerInfo.email" placeholder="请输入邮箱">
            <template #suffix>
              <component :is="CIcon" :icon="'Mail'" color="rgb(224,224,230)"></component>
            </template>
          </n-input>
        </n-form-item>
        <n-form-item path="phone" label="手机号">
          <n-input v-model:value="registerInfo.phone" placeholder="请输入手机号">
            <template #suffix>
              <component :is="CIcon" :icon="'Call'" color="rgb(224,224,230)"></component>
            </template>
          </n-input>
        </n-form-item>

        <n-form-item v-if="!!registerInfo.code">
          <n-grid :cols="12" :x-gap="12">
            <n-form-item-gi :span="7">
              <n-input v-model:value="registerInfo.captcha" placeholder="验证码">
                <template #suffix>
                  <component :is="CIcon" :icon="'QrCode'" color="rgb(224,224,230)"></component>
                </template>
              </n-input>
            </n-form-item-gi>
            <n-form-item-gi :span="5">
              <img :src="codeUrl" @click="getCaptchaCode" alt="验证码一枚"/>
            </n-form-item-gi>
          </n-grid>
        </n-form-item>

        <n-form-item>
          <n-button
              :disabled="!registerInfo.userName || !registerInfo.password || !registerInfo.confirmPassword || (!registerInfo.captcha && !!registerInfo.code)"
              @click="handleRegister"
              class="register-button" type="success">
            注册
          </n-button>
        </n-form-item>
        
        <n-form-item class="login-link">
          <n-button text @click="goToLogin">
            已有账号？返回登录
          </n-button>
        </n-form-item>
      </n-form>
    </div>
  </div>
</template>

<script setup>
import CIcon from '@/components/icon/index.vue'
import {onMounted, ref, computed} from 'vue'
import {useRouter} from 'vue-router'
import {captchaImage} from '@/api/system/login'
import {registerUser} from '@/api/system/register'

/** 初始化相关数据 **/
onMounted(() => {
  getCaptchaCode()
  setTimeout(() => {
    registerInfo.value.userName = ''
    registerInfo.value.password = ''
    registerInfo.value.confirmPassword = ''
  }, 100)
})

const formRef = ref(null)
const router = useRouter()

// 密码强度检测
const passwordStrength = computed(() => {
  const password = registerInfo.value.password || '';
  return {
    length: password.length >= 8 && password.length <= 20,
    hasNumber: /.*\d+.*/.test(password),
    hasLowercase: /.*[a-z]+.*/.test(password),
    hasUppercase: /.*[A-Z]+.*/.test(password),
    hasSpecial: /.*[!@#$%^&*()_+\-=\[\]{};':"\\\|,.<>\/?]+.*/.test(password)
  };
});

// 检查密码是否有效
const isPasswordValid = computed(() => {
  const strength = passwordStrength.value;
  return strength.length && strength.hasNumber && strength.hasLowercase && 
         strength.hasUppercase && strength.hasSpecial;
});

// 表单验证规则
const rules = {
  userName: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' }
  ],
  nickName: [
    { required: true, message: '请输入昵称', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { 
      validator: (rule, value) => {
        if (!value) return true;
        const strength = passwordStrength.value;
        if (!strength.length) {
          return new Error('密码长度必须在8-20位之间');
        }
        if (!strength.hasNumber) {
          return new Error('密码必须包含至少一个数字');
        }
        if (!strength.hasLowercase) {
          return new Error('密码必须包含至少一个小写字母');
        }
        if (!strength.hasUppercase) {
          return new Error('密码必须包含至少一个大写字母');
        }
        if (!strength.hasSpecial) {
          return new Error('密码必须包含至少一个特殊字符');
        }
        return true;
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (rule, value) => {
        return value === registerInfo.value.password
      },
      message: '两次输入的密码不一致',
      trigger: 'blur'
    }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 注册信息
const registerInfo = ref({
  userName: '',
  nickName: '',
  password: '',
  confirmPassword: '',
  email: '',
  phone: '',
  captcha: '',
  code: ''
})

// 处理注册
const handleRegister = (e) => {
  e.preventDefault()
  formRef.value?.validate((errors) => {
    if (!errors) {
      // 验证通过，提交注册
      const registerData = {
        userName: registerInfo.value.userName,
        nickName: registerInfo.value.nickName,
        password: registerInfo.value.password,
        email: registerInfo.value.email,
        phone: registerInfo.value.phone,
        captcha: registerInfo.value.captcha,
        code: registerInfo.value.code
      }
      
      registerUser(registerData).then(res => {
        window.$message.success('注册成功，即将跳转到登录页面')
        // 使用setTimeout给用户一个视觉反馈的时间
        setTimeout(() => {
          router.replace('/login')
        }, 1500)
      }).catch(() => {
        // 注册失败处理
        getCaptchaCode()
        registerInfo.value.captcha = ''
      })
    } else {
      window.$message.error('请完善表单信息')
    }
  })
}

// 跳转到登录页面
const goToLogin = () => {
  router.push('/login')
}

/** 验证码 **/
const codeUrl = ref(null)
const getCaptchaCode = () => {
  captchaImage().then(res => {
    if (res.data.code) {
      codeUrl.value = 'data:image/gif;base64,' + res.data.image
      registerInfo.value.code = res.data.code
    }
  })
}
</script>

<style scoped lang="scss">
.register-view {
  height: 100%;
  background-image: url("../../assets/image/login-bg.jpeg");
  background-size: cover;
  position: relative;
  display: flex;

  .register-box {
    background-color: rgba(255, 255, 255, 0.6);
    width: 400px;
    padding: 38px;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    margin: auto;

    .register-title {
      margin-bottom: 28px;
      font-weight: bold;
      text-align: center;
    }

    .register-button {
      width: 100%;
    }
    
    .login-link {
      text-align: center;
      margin-top: 10px;
    }
    
    .password-requirements {
      margin-top: 8px;
      font-size: 12px;
      
      .requirement-title {
        font-weight: bold;
        margin-bottom: 5px;
        color: #606266;
      }
      
      .requirement-item {
        display: flex;
        align-items: center;
        margin-bottom: 3px;
        color: #909399;
        
        .n-icon {
          margin-right: 5px;
        }
        
        &.satisfied {
          color: #18a058;
        }
      }
    }
  }
}
</style> 