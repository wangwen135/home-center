相关的依赖库
```
# 创建目录
mkdir -p src/main/resources/static/lib/{vue,element-ui,axios}

# 下载文件
wget https://cdn.bootcdn.net/ajax/libs/vue/2.6.14/vue.min.js -O src/main/resources/static/lib/vue/vue.min.js
wget https://cdn.bootcdn.net/ajax/libs/element-ui/2.15.13/index.js -O src/main/resources/static/lib/element-ui/element-ui.js
wget https://cdn.bootcdn.net/ajax/libs/element-ui/2.15.13/theme-chalk/index.css -O src/main/resources/static/lib/element-ui/element-ui.css
wget https://cdn.bootcdn.net/ajax/libs/axios/0.21.1/axios.min.js -O src/main/resources/static/lib/axios/axios.min.js

# 下载 Element UI 字体文件
wget https://cdn.bootcdn.net/ajax/libs/element-ui/2.15.13/theme-chalk/fonts/element-icons.woff -O src/main/resources/static/lib/element-ui/fonts/element-icons.woff
wget https://cdn.bootcdn.net/ajax/libs/element-ui/2.15.13/theme-chalk/fonts/element-icons.ttf -O src/main/resources/static/lib/element-ui/fonts/element-icons.ttf

```
