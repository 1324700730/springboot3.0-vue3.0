import router, {addError} from '@/frame/router/index'
import store from '@/frame/store'
import {getToken} from '@/utils/system/token'


//不经权限验证的白名单
const whiteList = ['/login', '/register']

/*路由守卫*/
router.beforeEach((to, from, next) => {
    // 如果是在白名单路径之间跳转，不显示任何提示，直接放行
    if (whiteList.includes(from.path) && whiteList.includes(to.path)) {
        return next();
    }
    
    if (getToken()) {
        //token存在时，如果访问登录页或注册页跳转到首页
        if (whiteList.includes(to.path)) {
            next({path: '/'})
        } else {
            //用户登录相关信息不存在
            console.log('store userName', store._state.data.loginUser)
            if (!store._state.data.loginUser.name) {
                //用户登录相关信息不存在
                store.dispatch('userInfoSet').then(res => {
                    //处理路由权限等信息
                    store.dispatch('routerGenerate').then(routes => {
                        //在父路由添加动态路由
                        let layout = router.getRoutes().find(r => {
                            return r.name === 'bas-layout'
                        })
                        layout.children = routes.concat(layout.children)
                        //layout.children = routes
                        router.addRoute(layout)
                        //动态添加404
                        addError()

                        console.log('addRoute', router.getRoutes())
                        //确保路由加载完成
                        next({...to, replace: true})
                    })
                }).catch(err => {
                    store.dispatch('userLoginOut').then(() => {
                        window.$message.error(err)
                        next({path: '/'})
                    })
                })
            } else {
                next()
            }
        }
    } else {
        console.log('router no token')
        //token不存在时
        if (whiteList.indexOf(to.path) !== -1) {
            // 只有当跳转到登录页且不是从白名单页面来时，才显示登录过期提示
            if (to.path === '/login' && !whiteList.includes(from.path) && from.path !== '/') {
                window.$message.warning('登录状态已过期，请重新登录')
            }
            next()
        } else {
            next(`/login?redirect=${to.path}`)
        }
    }
})
