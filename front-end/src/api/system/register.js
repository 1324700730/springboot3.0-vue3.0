import request from '@/frame/axios/request'

// 用户注册
export function registerUser(data) {
    return request({
        url: '/register/user',
        data: data,
        method: 'post',
    })
} 