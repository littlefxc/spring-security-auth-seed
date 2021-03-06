# SpringCloud内置加密

Spring Cloud内置了加解密的支持，包括对称加密和非对称加密。使用对称加密只需要在bootstrap.yml文件中通过`encrypt.key`属性指定加密用的密钥

```yaml
encrypt:
  key: ABC
```

这样 SpringCloud 就会自动创建一个`org.springframework.security.crypto.encrypt.TextEncryptor`类型的bean。TextEncryptor可以用来进行加密和解密。

```java
@SpringBootTest(classes = Application.class)
@RunWith(SpringRunner.class)
public class TextEncryptorTest {

  @Autowired
  private TextEncryptor textEncryptor;

  @Test
  public void test() {
    for (int i=0; i<10; i++) {
      String plainText = "ABCDEFG";
      String encryptedText = this.textEncryptor.encrypt(plainText);
      String decryptedText = this.textEncryptor.decrypt(encryptedText);
      System.out.println(plainText + "----------" + encryptedText + "-----------" + decryptedText);
    }
  }

}
```

如果你运行上面的代码你会发现`TextEncryptor`对相同的文本加密10次后的结果每次加密后的结果都是不一样的，但是它们解密后的结果都是一样的。
这是因为底层使用的AES加密算法是`AES/CBC/PKCS5Padding`，其使用的IV值每次都是重新随机生成的，这样的话由于每次使用的都是不同的IV值，所以出来的加密结果也是不一样的。
详情可以参考`org.springframework.cloud.bootstrap.encrypt.EncryptionBootstrapConfiguration`源码

Spring Cloud也内置了对非对称加解密的支持。底层使用的是RSA算法，这需要我们基于RSA算法生成一个非对称加密的密钥，然后把它存到一个KeyStore中。
然后在`bootstrap.yml`中通过如下方式指定`KeyStore`的相关信息。

```yaml
encrypt:
  key-store:
    location: server.jks
    alias: testkey
    password: 123456
    secret: key123456
```

然后SpringCloud会自动创建一个基于RSA算法的TextEncryptor实现，RsaSecretEncryptor。
RsaSecretEncryptor将使用KeyStore里面存储的密钥对的公钥进行加密，使用私钥进行解密。
它加密的内容会先通过随机生成的密钥使用AES算法进行一次加密，再把AES加密用的密钥长度、密钥和加密后的密文一起经过公钥加密。
这样也可以达到同样的内容每次经过加密后的结果都是不一样的。
在进行解密的时候只要进行反向操作即可拿到明文了。
详情可参考RsaSecretEncryptor的源码

当同时指定了`encrypt.key`和`encrypt.keyStore.xxx`相关信息时，Spring Cloud将优先创建基于RSA的TextEncryptor。

## 生成证书文件

```sh
keytool -genkeypair -alias changeme -keyalg RSA \
  -dname "CN=Web Server,OU=Unit,O=Organization,L=City,S=State,C=US" \
  -keypass changeme -keystore server.jks -storepass changeme
```

在当前目录下生成server.jks文件（证书文件）

## 参考资源

[spring cloud config学习三：安全与加密解密](https://www.jianshu.com/p/1dbd9a83880f)