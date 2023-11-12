package br.ufsm.csi.tapw.pilacoin.service;

import br.ufsm.csi.tapw.pilacoin.model.Dificuldade;
import br.ufsm.csi.tapw.pilacoin.model.json.DificuldadeJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DificuldadeService {

    private static final Logger logger = LoggerFactory.getLogger(DificuldadeService.class);
    private Dificuldade ultimaDificuldade;

    public void salvarDificuldade(String strDificuldadeJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            DificuldadeJson dificuldadeJson = objectMapper.readValue(strDificuldadeJson, DificuldadeJson.class);

            Dificuldade dificuldadeAtual = new Dificuldade(new BigInteger(dificuldadeJson.getDificuldade(), 16), dificuldadeJson.getInicio(), dificuldadeJson.getValidadeFinal());

            if (this.ultimaDificuldade == null || !dificuldadeAtual.equals(this.ultimaDificuldade)) {
                // Atualize a dificuldade atual
                this.ultimaDificuldade = dificuldadeAtual;
                logger.info("A dificuldade foi atualizada: {}", dificuldadeAtual);
                // Realize ações necessárias com a nova dificuldade
            } else {
                logger.info("A dificuldade não foi alterada.");
            }
        } catch (JsonProcessingException e) {
            logger.error("Erro ao processar JSON da dificuldade", e);
            // Tratar a exceção de acordo com os requisitos do seu aplicativo
        }
    }

    public Dificuldade getUltimaDificuldade() {
        return ultimaDificuldade;
    }
}