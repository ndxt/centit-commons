# centit-utils / image 子包

> 包路径: `com.centit.support.image`
> 图像处理工具。

---

## CaptchaImageUtil

验证码图片生成（简单易辨识，抽象类）。

| 方法 | 描述 |
|------|------|
| `static String getRandomString(int len)` | 获取随机字符串 |
| `static BufferedImage generateCaptchaImage(String checkcode)` | 生成验证码图片 |
| `static boolean checkcodeMatch(String session, String request)` | 验证码比对（忽略大小写） |

---

## ImageOpt

图像操作工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static void createThumbnail(String filename, int w, int h, int quality, String outFilename)` | 创建缩略图 |
| `static void captureScreen(String fileName)` | 屏幕截图 |
| `static BufferedImage makeRoundBorder(BufferedImage image, int radius, int border, Color fillColor)` | 圆角边框 |
| `static byte[] imageToByteArray(BufferedImage image)` | 图片转 byte[] |
| `static String imageMD5(BufferedImage image)` | 图片 MD5 |

---

## QrCodeGenerator

二维码生成器（基于 ZXing，抽象类）。

| 方法 | 描述 |
|------|------|
| `static BufferedImage generateQrCode(QrCodeConfig qrCodeConfig)` | 根据配置生成二维码 |
| `static void insertLogo(BufferedImage qrCode, BufferedImage logImage)` | 插入 Logo |

---

## QrCodeConfig

二维码配置类。包含内容、尺寸、颜色、Logo、文字等配置项。

| 属性 | 描述 |
|------|------|
| 内容、宽高、前景色/背景色 | 基本配置 |
| Logo图片及比例 | 中心 Logo |
| 底部文字及字体 | 底部标签文字 |

---

## SvgUtils

SVG 工具。

| 方法 | 描述 |
|------|------|
| `static boolean removeSvgJSAction(String svgFilePath, String outputPath)` | 移除 SVG 中的 JavaScript 动作 |
