# Privacy Guard

The privacy guard is a peripheral that adds the ability to create GPG keys, decrypt data with GPG keys, and encrypt
 data with GPG keys.
 
## Implementation

The backend utilizes [Bouncy Castle] for all cryptography related tasks. PeripheralsPlusOne does not guarantee that the
 implementation is secure. Use a different GPG tool if security is of concern.
 
All operations will output ASCII armored text and will expect it as an input. GPG keys exported to ASCII armored text
 from other sources should work as well.
 
**Note: When working with keys, they must have an email set in their ID and when specifying keys, only the email may
 be used.**
 
 
## Functions
                
<table>
  <tr>
    <th>Function</th>
    <th>Returns</th>
    <th>Description</th>
  </tr>
  <tr>
    <td>generateKey(<i>string</i> id[, <i>string</i> key_password[, <i>number</i> size]])</td>
    <td><i>table</i> key</td>
    <td>Generates a new GPG with the specified id in the standard format[1]. A password is optional and a default bit
     size of 2048 will be used if one is not provided. Keys are generated using RSA with AES256 and SHA256 preferred for
     the symmetric and hash algorithms. The returned table will contain an ASCII armored public and private key. 
     This function errors if a key fails to generate.</td>
  </tr>
  <tr>
    <td>readKey(<i>string</i> key)</td>
    <td><i>table</i> key_info</td>
    <td>Returns information about the key. Errors if a key is invalid</td>
  </tr>
  <tr>
    <td>decrypt(<i>string</i> key, @Nillable <i>string</i> key_password, <i>string</i> encoded_string,
     [, <i>table</i> verification_keys, [<i>table/boolean</i> ids_to_verify])</td>
    <td><i>string</i> plaintext</td>
    <td>Attempts to decrypt a key. The specified key should be a private key with an option password provided if the
     key is password protected. Optional verification keys can be provided as a table, with only the specified IDs
     used to check which keys should have valid signatured attached to the encoded data. If <i>true</i> is passed
     instead of a table, all the verification keys will be checked against the encoded data. This function will error
     if a key is invalid, password is incorrect, decryption failed, or signature verification failed.</td>
  </tr>
  <tr>
    <td>encrypt(<i>string</i> key, <i>string</i> plaintext 
    [, @Nillable <i>string</i> signing_key, @Nillable <i>string</i> password 
    [,<i>string</i> recipient [, <i>string</i> signer])</td>
    <td><i>string</i> encoded_string</td>
    <td>Attempts to encrypt a key, returning ASCII armored text if successful. This function errors if a key fails
     to validate, there is an incorrect password, data fails to encrypt, or the specified signer ID could not be 
     found.</td>
  </tr>
</table>

1. ID format: `Some B. Ody (Somebody) <somebody@example.com>`



[Bouncy Castle]: https://bouncycastle.org/java.html