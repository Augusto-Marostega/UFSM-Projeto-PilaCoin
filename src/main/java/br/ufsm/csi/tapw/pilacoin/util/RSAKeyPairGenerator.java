package br.ufsm.csi.tapw.pilacoin.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

@Service
public class RSAKeyPairGenerator {

    private static final Logger logger = LoggerFactory.getLogger(RSAKeyPairGenerator.class);

    @Value("${chave.publica.arquivo:chavePublica.pem}")
    private String CHAVE_PUBLICA_ARQUIVO;

    @Value("${chave.privada.arquivo:chavePrivada.pem}")
    private String CHAVE_PRIVADA_ARQUIVO;

    public KeyPair generateOrLoadKeyPair() {
        KeyPair keyPair = loadKeyPair();

        if (keyPair == null) {
            keyPair = generateKeyPair();
            //saveKeyPair(keyPair);
        }

        return keyPair;
    }

    private KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            logger.error("[generateKeyPair] Erro ao gerar par de chaves", e);
            throw new RuntimeException("[generateKeyPair] Erro ao gerar par de chaves", e);
        }
    }

    private KeyPair loadKeyPair() {
        try {
            Path publicKeyPath = Paths.get(CHAVE_PUBLICA_ARQUIVO);
            Path privateKeyPath = Paths.get(CHAVE_PRIVADA_ARQUIVO);

            //logger.info("[loadKeyPair] Tentando carregar par de chaves de: {}", publicKeyPath);
            //logger.info("[loadKeyPair] Tentando carregar par de chaves de: {}", privateKeyPath);

            if (!Files.exists(publicKeyPath) || !Files.exists(privateKeyPath)) {
                logger.info("[loadKeyPair] Arquivos de chaves não encontrados. Gerando novo par de chaves.");

                // Gera um novo par de chaves
                KeyPair keyPair = generateKeyPair();
                saveKeyPair(keyPair);
                return keyPair;
            }

            try (ObjectInputStream oisPublic = new ObjectInputStream(Files.newInputStream(publicKeyPath));
                 ObjectInputStream oisPrivate = new ObjectInputStream(Files.newInputStream(privateKeyPath))) {

                PublicKey publicKey = (PublicKey) oisPublic.readObject();
                PrivateKey privateKey = (PrivateKey) oisPrivate.readObject();

                return new KeyPair(publicKey, privateKey);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("[loadKeyPair] Erro ao carregar par de chaves", e);
                throw new RuntimeException("[RSAKeyPairGenerator] Erro ao carregar par de chaves", e);
            }
        } catch (Exception e) {
            logger.error("[loadKeyPair] Erro ao tentar carregar par de chaves", e);
            throw new RuntimeException("[RSAKeyPairGenerator] Erro ao tentar carregar par de chaves", e);
        }
    }

    private void saveKeyPair(KeyPair keyPair) {
        try {
            Path publicKeyPath = Paths.get(CHAVE_PUBLICA_ARQUIVO);
            Path privateKeyPath = Paths.get(CHAVE_PRIVADA_ARQUIVO);

            // Obtém o diretório pai
            Path parentDir = publicKeyPath.getParent();

            // Verifica se o diretório existe, se não existir, cria-o
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            try (ObjectOutputStream oosPublic = new ObjectOutputStream(Files.newOutputStream(publicKeyPath));
                 ObjectOutputStream oosPrivate = new ObjectOutputStream(Files.newOutputStream(privateKeyPath))) {

                oosPublic.writeObject(keyPair.getPublic());
                oosPrivate.writeObject(keyPair.getPrivate());
            } catch (IOException e) {
                logger.error("[saveKeyPair] Erro ao salvar par de chaves", e);
                throw new RuntimeException("[saveKeyPair] Erro ao salvar par de chaves", e);
            }
        } catch (IOException e) {
            logger.error("[saveKeyPair] Erro ao criar diretórios para salvar as chaves", e);
            throw new RuntimeException("[saveKeyPair] Erro ao criar diretórios para salvar as chaves", e);
        }
    }


}
