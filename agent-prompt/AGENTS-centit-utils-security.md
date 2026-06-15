# centit-utils / security 子包

> 包路径: `com.centit.support.security`
> 安全与加密工具。

---

## AESSecurityUtils

AES 加解密工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String encode(String key, String data)` | AES 加密（返回 Base64） |
| `static String decode(String key, String data)` | AES 解密 |
| `static String encodeNoPadding(String key, String data)` | AES/ECB 无填充加密 |

---

## RSASecurityUtils

RSA 加解密工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String encrypt(String publicKey, String data)` | RSA 公钥加密 |
| `static String decrypt(String privateKey, String data)` | RSA 私钥解密 |
| `static String sign(String privateKey, String data)` | RSA 签名 |
| `static boolean verify(String publicKey, String data, String sign)` | RSA 验签 |

---

## SM2Util

国密 SM2 非对称加密/签名工具（抽象类）。

| 方法 | 描述 |
|------|------|
| 加密/解密 | SM2 公钥加密、私钥解密 |
| 签名/验签 | SM2 签名和验证 |

---

## SM3Util

国密 SM3 哈希算法工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String encode(String source)` | SM3 哈希 |

---

## SM4Util

国密 SM4 对称加密工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String encode(String key, String data)` | SM4 加密 |
| `static String decode(String key, String data)` | SM4 解密 |

---

## GMBaseUtil

国密算法基础工具类（抽象类），提供 SM 系列算法的底层支持。

---

## Md5Encoder

MD5 摘要算法工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String encode(String source)` | MD5 编码 |

---

## Sha1Encoder

SHA-1 摘要算法工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String encode(String source)` | SHA-1 编码 |

---

## HmacSha1Encoder

HMAC-SHA1 签名工具（抽象类）。

| 方法 | 描述 |
|------|------|
| `static String hmacSha1(String key, String data)` | HMAC-SHA1 签名 |

---

## SecurityOptUtils

安全操作辅助工具（抽象类），密钥生成、编解码等。

---

## DesensitizeOptUtils

数据脱敏工具（抽象类），支持手机号、身份证、邮箱等常见脱敏场景。

---

## FileEncryptUtils

文件加密工具（抽象类），支持文件级别的加密/解密操作。

---
