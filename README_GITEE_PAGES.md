# Gitee Pages 部署指南

## 📋 部署步骤

### 1. 准备文件

确保以下文件已准备好：
- ✅ `index.html` - 项目主页
- ✅ `_config.yml` - Jekyll 配置文件（可选）
- ✅ 其他文档文件（README.md, LICENSE 等）

### 2. 创建 Gitee 仓库

1. 登录 Gitee
2. 创建新仓库（或使用现有仓库）
3. 仓库名称建议：`formula4j-plus` 或 `formula4j-plus-pages`

### 3. 上传文件

```bash
# 初始化 Git 仓库（如果还没有）
git init

# 添加文件
git add index.html _config.yml Project-Overview.md LICENSE NOTICE

# 提交
git commit -m "feat: 添加 Gitee Pages 主页"

# 添加远程仓库
git remote add origin https://gitee.com/Formula4j-plus/formula4j-plus.git

# 推送到 Gitee
git push -u origin master
```

### 4. 启用 Gitee Pages

1. 进入仓库设置
2. 找到 "Gitee Pages" 选项
3. 选择部署分支（通常是 `master` 或 `main`）
4. 选择部署目录（根目录 `/`）
5. 点击 "启动" 或 "更新"

### 5. 访问网站

部署成功后，访问地址为：
```
https://Formula4j-plus.gitee.io/formula4j-plus
```

## 🔧 配置说明

### index.html

- 响应式设计，支持移动端
- 包含项目介绍、功能特性、使用示例等
- 使用纯 HTML/CSS/JavaScript，无需构建工具

### _config.yml

- Jekyll 配置文件（可选）
- 如果使用 Jekyll，可以启用更多功能
- 如果不使用 Jekyll，可以删除此文件

## 📝 自定义配置

### 修改仓库链接

在 `index.html` 中搜索并替换：
```html
<a href="https://github.com" class="btn btn-secondary" target="_blank">GitHub</a>
```
替换为您的实际 GitHub 或 Gitee 仓库地址。

### 修改文档链接

确保文档文件（如 `USAGE.md`、`CONTRIBUTING.md`）已上传到仓库，链接会自动生效。

### 修改联系方式

在 `index.html` 的 "联系方式" 部分修改邮箱和作者信息。

## 🎨 样式自定义

如果需要修改样式，编辑 `index.html` 中的 `<style>` 部分：

```css
:root {
    --primary-color: #2563eb;  /* 主色调 */
    --secondary-color: #1e40af; /* 次要颜色 */
    /* ... 其他颜色变量 */
}
```

## ⚠️ 注意事项

1. **文件路径**: 确保所有链接使用相对路径
2. **图片资源**: 如果使用图片，建议放在仓库中或使用 CDN
3. **更新内容**: 每次更新后需要重新部署 Gitee Pages
4. **HTTPS**: Gitee Pages 默认支持 HTTPS

## 🔄 更新网站

每次更新内容后：

```bash
# 提交更改
git add .
git commit -m "docs: 更新网站内容"
git push

# 在 Gitee 仓库设置中点击 "更新" Gitee Pages
```

## 📚 相关资源

- [Gitee Pages 文档](https://gitee.com/help/articles/4136)
- [Jekyll 文档](https://jekyllrb.com/)（如果使用 Jekyll）

---

**提示**: 如果遇到问题，请检查：
1. 文件是否正确上传
2. Gitee Pages 是否已启用
3. 部署分支和目录是否正确
4. 文件路径是否正确

