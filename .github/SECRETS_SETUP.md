# GitHub Secrets 配置指南

## 🔐 必需配置的密钥

### 1. 应用签名密钥库（Release 版本）

#### 生成密钥库文件
```bash
keytool -genkey -v -keystore wjfakelocation.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias wjfakelocation
```

#### 配置步骤
1. **上传密钥库到 GitHub**
   ```bash
   # 将密钥库转换为 Base64
   base64 wjfakelocation.jks > keystore.base64
   
   # 复制输出内容
   cat keystore.base64
   ```

2. **在 GitHub 添加 Secrets**
   - 进入项目 Settings → Secrets and variables → Actions
   - 点击 "New repository secret"
   - 添加以下密钥：

| Secret Name | Value | 说明 |
|-------------|-------|------|
| `KEYSTORE_BASE64` | (Base64 编码内容) | 密钥库文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | (你的密钥库密码) | 密钥库密码 |
| `KEY_ALIAS` | wjfakelocation | 密钥别名 |
| `KEY_PASSWORD` | (你的密钥密码) | 密钥密码 |

---

### 2. 地图 API Keys（可选）

#### 高德地图 API Key
| Secret Name | Value |
|-------------|-------|
| `AMAP_API_KEY` | 你的高德地图 API Key |

#### 百度地图 API Key
| Secret Name | Value |
|-------------|-------|
| `BAIDU_API_KEY` | 你的百度地图 API Key |

---

### 3. Supabase 云同步（可选）
| Secret Name | Value | 获取方式 |
|-------------|-------|----------|
| `SUPABASE_URL` | https://xxx.supabase.co | Supabase 项目设置 |
| `SUPABASE_ANON_KEY` | ey... | Supabase API 设置 |
| `SUPABASE_SERVICE_KEY` | ey... | Supabase API 设置 |

---

### 4. 发布平台（可选）

#### 蒲公英（PGYER）
| Secret Name | Value |
|-------------|-------|
| `PGYER_API_KEY` | 你的蒲公英 API Key |

#### 邮件通知
| Secret Name | Value | 说明 |
|-------------|-------|------|
| `EMAIL_USERNAME` | your@gmail.com | Gmail 账号 |
| `EMAIL_PASSWORD` | (应用专用密码) | Gmail 授权码 |
| `NOTIFICATION_EMAIL` | team@example.com | 接收通知的邮箱 |

---

## 📝 配置检查清单

- [ ] 密钥库文件已生成并上传
- [ ] 所有密码已添加到 GitHub Secrets
- [ ] API Keys 已配置（如需要）
- [ ] 发布平台密钥已配置（如需要）
- [ ] 邮件通知已配置（如需要）

---

## 🔍 验证配置

### 本地测试签名
```bash
# Windows PowerShell
.\gradlew assembleRelease

# 检查生成的 APK 是否已签名
jarsigner -verify -verbose -certs app\build\outputs\apk\release\app-release.apk
```

### GitHub Actions 测试
1. 推送一个测试标签：
   ```bash
   git tag v2.0.0-test
   git push origin v2.0.0-test
   ```

2. 查看 Actions 页面确认构建成功

3. 删除测试标签（如需要）：
   ```bash
   git tag -d v2.0.0-test
   git push origin :refs/tags/v2.0.0-test
   ```

---

## ⚠️ 安全提示

1. **永远不要**将密钥库文件提交到 Git
2. **永远不要**在代码中硬编码密码
3. 定期更新密钥和密码
4. 使用强密码（至少 12 位，包含大小写、数字、特殊字符）
5. 启用 GitHub 两步验证

---

## 🆘 故障排查

### Q: 构建失败，提示签名错误
**A**: 检查 Secrets 名称是否正确，确保没有多余空格

### Q: APK 未签名
**A**: 确认 `KEYSTORE_PATH` 环境变量已正确设置

### Q: 邮件发送失败
**A**: Gmail 需要使用应用专用密码，而非登录密码

### Q: 蒲公英上传失败
**A**: 检查 API Key 是否有效，网络是否通畅

---

**配置完成后，即可享受全自动 CI/CD 流程！** 🚀
