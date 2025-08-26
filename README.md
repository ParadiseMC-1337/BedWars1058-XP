# BedWars1058-XP

> 基于 BedWars1058 v25.2 的增强型分支版本，新增多项实用功能

[![QQ群-Paradise](https://img.shields.io/badge/QQ群-Paradise-blue?style=flat-square&logo=tencent-qq)](https://qm.qq.com/q/5o9RKWy6aI)

## 🚀 新增功能

本版本在原版 BedWars1058 v25.2 基础上增加了以下独特功能：

### ✨ 核心新功能

- **📜 回城卷轴** - 允许玩家快速传送回基地的特殊物品
- **🛡️ 自救平台** - 在关键时刻为玩家提供临时保护平台
- **🏰 防守墙** - 可部署的防御结构，增强基地防护能力
- **⭐ 经验起床** - 基于经验值的复活机制，为游戏增添策略深度

## 📝 游戏简介

BedWars 是一个团队对战小游戏，玩家需要保护自己的床并摧毁其他队伍的床。一旦床被摧毁，该队伍的玩家将无法复活。

## 💻 系统要求

- **服务端**: [Spigot](https://www.spigotmc.org/) 或 [Paper](https://papermc.io/)
- **Java 版本**: Java 11 或更高版本
- **支持版本**: Minecraft 1.8.8 - 1.20.x

### 推荐的世界管理插件

为了获得更好的地图重置性能，建议使用以下插件之一：

- [SlimeWorldManager](https://www.spigotmc.org/resources/slimeworldmanager.69974/) (v2.2.1)
- [AdvancedWorldManager](https://www.spigotmc.org/resources/advanced-slimeworldmanager.87209/) (v2.8.0)
- [AdvancedSlimePaper](https://github.com/InfernalSuite/AdvancedSlimePaper) 服务器核心 (1.20+)

## 🎮 核心特性

### 运行模式
- **SHARED** - 与其他小游戏共存，通过命令加入游戏
- **MULTIARENA** - 独占服务器实例，支持大厅世界和多种加入方式
- **BUNGEE-LEGACY** - 传统群组模式，一个游戏占用一个服务器实例
- **BUNGEE** - 全新可扩展群组模式，支持动态创建竞技场

### 多语言系统
- 每个玩家可以选择自己的语言 (`/bw lang`)
- 支持添加或删除语言包
- 队伍名称、商店内容等都可以本地化

### 商店系统
- 可配置的快速购买默认物品
- 支持添加/删除商品分类
- 永久物品和可降级物品系统
- 特殊物品：床虫、梦境守卫者、鸡蛋桥梁、TNT跳跃、直线火球等

### 队伍升级
- 不同竞技场组可配置不同的升级选项
- 支持物品附魔、药水效果、陷阱系统
- 可自定义龙的数量和生成器设置

### 数据统计
- 完整的玩家统计系统
- 支持第三方排行榜插件集成
- 内置统计GUI界面

## 🔧 安装配置

1. 下载插件文件
2. 将插件放入服务器的 `plugins` 文件夹
3. 启动服务器生成默认配置
4. 根据需要修改配置文件
5. 重启服务器完成安装

## 🎯 加入游戏的方式

- **命令加入**: `/bw join [竞技场名称/随机]`
- **GUI选择器**: `/bw gui [组名]`
- **NPC交互**: 需要安装 Citizens 插件
- **标识牌**: 支持状态显示的加入标识牌

## 🛠️ 开发者信息

### API支持
本插件提供完整的API接口，开发者可以：
- 创建自定义商店物品
- 实现自定义地图重置适配器
- 扩展队伍升级功能
- 集成第三方统计系统

### 第三方库
- [bStats](https://bstats.org/) - 插件统计
- [SidebarLib](https://github.com/andrei1058/SiderbarLib) - 计分板管理
- [Commons IO](https://commons.apache.org/) - 文件操作
- [HikariCP](https://github.com/brettwooldridge/HikariCP) - 数据库连接池
- [SLF4J](http://www.slf4j.org/) - 日志框架

## 📞 支持与反馈

如果您遇到问题或有功能建议，请通过以下方式联系：

**ParadiseMC QQ交流群**: [点击加入群聊【ParadiseMC】](https://qm.qq.com/q/5o9RKWy6aI)

## 📄 开源协议

本项目基于 GNU GPL 3.0 协议开源。

## 🙏 特别感谢

感谢 [Andrei Dascălu](https://github.com/andrei1058) 创建了优秀的 BedWars1058 原版插件。

---

**注意**: 本版本为社区增强版，包含原版未有的特色功能。如需获取最新的官方版本，请访问 [官方GitHub仓库](https://github.com/andrei1058/BedWars1058)。