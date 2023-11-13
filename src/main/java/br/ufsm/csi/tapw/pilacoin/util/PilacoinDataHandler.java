package br.ufsm.csi.tapw.pilacoin.util;

import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinJson;
import br.ufsm.csi.tapw.pilacoin.model.json.PilacoinValidadoJson;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;

@Service
public class PilacoinDataHandler {

    private static final Logger logger = LoggerFactory.getLogger(PilacoinDataHandler.class);

    private final ObjectMapper objectMapper;

    public PilacoinDataHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] getHash(Object o) {
        try {
            //String jsonString = objectMapper.writeValueAsString(o);
            ObjectMapper om = new ObjectMapper();
            String jsonString = om.writeValueAsString(o);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(jsonString.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("[getHash] Erro ao gerar hash para objeto", e);
            return null;
        }
    }

    public byte[] gerarAssinatura(Object anyObject, PrivateKey privateKey) throws IllegalBlockSizeException, BadPaddingException, JsonProcessingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        String strJson = objectMapper.writeValueAsString(anyObject);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] hash = this.getHash(strJson);
        return cipher.doFinal(hash);
    }

    public PilacoinJson strParaObjPilacoinJson(String strJson) {
        try {
            return objectMapper.readValue(strJson, PilacoinJson.class);
        } catch (Exception e) {
            logger.error("[strParaObjPilacoinJson] Erro ao converter JSON para objeto PilacoinJson", e);
            return null;
        }
    }

    public String pilacoinJsonParaStrJson(PilacoinJson pilacoinJson) {
        try {
            return objectMapper.writeValueAsString(pilacoinJson);
        } catch (Exception e) {
            logger.error("[pilacoinJsonParaStrJson] Erro ao converter PilacoinJson para JSON", e);
            return null;
        }
    }

    public PilacoinValidadoJson strParaObjPilacoinValidadoJson(String json) {
        try {
            return objectMapper.readValue(json, PilacoinValidadoJson.class);
        } catch (Exception e) {
            logger.error("[strParaObjPilacoinValidadoJson] Erro ao converter JSON para objeto PilacoinJson", e);
            return null;
        }
    }

    public String pilacoinValidadoJsonParaStrJson(PilacoinValidadoJson pilacoinValidadoJson) {
        try {
            return objectMapper.writeValueAsString(pilacoinValidadoJson);
        } catch (Exception e) {
            logger.error("[pilacoinValidadoJsonParaStrJson] Erro ao converter PilacoinValidadoJson para JSON", e);
            return null;
        }
    }


}
