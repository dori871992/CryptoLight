# CryptoLight
[![](https://jitpack.io/v/dori871992/CryptoLight.svg)](https://jitpack.io/#dori871992/CryptoLight)

Library for digitally signing media (Photos or Videos) on the device.

<ul> 
<li> Generate digital encrypted media signatures via private key</li>
<li> Signature verification of files via public key</li>
<li> Easy encryption and decryption of texts</li>
<li> Confirmation if signature belongs to media, ensuring media was not contaminated</li>
</ul>


# Installation

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
Add the dependency

	dependencies {
	        implementation 'com.github.dori871992:CryptoLight:1.1.0'
	}

 # Usage
 
**Initialize in onCreate() of Application class**

```java
public class LibraryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        CryptoLight.init(this);
    }
}
```

*It will generate keypair of public key and private key

**Private and public keys can be accessed like:**

```java
 String publicKey = CryptoLight.getPublicKey(this);
 String privateKey = CryptoLight.getPrivateKey(this);
```

**Generate signature for file:**

```java
 File file = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera/IMG_20190603_164516.jpg");
 String filePath = file.getAbsolutePath();
      
 File signatureFile = CryptoLight.generateDigitalSignature(this, filePath);}
```

**Verify signature:**

- **filePath** {path of file (photo or vide) to to be verified}
- **signatureToVerify** {path of signature file of photo or vide to to be verified against}
- **publicKeyFilePath** {path of public key file where signanture was generated from}

```java
 boolean verified = CryptoLight.verifySignature(this, filePath, signatureToVerify, publicKeyFilePath);

```

**Encryption and Decryption:**

```java
  String encryptedMessage = CryptoLight.encrypt(this, "Dorian Musaj is coming amigos!");
  String decryptedMessage = CryptoLight.decrypt(this, encryptedMessage);  //output will be "Dorian Musaj is coming amigos!"
  ```
 
