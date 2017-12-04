package com.austinv11.peripheralsplusplus.tiles;

import com.austinv11.collectiveframework.minecraft.reference.ModIds;
import com.austinv11.peripheralsplusplus.utils.IPlusPlusPeripheral;
import com.austinv11.peripheralsplusplus.utils.OpenComputersPeripheral;
import com.austinv11.peripheralsplusplus.utils.OpenComputersUtil;
import com.austinv11.peripheralsplusplus.utils.Util;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BouncyGPG;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BuildDecryptionInputStreamAPI;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.BuildEncryptionOutputStreamAPI;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallback;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.callbacks.KeyringConfigCallbacks;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.InMemoryKeyring;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.keys.keyrings.KeyringConfigs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.Optional;
import org.bouncycastle.bcpg.CRC24;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.SymmetricKeyAlgorithmTags;
import org.bouncycastle.bcpg.sig.Features;
import org.bouncycastle.bcpg.sig.KeyFlags;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPKeyPair;
import org.bouncycastle.openpgp.PGPKeyRingGenerator;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor;
import org.bouncycastle.openpgp.operator.PGPDigestCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyEncryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;
import org.bouncycastle.openpgp.operator.bc.BcPGPKeyPair;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.io.Streams;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TileEntityPrivacyGuard extends TileEntity implements IPlusPlusPeripheral, OpenComputersPeripheral {
    private static final String ENCODING = "US-ASCII";
    private Node node;

    public TileEntityPrivacyGuard() {
        super();
        node = OpenComputersUtil.createNode(this, getType());
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null)
            Security.addProvider(new BouncyCastleProvider());
    }

    @Nonnull
    @Override
    public String getType() {
        return "privacy_guard";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{
                "generateKey", // (string id, [int size[, string key_password]]) table keys
                "readKey", // (string key) table info
                "decrypt", // (string key, string key_password, string encoded_string, [, table verification_keys, [table/boolean ids_to_verify]) string decoded_string
                "encrypt", // (string key, string decoded_string [, string signing_key, string password [, string recipient [, string signer]]) string encoded_string
        };
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method,
                               @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0:
                return generateKeyLua(arguments);
            case 1:
                return readKeyLua(arguments);
            case 2:
                return decryptLua(arguments);
            case 3:
                return encryptLua(arguments);
        }
        throw new LuaException("No such method");
    }

    /**
     * Encrypt a string using the provided key
     * @param arguments key, plaintext, sign_key, password, recipient, signer
     * @return encrypted string
     */
    private Object[] encryptLua(Object[] arguments) throws LuaException {
        if (arguments.length < 2)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof String))
            throw new LuaException("Argument #1 expected to be a string");
        if (!(arguments[1] instanceof String))
            throw new LuaException("Argument #2 expected to be a string");
        if (arguments.length > 2 && !(arguments[2] instanceof String) && arguments[2] != null)
            throw new LuaException("Argument #3 expected to be a string or nil");
        if ((arguments.length > 3 || arguments[2] != null) && !(arguments[3] instanceof String))
            throw new LuaException("Argument #4 expected to be a string");
        if (arguments.length > 4 && !(arguments[4] instanceof String))
            throw new LuaException("Argument #5 expected to be a string");
        if (arguments.length > 5 && !(arguments[5] instanceof String))
            throw new LuaException("Argument #6 expected to be a string");
        String keyString = (String) arguments[0];
        String plaintext = (String) arguments[1];
        String signKeyString = arguments.length > 2 ? (String) arguments[2] : null;
        String password = arguments.length > 3 ? (String) arguments[3] : null;
        String recipient = arguments.length > 4 ? (String) arguments[4] : null;
        String signer = arguments.length > 5 ? (String) arguments[5] : null;
        // Read key
        KeyringConfigCallback keyringCallback = KeyringConfigCallbacks.withUnprotectedKeys();
        if (password != null)
            keyringCallback = KeyringConfigCallbacks.withPassword(password);
        InMemoryKeyring keyring;
        try {
            keyring = KeyringConfigs.forGpgExportedKeys(keyringCallback);
        }
        catch (IOException | PGPException e) {
            throw new LuaException(e.getMessage());
        }
        PGPPublicKeyRing key;
        PGPSecretKeyRing signKey = null;
        try {
            key = new PGPPublicKeyRing(
                    PGPUtil.getDecoderStream(new ByteArrayInputStream(keyString.getBytes(ENCODING))),
                    new BcKeyFingerprintCalculator()
            );

            keyring.addPublicKey(keyString.getBytes(ENCODING));
            if (signKeyString != null) {
                signKey = new PGPSecretKeyRing(
                        PGPUtil.getDecoderStream(new ByteArrayInputStream(signKeyString.getBytes(ENCODING))),
                        new BcKeyFingerprintCalculator()
                );
                keyring.addSecretKey(signKeyString.getBytes(ENCODING));
            }
        }
        catch (IOException | PGPException e) {
            throw new LuaException("Invalid key: " + e.getMessage());
        }
        // Encrypt
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        OutputStream encryptStream;
        try {
            BuildEncryptionOutputStreamAPI.WithAlgorithmSuite.To.SignWith signWith = BouncyGPG
                    .encryptToStream()
                    .withConfig(keyring)
                    .withStrongAlgorithms()
                    // BouncyGPG requires the key to contain a populated email field
                    .toRecipient(recipient == null ? extractEmailFromKey(key) : recipient);
            BuildEncryptionOutputStreamAPI.WithAlgorithmSuite.To.SignWith.Armor armor;
            if (signKey != null)
                armor = signWith.andSignWith(signer == null ? extractEmailFromKey(signKey) : signer);
            else
                armor = signWith.andDoNotSign();
            encryptStream = armor.armorAsciiOutput()
                    .andWriteTo(byteOutput);
        } catch (PGPException | SignatureException | NoSuchAlgorithmException | NoSuchProviderException |
                IOException e) {
            throw new LuaException(e.getMessage());
        }
        try {
            encryptStream.write(plaintext.getBytes());
            encryptStream.flush();
            encryptStream.close();
        }
        catch (IOException e) {
            throw new LuaException("IO error: " + e.getMessage());
        }
        try {
            return new Object[]{byteOutput.toString(ENCODING)};
        } catch (UnsupportedEncodingException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * @see TileEntityPrivacyGuard#extractEmailFromKey(PGPPublicKeyRing)
     */
    private String extractEmailFromKey(PGPSecretKeyRing key) throws LuaException {
        Iterator<PGPSecretKey> keys = key.getSecretKeys();
        while (keys.hasNext()) {
            String email = extractEmailFromUserIds(keys.next().getUserIDs());
            if (email != null)
                return email;
        }
        throw new LuaException("Missing email feild in GPG key");
    }

    /**
     * Extract the email field from the id
     * This is needed because BouncyGPG only allows key selection via email.
     * @param key key to search
     * @return email field in id
     */
    private String extractEmailFromKey(PGPPublicKeyRing key) throws LuaException {
        Iterator<PGPPublicKey> keys = key.getPublicKeys();
        while (keys.hasNext()) {
            String email = extractEmailFromUserIds(keys.next().getUserIDs());
            if (email != null)
                return email;
        }
        throw new LuaException("Missing email field in GPG key");
    }

    /**
     * Loop through a list of ids and return the first email address found
     * @param ids key ids
     * @return email address or null if no email addresses were found
     */
    @Nullable
    private String extractEmailFromUserIds(Iterator<String> ids) {
        Pattern emailRegex = Pattern.compile(".*<(.*@.*\\..*)>");
        while (ids.hasNext()) {
            Matcher matcher = emailRegex.matcher(ids.next());
            if (!matcher.matches())
                continue;
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Decrypt a string using the provided key
     * @param arguments key, pass, string, keys, ids
     * @return decrypted string
     */
    private Object[] decryptLua(Object[] arguments) throws LuaException {
        if (arguments.length < 3)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof String))
            throw new LuaException("Argument #1 expected to be a string");
        if (!(arguments[1] instanceof String) && arguments[1] != null)
            throw new LuaException("Argument #2 expected to be a string or nil");
        if (!(arguments[2] instanceof String))
            throw new LuaException("Argument #3 expected to be a string");
        if (arguments.length > 3 && !(arguments[3] instanceof Map)) {
            throw new LuaException("Argument #4 expected to be a table");
        }
        if (arguments.length > 4 && !(arguments[4] instanceof Map) && !(arguments[4] instanceof Boolean))
            throw new LuaException("Argument #5 expected to be a table or boolean");
        String keyString = (String) arguments[0];
        String password = (String) arguments[1];
        String encodedText = (String) arguments[2];
        List<String> verificationKeys = arguments.length > 3 ? luaMapToStringList((Map<Double, String>) arguments[3]) :
                new ArrayList<>();
        List<String> verificationIds = arguments.length > 4 && arguments[4] instanceof Map ?
                luaMapToStringList((Map<Double, String>) arguments[4]) : new ArrayList<>();
        boolean verifyAll = arguments.length > 4 && arguments[4] instanceof Boolean && (Boolean) arguments[4];
        // Read key
        KeyringConfigCallback keyringCallback = KeyringConfigCallbacks.withUnprotectedKeys();
        if (password != null)
            keyringCallback = KeyringConfigCallbacks.withPassword(password);
        InMemoryKeyring keyring;
        try {
            keyring = KeyringConfigs.forGpgExportedKeys(keyringCallback);
        }
        catch (IOException | PGPException e) {
            throw new LuaException(e.getMessage());
        }
        try {
            keyring.addSecretKey(keyString.getBytes(ENCODING));
        }
        catch (IOException | PGPException e) {
            throw new LuaException("Invalid key: " + e.getMessage());
        }
        // Add verification keys to keyring
        for (String verificationKey : verificationKeys) {
            try {
                keyring.addPublicKey(verificationKey.getBytes(ENCODING));
            }
            catch (IOException | PGPException e) {
                throw new LuaException("Invalid verification key: " + e.getMessage());
            }
        }
        // Decrypt
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        BuildDecryptionInputStreamAPI.Validation validation = BouncyGPG
                .decryptAndVerifyStream()
                .withConfig(keyring);
        BuildDecryptionInputStreamAPI.Build build;
        // Verification options
        if (verificationIds.size() == 0 && verificationKeys.size() == 0)
            build = validation.andIgnoreSignatures();
        else
            try {
                if (verifyAll)
                    build = validation.andRequireSignatureFromAllKeys(extractAllIdsFromPublicKeys(verificationKeys));
                else
                    build = validation.andRequireSignatureFromAllKeys(
                            verificationIds.toArray(new String[verificationIds.size()]));
            }
            catch (PGPException | IOException e) {
                throw new LuaException("Failed to verify signature: " + e.getMessage());
            }
        // Attempt the decrypt
        InputStream decryptStream;
        try {
            decryptStream = build.fromEncryptedInputStream(
                    new ByteArrayInputStream(encodedText.getBytes(ENCODING)));
        }
        catch (IOException | NoSuchProviderException e) {
            if (e.getCause() instanceof PGPException)
                throw new LuaException(e.getMessage() + ": " + e.getCause().getMessage());
            throw new LuaException(e.getMessage());
        }
        try {
            Streams.pipeAll(decryptStream, byteOutput);
            decryptStream.close();
            byteOutput.flush();
        }
        catch (IOException e) {
            throw new LuaException("IO Error: " + e.getMessage());
        }
        try {
            return new Object[]{byteOutput.toString(ENCODING)};
        }
        catch (UnsupportedEncodingException e) {
            throw new LuaException(e.getMessage());
        }
    }

    /**
     * Parse a list of string into public keys and extract their ids to an array
     * This will only return IDs of found signing keys, ignoring encryption keys
     * @param keys list public keys
     * @return ids
     */
    private Long[] extractAllIdsFromPublicKeys(List<String> keys) throws IOException {
        List<Long> ids = new ArrayList<>();
        BcKeyFingerprintCalculator calc = new BcKeyFingerprintCalculator();
        for (String keyString : keys) {
            PGPPublicKeyRing key = new PGPPublicKeyRing(
                    PGPUtil.getDecoderStream(new ByteArrayInputStream(keyString.getBytes(ENCODING))),
                    calc
            );
            Iterator<PGPPublicKey> publicKeys = key.getPublicKeys();
            while (publicKeys.hasNext()) {
                PGPPublicKey publicKey =  publicKeys.next();
                if (publicKey.isEncryptionKey())
                    continue;
                ids.add(publicKey.getKeyID());
            }
        }
        return ids.toArray(new Long[ids.size()]);
    }

    /**
     * Attempt to parse a map to a list of strings
     * @param map map
     * @return list of strings
     */
    private List<String> luaMapToStringList(Map<Double, String> map) throws LuaException {
        List<String> list = new ArrayList<>();
        for (Double index = 1d; index <= map.size(); index++) {
            if (!map.containsKey(index))
                throw new LuaException("Table contains invalid key");
            list.add(map.get(index));
        }
        return list;
    }

    /**
     * Read a key
     * @param arguments key, password
     * @return key info
     */
    private Object[] readKeyLua(Object[] arguments) throws LuaException {
        if (arguments.length < 1)
            throw new LuaException("Not enough arguments");
        if (!(arguments[0] instanceof String))
            throw new LuaException("Argument #1 expected to be a string");
        String keyString = (String) arguments[0];
        List<Map<String, Object>> keys = new ArrayList<>();
        // Try to parse a public key
        try {
            PGPPublicKeyRing key = new PGPPublicKeyRing(
                    PGPUtil.getDecoderStream(new ByteArrayInputStream(keyString.getBytes(ENCODING))),
                    new BcKeyFingerprintCalculator()
            );
            Iterator<PGPPublicKey> keysIter = key.getPublicKeys();
            while (keysIter.hasNext()) {
                Map<String, Object> keyMap = new HashMap<>();
                PGPPublicKey publicKey = keysIter.next();
                keyMap.put("private", false);
                keyMap.put("masterKey", publicKey.isMasterKey());
                keyMap.put("signingKey", !publicKey.isEncryptionKey());
                keyMap.put("id", Double.parseDouble(String.valueOf(publicKey.getKeyID())));
                keyMap.put("version", publicKey.getVersion());
                keyMap.put("algorithm", publicKey.getAlgorithm());
                keyMap.put("strength", publicKey.getBitStrength());
                keyMap.put("creationTime", publicKey.getCreationTime());
                keyMap.put("userIds", Util.iteratorToMap(publicKey.getUserIDs()));
                keyMap.put("fingerprint", Util.byteArraytoUnsignedIntArray(publicKey.getFingerprint()));
                keyMap.put("signatures", Util.iteratorToMap(publicKey.getKeySignatures()));
                keyMap.put("validSeconds", Double.parseDouble(String.valueOf(publicKey.getValidSeconds())));
                keys.add(keyMap);
            }
        }
        catch (IOException ignore) {}
        // Try to parse a private key
        try {
            PGPSecretKeyRing key = new PGPSecretKeyRing(
                    PGPUtil.getDecoderStream(new ByteArrayInputStream(keyString.getBytes(ENCODING))),
                    new BcKeyFingerprintCalculator()
            );
            Iterator<PGPSecretKey> keysIter = key.getSecretKeys();
            while (keysIter.hasNext()) {
                Map<String, Object> keyMap = new HashMap<>();
                PGPSecretKey secretKey = keysIter.next();
                keyMap.put("private", true);
                keyMap.put("masterKey", secretKey.isMasterKey());
                keyMap.put("signingKey", secretKey.isSigningKey());
                keyMap.put("id", Double.parseDouble(String.valueOf(secretKey.getKeyID())));
                keyMap.put("s2k", secretKey.getS2KUsage());
                keyMap.put("algorithm", secretKey.getKeyEncryptionAlgorithm());
                keyMap.put("userIds", Util.iteratorToMap(secretKey.getUserIDs()));
                keys.add(keyMap);
            }
        }
        catch (IOException ignore) {}
        catch (PGPException e) {
            throw new LuaException(e.getMessage());
        }
        return new Object[]{Util.arrayToMap(keys.toArray())};
    }

    /**
     * Generate a private/public keypair
     * @param arguments id, size, password
     * @return table with string keys
     */
    private Object[] generateKeyLua(Object[] arguments) throws LuaException {
        int size = 2048;
        String password = null;
        String id;
        if (arguments.length < 1 || !(arguments[0] instanceof String))
            throw new LuaException("Argument #1 expected to be a string");
        id = ((String)arguments[0]);
        if (arguments.length > 1) {
            if (!(arguments[1] instanceof Double))
                throw new LuaException("Argument #2 expected to be an integer");
            size = ((Double)arguments[1]).intValue();
        }
        if (arguments.length > 2) {
            if (!(arguments[2] instanceof String))
                throw new LuaException("Argument #3 expected to be a string");
            password = (String) arguments[2];
        }
        // Generate new key
        PGPKeyRingGenerator generator = createPGPKeyRingGenerator(id, size, password);
        PGPPublicKeyRing publicKeyRing = generator.generatePublicKeyRing();
        PGPSecretKeyRing secretKeyRing = generator.generateSecretKeyRing();
        // Convert to string
        Map<String, String> keyMap = new HashMap<>();
        try {
            keyMap.put("public", wrapGpgKey(publicKeyRing.getEncoded(), KeyType.PUBLIC));
            keyMap.put("secret", wrapGpgKey(secretKeyRing.getEncoded(), KeyType.PRIVATE));
        } catch (IOException e) {
            throw new LuaException("Failed to generate keys: " + e.getMessage());
        }
        return new Object[]{keyMap};
    }

    /**
     * Add the header and footer to a GPG block string
     * @param key gpg key
     * @param keyType type of key
     * @return wrapped gpg block ready for file export
     */
    private String wrapGpgKey(byte[] key, KeyType keyType) throws IOException {
        String header = String.format("-----BEGIN PGP %s KEY BLOCK-----", keyType.name());
        String footer = String.format("-----END PGP %s KEY BLOCK-----", keyType.name());

        // Calulate crc
        CRC24 checksum = new CRC24();
        for (byte b : key)
            checksum.update(b);
        byte[] crcArray = BigInteger.valueOf(checksum.getValue()).toByteArray();
        if (crcArray.length > 3)
            System.arraycopy(crcArray, 1, crcArray, 0, 3);
        String crc = String.format("=%s", Base64.toBase64String(crcArray));

        String keyBlock = Base64.toBase64String(key);
        return String.format("%s\n\n%s\n%s\n%s", header, keyBlock, crc, footer);
    }

    /**
     * Creates a GPG key ring generator
     * Most of this was referenced from https://bouncycastle-pgp-cookbook.blogspot.de
     * @param size bits
     * @param password password to use for key
     * @return generator
     * @throws LuaException any error
     */
    private PGPKeyRingGenerator createPGPKeyRingGenerator(String id, int size, @Nullable String password)
            throws LuaException {
        // Generate the keys
        RSAKeyPairGenerator keyPairGenerator = createKeyPairGenerator(size);
        PGPKeyPair signKey;
        PGPKeyPair encryptKey;
        try {
            signKey = new BcPGPKeyPair(PGPPublicKey.RSA_SIGN, keyPairGenerator.generateKeyPair(), new Date());
            encryptKey = new BcPGPKeyPair(PGPPublicKey.RSA_ENCRYPT, keyPairGenerator.generateKeyPair(), new Date());
        } catch (PGPException e) {
            throw new LuaException("Could not create key: " + e.getMessage());
        }
        // Create signing key options
        PGPSignatureSubpacketGenerator signSignatureGenerator = new PGPSignatureSubpacketGenerator();
        signSignatureGenerator.setKeyFlags(false, KeyFlags.SIGN_DATA | KeyFlags.CERTIFY_OTHER);
        signSignatureGenerator.setPreferredSymmetricAlgorithms(
                false,
                new int[] {
                        SymmetricKeyAlgorithmTags.AES_256,
                        SymmetricKeyAlgorithmTags.AES_192,
                        SymmetricKeyAlgorithmTags.AES_128
                }
        );
        signSignatureGenerator.setPreferredHashAlgorithms(
                false,
                new int[] {
                        HashAlgorithmTags.SHA256,
                        HashAlgorithmTags.SHA1,
                        HashAlgorithmTags.SHA384,
                        HashAlgorithmTags.SHA512,
                        HashAlgorithmTags.SHA224
                }
        );
        signSignatureGenerator.setFeature(false, Features.FEATURE_MODIFICATION_DETECTION);
        // Create encryption key options
        PGPSignatureSubpacketGenerator encryptSignatureGenerator = new PGPSignatureSubpacketGenerator();
        encryptSignatureGenerator.setKeyFlags(
                false,
                KeyFlags.ENCRYPT_COMMS | KeyFlags.ENCRYPT_STORAGE
        );
        // Secret key encryption algorithms
        PGPDigestCalculator sha1Calc;
        PGPDigestCalculator sha256Calc;
        try {
            sha1Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA1);
            sha256Calc = new BcPGPDigestCalculatorProvider().get(HashAlgorithmTags.SHA256);
        } catch (PGPException e) {
            throw new LuaException("Unsupported hashing algorithm: " + e.getMessage());
        }
        PBESecretKeyEncryptor keyEncryptor;
        if (password != null) {
            keyEncryptor = new BcPBESecretKeyEncryptorBuilder(
                    PGPEncryptedData.AES_256,
                    sha256Calc,
                    0xc0
            ).build(password.toCharArray());
        }
        else
            keyEncryptor = new BcPBESecretKeyEncryptorBuilder(PGPEncryptedData.NULL).build(new char[0]);
        // Create the key ring generator
        PGPKeyRingGenerator generator;
        try {
            generator = new PGPKeyRingGenerator(
                    PGPSignature.POSITIVE_CERTIFICATION,
                    signKey,
                    id,
                    sha1Calc,
                    signSignatureGenerator.generate(),
                    null,
                    new BcPGPContentSignerBuilder(signKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1),
                    keyEncryptor
            );
            generator.addSubKey(encryptKey, encryptSignatureGenerator.generate(), null);
        }
        catch (PGPException e) {
            throw new LuaException(e.getMessage());
        }
        return generator;
    }

    /**
     * Creates a RSA key pair generator
     * @param size bits
     * @return RSA key
     * @throws LuaException on any errors
     */
    private RSAKeyPairGenerator createKeyPairGenerator(int size) throws LuaException {
        RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
        try {
            generator.init(new RSAKeyGenerationParameters(
                    BigInteger.valueOf(0x10001),
                    SecureRandom.getInstanceStrong(),
                    size,
                    80
            ));
        }
        catch (NoSuchAlgorithmException e) {
            throw new LuaException("RSA is not supported");
        }
        return generator;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public Node node() {
        return node;
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public String[] methods() {
        return getMethodNames();
    }

    @Override
    @Optional.Method(modid = ModIds.OPEN_COMPUTERS_CORE)
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        callMethod(null, null, Arrays.asList(getMethodNames()).indexOf(method), args.toArray());
        throw new NoSuchMethodException(method);
    }

    @Override
    public void update() {
        OpenComputersUtil.updateNode(this, node);
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        OpenComputersUtil.removeNode(node);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        OpenComputersUtil.removeNode(node);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        OpenComputersUtil.readFromNbt(compound, node);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        OpenComputersUtil.writeToNbt(compound, node);
        return compound;
    }

    private enum  KeyType {
        PUBLIC, PRIVATE
    }
}
