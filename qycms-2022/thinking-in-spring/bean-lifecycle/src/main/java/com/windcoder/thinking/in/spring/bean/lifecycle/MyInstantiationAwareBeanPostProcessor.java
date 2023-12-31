package com.windcoder.thinking.in.spring.bean.lifecycle;

import com.windcoder.thinking.in.spring.ioc.overview.domain.SuperUser;
import com.windcoder.thinking.in.spring.ioc.overview.domain.User;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor;
import org.springframework.util.ObjectUtils;

class MyInstantiationAwareBeanPostProcessor implements InstantiationAwareBeanPostProcessor {
   @Override
   public Object postProcessBeforeInstantiation(Class<?> beanClass, String beanName) throws BeansException {
       if (ObjectUtils.nullSafeEquals("superUser", beanName) && SuperUser.class.equals(beanClass)) {
           // 把配置完成 SuperUser Bean 覆盖
           return new SuperUser();
       }
       return null; //保持 Spring Ioc 容器的实例化操作
   }
   @Override
   public boolean postProcessAfterInstantiation(Object bean, String beanName) throws BeansException {
       if (ObjectUtils.nullSafeEquals("user", beanName) && User.class.equals(bean.getClass())) {
           User user = (User)bean;
           user.setId(2L);
           user.setName("mylife");

           // “user” 对象不允许属性赋值（填入）（配置元信息 -》 属性赋值）
           return false;
       }
       return true;
   }

   // user 跳过 Bean 属性赋值（填入）
   // superUser 完全跳过 Bean 实例化（Bean 属性赋值（填入））

   @Override
   public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName)
           throws BeansException {
       if (ObjectUtils.nullSafeEquals("userHolder", beanName) && UserHolder.class.equals(bean.getClass())) {
           //  <property name="number" value="1" /> 配置的话，那么在 PropertyValues 就包含一个 PropertyValue{number=1}
           final MutablePropertyValues propertyValues;

           if (pvs instanceof MutablePropertyValues) {
               propertyValues = (MutablePropertyValues) pvs;
           } else {
               propertyValues = new MutablePropertyValues();
           }

           propertyValues.addPropertyValue("number", "1");
           //  原始配置 <property name="description" value=" user description" />
           // 如果存在 "description" 属性配置
           if (propertyValues.contains("description")) {
               // PropertyValue value 是不可变的
//                    PropertyValue description = propertyValues.getPropertyValue("description");
               propertyValues.removePropertyValue("description");
               propertyValues.addPropertyValue("description", "The user holder v2");
           }
           return propertyValues;
       }
       return null;
   }
}
