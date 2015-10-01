
package uk.sipperfly.utils;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import sun.misc.*;

/**
 * 
 * @author Rimsha Khalid(rimsha@avpreserve.com)
 */
public class EncryptDecryptUtil {

    private static final String ALGO = "AES";
    private static final byte[] keyValue =
            new byte[]{'$','u','k','_','S','i','p','9','e','r','f','1','y','K','3','Y'};
    
	/**
	 * Encrypt the password
	 * 
	 * @param Data text to encrypt
	 * @return encrypted Value
	 * @throws Exception 
	 */
    public static String encrypt(String Data) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(Data.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encVal);
        return encryptedValue;
    }

	/**
	 * Decrypt the password
	 * 
	 * @param encryptedData  text to decrypt
	 * @return decrypted Value
	 * @throws Exception 
	 */
    public static String decrypt(String encryptedData) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGO);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedData);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }
	
	/**
	 * generate Key using salt and algo
	 * @return key
	 * @throws Exception 
	 */
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGO);
        return key;
}
}
