import Vue from 'vue'
import Router from 'vue-router'
import Login from './views/login.vue'
import Admin from './views/admin.vue'
import Welcome from './views/admin/welcome.vue'
import Chapter from './views/admin/chapter.vue'
import Section from './views/admin/section.vue'
import Course from './views/admin/course.vue'
import Category from './views/admin/category.vue'
import Teacher from './views/admin/teacher.vue'
import File from './views/admin/file.vue'
import Content from './views/admin/content.vue'
import User from './views/admin/user.vue'
import Resource from './views/admin/resource.vue'
import Role from './views/admin/role.vue'
import Member from "./views/admin/member.vue"

Vue.use(Router);

export default new Router({
    mode: 'history',// hash 会变成 xxx.com/#/xxx
    base: process.env.BASE_URL,
    routes: [{
        path: '*',
        redirect: '/login',
    },{
        path: '',
        redirect: '/login',
    }, {
        path: '/login',
        component: Login
    }, {
        path: '/',
        name: 'admin', // 为每个路由增加name属性，后续做通用sidebar激活样式方法时需要用到，
        component: Admin,
        meta:{
            loginRequire: true // 属性名称可以自定义
        },
        children: [{
            path: 'welcome',
            name: 'welcome',
            component: Welcome,
        },{
            path: 'business/category',
            name: 'business/category',
            component: Category,
        },{
            path: 'business/course',
            name: 'business/course',
            component: Course,
        },{
            path: 'business/chapter',
            name: 'business/chapter',
            component: Chapter,
        },{
            path: 'business/section',
            name: 'business/section',
            component: Section,
        },{
            path: 'business/content',
            name: 'business/content',
            component: Content,
        },{
            path: 'business/teacher',
            name: 'business/teacher',
            component: Teacher,
        }, {
            path: "business/member",
            name: 'business/member',
            component: Member,
        },{
            path: 'file/file',
            name: 'file/file',
            component: File,
        },{
            path: 'system/user',
            name: 'system/user',
            component: User,
        },{
            path: 'system/resource',
            name: 'system/resource',
            component: Resource,
        },{
            path: 'system/role',
            name: 'system/role',
            component: Role,
        }]
    }]
})