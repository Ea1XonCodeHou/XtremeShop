# Xtreme-Shop
分布式软件设计个人作业 - 分布式秒杀平台，基于SpringBoot + Vue开发。

## 初始化日志
- 2026-03-17：完成后端 Maven 父子模块重构，统一依赖版本管理。
- 2026-03-17：完成后端基础三层拆分：common（通用能力）、pojo（实体模型）、server（业务服务与唯一启动入口）。
- 2026-03-17：完成前端目录标准化，保留 src/assets、src/components、src/views、src/api、src/router、src/store。
- 2026-03-17：完成工程清理，移除无用示例文件、IDE 缓存目录与构建产物目录。
- 2026-03-17：新增根目录环境与隐私保护基础文件：.env（空模板）与 .gitignore（隐私/本地文件屏蔽）。

## 底层搭建与结构分层
### 后端分层
- xtreme_common：工具类、常量、基础封装，作为公共库模块。
- xtreme_pojo：数据库实体对象与共享模型，作为实体库模块。
- xtreme_server：业务处理与接口服务，作为唯一可运行 Spring Boot 模块。

### 前端分层
- src/views：页面级视图。
- src/components：通用组件。
- src/api：接口封装。
- src/router：路由配置。
- src/store：状态管理。
- src/assets：静态资源与基础样式。
