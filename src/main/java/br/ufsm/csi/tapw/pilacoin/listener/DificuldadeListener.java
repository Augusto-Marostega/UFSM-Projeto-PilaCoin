package br.ufsm.csi.tapw.pilacoin.listener;

import br.ufsm.csi.tapw.pilacoin.model.Dificuldade;
import br.ufsm.csi.tapw.pilacoin.model.json.DificuldadeJson;
import br.ufsm.csi.tapw.pilacoin.service.DificuldadeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigInteger;

@Service
public class DificuldadeListener {

    private static final Logger logger = LoggerFactory.getLogger(DificuldadeListener.class);

    private final DificuldadeService dificuldadeService;

    @Autowired
    public DificuldadeListener(DificuldadeService dificuldadeService) {
        this.dificuldadeService = dificuldadeService;
    }

    @RabbitListener(queues = "dificuldade")
    public void handleDificuldadeMessage(String strDificuldadeJson) {
        try {
            dificuldadeService.salvarDificuldade(strDificuldadeJson);
            //logger.info("Mensagem de dificuldade processada com sucesso.");
        } catch (Exception e) {
            logger.error("[handleDificuldadeMessage] Erro ao processar mensagem de dificuldade", e);
            // Tratar a exceção de acordo com os requisitos do seu aplicativo
        }
    }
}